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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Assert;

/**
 * A very simple HTTP server that binds to a port and responds to all requests
 * with a predefined response.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public final class MockHttpServer implements Runnable
{

    // Instance Variables ------------------------------------------------------

    /**
     * The port to bind to.
     */
    private int port;

    /**
     * The content of the request to send back on any request.
     */
    private String response;

    /**
     * Flag indicating whether the server should be stopped.
     */
    private volatile boolean stopFlag = false;

    /**
     * The actual method requested.
     */
    private String actualMethod;

    /**
     * The HTTP method expected.
     */
    private String expectedMethod;

    /**
     * The actual request URI.
     */
    private String actualUri;

    /**
     * The request URI expected.
     */
    private String expectedUri;

    /**
     * The expected number of requests.
     */
    private int expectedRequestCount = -1;

    /**
     * The number of requests (excluding the internal SHUTDOWN request).
     */
    private int actualRequestCount = 0;

    /**
     * The log to write messages to.
     */
    private Log log = LogFactory.getLog(MockHttpServer.class);

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param thePort The port to bind to
     */
    public MockHttpServer(int thePort)
    {
        if (thePort <= 0)
        {
            throw new IllegalArgumentException("Invalid port number");
        }
        this.port = thePort;
    }

    // Runnable Implementation -------------------------------------------------

    /**
     * The main server thread. The server will wait for connections until it 
     * receives a special request containing the string 'SHUTDOWN'.
     */
    public void run()
    {
        if (this.response == null)
        {
            throw new IllegalStateException("Response content not set");
        }

        try
        {
            ServerSocket serverSocket = new ServerSocket(port);
            while (!this.stopFlag)
            {
                Socket socket = serverSocket.accept();
                try
                {
                    if (!this.stopFlag)
                    {
                        processRequest(socket);
                    }
                }
                catch (IOException ioe)
                {
                    this.log.error("Couldn't process request", ioe);
                }
                finally
                {
                    socket.close();
                }
            }
            serverSocket.close();
        }
        catch (IOException ioe)
        {
            this.log.error("Problem with server socket", ioe);
        }
    }

    // Public Methods ----------------------------------------------------------

    /**
     * Advise the server to expect a specific HTTP method in requests.
     * 
     * @param theMethod The HTTP method to expect
     */
    public void expectMethod(String theMethod)
    {
        this.expectedMethod = theMethod;
    }

    /**
     * Advise the server to expect a specific number of requests.
     * 
     * @param theRequestCount The number of requests to expect
     */
    public void expectRequestCount(int theRequestCount)
    {
        this.expectedRequestCount = theRequestCount;
    }

    /**
     * Advise the server to expect a specific request URI in requests.
     * 
     * @param theUri The request URI to expect
     */
    public void expectUri(String theUri)
    {
        this.expectedUri = theUri;
    }

    /**
     * Returns the port number the server is listening on.
     * 
     * @return The port
     */
    public int getPort()
    {
        return this.port;
    }

    /**
     * Returns whether the server is stopped (or about to stop, to be precise).
     * 
     * @return Whether the server is stopped
     */
    public boolean isStopped()
    {
        return this.stopFlag;
    }

    /**
     * Sets the content of the request to send back on any request.
     * 
     * @param theResponse The content of the HTTP response
     */
    public void setResponse(String theResponse)
    {
        this.response = theResponse;
    }

    /**
     * Stops the server.
     */
    public void stop()
    {
        this.stopFlag = true;
        try
        {
            Socket sock = new Socket("localhost", this.port);
            sock.getOutputStream().write("SHUTDOWN\n".getBytes());
        }
        catch (IOException ioe)
        {
            this.log.error("Error while trying to stop", ioe);
        }
    }

    /**
     * Verifies whether the requests sent to the server matched those expected.
     */
    public void verify()
    {
        if (this.expectedRequestCount >= 0)
        {
            Assert.assertTrue("Expected " + this.expectedRequestCount
                + " requests, but got " + this.actualRequestCount,
                this.expectedRequestCount == this.actualRequestCount);
        }
        if (this.expectedMethod != null)
        {
            Assert.assertEquals(this.expectedMethod, this.actualMethod);
        }
        if (this.expectedUri != null)
        {
            Assert.assertEquals(this.expectedUri, this.actualUri);
        }
    }

    // Public Static Methods ---------------------------------------------------

    /**
     * Returns a free port number on the specified host within the given range.
     * 
     * @param theHost The name or IP addres of host on which to find a free port
     * @param theLowest The port number from which to start searching 
     * @param theHighest The port number at which to stop searching
     * @return A free port in the specified range, or -1 of none was found
     * @throws IOException If an I/O error occurs
     */
    public static int findUnusedLocalPort(String theHost, int theLowest,
        int theHighest)
        throws IOException
    {
        final Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 10; i++)
        {
            int port = (int)
                (random.nextFloat() * (theHighest - theLowest)) + theLowest;
            Socket s = null;
            try
            {
                s = new Socket(theHost, port);
            }
            catch (ConnectException e)
            {
                return port;
            }
            finally
            {
                if (s != null)
                {
                    s.close();
                }
            }
        }
        return -1;
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Processes an incoming request.
     * 
     * @param theSocket The socket to which the connection was established
     * @throws IOException If an I/O error occurs
     */
    private void processRequest(Socket theSocket) throws IOException
    {
        BufferedReader in = null;
        OutputStream out = null; 
        try
        {
            readRequest(theSocket.getInputStream());
            writeResponse(theSocket.getOutputStream());
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
            if (out != null)
            {
                out.close();
            }
        }
    }

    /**
     * Reads the request and stores the HTTP method and request URI.
     * 
     * @param theIn The socket input stream
     * @throws IOException If an I/O error occurs
     */
    private void readRequest(InputStream theIn) throws IOException
    {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(theIn));

        String statusLine = reader.readLine();
        StringTokenizer tokenizer = new StringTokenizer(statusLine);
        this.actualRequestCount++;
        this.actualMethod = tokenizer.nextToken();
        this.actualUri = tokenizer.nextToken();
    }

    /**
     * Writes the user-defined response to the socket output stream.
     * 
     * @param theOut The socket output stream
     * @throws IOException If an I/O error occurs
     */
    private void writeResponse(OutputStream theOut) throws IOException
    {
        // Construct the response message.
        if (this.response != null)
        {
            BufferedReader reader = new BufferedReader(
                new StringReader(this.response));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                theOut.write(line.getBytes());
                theOut.write("\r\n".getBytes());
            }
        }
    }

}
