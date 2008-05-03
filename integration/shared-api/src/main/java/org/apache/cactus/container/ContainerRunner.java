/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
 */
package org.apache.cactus.container;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.cactus.integration.api.exceptions.CactusRuntimeException;
import org.codehaus.cargo.util.log.Logger;

/**
 * Support class that handles the lifecycle of a container, which basically
 * consists of startup and shutdown.
 * 
 * @version $Id: ContainerRunner.java 239130 2005-01-29 15:49:18Z vmassol $
 */
public final class ContainerRunner
{
    // Instance Variables ------------------------------------------------------

    /**
     * The container to run.
     */
    //private org.codehaus.cargo.container.Container container;
    
    private ContainerWrapper containerWrapper = null;

    /**
     * The URL that is continuously pinged to check if the container is running.
     */
    private URL testURL;

    /**
     * Timeout in milliseconds after which to give up connecting to the
     * container.
     */
    private long timeout = 180000;

    /**
     * The time interval in milliseconds to sleep between polling the container.
     */
    private long checkInterval = 500;

    /**
     * The time to sleep after the container has shut down. 
     */
    private long shutDownWait = 2000;

    /**
     * Whether the container had already been running before.
     */
    private boolean alreadyRunning;

    /**
     * The server name as returned in the 'Server' header of the server's
     * HTTP response.
     */
    private String serverName;

    /**
     * The logger to use.
     */
    private transient Logger logger;

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param theContainerWrapper The container to run
     */
    public ContainerRunner(ContainerWrapper theContainerWrapper)
    {
        //this.container = theContainer;
        this.containerWrapper = theContainerWrapper;
    }

    // Public Methods ----------------------------------------------------------

    /**
     * Returns the server name as reported in the 'Server' header of HTTP 
     * responses from the server.
     * 
     * @return The server name
     */
    public String getServerName()
    {
        return this.serverName;
    }

