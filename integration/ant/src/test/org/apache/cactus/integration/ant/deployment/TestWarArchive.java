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

import java.io.File;

import junit.framework.TestCase;

/**
 * Unit tests for {@link WarArchive}.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
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
