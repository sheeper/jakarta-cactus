/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.container.resin;

import org.apache.cactus.integration.ant.AntTestCase;
import org.apache.tools.ant.BuildException;

/**
 * Unit tests for {@link Resin3xTask}.
 * 
 * @version $Id$
 */
public final class TestResin3xTask extends AntTestCase
{
    /**
     * True if Resin 3.x is installed.
     */
    private boolean isResinInstalled;
    
    // Constructors ------------------------------------------------------------

    /**
     * @see AntTestCase#AntTestCase
     */
    public TestResin3xTask()
    {
        super("org/apache/cactus/integration/ant/container/resin/"
            + "test-resin3x.xml");
    }
    
    // TestCase Implementation -------------------------------------------------

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        getProject().addTaskDefinition("resin3x", Resin3xTask.class);
        
        // If cactus.home.resin3x system property has been defined, pass
        // it to the Ant project as a property
        String resin3xHome = System.getProperty("cactus.home.resin3x"); 
        if (resin3xHome != null)
        {
            getProject().setProperty("cactus.home.resin3x", resin3xHome);
            this.isResinInstalled = true;
        }

        // Allow build script to use a port different than the default (8080)
        // for testing. Useful if a server is already started on that port.
        getProject().setProperty("cactus.port", 
            System.getProperty("cactus.port", "8080"));

        // Pass clover setting
        String cloverEnabled = System.getProperty("clover.enable");
        if (cloverEnabled != null)
        {
            getProject().setProperty("clover.enable", cloverEnabled);
            getProject().setProperty("clover.jar",
                System.getProperty("clover.jar"));
        }
    }

    // Test Methods ------------------------------------------------------------

    /**
     * Verifies that the task throws an exception when the action attribute 
     * has not been set.
     */
    public void testExecuteWhenNoDirSpecified()
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertEquals("You must specify the mandatory [dir] attribute",
                expected.getMessage());
        }
    }    
    
    /**
     * Verifies that the task throws an exception when the action attribute 
     * has not been set.
     */
    public void testExecuteWhenNoActionSpecified()
    {
        if (this.isResinInstalled)
        {
            try
            {
                executeTestTarget();
                fail("Expected BuildException");
            }
            catch (BuildException expected)
            {
                assertEquals("You must specify an [action] attribute",
                    expected.getMessage());
            }
        }
    }    

    /**
     * Verifies that the task throws an exception when the action attribute 
     * has an invalid value.
     */
    public void testExecuteWhenWrongActionSpecified()
    {
        if (this.isResinInstalled)
        {
            try
            {
                executeTestTarget();
                fail("Expected BuildException");
            }
            catch (BuildException expected)
            {
                assertEquals("Valid actions are: [start] and [stop]",
                    expected.getMessage());
            }
        }
    }    

    /**
     * Verifies that the task can start and stop a Resin 3.x instance
     * when there is nothing to deploy and when we don't use a test URL 
     * to wait for the container to be started.
     */
    public void testExecuteStartWithNoDeployableAndNoTestURL()
    {
        if (this.isResinInstalled)
        {
            executeTestTarget();
        }
    }    

    /**
     * Verifies that the task can start and stop a Resin 3.x instance
     * when there is a webapp to deploy and when we don't use a test URL 
     * to wait for the container to be started.
     */
    public void testExecuteStartWithWarDeployableAndNoTestURL()
    {
        if (this.isResinInstalled)
        {
            executeTestTarget();
        }
    }    
    
    /**
     * Verifies that the task can start and stop a Resin 3.x instance
     * when there is nothing to deploy and when we use a test URL 
     * to wait for the container to be started.
     */
    public void testExecuteStartWithNoDeployableAndWithTestURL()
    {
        if (this.isResinInstalled)
        {
            executeTestTarget();
        }
    }    
}