    /**
     * Method called by the task to perform the startup of the container. This
     * method takes care of starting the container in another thread, and
     * polling the test URL to check whether startup has completed. As soon as
     * the URL is available (or the timeout is exceeded), control is returned to
     * the caller.
     * 
     * @throws IllegalStateException If the 'url' property is <code>null</code>
     */
    public void startUpContainer() throws IllegalStateException
    {
        if (this.testURL == null)
        {
            throw new IllegalStateException("Property [url] must be set");
        }

        // Try connecting in case the server is already running. If so, does
        // nothing
        this.alreadyRunning = isAvailable(testConnectivity(this.testURL));
        if (this.alreadyRunning)
        {
            // Server is already running. Record this information so that we
            // don't stop it afterwards.
            this.logger.debug("Server is already running",
                getClass().toString());
            return;
        }

        // Now start the server in another thread
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                containerWrapper.startUp();
            }
        });
        thread.start();

        // Continuously try calling the test URL until it succeeds or
        // until a timeout is reached (we then throw a build exception).
        long startTime = System.currentTimeMillis();
        int responseCode = -1;
        do
        {
            if ((System.currentTimeMillis() - startTime) > this.timeout)
            {
                throw new CactusRuntimeException("Failed to start the container after "
                    + "more than [" + this.timeout + "] ms. Trying to connect "
                    + "to the [" + this.testURL + "] test URL yielded a ["
                    + responseCode + "] error code. Please run in debug mode "
                    + "for more details about the error.");
            }
            sleep(this.checkInterval);
            this.logger.debug("Checking if server is up ...",
                getClass().toString());
            responseCode = testConnectivity(this.testURL);
        } while (!isAvailable(responseCode));

        // Wait a few ms more (just to be sure !)
        sleep(this.containerWrapper.getStartUpWait());

        this.serverName = retrieveServerName(this.testURL);
        this.logger.info("Server [" + this.serverName + "] started",
            getClass().toString());
    }

    /**
     * Method called by the task to perform the stopping of the container. This
     * method takes care of stopping the container in another thread, and
     * polling the test URL to check whether shutdown has completed. As soon as
     * the URL stops responding, control is returned to the caller.
     * 
     * @throws IllegalStateException If the 'url' property is <code>null</code>
     */
    public void shutDownContainer() throws IllegalStateException
    {
        if (this.testURL == null)
        {
            throw new IllegalStateException("Property [url] must be set");
        }

        // Don't shut down a container that has not been started by us
        if (this.alreadyRunning)
        {
            return;
        }
        
        if (!isAvailable(testConnectivity(this.testURL)))
        {
            this.logger.debug("Server isn't running!", getClass().toString());
            return;
        }

        // Call the target that stops the server, in another thread. The called
        // target must be blocking.
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                containerWrapper.shutDown();
            }
        });
        thread.start();

        // Continuously try calling the test URL until it fails
        do 
        {
            sleep(this.checkInterval);
        } while (isAvailable(testConnectivity(this.testURL)));

        // sleep a bit longer to be sure the container has terminated
        sleep(this.shutDownWait);
        
        this.logger.debug("Server stopped!", getClass().toString());
    }

    /**
     * Sets the time interval to sleep between polling the container. 
     * 
     * The default interval is 500 milliseconds.
     * 
     * @param theCheckInterval The interval in milliseconds
     */
    public void setCheckInterval(long theCheckInterval)
    {
        this.checkInterval = theCheckInterval;
    }

    /**
     * Sets the log to write to.
     *  
     * @param theLogger The log to set
     */
    public void setLogger(Logger theLogger)
    {
        this.logger = theLogger;
    }

    /**
     * Sets the time to wait after the container has been shut down.
     * 
     * The default time is 2 seconds.
     * 
     * @param theShutDownWait The time to wait in milliseconds
     */
    public void setShutDownWait(long theShutDownWait)
    {
        this.shutDownWait = theShutDownWait;
    }

    /**
     * Sets the timeout after which to stop trying to call the container.
     * 
     * The default timeout is 3 minutes.
     * 
     * @param theTimeout The timeout in milliseconds
     */
    public void setTimeout(long theTimeout)
    {
        this.timeout = theTimeout;
    }

    /**
     * Sets the HTTP/HTTPS URL that will be continuously pinged to check if the
     * container is running.
     * 
     * @param theTestURL The URL to set
     */
    public void setURL(URL theTestURL)
    {
        if (!(theTestURL.getProtocol().equalsIgnoreCase("http") 
            || theTestURL.getProtocol().equalsIgnoreCase("https")))
        {
            throw new IllegalArgumentException("Not a HTTP or HTTPS URL");
        } 
        this.testURL = theTestURL;
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Tests whether we are able to connect to the HTTP server identified by the
     * specified URL.
     * 
     * @param theUrl The URL to check
     * @return the HTTP response code or -1 if no connection could be 
     *         established
     */
    public int testConnectivity(URL theUrl)
    {
        int code;
        try
        {
            HttpURLConnection connection = 
                (HttpURLConnection) theUrl.openConnection();
            connection.setRequestProperty("Connection", "close");
            connection.connect();
            readFully(connection);
            connection.disconnect();
            code = connection.getResponseCode();
        }
        catch (IOException e)
        {
            this.logger.debug("Failed to connect to [" + theUrl + "]",
                 e.getMessage());
            code = -1;
        }
        return code;
    }


    /**
     * Tests whether an HTTP return code corresponds to a valid connection
     * to the test URL or not. Success is 200 up to but excluding 300.
     * 
     * @param theCode the HTTP response code to verify
     * @return <code>true</code> if the test URL could be called without error,
     *         <code>false</code> otherwise
     */
    private boolean isAvailable(int theCode)
    {
        boolean result;
        if ((theCode != -1) && (theCode < 300)) 
        {
            result = true;            
        }
        else
        {
            result = false;
        }
        return result;
    }

    /**
     * Retrieves the server name of the container.
     * 
     * @param theUrl The URL to retrieve
     * @return The server name, or <code>null</code> if the server name could 
     *         not be retrieved
     */
    private String retrieveServerName(URL theUrl)
    {
        String retVal = null;
        try
        {
            HttpURLConnection connection = 
                (HttpURLConnection) theUrl.openConnection();
            connection.connect();
            retVal = connection.getHeaderField("Server");
            connection.disconnect();
        }
        catch (IOException e)
        {
            this.logger.debug("Could not get server name from [" 
                + theUrl + "]", e.getMessage());
        }
        return retVal;
    }

    /**
     * Fully reads the input stream from the passed HTTP URL connection to
     * prevent (harmless) server-side exception.
     *
     * @param theConnection the HTTP URL connection to read from
     * @exception IOException if an error happens during the read
     */
    static void readFully(HttpURLConnection theConnection)
                   throws IOException
    {
        // Only read if there is data to read ... The problem is that not
        // all servers return a content-length header. If there is no header
        // getContentLength() returns -1. It seems to work and it seems
        // that all servers that return no content-length header also do
        // not block on read() operations!
        if (theConnection.getContentLength() != 0)
        {
            byte[] buf = new byte[256];
            InputStream in = theConnection.getInputStream();
            while (in.read(buf) != -1)
            {
                // Make sure we read all the data in the stream
            }
        }
    }

    /**
     * Pauses the current thread for the specified amount.
     *
     * @param theMs The time to sleep in milliseconds
     * @throws CactusRuntimeException If the sleeping thread is interrupted
     */
    private void sleep(long theMs) throws CactusRuntimeException
    {
        try
        {
            Thread.sleep(theMs);
        }
        catch (InterruptedException e)
        {
            throw new CactusRuntimeException("Interruption during sleep", e);
        }
    }
}
