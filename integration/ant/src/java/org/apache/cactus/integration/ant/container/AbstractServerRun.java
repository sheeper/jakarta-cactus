/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.container;

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
    private String[] args;

    /**
     * Flag that specifies if the server is already started to prevent
     * starting it if it is.
     */
    private boolean isStarted = false;

    /**
     * Thread in which the server is running
     */
    private Thread runningServerThread;
    
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
     * @return the thread in which the server has been started
     * @param theArgs the command line arguments
     * @exception Exception if any error happens when starting the server
     */
    protected abstract Thread doStartServer(String[] theArgs) throws Exception;

    /**
     * Stops the server by connecting to the socket set up when the server
     * was started.
     *
     * @param theArgs the command line arguments
     * @param theRunningServerThread the thread in which the server is running.
     *        This is useful for example if there is no simple way to stop the
     *        server and thus you need to simply try to stop the running thread.
     * @exception Exception if any error happens when stopping the server
     */
    protected abstract void doStopServer(String[] theArgs,
        Thread theRunningServerThread) throws Exception;

    /**
     * Parse and process the command line to start/stop the server.
     */
    protected final void doRun()
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
            this.runningServerThread = doStartServer(this.args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error starting server");
        }

        // Server is now started
        this.isStarted = true;

        // Start a socket listener that will listen for stop commands. 
        start();
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
            this.doStopServer(this.args, this.runningServerThread);
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
