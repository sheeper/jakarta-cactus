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
package org.apache.cactus.integration.ant.deployment.webapp;

import junit.framework.TestCase;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.codehaus.cargo.module.webapp.WebXmlUtils;
import org.codehaus.cargo.module.webapp.WebXmlVersion;
import org.codehaus.cargo.module.webapp.elements.FilterMapping;
import org.jdom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;

/**
 * Unit tests for {@link WebXml}.
 *
 * @version $Id: TestWebXml.java 239138 2005-02-11 09:17:14Z vmassol $
 */
public final class TestWebXml extends TestCase
{
    /**
     * The document builder factory.
     */
    private DocumentBuilderFactory factory;

    /**
     * The JAXP document builder.
     */
    private DocumentBuilder builder;

    /**
     * {@inheritDoc}
     * @see TestCase#setUp
     */
    public void setUp() throws ParserConfigurationException
    {
        factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);

        builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver()
        {
            public InputSource resolveEntity(String thePublicId, 
                String theSystemId) throws SAXException
            {
                return new InputSource(new StringReader(""));
            }
        });
    }
    
    /**
     * Tests whether the construction of a WebXml object with a
     * <code>null</code> parameter for the DOM document throws a
     * <code>NullPointerException</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testConstructionWithNullDocument() throws Exception
    {
        try
        {
            new WebXml(null, null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // expected
        }
        
    }
    
    /**
     * Tests whether a servlet API version 2.2 descriptor is correctly detected.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetVersion22() throws Exception
    {
        String xml = "<!DOCTYPE web-app "
            + "PUBLIC '-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN' "
            + "'http://java.sun.com/j2ee/dtds/web-app_2.2.dtd'>"
            + "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        assertEquals(WebXmlVersion.V2_2, webXml.getVersion());
    }
    
    /**
     * Tests whether a servlet API version 2.3 descriptor is correctly detected.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetVersion23() throws Exception
    {
        String xml = "<!DOCTYPE web-app "
            + "PUBLIC '-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN' "
            + "'http://java.sun.com/dtd/web-app_2_3.dtd'>"
            + "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        assertEquals(WebXmlVersion.V2_3, webXml.getVersion());
    }
    
    /**
     * Tests whether WebXml#getVersion returns <code>null</code> when the public
     * ID of the <code>DOCTYPE</code> is not recognized.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetVersionUnknown() throws Exception
    {
        String xml = "<!DOCTYPE web-app "
            + "PUBLIC '-//Sun Microsystems, Inc.//DTD Web Application 1.9//EN' "
            + "'http://java.sun.com/dtd/web-app_1_9.dtd'>"
            + "<web-app></web-app>";
        WebXml webXml = null;

        webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
            xml.getBytes()), null);
        assertNull(webXml.getVersion());
    }
    
    /**
     * Tests whether WebXml#getVersion returns <code>null</code> when the
     * <code>DOCTYPE</code> is missing.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetVersionWithoutDoctype() throws Exception
    {
        String xml = "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        assertNull(webXml.getVersion());
    }
    
    /**
     * Tests whether calling {@link WebXml.hasFilter} with <code>null</code> as
     * filter name parameter results in a <code>NullPointerException</code> 
     * being thrown. 
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testHasFilterWithNullName() throws Exception
    {
        String xml = "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        try
        {
            WebXmlUtils.hasFilter(webXml, null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // expected
        }
        
    }
    
    /**
     * Tests whether {@link WebXml.hasFilter} returns the correct value for
     * a descriptor containing one filter definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testHasFilterWithOneFilter() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        assertTrue(WebXmlUtils.hasFilter(webXml, "f1"));
        assertTrue(!WebXmlUtils.hasFilter(webXml, "f2"));
    }

    /**
     * Tests whether {@link WebXml.hasFilter} returns the correct values for
     * a descriptor containing multiple filter definitions.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testHasFilterWithMultipleFilters() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "  <filter>"
            + "    <filter-name>f2</filter-name>"
            + "    <filter-class>fclass2</filter-class>"
            + "  </filter>"
            + "  <filter>"
            + "    <filter-name>f3</filter-name>"
            + "    <filter-class>fclass3</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
            xml.getBytes()), null);
        assertTrue(WebXmlUtils.hasFilter(webXml, "f1"));
        assertTrue(WebXmlUtils.hasFilter(webXml, "f2"));
        assertTrue(WebXmlUtils.hasFilter(webXml, "f3"));
        assertTrue(!WebXmlUtils.hasFilter(webXml, "f4"));
    }

    /**
//     * Tests whether a DOM element representing a single filter definition can
//     * be correctly retrieved from a descriptor containing only that filter.
//     * 
//     * @throws Exception If an unexpected error occurs
//     */
//    public void testGetFilterElementWithOneFilter() throws Exception
//    {
//        String xml = "<web-app>"
//            + "  <filter>".trim()
//            + "    <filter-name>f1</filter-name>".trim()
//            + "    <filter-class>fclass1</filter-class>".trim()
//            + "  </filter>".trim()
//            + "</web-app>";
//        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
//            xml.getBytes()), null);
//        org.jdom.Element jServletElement = WebXmlUtils.getFilter(webXml, "f1");
//        org.w3c.dom.Element servletElement = convertToDOM(jServletElement);
//        
//        assertNotNull(servletElement);
//        assertEquals("filter", servletElement.getNodeName()); 
//        assertEquals("filter-name",
//            servletElement.getFirstChild().getNodeName());
//        assertEquals("f1",
//            servletElement.getFirstChild().getFirstChild().getNodeValue());
//        assertEquals("filter-class",
//            servletElement.getLastChild().getNodeName());
//        assertEquals("fclass1",
//            servletElement.getLastChild().getFirstChild().getNodeValue());
//    }

    /**
     * Tests whether the filter names are retrieved in the expected order.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetFilterNames() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "  <filter>"
            + "    <filter-name>f2</filter-name>"
            + "    <filter-class>fclass2</filter-class>"
            + "  </filter>"
            + "  <filter>"
            + "    <filter-name>f3</filter-name>"
            + "    <filter-class>fclass3</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        Iterator filterNames = WebXmlUtils.getFilterNames(webXml);
        assertEquals("f1", filterNames.next());
        assertEquals("f2", filterNames.next());
        assertEquals("f3", filterNames.next());
        assertTrue(!filterNames.hasNext());
    }
    
    /**
     * Tests whether a retrieving a filter name by the name of the class
     * implementing the filter works correctly for a descriptor with a single
     * filter definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetFilterNamesForClassWithSingleFilter() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>f1class</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        Iterator filterNames = WebXmlUtils.getFilterNamesForClass(
                webXml, "f1class");
        assertEquals("f1", filterNames.next());
        assertTrue(!filterNames.hasNext());
    }
    
    /**
     * Tests whether a retrieving the filter names by the name of the class
     * implementing the filter works correctly for a descriptor with multiple
     * filter definitions.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetFilterNamesForClassWithMultipleFilters() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>f1class</filter-class>"
            + "  </filter>"
            + "  <filter>"
            + "    <filter-name>f2</filter-name>"
            + "    <filter-class>f2class</filter-class>"
            + "  </filter>"
            + "  <filter>"
            + "    <filter-name>f3</filter-name>"
            + "    <filter-class>f1class</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        Iterator filterNames = WebXmlUtils.getFilterNamesForClass(webXml, 
                "f1class");
        assertEquals("f1", filterNames.next());
        assertEquals("f3", filterNames.next());
        assertTrue(!filterNames.hasNext());
    }
    
    /**
     * Tests whether a filter-mapping is correctly retrieved from a descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetFilterMappingsWithOneMapping() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping</url-pattern>"
            + "  </filter-mapping>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        Iterator filterMappings = WebXmlUtils.getFilterMappings(webXml, "f1");
        assertEquals("/f1mapping", filterMappings.next());
        assertTrue(!filterMappings.hasNext());
    }
    
    /**
     * Tests whether multiple filter-mappings are correctly retrieved from a 
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetFilterMappingsWithMultipleMappings() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping1</url-pattern>"
            + "  </filter-mapping>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping2</url-pattern>"
            + "  </filter-mapping>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping3</url-pattern>"
            + "  </filter-mapping>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        Iterator filterMappings = WebXmlUtils.getFilterMappingElements(webXml,
                "f1");
        assertEquals("/f1mapping1", ((FilterMapping) 
                filterMappings.next()).getUrlPattern());
        assertEquals("/f1mapping2", ((FilterMapping) 
                filterMappings.next()).getUrlPattern());
        assertEquals("/f1mapping3", ((FilterMapping) 
                filterMappings.next()).getUrlPattern());
        assertTrue(!filterMappings.hasNext());
    }
    
    /**
     * Tests whether a filter-mapping is correctly retrieved from a descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetFilterMappingsWithFilter() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>f1class</filter-class>"
            + "  </filter>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping</url-pattern>"
            + "  </filter-mapping>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        Iterator filterMappings = WebXmlUtils.getFilterMappings(webXml, "f1");
        assertEquals("/f1mapping", filterMappings.next());
        assertTrue(!filterMappings.hasNext());
    }

    /**
     * Tests whether a single context-param is correctly inserted into an empty
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddContextParamToEmptyDocument() throws Exception
    {
        String xml = "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
               xml.getBytes()), null);
        WebXmlUtils.addContextParam(webXml, "param", "value");
        assertTrue(WebXmlUtils.hasContextParam(webXml, "param"));
    }
    
    /**
     * Tests whether a single filter is correctly inserted into an empty
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddFilterToEmptyDocument() throws Exception
    {
        String xml = "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
               new ByteArrayInputStream(xml.getBytes()), null);
        WebXmlUtils.addFilter(webXml, "f1", "f1class");
        assertTrue(WebXmlUtils.hasFilter(webXml, "f1"));
    }

    /**
     * Tests whether a single context param is correctly inserted into a 
     * descriptor that already contains an other context param definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddContextParamToDocumentWithAnotherContextParam() 
        throws Exception
    {
        String xml = "<web-app>"
            + "  <context-param>"
            + "    <param-name>param1</param-name>"
            + "    <param-value>value1</param-value>"
            + "  </context-param>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        WebXmlUtils.addContextParam(webXml, "param2", "value2");
        assertTrue(WebXmlUtils.hasContextParam(webXml, "param1"));
        assertTrue(WebXmlUtils.hasContextParam(webXml, "param2"));
    }
    
    /**
     * Tests whether a single filter is correctly inserted into a descriptor 
     * that already contains an other filter definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddFilterToDocumentWithAnotherFilter() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        WebXmlUtils.addFilter(webXml, "f2", "f2class");
        assertTrue(WebXmlUtils.hasFilter(webXml, "f1"));
        assertTrue(WebXmlUtils.hasFilter(webXml, "f2"));
    }

    /**
     * Tests whether trying to add a context param to a descriptor that already
     * contains a context param definition with the same name results in an
     * exception.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddContextParamToDocumentWithTheSameContextParam() 
        throws Exception
    {
        String xml = "<web-app>"
            + "  <context-param>"
            + "    <param-name>param</param-name>"
            + "    <param-value>value</param-value>"
            + "  </context-param>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        try
        {
            WebXmlUtils.addContextParam(webXml, "param", "value");
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException ise)
        {
            // expected
        }
    }

    /**
     * Tests whether trying to add a filter to a descriptor that already
     * contains a filter definition with the same name results in a exception.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddFilterToDocumentWithTheSameFilter() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        try
        {
            WebXmlUtils.addFilter(webXml, "f1", "f1class");
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException ise)
        {
            // expected
        }
    }
    
    /**
     * Tests whether a single initialization parameter can be added to a filter
     * definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddOneFilterInitParam() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        WebXmlUtils.addFilterInitParam(webXml, "f1", "f1param1", 
                "f1param1value");
        Iterator initParams = WebXmlUtils.getFilterInitParamNames(webXml, "f1");
        
        assertEquals("f1param1", initParams.next());
        assertTrue(!initParams.hasNext());
    }

    /**
     * Tests whether multiple initialization parameter can be added to a filter
     * definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddMultipleFilterInitParams() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        WebXmlUtils.addFilterInitParam(webXml, "f1", "f1param1", 
                "f1param1value");
        WebXmlUtils.addFilterInitParam(webXml, "f1", "f1param2", 
                "f1param2value");
        WebXmlUtils.addFilterInitParam(webXml, "f1", "f1param3", 
                "f1param3value");
        Iterator initParams = WebXmlUtils.getFilterInitParamNames(webXml, "f1");
        assertEquals("f1param1", initParams.next());
        assertEquals("f1param2", initParams.next());
        assertEquals("f1param3", initParams.next());
        assertTrue(!initParams.hasNext());
    }

    /**
     * Tests whether a single filter can be added using the method that takes 
     * a string for the filter name and a string for the filter class.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddFilterWithNameAndClass() throws Exception
    {
        String xml = "<web-app>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        WebXmlUtils.addServlet(webXml, "f1", "f1class");
        assertTrue(WebXmlUtils.hasServlet(webXml, "f1"));
    }

    /**
     * Tests whether calling {@link WebXml#hasServlet} with a <code>null</code> 
     * parameter as servlet name throws a <code>NullPointerException</code>.  
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testHasServletWithNullName() throws Exception
    {
        String xml = "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        try
        {
            WebXmlUtils.hasServlet(webXml, null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // expected
        }
        
    }
    
    /**
     * Tests whether {@link WebXml#hasServlet} reports the correct values for a
     * descriptor containing a single servlet definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testHasServletWithOneServlet() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        assertTrue(WebXmlUtils.hasServlet(webXml, "s1"));
        assertTrue(!WebXmlUtils.hasServlet(webXml, "s2"));
    }

    /**
     * Tests whether {@link WebXml#hasServlet} reports the correct values for a
     * descriptor containing multiple servlet definitions.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testHasServletWithMultipleServlets() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "  <servlet>"
            + "    <servlet-name>s2</servlet-name>"
            + "    <servlet-class>sclass2</servlet-class>"
            + "  </servlet>"
            + "  <servlet>"
            + "    <servlet-name>s3</servlet-name>"
            + "    <servlet-class>sclass3</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        assertTrue(WebXmlUtils.hasServlet(webXml, "s1"));
        assertTrue(WebXmlUtils.hasServlet(webXml, "s2"));
        assertTrue(WebXmlUtils.hasServlet(webXml, "s3"));
        assertTrue(!WebXmlUtils.hasServlet(webXml, "s4"));
    }

    /**
     * Tests whether a servlet element is correctly retrieved from a descriptor
     * containing only one servlet definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
//    public void testGetServletElementWithOneServlet() throws Exception
//    {
//        String xml = "<web-app>"
//            + "  <servlet>".trim()
//            + "    <servlet-name>s1</servlet-name>".trim()
//            + "    <servlet-class>sclass1</servlet-class>".trim()
//            + "  </servlet>".trim()
//            + "</web-app>";
//        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
//            xml.getBytes()), null);
//        org.jdom.Element jServletElement = (Element)WebXmlUtils.getServlet(
//            webXml, "s1");
//        org.w3c.dom.Element servletElement = convertToDOM(jServletElement);
//        
//        assertNotNull(servletElement);
//        assertEquals("servlet", servletElement.getNodeName());
//        assertEquals("servlet-name",
//            servletElement.getFirstChild().getNodeName());
//        assertEquals("s1",
//            servletElement.getFirstChild().getFirstChild().getNodeValue());
//        assertEquals("servlet-class",
//            servletElement.getLastChild().getNodeName());
//        assertEquals("sclass1",
//            servletElement.getLastChild().getFirstChild().getNodeValue());
//    }

    /**
     * Tests whether the names of the servlets defined in a descriptor are
     * correctly returned in the expected order.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetServletNames() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "  <servlet>"
            + "    <servlet-name>s2</servlet-name>"
            + "    <servlet-class>sclass2</servlet-class>"
            + "  </servlet>"
            + "  <servlet>"
            + "    <servlet-name>s3</servlet-name>"
            + "    <servlet-class>sclass3</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        Iterator servletNames = WebXmlUtils.getServletNames(webXml);
        assertEquals("s1", servletNames.next());
        assertEquals("s2", servletNames.next());
        assertEquals("s3", servletNames.next());
        assertTrue(!servletNames.hasNext());
    }

    /**
     * Tests whether a retrieving a servlet name by the name of the class
     * implementing the servlet works correctly for a descriptor with a single
     * servlet definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetServletNamesForClassWithSingleServlet() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>s1class</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        Iterator servletNames = WebXmlUtils.getServletNamesForClass(webXml,
                "s1class");
        assertEquals("s1", servletNames.next());
        assertTrue(!servletNames.hasNext());
    }
    
    /**
     * Tests whether a retrieving the servlet names by the name of the class
     * implementing the servlet works correctly for a descriptor with multiple
     * servlet definitions.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetServletNamesForClassWithMultipleServlets()
        throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "  <servlet>"
            + "    <servlet-name>s2</servlet-name>"
            + "    <servlet-class>sclass2</servlet-class>"
            + "  </servlet>"
            + "  <servlet>"
            + "    <servlet-name>s3</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        Iterator servletNames = WebXmlUtils.getServletNamesForClass(webXml, 
                "sclass1");
        assertEquals("s1", servletNames.next());
        assertEquals("s3", servletNames.next());
        assertTrue(!servletNames.hasNext());
    }
    
    /**
     * Tests whether a retrieving a servlet name by the path of the JSP file
     * implementing the servlet works correctly for a descriptor with a single
     * servlet definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetServletNamesForJspFileWithSingleServlet()
        throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <jsp-file>/s1.jsp</jsp-file>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        Iterator servletNames = WebXmlUtils.getServletNamesForJspFile(webXml, 
                "/s1.jsp");
        assertEquals("s1", servletNames.next());
        assertTrue(!servletNames.hasNext());
    }
    
    /**
     * Tests whether a retrieving the servlet names by the path of the JSP file
     * implementing the servlet works correctly for a descriptor with multiple
     * servlet definitions.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetServletNamesForJspFileWithMultipleServlets()
        throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <jsp-file>/s1.jsp</jsp-file>"
            + "  </servlet>"
            + "  <servlet>"
            + "    <servlet-name>s2</servlet-name>"
            + "    <servlet-class>sclass2</servlet-class>"
            + "  </servlet>"
            + "  <servlet>"
            + "    <servlet-name>s3</servlet-name>"
            + "    <jsp-file>/s3.jsp</jsp-file>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        Iterator servletNames = WebXmlUtils.getServletNamesForJspFile(webXml,
                "/s3.jsp");
        assertEquals("s3", servletNames.next());
        assertTrue(!servletNames.hasNext());
    }
    
    /**
     * Tests whether a single serrvlet-mapping is correctly retrieved from a
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetServletMappingsWithOneMapping() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet-mapping>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <url-pattern>/s1mapping</url-pattern>"
            + "  </servlet-mapping>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        Iterator servletMappings = WebXmlUtils.getServletMappings(webXml, "s1");
        assertEquals("/s1mapping", servletMappings.next());
        assertTrue(!servletMappings.hasNext());
    }

    /**
     * Tests whether multiple servlet mappings are correctly retrieved from a
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetServletMappingsWithMultipleMappings() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet-mapping>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <url-pattern>/s1mapping1</url-pattern>"
            + "  </servlet-mapping>"
            + "  <servlet-mapping>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <url-pattern>/s1mapping2</url-pattern>"
            + "  </servlet-mapping>"
            + "  <servlet-mapping>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <url-pattern>/s1mapping3</url-pattern>"
            + "  </servlet-mapping>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        Iterator servletMappings = WebXmlUtils.getServletMappings(webXml, "s1");
        assertEquals("/s1mapping1", servletMappings.next());
        assertEquals("/s1mapping2", servletMappings.next());
        assertEquals("/s1mapping3", servletMappings.next());
        assertTrue(!servletMappings.hasNext());
    }

    /**
     * Tests whether a single servlet can be added to an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddServletToEmptyDocument() throws Exception
    {
        String xml = "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        WebXmlUtils.addServlet(webXml, "s1", "s1class");
        assertTrue(WebXmlUtils.hasServlet(webXml, "s1"));
    }

    /**
     * Tests whether a single servlet can be added to a descriptor already 
     * containing an other servlet.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddServletToDocumentWithAnotherServlet() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        WebXmlUtils.addServlet(webXml, "s2", "s2class");
        assertTrue(WebXmlUtils.hasServlet(webXml, "s1"));
        assertTrue(WebXmlUtils.hasServlet(webXml, "s2"));
    }

    /**
     * Tests whether trying to add a servlet to a descriptor that already
     * contains a servlet with the same name results in an exception.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddServletToDocumentWithTheSameServlet() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        try
        {
            WebXmlUtils.addServlet(webXml, "s1", "s1class");
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException ise)
        {
            // expected
        }
    }

    /**
     * Tests whether a single initialization parameter is correctly added to an
     * existing servlet definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddOneServletInitParam() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        WebXmlUtils.addServletInitParam(webXml, "s1", "s1param1",
                "s1param1value");
        Iterator initParams = WebXmlUtils.getServletInitParamNames(webXml,
                "s1");
        assertEquals("s1param1", initParams.next());
        assertTrue(!initParams.hasNext());
    }

    /**
     * Tests whether multiple initialization parameters are correctly added to
     * an existing servlet definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddMultipleServletInitParams() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(
                new ByteArrayInputStream(xml.getBytes()), null);
        WebXmlUtils.addServletInitParam(webXml, "s1", "s1param1",
                "s1param1value");
        WebXmlUtils.addServletInitParam(webXml, "s1", "s1param2",
                "s1param2value");
        WebXmlUtils.addServletInitParam(webXml, "s1", "s1param3",
                "s1param3value");
        Iterator initParams = WebXmlUtils.getServletInitParamNames(webXml,
                "s1");
        assertEquals("s1param1", initParams.next());
        assertEquals("s1param2", initParams.next());
        assertEquals("s1param3", initParams.next());
        assertTrue(!initParams.hasNext());
    }

    /**
     * Tests whether a single servlet can be added using the method that takes 
     * a string for the servlet name and a string for the servlet class.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddServletWithNameAndClass() throws Exception
    {
        String xml = "<web-app>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        WebXmlUtils.addServlet(webXml, "s1", "s1class");
        assertTrue(WebXmlUtils.hasServlet(webXml, "s1"));
    }

    /**
     * Tests whether a single servlet can be added using the method that takes 
     * a string for the servlet name and a string for the JSP file.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddServletWithNameAndJspFile() throws Exception
    {
        String xml = "<web-app>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        WebXmlUtils.addJspFile(webXml, "s1", "s1.jsp");
        assertTrue(WebXmlUtils.hasServlet(webXml, "s1"));
    }

    /**
     * Tests whether a security-constraint with no roles is successfully added
     * to an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddSecurityConstraint()
        throws Exception
    {
        String xml = "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        WebXmlUtils.addSecurityConstraint(webXml, "wrn", "/url", 
                Collections.EMPTY_LIST);
        assertTrue(WebXmlUtils.hasSecurityConstraint(webXml, "/url"));
    }

//    /**
//     * Tests whether a security-constraint with two roles is successfully added
//     * to an empty descriptor.
//     * 
//     * @throws Exception If an unexpected error occurs
//     */
//    public void testAddSecurityConstraintWithRoles()
//        throws Exception
//    {
//        String xml = "<web-app></web-app>";
//        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(xml.getBytes()), null);
//        List roles = new ArrayList();
//        roles.add("role1");
//        roles.add("role2");
//        WebXmlUtils.addSecurityConstraint(webXml, "wrn", "/url", roles);
//        assertTrue(WebXmlUtils.hasSecurityConstraint(webXml, "/url"));
//        Element jSecurityConstraintElement =
//            (Element)WebXmlUtils.getSecurityConstraint(webXml, "/url");
//        assertNotNull(jSecurityConstraintElement);
//        
//        org.w3c.dom.Element securityConstraintElement = convertToDOM(jSecurityConstraintElement);
//        
//        org.w3c.dom.Element authConstraintElement = (org.w3c.dom.Element)
//            securityConstraintElement.getElementsByTagName(
//                "auth-constraint").item(0);
//        assertNotNull(authConstraintElement);
//        NodeList roleNameElements =
//            authConstraintElement.getElementsByTagName("role-name");
//        assertEquals(2, roleNameElements.getLength());
//        assertEquals("role1",
//            roleNameElements.item(0).getChildNodes().item(0).getNodeValue());
//        assertEquals("role2",
//            roleNameElements.item(1).getChildNodes().item(0).getNodeValue());
//    }

    /**
     * Tests whether checking an empty descriptor for a login configuration
     * results in <code>false</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testHasLoginConfigEmpty()
        throws Exception
    {
        String xml = "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        assertTrue(!WebXmlUtils.hasLoginConfig(webXml));
    }

    /**
     * Tests whether checking a descriptor with a login configuration for a
     * login configuration results in <code>true</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testHasLoginConfig()
        throws Exception
    {
        String xml = "<web-app>"
            + "  <login-config>"
            + "    <auth-method>BASIC</auth-method>"
            + "  </login-config>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                 xml.getBytes()), null);
        assertTrue(WebXmlUtils.hasLoginConfig(webXml));
    }

    /**
     * Tests retrieving the authentication method from a descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetLoginConfigAuthMethod()
        throws Exception
    {
        String xml = "<web-app>"
            + "  <login-config>"
            + "    <auth-method>BASIC</auth-method>"
            + "  </login-config>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        assertEquals("BASIC", WebXmlUtils.getLoginConfigAuthMethod(webXml));
    }

    /**
     * Tests retrieving the authentication method from a descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testSetLoginConfigAdding()
        throws Exception
    {
        String xml = "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        WebXmlUtils.setLoginConfig(webXml, "BASIC", "Test Realm");
        assertTrue(WebXmlUtils.hasLoginConfig(webXml));
        assertEquals("BASIC", WebXmlUtils.getLoginConfigAuthMethod(webXml));
    }

    /**
     * Tests retrieving the authentication method from a descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testSetLoginConfigReplacing()
        throws Exception
    {
        String xml = "<web-app>"
        + "  <login-config>"
        + "    <auth-method>DIGEST</auth-method>"
        + "  </login-config>"
        + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        WebXmlUtils.setLoginConfig(webXml, "BASIC", "Test Realm");
        assertTrue(WebXmlUtils.hasLoginConfig(webXml));
        assertEquals("BASIC", WebXmlUtils.getLoginConfigAuthMethod(webXml));
    }

    /**
     * Tests whether checking an empty descriptor for some security constraint
     * results in <code>false</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testHasSecurityConstraintEmpty()
        throws Exception
    {
        String xml = "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        assertTrue(!WebXmlUtils.hasSecurityConstraint(webXml, "/TestUrl"));
    }

    /**
     * Tests whether a single security-constraint element in the descriptor is
     * correctly retrieved.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetSingleSecurityConstraint()
        throws Exception
    {
        String xml = "<web-app>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>wr1</web-resource-name>"
            + "      <url-pattern>/url1</url-pattern>"
            + "    </web-resource-collection>"
            + "  </security-constraint>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        assertTrue(WebXmlUtils.hasSecurityConstraint(webXml, "/url1"));
        Element securityConstraintElement =
           (Element) WebXmlUtils.getSecurityConstraint(webXml, "/url1");
        assertNotNull(securityConstraintElement);
    }

    /**
     * Tests whether multiple security-constraint elements are returned in 
     * the expected order.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetMutlipleSecurityConstraints()
        throws Exception
    {
        String xml = "<web-app>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>wr1</web-resource-name>"
            + "      <url-pattern>/url1</url-pattern>"
            + "    </web-resource-collection>"
            + "  </security-constraint>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>wr2</web-resource-name>"
            + "      <url-pattern>/url2</url-pattern>"
            + "    </web-resource-collection>"
            + "  </security-constraint>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>wr3</web-resource-name>"
            + "      <url-pattern>/url3</url-pattern>"
            + "    </web-resource-collection>"
            + "  </security-constraint>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        assertTrue(WebXmlUtils.hasSecurityConstraint(webXml, "/url1"));
        assertTrue(WebXmlUtils.hasSecurityConstraint(webXml, "/url2"));
        assertTrue(WebXmlUtils.hasSecurityConstraint(webXml, "/url3"));
        Iterator securityConstraints =
            webXml.getElements(WebXmlType.SECURITY_CONSTRAINT);
        assertNotNull(securityConstraints.next());
        assertNotNull(securityConstraints.next());
        assertNotNull(securityConstraints.next());
        assertTrue(!securityConstraints.hasNext());
    }

    /**
     * Tests whether retrieving the login-config from an empty descriptor 
     * returns <code>null</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetLoginConfigEmpty()
        throws Exception
    {
        String xml = "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        assertTrue(!webXml.getElements(WebXmlType.LOGIN_CONFIG).hasNext());
    }

    /**
     * Tests whether the login-config element can be correctly retrieved.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetLoginConfig()
        throws Exception
    {
        String xml = "<web-app><login-config/></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        assertTrue(webXml.getElements(WebXmlType.LOGIN_CONFIG).hasNext());
    }

    /**
     * Tests whether checking an empty descriptor for some security roles
     * results in <code>false</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testHasSecurityRoleEmpty()
        throws Exception
    {
        String xml = "<web-app></web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        assertTrue(!WebXmlUtils.hasSecurityRole(webXml, "someRole"));
        assertTrue(!WebXmlUtils.getSecurityRoleNames(webXml).hasNext());
    }

//    /**
//     * Tests whether a single security-role element is correctly retrieved.
//     * 
//     * @throws Exception If an unexpected error occurs
//     */
//    public void testGetSingleSecurityRole()
//        throws Exception
//    {
//        String xml = "<web-app>"
//            + "  <security-role>".trim()
//            + "    <role-name>r1</role-name>".trim()
//            + "  </security-role>".trim()
//            + "</web-app>";
//        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(xml.getBytes()), null);
//        assertTrue(WebXmlUtils.hasSecurityRole(webXml, "r1"));
//        org.jdom.Element jSecurityRoleElement = (Element)WebXmlUtils.getSecurityRole(webXml, "r1");
//        org.w3c.dom.Element securityRoleElement = convertToDOM(jSecurityRoleElement);
//        
//        assertNotNull(securityRoleElement);
//        assertEquals("security-role", securityRoleElement.getNodeName());
//        assertEquals("role-name",
//            securityRoleElement.getFirstChild().getNodeName());
//        assertEquals("r1",
//            securityRoleElement.getFirstChild().getFirstChild().getNodeValue());
//        Iterator securityRoleNames = WebXmlUtils.getSecurityRoleNames(webXml);
//        assertTrue(securityRoleNames.hasNext());
//        assertEquals("r1", securityRoleNames.next());
//        assertTrue(!securityRoleNames.hasNext());
//    }

