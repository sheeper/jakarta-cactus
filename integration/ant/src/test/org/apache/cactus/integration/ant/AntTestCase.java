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
package org.apache.cactus.integration.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;

/**
 * An AntTestCase is a TestCase specialization for unit testing Ant tasks.
 * 
 * @author <a href="mailto:cmlenz@gmx.de">Christopher Lenz</a>
 *
 * @version $Id$
 */
public abstract class AntTestCase extends TestCase implements BuildListener
{

    // Instance Variables ------------------------------------------------------

    /**
     * The Ant project.
     */
    private Project project;

    /**
     * The name of the test build file.
     */
    private String buildFile;

    /**
     * Buffer containing all messages logged by Ant. Keys correspond to the 
     * message priority as <code>java.lang.Integer</code>, the values are are
     * <code>java.lang.StringBuffer</code>s containing the actual log messages.
     */
    private Map log = new HashMap();

    /**
     * The targets the have been executed.
     */
    private Set executedTargets = new HashSet();

    // Constructors ------------------------------------------------------------

    /**
     * Constructor
     * 
     * @param theBuildFile The Ant build file corresponding to the test fixture
     */
    public AntTestCase(String theBuildFile)
    {
        this.buildFile = theBuildFile;
    }

    // BuildListener Implementation --------------------------------------------

    /**
     * @see BuildListener#buildStarted
     */
    public void buildStarted(BuildEvent theEvent)
    {
    }

    /**
     * @see BuildListener#buildFinished
     */
    public void buildFinished(BuildEvent theEvent)
    {
    }

    /**
     * @see BuildListener#targetStarted
     */
    public void targetStarted(BuildEvent theEvent)
    {
    }

    /**
     * @see BuildListener#targetFinished
     */
    public void targetFinished(BuildEvent theEvent)
    {
        this.executedTargets.add(theEvent.getTarget().getName());
    }

    /**
     * @see BuildListener#taskStarted
     */
    public void taskStarted(BuildEvent theEvent)
    {
    }

    /**
     * @see BuildListener#taskFinished
     */
    public void taskFinished(BuildEvent theEvent)
    {
    }

    /**
     * @see BuildListener#messageLogged
     */
    public void messageLogged(BuildEvent theEvent)
    {
        StringBuffer buffer = (StringBuffer)
            log.get(new Integer(theEvent.getPriority()));
        if (buffer == null)
        {
            buffer = new StringBuffer();
            log.put(new Integer(theEvent.getPriority()), buffer);
        }
        buffer.append(theEvent.getMessage()).append("\n");
    }

    // TestCase Implementation -------------------------------------------------

    /**
     * Initializes a fresh Ant project with a target named after the name of the
     * test case.
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        this.project = new Project();
        this.project.addBuildListener(this);
        this.project.init();
        File buildFile = getBuildFile(this.buildFile);
        this.project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        ProjectHelper.configureProject(this.project, buildFile);
        if (getProject().getTargets().get("setUp") != null)
        {
            getProject().executeTarget("setUp");
        }
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        if (getProject().getTargets().get("tearDown") != null)
        {
            try
            {
                getProject().executeTarget("tearDown");
            }
            catch (BuildException be)
            {
                System.err.println("Exception in tearDown: " + be.getMessage());
            }
        }
    }

    // Protected Methods -------------------------------------------------------

    /**
     * Asserts that a specific message has been logged at a specific log level.
     * 
     * @param theMessage The message to check for
     * @param theLogLevel The log level of the message
     * @throws IOException If an error occurred reading the log buffer
     */
    protected void assertMessageLogged(String theMessage, int theLogLevel)
        throws IOException
    {
        StringBuffer buffer = (StringBuffer) log.get(new Integer(theLogLevel));
        if (buffer != null)
        {
            BufferedReader reader =
                new BufferedReader(new StringReader(buffer.toString()));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                if (line.equals(theMessage))
                {
                    return;
                }
            }
        }
        throw new AssertionFailedError(
            "Expected log message '" + theMessage + "'");
    }

    /**
     * Asserts that a message containing the specified substring has been logged
     * at a specific log level.
     * 
     * @param theSubstring The substring to check for
     * @param theLogLevel The log level of the message
     * @throws IOException If an error occurred reading the log buffer
     */
    protected void assertMessageLoggedContaining(String theSubstring,
        int theLogLevel)
        throws IOException
    {
        StringBuffer buffer = (StringBuffer) log.get(new Integer(theLogLevel));
        if (buffer != null)
        {
            BufferedReader reader =
                new BufferedReader(new StringReader(buffer.toString()));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                if (line.indexOf(theSubstring) >= 0)
                {
                    return;
                }
            }
        }
        throw new AssertionFailedError(
            "Expected log message containing '" + theSubstring + "'");
    }

    /**
     * Asserts that a named target has been executed.
     * 
     * @param theName The name of the target
     */
    protected void assertTargetExecuted(String theName)
    {
        assertTrue("Target '" + theName + "' should have been executed",
            this.executedTargets.contains(theName));
    }

    /**
     * Executes the target in the project that corresponds to the current test
     * case.
     */
    protected void executeTestTarget()
    {
        this.project.executeTarget(getName());
    }

    /**
     * Returns the Ant project.
     * 
     * @return The project
     */
    protected Project getProject()
    {
        return this.project;
    }

    /**
     * Returns the base directory of the Ant project.
     * 
     * @return The base directory
     */
    protected File getProjectDir()
    {
        return this.project.getBaseDir();
    }

    /**
     * Returns the target in the project that corresponds to the current test
     * case.
     * 
     * @return The test target
     */
    protected Target getTestTarget()
    {
        return (Target) getProject().getTargets().get(getName());
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Returns a file from the test inputs directory, which is determined by the
     * system property <code>testinput.dir</code>.
     * 
     * @param theFileName The name of the file relative to the test input
     *        directory 
     * @return The file from the test input directory
     */
    private File getBuildFile(String theFileName)
    {
        String testInputDirProperty = System.getProperty("testinput.dir");
        assertTrue("The system property 'testinput.dir' must be set",
            testInputDirProperty != null);
        File testInputDir = new File(testInputDirProperty);
        assertTrue("The system property 'testinput.dir' must point to an "
            + "existing directory", testInputDir.isDirectory());
        File buildFile = new File(testInputDir, theFileName);
        assertTrue("The test input " + theFileName + " does not exist",
            buildFile.exists());
        return buildFile;
    }

}
