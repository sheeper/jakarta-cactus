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

import org.apache.tools.ant.BuildException;

/**
 * Unit tests for {@link RunServerTestsTask}.
 * 
 * @version $Id$
 */
public final class TestRunServerTestsTask extends AntTestCase
{

    // Constructors ------------------------------------------------------------

    /**
     * @see AntTestCase#AntTestCase
     */
    public TestRunServerTestsTask()
    {
        super("org/apache/cactus/integration/ant/test-runservertests.xml");
    }

    // TestCase Implementation -------------------------------------------------

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        getProject().addTaskDefinition("runservertests",
            RunServerTestsTask.class);
    }

    // Test Methods ------------------------------------------------------------

    /**
     * Verifies that a build exception is thrown if neither the
     * <code>starttarget</code> nor a nested <code>start</code> element has been
     * specified.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testStartNotSet() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertEquals("You must specify either a nested [start] element or "
                + "the [starttarget] attribute", expected.getMessage());
        }
    }

    /**
     * Verifies that the <code>starttarget</code> is run before the timeout is 
     * exceeded. 
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testStartTimeout() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertEquals("Failed to start the container after more than [0] "
                + "ms.", expected.getMessage());
        }
        assertTargetExecuted("startDummy");
    }

    /**
     * Verifies that a build exception is thrown if neither the
     * <code>stoptarget</code> nor a nested <code>stop</code> element has been
     * specified.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testStopNotSet() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertEquals("You must specify either a nested [stop] element or "
                + "the [stoptarget] attribute", expected.getMessage());
        }
    }

    /**
     * Verifies that a build exception is thrown if neither the
     * <code>testtarget</code> nor a nested <code>test</code> element has been
     * specified.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testTestNotSet() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertEquals("You must specify either a nested [test] element or "
                + "the [testtarget] attribute", expected.getMessage());
        }
    }

    /**
     * Verifies that a build exception is thrown if the <code>testurl</code>
     * attribute is not set.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testTestUrlNotSet() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertEquals("The [testurl] attribute must be specified",
                expected.getMessage());
        }
    }

    /**
     * Verifies that a build exception is thrown if the <code>testurl</code>
     * attribute is set to a non-HTTP URL.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testNonHttpTestUrl() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals("Not a HTTP URL", expected.getMessage());
        }
    }

}