//    /**
//     * Tests whether multiple security-role elements are correctly retrieved
//     * in the expected order.
//     * 
//     * @throws Exception If an unexpected error occurs
//     */
//    public void testGetMutlipleSecurityRoles()
//        throws Exception
//    {
//        String xml = "<web-app>"
//            + "  <security-role>".trim()
//            + "    <role-name>r1</role-name>".trim()
//            + "  </security-role>".trim()
//            + "  <security-role>".trim()
//            + "    <role-name>r2</role-name>".trim()
//            + "  </security-role>".trim()
//            + "  <security-role>".trim()
//            + "    <role-name>r3</role-name>".trim()
//            + "  </security-role>".trim()
//            + "</web-app>";
//        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(xml.getBytes()), null);
//        assertTrue(WebXmlUtils.hasSecurityRole(webXml, "r1"));
//        org.jdom.Element jSecurityRoleElement1 = (Element)WebXmlUtils.getSecurityRole(webXml, "r1");
//        org.w3c.dom.Element securityRoleElement1 = convertToDOM(jSecurityRoleElement1);
//        
//        assertNotNull(securityRoleElement1);
//        assertEquals("security-role", securityRoleElement1.getNodeName());
//        assertEquals("role-name",
//            securityRoleElement1.getFirstChild().getNodeName());
//        assertEquals("r1",
//            securityRoleElement1.getFirstChild().getFirstChild().
//                getNodeValue());
//        assertTrue(WebXmlUtils.hasSecurityRole(webXml, "r2"));
//        org.jdom.Element jSecurityRoleElement2 = (Element)WebXmlUtils.getSecurityRole(webXml, "r2");
//        org.w3c.dom.Element securityRoleElement2 = convertToDOM(jSecurityRoleElement2);
//        
//        assertNotNull(securityRoleElement2);
//        assertEquals("security-role", securityRoleElement2.getNodeName());
//        assertEquals("role-name",
//            securityRoleElement2.getFirstChild().getNodeName());
//        assertEquals("r2",
//            securityRoleElement2.getFirstChild().getFirstChild().
//                getNodeValue());
//        assertTrue(WebXmlUtils.hasSecurityRole(webXml, "r3"));
//        org.jdom.Element jSecurityRoleElement3 = (Element)WebXmlUtils.getSecurityRole(webXml, "r3");
//        org.w3c.dom.Element securityRoleElement3 = convertToDOM(jSecurityRoleElement3);
//        
//        assertNotNull(securityRoleElement3);
//        assertEquals("security-role", securityRoleElement3.getNodeName());
//        assertEquals("role-name",
//            securityRoleElement3.getFirstChild().getNodeName());
//        assertEquals("r3",
//            securityRoleElement3.getFirstChild().getFirstChild().
//                getNodeValue());
//        Iterator securityRoleNames = WebXmlUtils.getSecurityRoleNames(webXml);
//        assertTrue(securityRoleNames.hasNext());
//        assertEquals("r1", securityRoleNames.next());
//        assertTrue(securityRoleNames.hasNext());
//        assertEquals("r2", securityRoleNames.next());
//        assertTrue(securityRoleNames.hasNext());
//        assertEquals("r3", securityRoleNames.next());
//        assertTrue(!securityRoleNames.hasNext());
//    }

