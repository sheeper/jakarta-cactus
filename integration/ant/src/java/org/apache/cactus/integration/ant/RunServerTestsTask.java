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
package org.apache.cactus.integration.ant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.taskdefs.CallTarget;

/**
 * Task to automate running in-container unit test. It has the following
 * syntax when used in Ant :
 * <code><pre>
 *   &lt;runservertests testURL="&t;url&gt;"
 *          starttarget="&lt;start target name&gt;"
 *          stoptarget="&lt;stop target name&gt;"
 *          testtarget="&lt;test target name&gt;"/>
 * </pre></code>
 * where <code>&lt;url&gt;</code> is the URL that is used by this task to
 * ensure that the server is running. Indeed, the algorithm is as follow :
 * <ul>
 *  <li>Checks if server is running by trying to open an HTTP connection to
 *  the URL,</li>
 *  <li>If it fails, call the start target and loop until the HTTP connection
 *  the URL can be established,</li>
 *  <li>Call the test target. This target is supposed to start the test,
 *  usually by running the junit Ant task,</li>
 *  <li>When the tests are finished, call the stop target to stop the server.
 *  Note: The stop target is called only if the server was not already running
 *  when this task was executed.</li>
 * </ul>
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class RunServerTestsTask extends Task
{

    // Inner Classes -----------------------------------------------------------

    /**
     * Class that represents the nested 'startup' and 'shutdown' elements. It
     * supports either an Ant target to delegate to, or a list of nested tasks
     * that are to be executed in order to perform the operation. 
     */
    public class Hook implements TaskContainer
    {
        
        // Instance Variables --------------------------------------------------
        
        /**
         * The target to call when the hook is executed. 
         */
        private String target;

        /**
         * Ordered list of the contained tasks that should be invoked when the
         * hook is executed.
         */
        private List tasks = new ArrayList();

        // Public Methods ------------------------------------------------------
        
        /**
         * Sets the target to call.
         * 
         * @param theTarget The name of the target
         */
        public void setTarget(String theTarget)
        {
            if (!this.tasks.isEmpty())
            {
                throw new BuildException("This element supports either "
                    + "a [target] attribute or nested tasks, but not both");
            }
            this.target = theTarget;
        }

        /**
         * @see org.apache.tools.ant.TaskContainer#addTask
         */
        public void addTask(Task theTask) throws BuildException
        {
            if (this.target != null)
            {
                throw new BuildException("This element supports either "
                    + "a [target] attribute or nested tasks, but not both");
            }
            this.tasks.add(theTask);
        }

        /**
         * Executes the hook by either calling the specified target, or invoking
         * all nested tasks.
         * 
         * @throws BuildException If thrown by the called target or one of the
         *         nested tasks
         */
        public void execute() throws BuildException
        {
            if (this.target != null)
            {
                CallTarget callee;
                callee = (CallTarget) project.createTask("antcall");
                callee.setOwningTarget(getOwningTarget());
                callee.setTaskName(getTaskName());
                callee.setLocation(location);
                callee.setInheritAll(true);
                callee.setInheritRefs(true);
                callee.init();
                callee.setTarget(this.target);
                callee.execute();
            }
            else
            {
                for (Iterator i = this.tasks.iterator(); i.hasNext();)
                {
                    Task task = (Task) i.next();
                    task.perform();
                }
            }
        }

    }

    // Instance Variables ------------------------------------------------------

    /**
     * The hook that is called when the tests should be run.
     */
    private Hook testHook;

    /**
     * The fully qualified name of the test task.
     * 
     * TODO: remove this when the hook-based approach is verified
     */
    private String testTask;

    /**
     * The hook that is called when the container should be started.
     */
    private Hook startHook;

    /**
     * The hook that is called when the container should be stopped.
     */
    private Hook stopHook;

    /**
     * The URL that is continuously pinged to verify if the server is running.
     */
    private URL testURL;

    /**
     * True if the server was already started when this task is executed.
     */
    private boolean isServerAlreadyStarted = false;

    /**
     * Timeout after which we stop trying to connect to the test URL (in ms).
     */
    private long timeout = 180000;
    
    // Task Implementation -----------------------------------------------------

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

        try
        {
            startServer();
            runTests();
        }
        finally
        {
            // Make sure we stop the server but only if it were not already
            // started before the execution of this task.
            if (!this.isServerAlreadyStarted)
            {
                stopServer();
            }
        }
    }

    // Public Methods ----------------------------------------------------------

    /**
     * Creates a nested start element.
     * 
     * @return The start element
     */
    public Hook createStart()
    {
        if (this.startHook != null)
        {
            throw new BuildException("Either specify the [starttarget] "
                + "attribute or the nested [start] element, but not both");
        }
        this.startHook = new Hook();
        return this.startHook;
    }

    /**
     * Sets the target to call to start the server.
     *
     * @param theStartTarget the Ant target to call
     */
    public void setStartTarget(String theStartTarget)
    {
        if (this.startHook != null)
        {
            throw new BuildException("Either specify the [starttarget] "
                + "attribute or the nested [start] element, but not both");
        }
        this.startHook = new Hook();
        this.startHook.setTarget(theStartTarget);
    }

    /**
     * Creates a nested stop element.
     * 
     * @return The stop element
     */
    public Hook createStop()
    {
        if (this.stopHook != null)
        {
            throw new BuildException("Either specify the [stoptarget] "
                + "attribute or the nested [stop] element, but not both");
        }
        this.stopHook = new Hook();
        return this.stopHook;
    }

    /**
     * Sets the target to call to stop the server.
     *
     * @param theStopTarget the Ant target to call
     */
    public void setStopTarget(String theStopTarget)
    {
        if (this.stopHook != null)
        {
            throw new BuildException("Either specify the [stoptarget] "
                + "attribute or the nested [stop] element, but not both");
        }
        this.stopHook = new Hook();
        this.stopHook.setTarget(theStopTarget);
    }

    /**
     * Creates a nested test element.
     * 
     * @return The test element
     */
    public Hook createTest()
    {
        if (this.testHook != null)
        {
            throw new BuildException("Either specify the [testtarget] "
                + "attribute or the nested [test] element, but not both");
        }
        this.testHook = new Hook();
        return this.testHook;
    }

    /**
     * Sets the target to call to run the tests.
     *
     * @param theTestTarget the Ant target to call
     */
    public void setTestTarget(String theTestTarget)
    {
        if (this.testHook != null)
        {
            throw new BuildException("Eitehr specify the [testtarget] "
                + "attribute or the nested [test] element, but not both");
        }
        this.testHook = new Hook();
        this.testHook.setTarget(theTestTarget);
    }

    /**
     * Sets the URL to call for testing if the server is running.
     *
     * @param theTestURL the test URL to ping
     */
    public void setTestURL(URL theTestURL)
    {
        if (!theTestURL.getProtocol().equals("http"))
        {
            throw new IllegalArgumentException("Not a HTTP URL");
        } 
        this.testURL = theTestURL;
    }

    /**
     * Sets the target to call to run the tests.
     *
     * @param theTestTask the Ant task to call
     */
    public void setTestTask(String theTestTask)
    {
        this.testTask = theTestTask;
    }

    /**
     * @param theTimeout the timeout after which we stop trying to call the test
     *        URL.
     */
    public void setTimeout(long theTimeout)
    {
        this.timeout = theTimeout;
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Calls the run tests task.
     * 
     * TODO: remove this when the hook-based approach is verified
     * 
     * @throws BuildException If an error occurred calling the test task
     */
    private void callTestTask() throws BuildException
    {
        // Create task object
        try
        {

            Class taskClass = Class.forName(testTask);
            Object task = taskClass.newInstance();
            Method[] methods = task.getClass().getMethods();
            for (int i = 0; i < methods.length; i++)
            {
                if (methods[i].getName().equals("setProject"))
                {
                    Class[] parameters = methods[i].getParameterTypes();
                    Object[] arg = new Object[parameters.length];
                    for (int j = 0; j < parameters.length; j++)
                    {
                        arg[j] = parameters[j].newInstance();
                    }
                    arg[0] = project;
                    methods[i].invoke(task, arg);
                }
            }
            taskClass.getMethod("execute", null).invoke(task, null);
        }
        catch (ClassNotFoundException e)
        {
            throw new BuildException(e);
        }
        catch (InstantiationException e)
        {
            throw new BuildException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new BuildException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new BuildException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new BuildException(e);
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

            this.log("Failed to call test URL. Reason :"
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
    private void readFully(HttpURLConnection theConnection)
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
                // Make sure we read all the data in the stream
            }
        }
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
     * Starts the server in another thread and blocks until the test URL becomes
     * available. 
     *
     * @throws BuildException If an error occurs during startup
     */
    private void startServer() throws BuildException
    {
        // Try connecting in case the server is already running. If so, does
        // nothing
        if (isURLCallable())
        {
            // Server is already running. Record this information so that we
            // don't stop it afterwards.
            this.isServerAlreadyStarted = true;
            log("Server is already running", Project.MSG_VERBOSE);
            return;
        }
        else
        {
            log("Server is not running", Project.MSG_DEBUG);
        }

        // Call the target that starts the server, in another thread. The called
        // target must be blocking.
        Thread thread = new Thread()
        {
            public void run()
            {
                if (startHook != null)
                {
                    startHook.execute();
                }
            }
        };
        thread.start();

        // Wait a few ms more (just to make sure the servlet engine is
        // ready to accept connections)
        sleep(1000);

        // Continuously try calling the test URL until it succeeds or
        // until a timeout is reached (we then throw a build exception).
        long startTime = System.currentTimeMillis();
        while (true)
        {
            if (System.currentTimeMillis() - startTime > this.timeout)
            {
                throw new BuildException("Failed to start the container after "
                    + "more than [" + this.timeout + "] ms.");
            }
            log("Checking if server is up ...", Project.MSG_DEBUG);
            if (!isURLCallable())
            {
                sleep(500);
                continue;
            }
            break;
        }

        // Wait a few ms more (just to be sure !)
        sleep(500);
        log("Server started", Project.MSG_VERBOSE);
    }

    /**
     * Runs the tests by executing the test hook.
     * 
     * @throws BuildException If an error occurs executing the test hook
     */
    private void runTests() throws BuildException
    {
        if (this.testTask != null)
        {
            // TODO: remove this when the hook-based approach is verified
            callTestTask();
        }
        else
        {
            this.testHook.execute();
        }
    }

    /**
     * Stops the server in another thread and blocks until the server stops
     * responding to HTTP requests.
     *
     * @throws BuildException If an error occurs during shutdown
     */
    private void stopServer() throws BuildException
    {
        if (!isURLCallable())
        {
            // Server is not running. Make this task a no-op.
            return;
        }

        // Call the target that stops the server, in another thread.
        Thread thread = new Thread()
        {
            public void run()
            {
                if (stopHook != null)
                {
                    stopHook.execute();
                }
            }
        };
        thread.start();

        // Continuously try calling the test URL until it fails
        do
        {
            sleep(500);
        } while (isURLCallable());

        sleep(1000);
        log("Server stopped!", Project.MSG_VERBOSE);
    }

}
