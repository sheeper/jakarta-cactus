/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */
package org.apache.commons.cactus.ant;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Abstract class for starting/stopping an application server
 * by setting up a listener socket.
 */
public abstract class AbstractServerRun extends Thread
{
	private int m_Port = 7777;

	protected static void doRun(AbstractServerRun run, String[] args)
	{
		// Look for a -start or -stop flag
		boolean isStart = true;
		Vector newArgs = new Vector();

		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-start")) {
				isStart = true;
			} else if (args[i].equalsIgnoreCase("-stop")) {
				isStart = false;
			} else if (args[i].equalsIgnoreCase("-port")) {
				run.m_Port = Integer.parseInt(args[i+1]);
				i++;
			} else {
				newArgs.add(args[i]);
			}
		}

		if (isStart) {
			String[] strArgs = new String[0];
			run.startServer((String[])newArgs.toArray(strArgs));
		} else {
			run.stopServer();
		}

	}

	// abstract function to actually start the server
	abstract protected void doStartServer(String[] args) throws Exception;

	// abstract function to actually stop the server
	abstract protected void doStopServer() throws Exception;

	private void startServer(String[] args)
	{
		try {
			doStartServer( args );
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error starting server");
		}

		// Set up listener socket for listening to request to stop server
		new Thread(this).start();
	}

	private void stopServer()
	{
		// Open socket connection
		Socket clientSocket = null;

		try {
			clientSocket = new Socket("127.0.0.1", m_Port);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error opening socket tp 127.0.0.1 on port [" + m_Port + "]");
		} finally {
			try {
				if (clientSocket != null) {
					clientSocket.close();
				}
			} catch (IOException e) {
				throw new RuntimeException("Cannot close client socket");
			}
		}
	}

	public void run()
	{
		ServerSocket serverSocket = setUpListenerSocket();

		// Accept a client socket connection
		Socket clientSocket = null;
		try {
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			throw new RuntimeException("Error accepting connection for server socket [" + serverSocket + "]");

		} finally {
			// Stop server socket
			try {
				serverSocket.close();
			} catch (IOException e) {
				throw new RuntimeException("Cannot close server socket [" + serverSocket + "]");
			}
		}

		// Stop server
		try {
			this.doStopServer();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot stop server");
		}

		// Stop server socket
		try {
			serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException("Cannot close server socket [" + serverSocket + "]");
		}

	}

	private ServerSocket setUpListenerSocket()
	{
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(m_Port);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error setting up the server listener socket");
		}

		return serverSocket;
	}

}