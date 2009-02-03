/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cactus.integration.ant.deployment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.codehaus.cargo.module.DefaultJarArchive;
import org.codehaus.cargo.module.JarArchive;

import junit.framework.TestCase;

/**
 * Unit tests for {@link JarArchive}.
 *
 * @version $Id: TestJarArchive.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public final class TestJarArchive extends TestCase
{

    // Test Methods ------------------------------------------------------------

    /**
     * Verifies that a <code>NullPointerException</code> is thrown when the 
     * constructor is passed a <code>null</code> argument as file.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testConstructorWithNullFile() throws Exception
    {
        try
        {
            new DefaultJarArchive((String) null);
            fail("NullPointerException expected");
        }
        catch (NullPointerException expected)
        {
            // expected
        }
    }

    /**
     * Verifies that a <code>NullPointerException</code> is thrown when the 
     * constructor is passed a <code>null</code> argument as input stream.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testConstructorWithNullInputStream() throws Exception
    {
        try
        {
            new DefaultJarArchive((InputStream) null);
            fail("NullPointerException expected");
        }
        catch (NullPointerException expected)
        {
            // expected
        }
    }

    /**
     * Verifies that random access to resources in the JAR is provided.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testRandomAccess() throws Exception
    {
        JarArchive jar = new DefaultJarArchive(getTestInput(
            "org/apache/cactus/integration/ant/deployment/randomaccess.jar")
            .getAbsolutePath());
        assertContains(jar.getResource("firstEntry.txt"), "firstEntry");
        assertContains(jar.getResource("secondEntry.txt"), "secondEntry");
        assertContains(jar.getResource("secondEntry.txt"), "secondEntry");
        assertContains(jar.getResource("firstEntry.txt"), "firstEntry");
    }

    /**
     * Verifies that the method <code>containsClass()</code> returns
     * <code>true</code> if the JAR contains the requested class.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testContainsClass() throws Exception
    {
        JarArchive jar = new DefaultJarArchive(getTestInput(
            "org/apache/cactus/integration/ant/deployment/containsclass.jar")
            .getAbsolutePath());
        assertTrue(jar.containsClass("test.Test"));
    }

    /**
     * Verifies that the method <code>containsClass()</code> returns
     * <code>false</code> if the JAR does not contain such a class.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testContainsClassEmpty() throws Exception
    {
        JarArchive jar = new DefaultJarArchive(getTestInput(
            "org/apache/cactus/integration/ant/deployment/empty.jar")
            .getAbsolutePath());
        assertTrue(!jar.containsClass("test.Test"));
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Asserts whether the content of the specified input stream matches the 
     * specified string line per line.
     * 
     * @param theInput The input stream to check
     * @param theExpectedString The expected string
     * @throws IOException If an I/O error occurs reading from the input stream
     */
    private void assertContains(InputStream theInput, String theExpectedString)
        throws IOException
    {
        try
        {
            BufferedReader inReader =
                new BufferedReader(new InputStreamReader(theInput));
            BufferedReader stringReader =
                new BufferedReader(new StringReader(theExpectedString));
            String line = null;
            while ((line = inReader.readLine()) != null)
            {
                assertEquals(stringReader.readLine(), line);
            }
        }
        finally
        {
            if (theInput != null)
            {
                theInput.close();
            }
        }
    }

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