//    /**
//     * Tests whether a filter is inserted before a servlet element.
//     * 
//     * @throws Exception If an unexpected error occurs
//     */
//    public void testElementOrderFilterBeforeServlet() throws Exception
//    {
//        String xml = "<web-app>"
//            + "  <servlet>".trim()
//            + "    <servlet-name>s1</servlet-name>".trim()
//            + "    <servlet-class>s1class</servlet-class>".trim()
//            + "  </servlet>".trim()
//            + "</web-app>";
//        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
//            xml.getBytes()), null);
//        WebXmlUtils.addFilter(webXml, "f1", "f1class");
//        org.w3c.dom.Document doc = builder.parse(
//            new ByteArrayInputStream(xml.getBytes()));
//        System.out.println("!@!@!@!@!@!@!@!@@!"+doc.getTextContent());
//        NodeList order = doc.getDocumentElement().getChildNodes();
//        assertEquals("filter", order.item(0).getNodeName());
//        assertEquals("servlet", order.item(1).getNodeName());
//    }

//    /**
//     * Tests whether a filter is inserted before the comment node preceding a 
//     * servlet definition.
//     * 
//     * @throws Exception If an unexpected error occurs
//     */
//    public void testElementOrderFilterBeforeServletWithComment()
//        throws Exception
//    {
//        String xml = "<web-app>"
//            + "  <!-- My servlets -->".trim()
//            + "  <servlet>".trim()
//            + "    <servlet-name>s1</servlet-name>".trim()
//            + "    <servlet-class>s1class</servlet-class>".trim()
//            + "  </servlet>".trim()
//            + "</web-app>";
//        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
//            xml.getBytes()), null);
//        WebXmlUtils.addFilter(webXml, "f1", "f1class");
//        org.w3c.dom.Document doc = builder.parse(
//            new ByteArrayInputStream(xml.getBytes()));
//        NodeList order = doc.getDocumentElement().getChildNodes();
//        assertEquals("filter", order.item(0).getNodeName());
//        assertEquals("#comment", order.item(1).getNodeName());
//        assertEquals("servlet", order.item(2).getNodeName());
//    }

