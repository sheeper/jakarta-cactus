/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
package org.apache.cactus.ant;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.Vector;

/**
 * Abstract class for starting/stopping an application server. When this
 * application is first called to start the server, a listener socket is
 * set up. Then, we it is later called to stop the server, we connect to the
 * listener socket and tell the server to stop.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:digital@ix.net.au">Robert Leftwich</a>
 *
 * @version $Id$
 */
public abstract class AbstractServerRun extends Thread
{
    /**
     * Internal socket port that we use to stop the server.
     */
    private int port = 7777;

    /**
     * Host name. We assume the server is started and stoppped in the same
     * local machine
     */
    private String host = "127.0.0.1";

    /**
     * The command line arguments
     */
    protected String[] args;

    /**
     * Flag that specifies if the server is already started to prevent
     * starting it if it is.
     */
    private boolean isStarted = false;

    /**
     * @param theArgs the command line arguments
     */
    public AbstractServerRun(String[] theArgs)
    {
        this.args = theArgs;
    }

    /**
     * Starts the server (in a blocking mode) and set up a socket listener.
     *
     * @exception Exception if any error happens when starting the server
     */
    protected abstract void doStartServer() throws Exception;

    /**
     * Stops the server by connecting to the socket set up when the server
     * was started.
     *
     * @exception Exception if any error happens when stopping the server
     */
    protected abstract void doStopServer() throws Exception;

    /**
     * Parse and process the command line to start/stop the server.
     */
    protected void doRun()
    {
        // Look for a -start or -stop flag
        boolean isStart = true;
        Vector newArgs = new Vector();

        for (int i = 0; i < this.args.length; i++)
        {
            if (this.args[i].equalsIgnoreCase("-start"))
            {
                isStart = true;
            }
            else if (this.args[i].equalsIgnoreCase("-stop"))
            {
                isStart = false;
            }
            else if (this.args[i].equalsIgnoreCase("-port"))
            {
                this.port = Integer.parseInt(this.args[i + 1]);
                i++;
            }
            else
            {
                newArgs.add(this.args[i]);
            }
        }

        // Remove the command line arguments that should not be part of the
        // server command line (i.e. our own arguments).
        String[] strArgs = new String[0];

        this.args = (String[]) newArgs.toArray(strArgs);

        if (isStart)
        {
            startServer();
        }
        else
        {
            stopServer();
        }
    }

    /**
     * Starts the server.
     */
    private void startServer()
    {
        // If the server is already started, do nothing
        if (this.isStarted)
        {
            return;
        }

        try
        {
            doStartServer();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error starting server");
        }


        // Server is now started
        this.isStarted = true;

        new Thread(this).start();
    }

    /**
     * Stops the running server.
     */
    private void stopServer()
    {
        // Open socket connection
        Socket clientSocket = null;

        try
        {
            clientSocket = new Socket(this.host, this.port);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error opening socket to " + this.host
                + ":" + this.port + "]");
        }
        finally
        {
            try
            {
                if (clientSocket != null)
                {
                    clientSocket.close();
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException("Cannot close client socket");
            }
        }
    }

    /**
     * Sets up a listener socket and wait until we receive a request on it to
     * stop the running server.
     */
    public void run()
    {
        ServerSocket serverSocket = setUpListenerSocket();

        // Accept a client socket connection
        try
        {
            serverSocket.accept();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error accepting connection for "
                + "server socket [" + serverSocket + "]");
        }
        finally
        {
            // Stop server socket
            try
            {
                serverSocket.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException("Cannot close server socket ["
                    + serverSocket + "]");
            }
        }

        // Stop server
        try
        {
            this.doStopServer();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Cannot stop server");
        }

        // Stop server socket
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Cannot close server socket ["
                + serverSocket + "]");
        }
    }

    /**
     * Sets up the listener socket.
     *
     * @return the listener socket that has been set up
     */
    private ServerSocket setUpListenerSocket()
    {
        ServerSocket serverSocket = null;

        try
        {
            serverSocket = new ServerSocket(this.port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error setting up the server "
                + "listener socket");
        }

        return serverSocket;
    }
}