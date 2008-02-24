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
package org.apache.cactus.integration.ant.deployment.webapp;

import java.io.File;

import junit.framework.TestCase;

/**
 * Unit tests for {@link WarArchive}.
 *
 * @version $Id: TestWarArchive.java 239003 2004-05-31 20:05:27Z vmassol $
 */
public final class TestWarArchive extends TestCase
{

    // Test Methods ------------------------------------------------------------

    /**
     * Verifies that the method <code>containsClass()</code> returns
     * <code>true</code> if the WAR contains the requested class in
     * <code>WEB-INF/classes</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testContainsClassInWebinfClasses() throws Exception
    {
        WarArchive war = new DefaultWarArchive(getTestInput(
            "org/apache/cactus/integration/ant/deployment/containsclass.war"));
        assertTrue(war.containsClass("test.Test"));
    }

    /**
     * Verifies that the method <code>containsClass()</code> returns
     * <code>true</code> if the WAR contains the requested class in a JAR in
     * <code>WEB-INF/lib</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testContainsClassInWebinfLib() throws Exception
    {
        WarArchive war = new DefaultWarArchive(getTestInput(
            "org/apache/cactus/integration/ant/deployment/"
            + "containsclasslib.war"));
        assertTrue(war.containsClass("test.Test"));
    }

    /**
     * Verifies that the method <code>containsClass()</code> returns
     * <code>false</code> if the WAR does not contain such a class.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testContainsClassEmpty() throws Exception
    {
        WarArchive war = new DefaultWarArchive(getTestInput(
            "org/apache/cactus/integration/ant/deployment/empty.war"));
        assertTrue(!war.containsClass("test.Test"));
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
    private File getTestInput(String theFileName)
    {
        String testInputDirProperty = System.getProperty("testinput.dir");
        assertTrue("The system property 'testinput.dir' must be set",
            testInputDirProperty != null);
        File testInputDir = new File(testInputDirProperty);
        assertTrue("The system property 'testinput.dir' must point to an "
            + "existing directory", testInputDir.isDirectory());
        File testInputFile = new File(testInputDir, theFileName);
        assertTrue("The test input " + theFileName + " does not exist",
            testInputFile.exists());
        return testInputFile;
    }

}