//    /**
//     * Tests whether a servlet is inserted after a filter.
//     * 
//     * @throws Exception If an unexpected error occurs
//     */
//    public void testElementOrderServletAfterFilter() throws Exception
//    {
//        String xml = "<web-app>"
//            + "  <filter>".trim()
//            + "    <filter-name>f1</filter-name>".trim()
//            + "    <filter-class>f1class</filter-class>".trim()
//            + "  </filter>".trim()
//            + "</web-app>";
//        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
//            xml.getBytes()), null);
//        WebXmlUtils.addServlet(webXml, "s1", "s1class");
//        org.w3c.dom.Document doc = builder.parse(
//            new ByteArrayInputStream(xml.getBytes()));
//        NodeList order = doc.getDocumentElement().getChildNodes();
//        assertEquals("filter", order.item(0).getNodeName());
//        assertEquals("servlet", order.item(1).getNodeName());
//    }

//    /**
//     * Tests whether a servlet is inserted after a filter that is preceded by
//     * a comment node.
//     * 
//     * @throws Exception If an unexpected error occurs
//     */
//    public void testElementOrderServletAfterFilterWithComment()
//        throws Exception
//    {
//        String xml = "<web-app>"
//            + "  <!-- My filters -->".trim()
//            + "  <filter>".trim()
//            + "    <filter-name>f1</filter-name>".trim()
//            + "    <filter-class>f1class</filter-class>".trim()
//            + "  </filter>".trim()
//            + "</web-app>";
//        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
//            xml.getBytes()), null);
//        WebXmlUtils.addServlet(webXml, "s1", "s1class");
//        org.w3c.dom.Document doc = builder.parse(
//            new ByteArrayInputStream(xml.getBytes()));
//        NodeList order = doc.getDocumentElement().getChildNodes();
//        assertEquals("#comment", order.item(0).getNodeName());
//        assertEquals("filter", order.item(1).getNodeName());
//        assertEquals("servlet", order.item(2).getNodeName());
//    }

    /**
     * Tests that the a servlets run-as role-name can be extracted.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetServletRunAsRole() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "    <run-as>"
            + "      <role-name>r1</role-name>"
            + "    </run-as>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        String roleName = WebXmlUtils.getServletRunAsRoleName(webXml, "s1");
        assertEquals("r1", roleName);
    }
    
    /**
     * Tests that a run-as role-name can be added to a servlet.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddServletRunAsRole() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml webXml = WebXmlIo.parseWebXml(new ByteArrayInputStream(
                xml.getBytes()), null);
        WebXmlUtils.addServletRunAsRoleName(webXml, "s1", "r1");
        String roleName = WebXmlUtils.getServletRunAsRoleName(webXml, "s1");
        assertEquals("r1", roleName);
    }
    
    // Private Methods ---------------------------------------------------------

//    /**
//     * Create a <code>context-param</code> element containing the specified 
//     * text in the child elements.
//     * 
//     * @param theDocument The DOM document
//     * @param theParamName The parameter name
//     * @param theParamValue The parameter value
//     * @return The created element
//     */
//    public Element createContextParamElement(Document theDocument,
//        String theParamName, String theParamValue)
//    {
//        Element contextParamElement = 
//            theDocument.createElement("context-param");
//        Element paramNameElement = theDocument.createElement("param-name");
//        paramNameElement.appendChild(theDocument
//            .createTextNode(theParamName));
//        contextParamElement.appendChild(paramNameElement);
//        Element paramValueElement = theDocument.createElement("param-value");
//        paramValueElement.appendChild(
//            theDocument.createTextNode(theParamValue));
//        contextParamElement.appendChild(paramValueElement);
//        return contextParamElement;
//    }
    
