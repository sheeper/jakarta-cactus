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
package org.apache.cactus.integration.ant.deployment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import junit.framework.TestCase;

/**
 * Unit tests for {@link JarArchive}.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public final class TestJarArchive extends TestCase
{

    // Constructors ------------------------------------------------------------

    /**
     * @see TestCase#TestCase(String)
     */
    public TestJarArchive(String theTestName)
    {
        super(theTestName);        
    }

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
            new JarArchive((File) null) { };
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
            new JarArchive((InputStream) null)
            {
            };
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
        JarArchive jar = new JarArchive(getTestInput(
            "org/apache/cactus/integration/ant/deployment/randomaccess.jar"))
        {
        };
        assertContains(jar.getResource("firstEntry.txt"), "firstEntry");
        assertContains(jar.getResource("secondEntry.txt"), "secondEntry");
        assertContains(jar.getResource("secondEntry.txt"), "secondEntry");
        assertContains(jar.getResource("firstEntry.txt"), "firstEntry");
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
