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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.CallTarget;

/**
 * Task to automate running in-container unit test. It has the following
 * syntax when used in Ant :
 * <code><pre>
 *   &lt;runservertests testURL="&t;url&gt;"
 *          startTarget="&lt;start target name&gt;"
 *          stopTarget="&lt;stop target name&gt;"
 *          testTarget="&lt;test target name&gt;"/>
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
    /**
     * the test target name.
     */
    private String testTarget;

    /**
     * the fully qualified name of the test task.
     */
    private String testTask;

    /**
     * The helper object used to start the server. We use a helper so that it
     * can also be reused in the <code>StartServerTask</code> task. Indeed,
     * with Ant 1.3 and before there are classloaders issues with calling a
     * custom task from another custom task. Using a helper is a workaround.
     */
    private StartServerHelper startHelper;

    /**
     * The helper object used to stop the server. We use a helper so that it
     * can also be reused in the <code>StopServerTask</code> task. Indeed,
     * with Ant 1.3 and before there are classloaders issues with calling a
     * custom task from another custom task. Using a helper is a workaround.
     */
    private StopServerHelper stopHelper;

    /**
     * Initialize the task.
     */
    public void init()
    {
        this.startHelper = new StartServerHelper(this);
        this.stopHelper = new StopServerHelper(this);
    }

    /**
     * @see Task#execute()
     */
    public void execute() throws BuildException
    {
        try
        {
            callStart();
            callTestTaskOrTarget();
        }
        finally
        {
            // Make sure we stop the server but only if it were not already
            // started before the execution of this task.
            if (!this.startHelper.isServerAlreadyStarted())
            {
                callStop();
            }
        }
    }

    /**
     * Call the test task or test target.
     */
    private void callTestTaskOrTarget() throws BuildException
    {
        if (testTarget != null)
        {
            callTestTarget();
        }
        else
            if (testTask != null)
            {
                callTestTask();
            }
            else
            {
                throw new BuildException("Missing required attribute, one of testTarget or testTask");
            }
    }

    /**
     * Call the start server task
     */
    private void callStart()
    {
        this.startHelper.execute();
    }

    /**
     * Call the stop server task
     */
    private void callStop()
    {
        this.stopHelper.execute();
    }

    /**
     * Call the run tests target
     */
    private void callTestTarget()
    {
        CallTarget callee;

        callee = (CallTarget) project.createTask("antcall");
        callee.setOwningTarget(target);
        callee.setTaskName(getTaskName());
        callee.setLocation(location);
        callee.setInheritAll(true);
        callee.setInheritRefs(true);
        callee.init();
        callee.setTarget(this.testTarget);
        callee.execute();
    }

    /**
     * Call the run tests target
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
				if (methods[i].getName().equals("setProject")) {
					Class[] parameters = methods[i].getParameterTypes();
					Object[] arg = new Object[parameters.length];
					for (int j=0;j<parameters.length;j++) {
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
        catch (InvocationTargetException e) {
        	throw new BuildException(e);
        }
		catch (NoSuchMethodException e) {
			throw new BuildException(e);
		}        
    }

    /**
     * Sets the target to call to start the server.
     *
     * @param theStartTarget the Ant target to call
     */
    public void setStartTarget(String theStartTarget)
    {
        this.startHelper.setStartTarget(theStartTarget);
    }

    /**
     * Sets the target to call to stop the server.
     *
     * @param theStopTarget the Ant target to call
     */
    public void setStopTarget(String theStopTarget)
    {
        this.stopHelper.setStopTarget(theStopTarget);
    }

    /**
     * Sets the URL to call for testing if the server is running.
     *
     * @param theTestURL the test URL to ping
     */
    public void setTestURL(String theTestURL)
    {
        this.startHelper.setTestURL(theTestURL);
        this.stopHelper.setTestURL(theTestURL);
    }

    /**
     * Sets the target to call to run the tests.
     *
     * @param theTestTarget the Ant target to call
     */
    public void setTestTarget(String theTestTarget)
    {
        this.testTarget = theTestTarget;
    }

    /**
     * Sets the target to call to run the tests.
     *
     * @param theTestTarget the Ant target to call
     */
    public void setTestTask(String theTestTask)
    {
        this.testTask = theTestTask;
    }

    /**
     * @param theTimeout the timeout after which we stop trying to call the test
     * URL.
     */
    public void setTimeout(long theTimeout)
    {
        this.startHelper.setTimeout(theTimeout);
    }

}