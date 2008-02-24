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

import java.io.File;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.codehaus.cargo.module.webapp.DefaultWarArchive;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlTag;
import org.codehaus.cargo.module.webapp.WebXmlVersion;
import org.codehaus.cargo.module.webapp.weblogic.WeblogicXml;
import org.codehaus.cargo.module.webapp.weblogic.WeblogicXmlTag;
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
 * @version $Id: TestCactifyWarTask.java 239162 2005-04-26 09:57:59Z grimsell $
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
     * {@inheritDoc}
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
            assertEquals("You need to specify either the [srcfile] or the "
                         + "[version] attribute", 
                         expected.getMessage());
        }
    }
    
    /**
     * Verifies an empty web was created when the source archive does not 
     * contain a web deployment descriptor but specifies the version.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testSrcFileWithoutWebXmlNewWebXml22() throws Exception
    {
        try
        {
            executeTestTarget();
        }
        catch (BuildException e)
        {
            fail("The WAR source file does not contain a "
                 + "WEB-INF/web.xml deployment descriptor, but Cactus "
                 + "should have created an empty one");
        }
    }
    
    /**
     * Verifies an empty web was created when the source archive does not 
     * contain a web deployment descriptor but specifies the version.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testSrcFileWithoutWebXmlNewWebXml23() throws Exception
    {
        try
        {
            executeTestTarget();
        }
        catch (BuildException e)
        {
            fail("The WAR source file does not contain a "
                 + "WEB-INF/web.xml deployment descriptor, but Cactus "
                 + "should have created an empty one");
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
        WarArchive destWar = new DefaultWarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertNull("The web.xml should not have a version specified",
            webXml.getVersion());
        assertServletMapping(webXml,
            "org.apache.cactus.server.ServletTestRedirector",
            "ServletRedirector",
            "/ServletRedirector");
        assertJspMapping(webXml, "/jspRedirector.jsp", "JspRedirector", 
            "/JspRedirector");
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
        WarArchive destWar = new DefaultWarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertEquals(WebXmlVersion.V2_2, webXml.getVersion());
        assertServletMapping(webXml, 
            "org.apache.cactus.server.ServletTestRedirector",
            "ServletRedirector",
            "/ServletRedirector");
        assertJspMapping(webXml, "/jspRedirector.jsp", "JspRedirector",
            "/JspRedirector");
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
        WarArchive destWar = new DefaultWarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertEquals(WebXmlVersion.V2_3, webXml.getVersion());
        assertServletMapping(webXml, 
            "org.apache.cactus.server.ServletTestRedirector",
            "ServletRedirector",
            "/ServletRedirector");
        assertJspMapping(webXml, "/jspRedirector.jsp", "JspRedirector",
            "/JspRedirector");
        assertFilterMapping(webXml,
            "org.apache.cactus.server.FilterTestRedirector",
            "FilterRedirector",
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
        WarArchive destWar = new DefaultWarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertEquals(WebXmlVersion.V2_2, webXml.getVersion());
        assertServletMapping(webXml,
            "org.apache.cactus.server.ServletTestRedirector",
            "ServletRedirector",
            "/ServletRedirector");
        assertJspMapping(webXml, "/jspRedirector.jsp", "JspRedirector",
            "/JspRedirector");
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
        WarArchive destWar = new DefaultWarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertEquals(WebXmlVersion.V2_3, webXml.getVersion());
        assertServletMapping(webXml,
            "org.apache.cactus.server.ServletTestRedirector",                
            "ServletRedirector",
            "/ServletRedirector");
        assertJspMapping(webXml, "/jspRedirector.jsp", "JspRedirector",
            "/JspRedirector");
        assertFilterMapping(webXml,
            "org.apache.cactus.server.FilterTestRedirector",
            "FilterRedirector",
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
        WarArchive destWar = new DefaultWarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertServletMapping(webXml,
            "org.apache.cactus.server.ServletTestRedirector",                
            "ServletRedirector", 
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
        WarArchive destWar = new DefaultWarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertJspMapping(webXml, "/jspRedirector.jsp", "JspRedirector",
            "/test/jspRedirector");
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
        WarArchive destWar = new DefaultWarArchive(destFile);
        WebXml webXml = destWar.getWebXml();
        assertFilterMapping(webXml,
            "org.apache.cactus.server.FilterTestRedirector",
            "FilterRedirector",
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
        WarArchive destWar = new DefaultWarArchive(destFile);
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
        WarArchive destWar = new DefaultWarArchive(destFile);
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
        WarArchive destWar = new DefaultWarArchive(destFile);
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
        WarArchive destWar = new DefaultWarArchive(destFile);
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
        WarArchive destWar = new DefaultWarArchive(destFile);
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
        WarArchive destWar = new DefaultWarArchive(destFile);
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

    /**
     * Tests that ejb refs can be added for weblogic.
     * 
     * @throws Exception iIf an unexpected error occurs
     */
    public void testAddWeblogicEjbRefs() throws Exception
    {
        executeTestTarget();
        
        File destFile = getProject().resolveFile("work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile);
        
        // test web.xml
        WebXml webXml = destWar.getWebXml();
        Iterator i = webXml.getElements(WebXmlTag.EJB_LOCAL_REF);
        Element e = (Element) i.next();
        NodeList nl = e.getElementsByTagName("ejb-ref-name");
        Element f = (Element) nl.item(0);
        assertEquals("MyEjb", f.getFirstChild().getNodeValue());
        nl = e.getElementsByTagName("ejb-ref-type");
        f = (Element) nl.item(0);
        assertEquals("Session", f.getFirstChild().getNodeValue());
        nl = e.getElementsByTagName("local-home");
        f = (Element) nl.item(0);
        assertEquals("com.wombat.MyEjbHome", f.getFirstChild().getNodeValue());
        nl = e.getElementsByTagName("local");
        f = (Element) nl.item(0);
        assertEquals("com.wombat.MyEjb", f.getFirstChild().getNodeValue());
        
        // test weblogic.xml
        WeblogicXml weblogicXml = (WeblogicXml) webXml.getVendorDescriptor();
        i = weblogicXml.getElements(WeblogicXmlTag.EJB_REFERENCE_DESCRIPTION);
        e = (Element) i.next();
        nl = e.getElementsByTagName("ejb-ref-name");
        f = (Element) nl.item(0);
        assertEquals("MyEjb", f.getFirstChild().getNodeValue());
        nl = e.getElementsByTagName("jndi-name");
        f = (Element) nl.item(0);
        assertEquals("/wombat/MyEjb", f.getFirstChild().getNodeValue());
    }
    
    // Private Methods ---------------------------------------------------------

    /**
     * Asserts that a filter of the specified class is defined in the given
     * deployment descriptor and mapped to a specific URL-pattern.
     * 
     * @param theWebXml The deployment descriptor
     * @param theFilterClass The name of the filter class
     * @param theFilterName The name of the filter
     * @param theMapping The URL-pattern that the filter is expected to be
     *        mapped to
     */
    private void assertFilterMapping(WebXml theWebXml, String theFilterClass,
        String theFilterName, String theMapping)
    {
        Iterator names = theWebXml.getFilterNamesForClass(theFilterClass);

        // Look for the definition that matches the JSP servlet name
        boolean found = false; 
        String name = null;
        while (names.hasNext())
        {
            name = (String) names.next();
            if (name.equals(theFilterName))
            {
                found = true;
                break;
            }
        }
        
        if (!found)
        {
            fail("Definition of [" + theFilterClass + "(" + theFilterName
                + ")] not found");
        }

        Iterator mappings = theWebXml.getFilterMappings(name);
        assertTrue("Mapping for [" + theFilterClass + "(" + theFilterName
            + ")] not found", mappings.hasNext());
        assertEquals(theMapping, mappings.next());
    }

    /**
     * Asserts that the specified JSP file is defined in the given deployment
     * descriptor and mapped to a specific URL-pattern.
     * 
     * @param theWebXml The deployment descriptor
     * @param theJspFile The JSP file name
     * @param theJspName The JSP servlet name
     * @param theMapping The URL-pattern that the JSP file is expected to be
     *        mapped to
     */
    private void assertJspMapping(WebXml theWebXml, String theJspFile,
        String theJspName, String theMapping)
    {
        Iterator names = theWebXml.getServletNamesForJspFile(theJspFile);

        // Look for the definition that matches the JSP servlet name
        boolean found = false; 
        String name = null;
        while (names.hasNext())
        {
            name = (String) names.next();
            if (name.equals(theJspName))
            {
                found = true;
                break;
            }
        }
        
        if (!found)
        {
            fail("Definition of [" + theJspFile + "(" + theJspName
                + ")] not found");
        }
        
        Iterator mappings = theWebXml.getServletMappings(name);
        assertTrue("Mapping for [" + theJspFile + "(" + theJspName
            + ")] not found", mappings.hasNext());
        assertEquals(theMapping, mappings.next());
    }

    /**
     * Asserts that a servlet of the specified name is defined in the given
     * deployment descriptor and mapped to a specific URL-pattern.
     * 
     * @param theWebXml The deployment descriptor
     * @param theServletClass The name of servlet class
     * @param theServletName The name of the servlet
     * @param theMapping The URL-pattern that the servlet is expected to be
     *        mapped to
     */
    private void assertServletMapping(WebXml theWebXml, String theServletClass, 
        String theServletName, String theMapping)
    {
        Iterator names = theWebXml.getServletNamesForClass(theServletClass);
        
        // Look for the definition that matches the servlet name
        boolean found = false; 
        String name = null;
        while (names.hasNext())
        {
            name = (String) names.next();
            if (name.equals(theServletName))
            {
                found = true;
                break;
            }
        }

        if (!found)
        {
            fail("Definition of [" + theServletClass + "(" + theServletName
                + ")] not found");
        }
        
        Iterator mappings = theWebXml.getServletMappings(name);
        assertTrue("Mapping for [" + theServletClass + "(" + theServletName
            + ")] not found", mappings.hasNext());
        assertEquals(theMapping, mappings.next());
    }

}
