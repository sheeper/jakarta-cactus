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
 *
 * @version @version@
 */
public class RunServerTestsTask extends Task
{
    private String m_TestTarget;

    /**
     *
     */
    private StartServerHelper m_StartHelper;

    /**
     *
     */
    private StopServerHelper m_StopHelper;

    public void init()
    {
        m_StartHelper = new StartServerHelper(this);
        m_StopHelper = new StopServerHelper(this);
    }

    /**
     * Executes the task.
     */
    public void execute() throws BuildException
    {
        try {
            callStart();
            callTests();
        } finally {
            // Make sure we stop the server
            callStop();
        }
    }

    /**
     * Call the start server task
     */
    private void callStart()
    {
        m_StartHelper.execute();
    }

    /**
     * Call the stop server task
     */
    private void callStop()
    {
        m_StopHelper.execute();
    }

    /**
     * Call the run tests target
     */
    private void callTests()
    {
        CallTarget callee;
        callee = (CallTarget)project.createTask("antcall");
        callee.setOwningTarget(target);
        callee.setTaskName(getTaskName());
        callee.setLocation(location);
        callee.init();
        callee.setTarget(m_TestTarget);
        callee.execute();
    }

    /**
     * Sets the target to call to start the server.
     *
     * @param theStartTarget the Ant target to call
     */
    public void setStartTarget(String theStartTarget)
    {
        m_StartHelper.setStartTarget(theStartTarget);
    }

    /**
     * Sets the target to call to stop the server.
     *
     * @param theStopTarget the Ant target to call
     */
    public void setStopTarget(String theStopTarget)
    {
        m_StopHelper.setStopTarget(theStopTarget);
    }

    /**
     * Sets the URL to call for testing if the server is running.
     *
     * @param theTestURL the test URL to ping
     */
    public void setTestURL(String theTestURL)
    {
        m_StartHelper.setTestURL(theTestURL);
        m_StopHelper.setTestURL(theTestURL);
    }

    /**
     * Sets the target to call to run the tests.
     *
     * @param theTerstTarget the Ant target to call
     */
    public void setTestTarget(String theTestTarget)
    {
        m_TestTarget = theTestTarget;
    }

}