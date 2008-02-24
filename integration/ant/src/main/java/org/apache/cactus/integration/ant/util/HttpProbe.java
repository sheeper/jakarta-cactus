/* 
 * ========================================================================
 * 
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.cactus.integration.ant.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Support class that probes a URL. 
 * 
 * 
 * @version $Id: HttpProbe.java,v 1.0 2005/08/29 10:19:57 xnguyen Exp $
 */
public class HttpProbe
{

    // Instance Variables ------------------------------------------------------

    /**
     * The URL.
     */
    private URL url;

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param theUrl The Url
     */
    public HttpProbe(URL theUrl) 
    {
        this.url = theUrl;
    }
    
    /**
     * In thread tests whether we are able to connect to the 
     * HTTP server identified by the
     * specified URL. The caller thread is blocked.
     * @param theWaitedTime The time waiting 
     * @param theCheckedInterval The interval to check
     * @throws InterruptedException If the current Thread is interupted
     * @throws IOException If there is error with reading
     * @return the HTTP response code or -1 if no connection could be
     *         established
     */
    public boolean timeout(long theWaitedTime, long theCheckedInterval)
        throws InterruptedException, IOException
    {
        //Ping the container
        //Continuously try calling the test URL until it succeeds or
        // until a timeout is reached (we then throw a build exception).
        long startTime = System.currentTimeMillis();
        int responseCode = -1;
        do
        {
            if ((System.currentTimeMillis() - startTime) > theWaitedTime)
            {
                return true;

            }

            sleep(theCheckedInterval);

            responseCode = testConnectivity();


        } while (!isAvailable(responseCode));
        
        return false;

    }
 
 
    /**
     * Tests whether we are able to connect to the HTTP server identified by the
     * specified URL.
     * @throws IOException If there is reading error
     * @return the HTTP response code or -1 if no connection could be
     *         established
     */
    public int testConnectivity() throws IOException
    {
        int code = -1;
        HttpURLConnection connection = null;
       
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Connection", "close");
        connection.connect();
        code = connection.getResponseCode();
        readFully(connection);
        connection.disconnect();
      
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
     * @return The server name, or <code>null</code> if the server name could
     *         not be retrieved
     * @throws IOException If there is error when reading
     */
    private String retrieveServerName() throws IOException
    {
        String retVal = null;
        
        HttpURLConnection connection =
            (HttpURLConnection) url.openConnection();
        connection.connect();
        retVal = connection.getHeaderField("Server");
        connection.disconnect();
       
        return retVal;
    }

    /**
     * Fully reads the input stream from the passed HTTP URL connection to
     * prevent (harmless) server-side exception.
     *
     * @param theConnection the HTTP URL connection to read from
     * @exception IOException if an error happens during the read
     */
    private void readFully(HttpURLConnection theConnection)
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
     * @throws InterruptedException If the sleeping thread is interrupted
     */
    private void sleep(long theMs) throws InterruptedException
    {

            Thread.sleep(theMs);
 
    }

    // Private Methods ---------------------------------------------------------



}
