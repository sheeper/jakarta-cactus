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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.CallTarget;

/**
 * A helper class for an Ant Task that does the following :
 * <ul>
 *   <li>create a java thread,</li>
 *   <li>start another Ant target in that thread. This target must be a
 *       blocking target that starts a web server/servlet engine,</li>
 *   <li>wait for that server to be started. This is done by continuously
 *       trying to call a URL.</li>
 * </ul>.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class StartServerHelper implements Runnable
{
    /**
     * The URL that is continuously pinged to verify if the server is running.
     */
    private URL testURL;

    /**
     * The Ant target name that will start the web server/servlet engine.
     */
    private String startTarget;

    /**
     * The tasks that wraps around this helper class
     */
    private Task task;

    /**
     * True if the server was already started when this task is executed.
     */
    private boolean isServerAlreadyStarted = false;

    /**
     * @param theTask the Ant task that is calling this helper
     */
    public StartServerHelper(Task theTask)
    {
        this.task = theTask;
    }

    /**
     * @return true if the server has already been started.
     */
    public boolean isServerAlreadyStarted()
    {
        return this.isServerAlreadyStarted;
    }

    /**
     * @see Task#execute()
     */
    public void execute() throws BuildException
    {
        // Verify that a test URL has been specified
        if (this.testURL == null)
        {
            throw new BuildException("A testURL attribute must be specified");
        }

        // Verify that a start target has been specified
        if (this.startTarget == null)
        {
            throw new BuildException("A startTarget Ant target name must "
                + "be specified");
        }

        // Try connecting in case the server is already running. If so, does
        // nothing
        if (isURLCallable())
        {
            // Server is already running. Record this information so that we
            // don't stop it afterwards.
            this.isServerAlreadyStarted = true;
            this.task.log("Server is already running", Project.MSG_DEBUG);

            return;
        }
        else
        {
            this.task.log("Server is not running", Project.MSG_DEBUG);
        }

        // Call the target that starts the server, in another thread. The called
        // target must be blocking.
        Thread thread = new Thread(this);

        thread.start();


        // Wait a few ms more (just to make sure the servlet engine is
        // ready to accept connections)
        sleep(1000);

        // Continuously try calling the test URL until it succeeds
        while (true)
        {
            this.task.log("Checking if server is up ...", Project.MSG_DEBUG);

            if (!isURLCallable())
            {
                sleep(500);

                continue;
            }

            this.task.log("Server is up !", Project.MSG_DEBUG);

            break;
        }


        // Wait a few ms more (just to be sure !)
        sleep(500);

        this.task.log("Server started", Project.MSG_DEBUG);

        // We're done ... Ant will continue processing other tasks
    }

    /**
     * Sleeps n milliseconds.
     *
     * @param theMs the number of milliseconds to wait
     * @throws BuildException if the sleeping thread is interrupted
     */
    private void sleep(int theMs) throws BuildException
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

    /**
     * @return true if the test URL could be called without error or false
     *         otherwise
     */
    private boolean isURLCallable()
    {
        boolean isURLCallable = false;

        try
        {
            HttpURLConnection connection = 
                (HttpURLConnection) this.testURL.openConnection();

            connection.connect();
            readFully(connection);
            connection.disconnect();
            isURLCallable = true;
        }
        catch (IOException e)
        {
            // Log an information in debug mode
            // Get stacktrace text
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(baos);

            e.printStackTrace(writer);
            writer.close();

            this.task.log("Failed to call test URL. Reason :"
                + new String(baos.toByteArray()), Project.MSG_DEBUG);
        }

        return isURLCallable;
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
        // not block on read() operations !
        if (theConnection.getContentLength() != 0)
        {
            byte[] buf = new byte[256];

            InputStream is = theConnection.getInputStream();

            while (-1 != is.read(buf))
            {
            }
        }
    }

    /**
     * The thread that calls the Ant target that starts the web server/servlet
     * engine. Must be a blocking target.
     */
    public void run()
    {
        // Call the Ant target using the "antcall" task.
        CallTarget callee;

        callee = (CallTarget) (this.task.getProject().createTask("antcall"));
        callee.setOwningTarget(this.task.getOwningTarget());
        callee.setTaskName(this.task.getTaskName());
        callee.setLocation(this.task.getLocation());
        callee.setInheritAll(true);

        callee.init();

        callee.setTarget(this.startTarget);
        callee.execute();

        // Should never reach this point as the target is blocking, unless the
        // server is stopped.
    }

    /**
     * @param theTestURL the test URL to ping
     */
    public void setTestURL(String theTestURL)
    {
        try
        {
            this.testURL = new URL(theTestURL);
        }
        catch (MalformedURLException e)
        {
            throw new BuildException("Bad URL [" + theTestURL + "]", e);
        }

        this.task.log("Test URL = [" + this.testURL + "]", Project.MSG_DEBUG);
    }

    /**
     * @param theStartTarget the Ant target to call
     */
    public void setStartTarget(String theStartTarget)
    {
        this.startTarget = theStartTarget;
    }
}