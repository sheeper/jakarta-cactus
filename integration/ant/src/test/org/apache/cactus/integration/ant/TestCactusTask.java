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

import org.apache.tools.ant.BuildException;

/**
 * Unit tests for {@link CactusTask}.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
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