//    /**
//     * Create a <code>filter</code> element containing the specified text in 
//     * the child elements.
//     * 
//     * @param theDocument The DOM document
//     * @param theFilterName The name of the filter
//     * @param theFilterClass The name of the filter implementation class
//     * @return The created element
//     */
//    public Element createFilterElement(Document theDocument,
//            String theFilterName, String theFilterClass)
//    {
//        Element filterElement = theDocument.createElement("filter");
//        Element filterNameElement = theDocument.createElement("filter-name");
//        filterNameElement.appendChild(
//            theDocument.createTextNode(theFilterName));
//        filterElement.appendChild(filterNameElement);
//        Element filterClassElement = theDocument.createElement(
//            "filter-class");
//        filterClassElement.appendChild(
//            theDocument.createTextNode(theFilterClass));
//        filterElement.appendChild(filterClassElement);
//        return filterElement;
//    }
//    
//    /**
//     * Create a <code>servlet</code> element containing the specified text in 
//     * the child elements.
//     * 
//     * @param theDocument The DOM document
//     * @param theServletName The name of the servlet
//     * @param theServletClass The name of the servlet implementation class
//     * @return The created element
//     */
//    public Element createServletElement(Document theDocument,
//            String theServletName, String theServletClass)
//    {
//        Element filterElement = theDocument.createElement("servlet");
//        Element filterNameElement = theDocument.createElement("servlet-name");
//        filterNameElement.appendChild(
//            theDocument.createTextNode(theServletName));
//        filterElement.appendChild(filterNameElement);
//        Element filterClassElement = theDocument
//            .createElement("servlet-class");
//        filterClassElement.appendChild(
//            theDocument.createTextNode(theServletClass));
//        filterElement.appendChild(filterClassElement);
//        return filterElement;
//    }
    
//    public org.w3c.dom.Document convertToDOM(org.jdom.Document jdomDocument) 
//    throws JDOMException
//    {
//        DOMOutputter outputter = new DOMOutputter();
//        return outputter.output(jdomDocument);
//    
//    }
//    
//    public org.w3c.dom.Element convertToDOM(org.jdom.Element jdomElement) 
//    throws JDOMException
//    {
//        org.w3c.dom.Document document = convertToDOM(
//            jdomElement.getDocument());
//        return document.getElementById(jdomElement.getName());
//    }
    
}
