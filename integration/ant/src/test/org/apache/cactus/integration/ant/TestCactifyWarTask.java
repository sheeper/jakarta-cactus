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

import java.io.File;
import java.util.Iterator;

import org.apache.cactus.integration.ant.deployment.WarArchive;
import org.apache.cactus.integration.ant.deployment.WebXml;
import org.apache.cactus.integration.ant.deployment.WebXmlTag;
import org.apache.cactus.integration.ant.deployment.WebXmlVersion;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Unit tests for {@link CactifyWarTask}.
 * 
 * TODO: test whether all files contained by the source WAR are also added to
 *       the cactified WAR
 * TODO: test whether the mergewebxml is actually merged into the cactified 
 *       web.xml  
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public final class TestCactifyWarTask extends AntTestCase
{

    // Constructors ------------------------------------------------------------

    /**
     * @see AntTestCase#AntTestCase
     */
    public TestCactifyWarTask()
    {
        super("org/apache/cactus/integration/ant/test-cactifywar.xml");
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
     * Verifies the error raised when the source archive does not contain a web
     * deployment descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testSrcFileWithoutWebXml() throws Exception
    {
        try
        {
            executeTestTarget();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            assertEquals("The source file does not contain a deployment "
                + "descriptor", expected.getMessage());
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
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
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
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertEquals(WebXmlVersion.V2_2, webXml.getVersion());
        assertServletMapping(webXml,
            "org.apache.cactus.server.ServletTestRedirector",
            "/ServletRedirector");
        assertJspMapping(webXml, "/jspRedirector.jsp", "/JspRedirector");
        assertTrue("Filter test redirector should not have been defined",
            !webXml.getFilterNames().hasNext());
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
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertEquals(WebXmlVersion.V2_3, webXml.getVersion());
        assertServletMapping(webXml,
            "org.apache.cactus.server.ServletTestRedirector",
            "/ServletRedirector");
        assertJspMapping(webXml, "/jspRedirector.jsp", "/JspRedirector");
        assertFilterMapping(webXml,
            "org.apache.cactus.server.FilterTestRedirector",
            "/FilterRedirector");
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
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertEquals(WebXmlVersion.V2_2, webXml.getVersion());
        assertServletMapping(webXml,
            "org.apache.cactus.server.ServletTestRedirector",
            "/ServletRedirector");
        assertJspMapping(webXml, "/jspRedirector.jsp", "/JspRedirector");
        assertTrue("Filter test redirector should not have been defined",
            !webXml.getFilterNames().hasNext());
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
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertEquals(WebXmlVersion.V2_3, webXml.getVersion());
        assertServletMapping(webXml,
            "org.apache.cactus.server.ServletTestRedirector",
            "/ServletRedirector");
        assertJspMapping(webXml, "/jspRedirector.jsp", "/JspRedirector");
        assertFilterMapping(webXml,
            "org.apache.cactus.server.FilterTestRedirector",
            "/FilterRedirector");
    }

    /**
     * Verifies that the mapping of the servlet redirector is correctly
     * overridden by a nested 'servletredirector' element.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testCustomServletRedirectorMapping() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertServletMapping(webXml,
            "org.apache.cactus.server.ServletTestRedirector",
            "/test/servletRedirector");
    }

    /**
     * Verifies that the mapping of the JSP redirector is correctly overridden
     * by a nested 'jspredirector' element.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testCustomJspRedirectorMapping() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertJspMapping(webXml, "/jspRedirector.jsp", "/test/jspRedirector");
    }

    /**
     * Verifies that the mapping of the filter redirector is correctly
     * overridden by a nested 'filterredirector' element.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testCustomFilterRedirectorMapping() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertFilterMapping(webXml,
            "org.apache.cactus.server.FilterTestRedirector",
            "/test/filterRedirector");
    }

    /**
     * Verifies that no definition of the filter redirector is added to a 
     * Servlet 2.2 descriptor, even when explicitly requested by a nested
     * 'filterredirector' element.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testCustomFilterRedirectorMappingIgnored() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertTrue("The filter redirector should not have been defined",
            !webXml.getFilterNamesForClass(
                "org.apache.cactus.server.FilterTestRedirector").hasNext());
    }

    /**
     * Verifies that two servlet redirectors with different names and mappings
     * are added as expected.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMultipleNamedServletRedirectors() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertTrue(webXml.hasServlet("ServletRedirector"));
        assertEquals("/test/ServletRedirector",
            webXml.getServletMappings("ServletRedirector").next());
        assertTrue(webXml.hasServlet("ServletRedirectorSecure"));
        assertEquals("/test/ServletRedirectorSecure",
            webXml.getServletMappings("ServletRedirectorSecure").next());
    }

    /**
     * Verifies that two JSP redirectors with different names and mappings
     * are added as expected.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMultipleNamedJspRedirectors() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertTrue(webXml.hasServlet("JspRedirector"));
        assertEquals("/test/JspRedirector",
            webXml.getServletMappings("JspRedirector").next());
        assertTrue(webXml.hasServlet("JspRedirectorSecure"));
        assertEquals("/test/JspRedirectorSecure",
            webXml.getServletMappings("JspRedirectorSecure").next());
    }

    /**
     * Verifies that two filter redirectors with different names and mappings
     * are added as expected.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMultipleNamedFilterRedirectors() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertTrue(webXml.hasFilter("FilterRedirector"));
        assertEquals("/test/FilterRedirector",
            webXml.getFilterMappings("FilterRedirector").next());
        assertTrue(webXml.hasFilter("FilterRedirectorSecure"));
        assertEquals("/test/FilterRedirectorSecure",
            webXml.getFilterMappings("FilterRedirectorSecure").next());
    }

    /**
     * Verifies that a secured servlet redirector gets added alongside the role
     * names.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testSecuredServletRedirector() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertTrue(webXml.hasServlet("ServletRedirectorSecure"));
        assertEquals("/ServletRedirectorSecure",
            webXml.getServletMappings("ServletRedirectorSecure").next());
        assertTrue(webXml.hasSecurityRole("test"));
        assertTrue(webXml.hasSecurityRole("cactus"));
        assertTrue(webXml.hasSecurityConstraint("/ServletRedirectorSecure"));
        Element securityConstraintElement =
            webXml.getSecurityConstraint("/ServletRedirectorSecure");
        assertNotNull(securityConstraintElement);
        Element authConstraintElement = (Element)
            securityConstraintElement.getElementsByTagName(
                "auth-constraint").item(0);
        assertNotNull(authConstraintElement);
        NodeList roleNameElements =
            authConstraintElement.getElementsByTagName("role-name");
        assertEquals(2, roleNameElements.getLength());
        assertEquals("test",
            roleNameElements.item(0).getChildNodes().item(0).getNodeValue());
        assertEquals("cactus",
            roleNameElements.item(1).getChildNodes().item(0).getNodeValue());
        Iterator loginConfigElements =
            webXml.getElements(WebXmlTag.LOGIN_CONFIG);
        assertTrue(loginConfigElements.hasNext());
        Element loginConfigElement = (Element) loginConfigElements.next();
        Element authMethodElement = (Element)
            loginConfigElement.getElementsByTagName("auth-method").item(0);
        assertEquals("BASIC",
            authMethodElement.getChildNodes().item(0).getNodeValue());
    }

    /**
     * Verifies that a already existent login configuration does not get
     * replaced by the default BASIC login configuration when secured
     * redirectors are defined.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testLoginConfigNotOverwritten() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/destfile.war");
        WarArchive destWar = new WarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertEquals("FORM", webXml.getLoginConfigAuthMethod());
    }

    /**
     * Verifies that JARs already contained by the source archive are not added
     * again.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testNoDuplicateJars() throws Exception
    {
        executeTestTarget();

        assertMessageLogged("The AspectJ Runtime JAR is already present in the "
            + "WAR", Project.MSG_VERBOSE);
        assertMessageLogged("The Cactus Framework JAR is already present in "
            + "the WAR", Project.MSG_VERBOSE);
        assertMessageLogged("The Commons-Logging JAR is already present in the "
            + "WAR", Project.MSG_VERBOSE);
        assertMessageLogged("The Commons-HttpClient JAR is already present in "
            + "the WAR", Project.MSG_VERBOSE);
        assertMessageLogged("The JUnit JAR is already present in the WAR",
            Project.MSG_VERBOSE);
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
