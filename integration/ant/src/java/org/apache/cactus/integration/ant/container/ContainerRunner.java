/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Cactus" and "Apache Software
 *    Foundation" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.cactus.integration.ant.container;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.cactus.integration.ant.util.AntLog;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.BuildException;

/**
 * Support class that handles the lifecycle of a container, which basically
 * consists of startup and shutdown.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public final class ContainerRunner
{

    // Instance Variables ------------------------------------------------------

    /**
     * The container to run.
     */
    private Container container;

    /**
     * The URL that is continuously pinged to check if the container is running.
     */
    private URL url;

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
     * The time to sleep after the container has started up. 
     */
    private long startUpWait = 1000;

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
     * The log to use.
     */
    private transient Log log = AntLog.NULL;

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param theContainer The container to run
     */
    public ContainerRunner(Container theContainer)
    {
        this.container = theContainer;
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
        if (this.url == null)
        {
            throw new IllegalStateException("Property 'url' must be set");
        }

        // Try connecting in case the server is already running. If so, does
        // nothing
        this.alreadyRunning = isAvailable(this.url);
        if (this.alreadyRunning)
        {
            // Server is already running. Record this information so that we
            // don't stop it afterwards.
            this.log.debug("Server is already running");
            return;
        }

        // Now start the server in another thread
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                container.startUp();
            }
        });
        thread.start();

        // Continuously try calling the test URL until it succeeds or
        // until a timeout is reached (we then throw a build exception).
        long startTime = System.currentTimeMillis();
        do
        {
            if ((System.currentTimeMillis() - startTime) > this.timeout)
            {
                throw new BuildException("Failed to start the container after "
                    + "more than [" + this.timeout + "] ms.");
            }
            sleep(this.checkInterval);
            this.log.debug("Checking if server is up ...");
        } while (!isAvailable(this.url));

        // Wait a few ms more (just to be sure !)
        sleep(this.startUpWait);

        this.serverName = retrieveServerName(this.url);
        this.log.trace("Server '" + this.serverName + "' started");
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
        if (this.url == null)
        {
            throw new IllegalStateException("Property 'url' must be set");
        }

        // Don't shut down a container that has not been started by us
        if (this.alreadyRunning)
        {
            return;
        }
        
        if (!isAvailable(this.url))
        {
            this.log.debug("Server isn't running!");
            return;
        }

        // Call the target that starts the server, in another thread. The called
        // target must be blocking.
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                container.shutDown();
            }
        });
        thread.start();

        // Continuously try calling the test URL until it fails
        do 
        {
            sleep(this.checkInterval);
        } while (isAvailable(this.url));

        // sleep a bit longer to be sure the container has terminated
        sleep(this.shutDownWait);
        
        this.log.debug("Server stopped!");
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
     * @param theLog The log to set
     */
    public void setLog(Log theLog)
    {
        this.log = theLog;
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
     * Sets the time to wait after the container has been started up.
     * 
     * The default time is 1 second.
     * 
     * @param theStartUpWait The time to wait in milliseconds
     */
    public void setStartUpWait(long theStartUpWait)
    {
        this.startUpWait = theStartUpWait;
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
     * Sets the HTTP URL that will be continuously pinged to check if the
     * container is running.
     * 
     * @param theUrl The URL to set
     */
    public void setUrl(URL theUrl)
    {
        if (!theUrl.getProtocol().equals("http"))
        {
            throw new IllegalArgumentException("Not a HTTP URL");
        } 
        this.url = theUrl;
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Tests whether the resource pointed to by the specified HTTP URL is
     * available.
     * 
     * @param theUrl The URL to check
     * @return <code>true</code> if the test URL could be called without error,
     *         <code>false</code> otherwise
     */
    private boolean isAvailable(URL theUrl)
    {
        boolean isUrlCallable = false;
        try
        {
            HttpURLConnection connection = 
                (HttpURLConnection) theUrl.openConnection();
            connection.setRequestProperty("Connection", "close");
            connection.connect();
            readFully(connection);
            connection.disconnect();
            isUrlCallable = true;
        }
        catch (IOException e)
        {
            this.log.debug("Failed to connect to " + theUrl, e);
        }
        return isUrlCallable;
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
            this.log.debug("Could not get server name from " + theUrl, e);
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
     * @throws BuildException If the sleeping thread is interrupted
     */
    private void sleep(long theMs) throws BuildException
    {
        try
        {
            Thread.sleep(theMs);
        }
        catch (InterruptedException e)
        {
            throw new BuildException("Interruption during sleep", e);
        }
    }

}
