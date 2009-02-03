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
package org.apache.cactus.integration.ant;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.codehaus.cargo.module.webapp.DefaultWarArchive;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.codehaus.cargo.module.webapp.WebXmlUtils;
import org.codehaus.cargo.module.webapp.WebXmlVersion;
import org.codehaus.cargo.module.webapp.elements.SecurityConstraint;
import org.codehaus.cargo.module.webapp.weblogic.WeblogicXml;
import org.codehaus.cargo.module.webapp.weblogic.WeblogicXmlTag;
import org.jdom.Element;

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
        executeTestTarget();
        
        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
        WebXml webXml = destWar.getWebXml();
        //When specifying a srcFile with no web.xml in it, 
        //cargo creates a new web.xml with a null version.
        assertNull(webXml.getVersion());
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
        WebXml webXml = destWar.getWebXml();
        assertNull("The web.xml should not have a version specified",
            webXml.getDocument().getDocType());
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
            !WebXmlUtils.getFilterNames(webXml).hasNext());
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
        WebXml webXml = destWar.getWebXml();
        assertEquals(WebXmlVersion.V2_2, webXml.getVersion());
        assertServletMapping(webXml, 
            "org.apache.cactus.server.ServletTestRedirector",
            "ServletRedirector",
            "/ServletRedirector");
        assertJspMapping(webXml, "/jspRedirector.jsp", "JspRedirector",
            "/JspRedirector");
        assertTrue("Filter test redirector should not have been defined",
            !WebXmlUtils.getFilterNames(webXml).hasNext());
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
        WebXml webXml = destWar.getWebXml();
        assertEquals(WebXmlVersion.V2_2, webXml.getVersion());
        assertServletMapping(webXml,
            "org.apache.cactus.server.ServletTestRedirector",
            "ServletRedirector",
            "/ServletRedirector");
        assertJspMapping(webXml, "/jspRedirector.jsp", "JspRedirector",
            "/JspRedirector");
        assertTrue("Filter test redirector should not have been defined",
            !WebXmlUtils.getFilterNames(webXml).hasNext());
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
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
        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
        WebXml webXml = destWar.getWebXml();
        assertTrue("The filter redirector should not have been defined",
            !WebXmlUtils.getFilterNamesForClass(webXml,
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
        WebXml webXml = destWar.getWebXml();
        assertTrue(WebXmlUtils.hasServlet(webXml, "ServletRedirector"));
        assertEquals("/test/ServletRedirector",
            WebXmlUtils.getServletMappings(webXml, "ServletRedirector").next());
        assertTrue(WebXmlUtils.hasServlet(webXml, "ServletRedirectorSecure"));
        assertEquals("/test/ServletRedirectorSecure",
            WebXmlUtils.getServletMappings(webXml, 
            "ServletRedirectorSecure").next());
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
        WebXml webXml = destWar.getWebXml();
        assertTrue(WebXmlUtils.hasServlet(webXml, "JspRedirector"));
        assertEquals("/test/JspRedirector",
            WebXmlUtils.getServletMappings(webXml, "JspRedirector").next());
        assertTrue(WebXmlUtils.hasServlet(webXml, "JspRedirectorSecure"));
        assertEquals("/test/JspRedirectorSecure",
            WebXmlUtils.getServletMappings(webXml, 
            "JspRedirectorSecure").next());
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
        WebXml webXml = destWar.getWebXml();
        assertTrue(WebXmlUtils.hasFilter(webXml, "FilterRedirector"));
        assertEquals("/test/FilterRedirector",
            WebXmlUtils.getFilterMappings(webXml, "FilterRedirector").next());
        assertTrue(WebXmlUtils.hasFilter(webXml, "FilterRedirectorSecure"));
        assertEquals("/test/FilterRedirectorSecure",
            WebXmlUtils.getFilterMappings(webXml, 
            "FilterRedirectorSecure").next());
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
        WebXml webXml = destWar.getWebXml();
        assertTrue(WebXmlUtils.hasServlet(webXml, "ServletRedirectorSecure"));
        assertEquals("/ServletRedirectorSecure",
            WebXmlUtils.getServletMappings(webXml, 
            "ServletRedirectorSecure").next());
        assertTrue(WebXmlUtils.hasSecurityRole(webXml, "test"));
        assertTrue(WebXmlUtils.hasSecurityRole(webXml, "cactus"));
        assertTrue(WebXmlUtils.hasSecurityConstraint(webXml,
            "/ServletRedirectorSecure"));
        org.jdom.Element securityConstraintElement =
            (org.jdom.Element) WebXmlUtils.getSecurityConstraint (webXml, 
            "/ServletRedirectorSecure");
        assertNotNull(securityConstraintElement);
        Element authConstraintElement = (Element)
            ((SecurityConstraint) securityConstraintElement).getChildren(
                "auth-constraint").get(0);
        assertNotNull(authConstraintElement);
        List roleNameElements =
            authConstraintElement.getChildren("role-name");
        assertEquals(2, roleNameElements.size());
        assertEquals("test",
                ((Element) roleNameElements.get(0)).getValue());
        assertEquals("cactus",
                ((Element) roleNameElements.get(1)).getValue());
        Iterator loginConfigElements =
            webXml.getElements(WebXmlType.LOGIN_CONFIG);
        assertTrue(loginConfigElements.hasNext());
        Element loginConfigElement = (Element) loginConfigElements.next();
        Element authMethodElement = (Element) loginConfigElement.getChildren(
            "auth-method").get(0);
        assertEquals("BASIC",
            ((Element) authMethodElement).getValue());
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

        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
        WebXml webXml = destWar.getWebXml();
        assertEquals("FORM", WebXmlUtils.getLoginConfigAuthMethod(webXml));
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
        
        File destFile = getProject().resolveFile("target/work/destfile.war");
        WarArchive destWar = new DefaultWarArchive(destFile.getAbsolutePath());
        
        // test web.xml
        WebXml webXml = destWar.getWebXml();
        Iterator i = webXml.getElements(WebXmlType.EJB_LOCAL_REF);
        org.jdom.Element e = (org.jdom.Element) i.next();
        
        List nl = e.getChildren("ejb-ref-name");
        Element f = (Element) nl.get(0);
        assertEquals("MyEjb", ((Element) f).getValue());
        nl = e.getChildren("ejb-ref-type");
        f = (Element) nl.get(0);
        assertEquals("Session", ((Element) f).getValue());
        nl = e.getChildren("local-home");
        f = (Element) nl.get(0);
        assertEquals("com.wombat.MyEjbHome", ((Element) f).getValue());
        nl = e.getChildren("local");
        f = (Element) nl.get(0);
        assertEquals("com.wombat.MyEjb", ((Element) f).getValue());
        
        // test weblogic.xml
        
        Iterator iter = webXml.getVendorDescriptors();
        WeblogicXml weblogicXml = null;
        while (iter.hasNext())
        {
            weblogicXml = (WeblogicXml) iter.next();
        }
        
        i = weblogicXml.getElements(WeblogicXmlTag.EJB_REFERENCE_DESCRIPTION);
        e = (Element) i.next();
        nl = e.getChildren("ejb-ref-name");
        f = (Element) nl.get(0);
        assertEquals("MyEjb", ((Element) f).getValue());
        nl = e.getChildren("jndi-name");
        f = (Element) nl.get(0);
        assertEquals("/wombat/MyEjb", ((Element) f).getValue());
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
        Iterator names = WebXmlUtils.getFilterNamesForClass(theWebXml, 
            theFilterClass);

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

        Iterator mappings = WebXmlUtils.getFilterMappings(theWebXml, name);
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
        Iterator names = WebXmlUtils.getServletNamesForJspFile(theWebXml,
            theJspFile);

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
        
        Iterator mappings = WebXmlUtils.getServletMappings(theWebXml, name);
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
        Iterator names = WebXmlUtils.getServletNamesForClass(theWebXml,
            theServletClass);
        
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
        
        Iterator mappings = WebXmlUtils.getServletMappings(theWebXml, name);
        assertTrue("Mapping for [" + theServletClass + "(" + theServletName
            + ")] not found", mappings.hasNext());
        assertEquals(theMapping, mappings.next());
    }
}
