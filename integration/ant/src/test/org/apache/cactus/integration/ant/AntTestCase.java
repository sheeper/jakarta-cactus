/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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
    public final void buildStarted(BuildEvent theEvent)
    {
    }

    /**
     * @see BuildListener#buildFinished
     */
    public final void buildFinished(BuildEvent theEvent)
    {
    }

    /**
     * @see BuildListener#targetStarted
     */
    public final void targetStarted(BuildEvent theEvent)
    {
    }

    /**
     * @see BuildListener#targetFinished
     */
    public final void targetFinished(BuildEvent theEvent)
    {
        this.executedTargets.add(theEvent.getTarget().getName());
    }

    /**
     * @see BuildListener#taskStarted
     */
    public final void taskStarted(BuildEvent theEvent)
    {
    }

    /**
     * @see BuildListener#taskFinished
     */
    public final void taskFinished(BuildEvent theEvent)
    {
    }

    /**
     * @see BuildListener#messageLogged
     */
    public final void messageLogged(BuildEvent theEvent)
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
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        helper.parse(this.project, buildFile);
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
                // exception has been logged
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
    protected final void assertMessageLogged(String theMessage, int theLogLevel)
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
    protected final void assertMessageLoggedContaining(String theSubstring,
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
    protected final void assertTargetExecuted(String theName)
    {
        assertTrue("Target '" + theName + "' should have been executed",
            this.executedTargets.contains(theName));
    }

    /**
     * Executes the target in the project that corresponds to the current test
     * case.
     */
    protected final void executeTestTarget()
    {
        this.project.executeTarget(getName());
    }

    /**
     * Returns the Ant project.
     * 
     * @return The project
     */
    protected final Project getProject()
    {
        return this.project;
    }

    /**
     * Returns the base directory of the Ant project.
     * 
     * @return The base directory
     */
    protected final File getProjectDir()
    {
        return this.project.getBaseDir();
    }

    /**
     * Returns the target in the project that corresponds to the current test
     * case.
     * 
     * @return The test target
     */
    protected final Target getTestTarget()
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
