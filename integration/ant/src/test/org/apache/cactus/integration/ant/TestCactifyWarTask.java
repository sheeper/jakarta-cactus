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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.jar.JarFile;

import org.apache.cactus.integration.ant.webxml.WebXml;
import org.apache.cactus.integration.ant.webxml.WebXmlIo;
import org.apache.cactus.integration.ant.webxml.WebXmlVersion;
import org.apache.tools.ant.BuildException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Unit tests for {@link CactifyWarTask}.
 * 
 * TODO: test whether all files contained by the source WAR are also added to
 *       the cactified WAR
 * TODO: test whether the nested redirector definitions get inserted into the
 *       cactified web.xml correctly
 * TODO: test whether the mergewebxml is actually merged into the cactified 
 *       web.xml  
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public final class TestCactifyWarTask extends AntTestCase
{

    // Inner Classes -----------------------------------------------------------

    /**
     * Entity resolver implementation that simply returns <code>null</code>
     * for each request. 
     */
    private static class NullEntityResolver implements EntityResolver
    {

        /**
         * @see org.xml.sax.EntityResolver#resolveEntity
         */
        public InputSource resolveEntity(String thePublicId, String theSystemId)
            throws SAXException, IOException
        {
            return new InputSource(new ByteArrayInputStream("".getBytes()));
        }

    }

    // Constructors ------------------------------------------------------------

    /**
     * @see TestCase#TestCase(String)
     */
    public TestCactifyWarTask(String theTestName)
    {
        super(theTestName,
            "org/apache/cactus/integration/ant/test-cactifywar.xml");
    }

    // TestCase Implementation -------------------------------------------------

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        getProject().addTaskDefinition("cactifywar", CactifyWarTask.class);
    }

    // Test Methods ------------------------------------------------------------

    /**
     * Verifies that the task throws an exception when neither the srcfile
     * nor the version attribute has been set.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testNeitherSrcFileNorVersionSet() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertEquals("You need to specify either the [srcfile] or the "
                + "[version] attribute", expected.getMessage());
        }
    }

    /**
     * Verifies that the task throws an exception when the destfile attribute 
     * has not been set.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testDestFileNotSet() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertEquals("You must specify the war file to create!",
                expected.getMessage());
        }
    }

    /**
     * Tests whether the Cactus test redirectors are correctly added to the 
     * descriptor of the cactified WAR. 
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testDefaultRedirectorsNoDoctype() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        JarFile destWar = new JarFile(destFile);
        try
        {
            WebXml webXml = WebXmlIo.parseWebXml(destWar,
                new NullEntityResolver());
            assertNull("The web.xml should not have a version specified",
                webXml.getVersion());
            assertServletMapping(webXml,
                "org.apache.cactus.server.ServletTestRedirector",
                "/ServletRedirector");
            assertJspMapping(webXml, "/jspRedirector.jsp", "/JspRedirector");
            // As the deployment descriptor in the source WAR doesn't contain a 
            // DOCTYPE, it is assumed to be a version 2.2 descriptor. Thus it 
            // should not contain a definition of the filter test redirector.
            // Assert that.
            assertTrue("Filter test redirector should not have been defined",
                !webXml.getFilterNames().hasNext());
        }
        finally
        {
            destWar.close();
        }
    }

    /**
     * Tests whether the Cactus test redirectors are correctly added to the 
     * descriptor of the cactified WAR. 
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testDefaultRedirectors22Doctype() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        JarFile destWar = new JarFile(destFile);
        try
        {
            WebXml webXml = WebXmlIo.parseWebXml(destWar,
                new NullEntityResolver());
            assertEquals(WebXmlVersion.V2_2, webXml.getVersion());
            assertServletMapping(webXml,
                "org.apache.cactus.server.ServletTestRedirector",
                "/ServletRedirector");
            assertJspMapping(webXml, "/jspRedirector.jsp", "/JspRedirector");
            assertTrue("Filter test redirector should not have been defined",
                !webXml.getFilterNames().hasNext());
        }
        finally
        {
            destWar.close();
        }
    }

    /**
     * Tests whether the Cactus test redirectors are correctly added to the 
     * descriptor of the cactified WAR. 
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testDefaultRedirectors23Doctype() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        JarFile destWar = new JarFile(destFile);
        try
        {
            WebXml webXml = WebXmlIo.parseWebXml(destWar,
                new NullEntityResolver());
            assertEquals(WebXmlVersion.V2_3, webXml.getVersion());
            assertServletMapping(webXml,
                "org.apache.cactus.server.ServletTestRedirector",
                "/ServletRedirector");
            assertJspMapping(webXml, "/jspRedirector.jsp", "/JspRedirector");
            assertFilterMapping(webXml,
                "org.apache.cactus.server.FilterTestRedirector",
                "/FilterRedirector");
        }
        finally
        {
            destWar.close();
        }
    }

    /**
     * Tests whether the Cactus test redirectors are correctly added to the 
     * descriptor of a WAR when no srcfile attribute had been set, and the 
     * version has been set to 2.2.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testDefaultRedirectorsNewWar22() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        JarFile destWar = new JarFile(destFile);
        try
        {
            WebXml webXml = WebXmlIo.parseWebXml(destWar,
                new NullEntityResolver());
            assertEquals(WebXmlVersion.V2_2, webXml.getVersion());
            assertServletMapping(webXml,
                "org.apache.cactus.server.ServletTestRedirector",
                "/ServletRedirector");
            assertJspMapping(webXml, "/jspRedirector.jsp", "/JspRedirector");
            assertTrue("Filter test redirector should not have been defined",
                !webXml.getFilterNames().hasNext());
        }
        finally
        {
            destWar.close();
        }
    }

    /**
     * Tests whether the Cactus test redirectors are correctly added to the 
     * descriptor of a WAR when no srcfile attribute had been set, and the 
     * version has been set to 2.3.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testDefaultRedirectorsNewWar23() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        JarFile destWar = new JarFile(destFile);
        try
        {
            WebXml webXml = WebXmlIo.parseWebXml(destWar,
                new NullEntityResolver());
            assertEquals(WebXmlVersion.V2_3, webXml.getVersion());
            assertServletMapping(webXml,
                "org.apache.cactus.server.ServletTestRedirector",
                "/ServletRedirector");
            assertJspMapping(webXml, "/jspRedirector.jsp", "/JspRedirector");
            assertFilterMapping(webXml,
                "org.apache.cactus.server.FilterTestRedirector",
                "/FilterRedirector");
        }
        finally
        {
            destWar.close();
        }
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Asserts that a filter of the specified class is defined in the given
     * deployment descriptor and mapped to a specific URL-pattern.
     * 
     * @param theWebXml The deployment descriptor
     * @param theFilterClass The name of the filter class
     * @param theMapping The URL-pattern that the filter is expected to be
     *        mapped to
     */
    private void assertFilterMapping(WebXml theWebXml, String theFilterClass,
        String theMapping)
    {
        Iterator names = theWebXml.getFilterNamesForClass(theFilterClass);
        assertTrue("Definition of " + theFilterClass + " not found",
            names.hasNext());
        String name = (String) names.next();
        Iterator mappings = theWebXml.getFilterMappings(name);
        assertTrue("Mapping for " + theFilterClass + " not found",
            mappings.hasNext());
        assertEquals(theMapping, mappings.next());
    }

    /**
     * Asserts that the specified JSP file is defined in the given deployment
     * descriptor and mapped to a specific URL-pattern.
     * 
     * @param theWebXml The deployment descriptor
     * @param theJspFile The JSP file name
     * @param theMapping The URL-pattern that the JSP file is expected to be
     *        mapped to
     */
    private void assertJspMapping(WebXml theWebXml, String theJspFile,
        String theMapping)
    {
        Iterator names = theWebXml.getServletNamesForJspFile(theJspFile);
        assertTrue("Definition of " + theJspFile + " not found",
            names.hasNext());
        String name = (String) names.next();
        Iterator mappings = theWebXml.getServletMappings(name);
        assertTrue("Mapping for " + theJspFile + " not found",
            mappings.hasNext());
        assertEquals(theMapping, mappings.next());
    }

    /**
     * Asserts that a servlet of the specified class is defined in the given
     * deployment descriptor and mapped to a specific URL-pattern.
     * 
     * @param theWebXml The deployment descriptor
     * @param theServletClass The name of servlet class
     * @param theMapping The URL-pattern that the servlet is expected to be
     *        mapped to
     */
    private void assertServletMapping(WebXml theWebXml, String theServletClass,
        String theMapping)
    {
        Iterator names = theWebXml.getServletNamesForClass(theServletClass);
        assertTrue("Definition of " + theServletClass + " not found",
            names.hasNext());
        String name = (String) names.next();
        Iterator mappings = theWebXml.getServletMappings(name);
        assertTrue("Mapping for " + theServletClass + " not found",
            mappings.hasNext());
        assertEquals(theMapping, mappings.next());
    }

}
