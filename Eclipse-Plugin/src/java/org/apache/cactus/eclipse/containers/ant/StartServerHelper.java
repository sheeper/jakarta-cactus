/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
package org.apache.cactus.eclipse.containers.ant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.cactus.eclipse.ui.CactusMessages;
import org.apache.tools.ant.BuildException;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

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
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 *
 * @version $Id: $
 */
public class StartServerHelper implements Runnable
{
    /**
     * The progress monitor that reflects progress made while starting the
     * container.
     */
    private IProgressMonitor pm;

    /**
     * The URL that is continuously pinged to verify if the server is running.
     */
    private URL testURL;

    /**
     * The tasks that wraps around this helper class
     */
    private AntRunner runner;

    /**
     * True if the server was already started when this task is executed.
     */
    private boolean isServerAlreadyStarted = false;

    /**
     * @param theRunner the Ant runner that this helper is calling
     */
    public StartServerHelper(AntRunner theRunner)
    {
        this.runner = theRunner;
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
            throw new BuildException(
                CactusMessages.getString(
                    "CactusLaunch.message.start.url.error"));
        }

        // Try connecting in case the server is already running. If so, does
        // nothing
        if (isURLCallable())
        {
            // Server is already running. Record this information so that we
            // don't stop it afterwards.
            this.isServerAlreadyStarted = true;

            return;
        }
        else
        {
        }

        // Call the target that starts the server, in another thread. The called
        // target must be blocking.
        Thread thread = new Thread(this);

        thread.start();
        // Wait a few ms more (just to make sure the servlet engine is
        // ready to accept connections)
        sleep(1000);
        // UI contribution, the number is relative to the task which
        // total is 4
        pm.worked(1);
        // Continuously try calling the test URL until it succeeds
        while (true)
        {

            if (!isURLCallable())
            {
                sleep(500);
                pm.worked(1);
                continue;
            }
            if (pm.isCanceled())
            {
                throw new BuildException(
                    CactusMessages.getString(
                        "CactusLaunch.message.start.error"));
            }
            break;
        }

        // Wait a few ms more (just to be sure !)
        sleep(500);

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
            throw new BuildException(
                CactusMessages.getString(
                    "CactusLaunch.message.start.sleep.error"),
                e);
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
    static void readFully(HttpURLConnection theConnection) throws IOException
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
                // Make sure we read all the data in the stream
            }
        }
    }

    /**
     * The thread that calls the Ant target that starts the web server/servlet
     * engine. Must be a blocking target.
     */
    public void run()
    {
        // Call the AntRunner .
        try
        {
            runner.run(pm);
        }
        catch (CoreException e)
        {
            // An error or cancelation has occured during the start build
            pm.setCanceled(true);
        }
        // Since the target is blocking this point will be reached
        // when the server is stopped.
    }

    /**
     * @param theTestURL the test URL to ping
     */
    public void setTestURL(URL theTestURL)
    {
        this.testURL = theTestURL;
    }

    /**
     * @param thePM the progress monitor to use
     */
    public void setProgressMonitor(IProgressMonitor thePM)
    {
        this.pm = thePM;
    }
}