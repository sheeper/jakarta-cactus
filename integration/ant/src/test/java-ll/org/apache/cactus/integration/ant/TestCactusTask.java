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
 * Unit tests for {@link CactusTask}.
 * 
 * @version $Id: TestCactusTask.java 238812 2004-02-29 10:21:34Z vmassol $
 */
public final class TestCactusTask extends AntTestCase
{

    // Constructors ------------------------------------------------------------

    /**
     * @see AntTestCase#AntTestCase
     */
    public TestCactusTask()
    {
        super("org/apache/cactus/integration/ant/test-cactus.xml");
    }

    // TestCase Implementation -------------------------------------------------

    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        getProject().addTaskDefinition("cactus", CactusTask.class);
    }

    // Test Methods ------------------------------------------------------------

    /**
     * Verifies that the task throws an exception when the warfile attribute 
     * has not been set.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testNeitherWarFileNorEarFileSet() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertEquals("You must specify either the [warfile] or the "
                + "[earfile] attribute", expected.getMessage());
        }
    }

    /**
     * Verifies that the task throws an exception when the warfile attribute 
     * is set to a non-existing file.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testWarFileNotExisting() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertTrue(true);
        }
    }

    /**
     * Verifies that the task throws an exception when the warfile attribute 
     * is set to a web-app archive that has not been cactified.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testWarFileNotCactified() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertEquals("The WAR has not been cactified",
                expected.getMessage());
        }
    }

    /**
     * Verifies that the task does nothing if it is given a cactified web
     * application, but neither tests nor containers.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testWarFileCactified() throws Exception
    {
        executeTestTarget();
    }

    /**
     * Verifies that the task throws an exception when the earfile attribute 
     * is set to an empty enterprise application archive.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testEarFileEmpty() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertTrue(true);
        }
    }

    /**
     * Verifies that the task throws an exception when the earfile attribute 
     * is set to an enterprise application archive that doesn't contain a web
     * module with the definition of the test redirectors.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testEarFileNotCactified() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertTrue(true);
        }
    }

    /**
     * Verifies that the task does nothing if it is given a cactified enterprise
     * application, but neither tests nor containers.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testEarFileCactified() throws Exception
    {
        executeTestTarget();
    }

}
