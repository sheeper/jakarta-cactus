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

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;

/**
 * A helper class for an Ant task that does the following : stop a running
 * server by calling an Ant target to stop it in another thread and wait for
 * the servlet engine to be stopped by trying to continously connect to a test
 * URL. If it succeeds it means the server is not stopped yet !
 *
 * @version @version@
 */
public class StopServerHelper implements Runnable
{
    /**
     * The URL that is continuously pinged to verify if the server is stopped
     */
    private URL m_TestURL;

    /**
     * The Ant target name that will stop the web server/servlet engine.
     */
    private String m_StopTarget;

    /**
     * The tasks that wraps around this helper class
     */
    private Task m_Task;

    /**
     *
     */
    public StopServerHelper(Task theTask)
    {
        m_Task = theTask;
    }

    /**
     * Executes the task.
     */
    public void execute() throws BuildException
    {
        // Verify that a test URL has been specified
        if (m_TestURL == null) {
            throw new BuildException("A testURL attribute must be specified");
        }

        // Verify that a stop target has been specified
        if (m_StopTarget == null) {
            throw new BuildException("A stopTarget Ant target name must be specified");
        }

        // Try connecting in case the server is already stopped.
        try {
            HttpURLConnection connection = (HttpURLConnection)m_TestURL.openConnection();
            connection.connect();
            StartServerHelper.readFully(connection);
            connection.disconnect();
        } catch (IOException e) {
            // Server is not running. Make this task a no-op.
            return;
        }

        // Call the target that stops the server, in another thread.
        Thread thread = new Thread(this);
        thread.start();

        // Wait a few ms more (just to make sure)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new BuildException("Interruption during sleep", e);
        }

        // Continuously try calling the test URL until it fails
        while (true) {

            try {
                HttpURLConnection connection = (HttpURLConnection)m_TestURL.openConnection();
                connection.connect();
                StartServerHelper.readFully(connection);
                connection.disconnect();
            } catch (IOException e) {
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ee) {
                throw new BuildException("Interruption during sleep", ee);
            }

        }

        // Wait a few ms more (just to be sure !)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new BuildException("Interruption during sleep", e);
        }

        m_Task.log("Server stopped !");

        // We're done ... Ant will continue processing other tasks
    }

    /**
     * The thread that calls the Ant target that stops the web server/servlet
     * engine.
     */
    public void run()
    {
        // Call the Ant target using the "antcall" task.
        CallTarget callee;
        callee = (CallTarget)(m_Task.getProject().createTask("antcall"));
        callee.setOwningTarget(m_Task.getOwningTarget());
        callee.setTaskName(m_Task.getTaskName());
        callee.setLocation(m_Task.getLocation());

        callee.init();

        callee.setTarget(m_StopTarget);
        callee.execute();
    }

    /**
     * @param theTestURL the test URL to ping
     */
    public void setTestURL(String theTestURL)
    {
        try {
            m_TestURL = new URL(theTestURL);
        } catch (MalformedURLException e) {
            throw new BuildException("Bad URL [" + theTestURL + "]", e);
        }
    }

    /**
     * @param theStopTarget the Ant target to call
     */
    public void setStopTarget(String theStopTarget)
    {
        m_StopTarget = theStopTarget;
    }

}