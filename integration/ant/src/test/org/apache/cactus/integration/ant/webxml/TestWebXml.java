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
package org.apache.cactus.integration.ant.webxml;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Unit tests for {@link WebXml}.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class TestWebXml extends TestCase
{
    private DocumentBuilderFactory factory;

	private DocumentBuilder builder;

	/**
     * @see TestCase#TestCase(String)
     */
    public TestWebXml(String theTestName)
    {
        super(theTestName);        
    }

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
    
    public void testConstructionWithNullDocument() throws Exception
    {
        try
        {
            new WebXml(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // expected
        }
        
    }
    
    public void testGetVersion22() throws Exception
    {
        String xml = "<!DOCTYPE web-app "
            + "PUBLIC '-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN' "
            + "'http://java.sun.com/j2ee/dtds/web-app_2.2.dtd'>"
            + "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        assertEquals(WebXml.SERVLET_VERSION_2_2, webXml.getVersion());
    }
    
    public void testGetVersion23() throws Exception
    {
        String xml = "<!DOCTYPE web-app "
            + "PUBLIC '-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN' "
            + "'http://java.sun.com/dtd/web-app_2_3.dtd'>"
            + "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        assertEquals(WebXml.SERVLET_VERSION_2_3, webXml.getVersion());
    }
    
    public void testGetVersionUnknown() throws Exception
    {
        String xml = "<!DOCTYPE web-app "
            + "PUBLIC '-//Sun Microsystems, Inc.//DTD Web Application 1.9//EN' "
            + "'http://java.sun.com/dtd/web-app_1_9.dtd'>"
            + "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        assertNull(webXml.getVersion());
    }
    
    public void testGetVersionWithoutDoctype() throws Exception
    {
        String xml = "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        assertNull(webXml.getVersion());
    }
    
    public void testHasFilterWithNullName() throws Exception
    {
        String xml = "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        try
        {
            webXml.hasFilter(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // expected
        }
        
    }
    
    public void testHasFilterWithOneFilter() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        assertTrue(webXml.hasFilter("f1"));
        assertTrue(!webXml.hasFilter("f2"));
    }

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
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        assertTrue(webXml.hasFilter("f1"));
        assertTrue(webXml.hasFilter("f2"));
        assertTrue(webXml.hasFilter("f3"));
        assertTrue(!webXml.hasFilter("f4"));
    }

    public void testGetFilterElementWithOneFilter() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>".trim()
            + "    <filter-name>f1</filter-name>".trim()
            + "    <filter-class>fclass1</filter-class>".trim()
            + "  </filter>".trim()
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Element servletElement = webXml.getFilter("f1");
        assertNotNull(servletElement);
        assertEquals("filter", servletElement.getNodeName());
        assertEquals("filter-name",
            servletElement.getFirstChild().getNodeName());
        assertEquals("f1",
            servletElement.getFirstChild().getFirstChild().getNodeValue());
        assertEquals("filter-class",
            servletElement.getLastChild().getNodeName());
        assertEquals("fclass1",
            servletElement.getLastChild().getFirstChild().getNodeValue());
    }

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
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator filterNames = webXml.getFilterNames();
        assertEquals("f1", filterNames.next());
        assertEquals("f2", filterNames.next());
        assertEquals("f3", filterNames.next());
        assertTrue(!filterNames.hasNext());
    }
    
    public void testGetFilterMappingsWithOneMapping() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping</url-pattern>"
            + "  </filter-mapping>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator filterMappings = webXml.getFilterMappings("f1");
        assertEquals("/f1mapping", filterMappings.next());
        assertTrue(!filterMappings.hasNext());
    }
    
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
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator filterMappings = webXml.getFilterMappings("f1");
        assertEquals("/f1mapping1", filterMappings.next());
        assertEquals("/f1mapping2", filterMappings.next());
        assertEquals("/f1mapping3", filterMappings.next());
        assertTrue(!filterMappings.hasNext());
    }
    
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
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator filterMappings = webXml.getFilterMappings("f1");
        assertEquals("/f1mapping", filterMappings.next());
        assertTrue(!filterMappings.hasNext());
    }
    
    public void testAddFilterToEmptyDocument() throws Exception
    {
        String xml = "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Element filterElement = doc.createElement("filter");
        Element filterNameElement = doc.createElement("filter-name");
        filterNameElement.appendChild(doc.createTextNode("f1"));
        filterElement.appendChild(filterNameElement);
        Element filterClassElement = doc.createElement("filter-class");
        filterClassElement.appendChild(doc.createTextNode("f1class"));
        filterElement.appendChild(filterClassElement);
        webXml.addFilter(filterElement);
        assertTrue(webXml.hasFilter("f1"));
    }
    
    public void testAddFilterToDocumentWithAnotherFilter() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Element filterElement = doc.createElement("filter");
        Element filterNameElement = doc.createElement("filter-name");
        filterNameElement.appendChild(doc.createTextNode("f2"));
        filterElement.appendChild(filterNameElement);
        Element filterClassElement = doc.createElement("filter-class");
        filterClassElement.appendChild(doc.createTextNode("f2class"));
        filterElement.appendChild(filterClassElement);
        webXml.addFilter(filterElement);
        assertTrue(webXml.hasFilter("f1"));
        assertTrue(webXml.hasFilter("f2"));
    }

    public void testAddFilterToDocumentWithTheSameFilter() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Element filterElement = doc.createElement("filter");
        Element filterNameElement = doc.createElement("filter-name");
        filterNameElement.appendChild(doc.createTextNode("f1"));
        filterElement.appendChild(filterNameElement);
        Element filterClassElement = doc.createElement("filter-class");
        filterClassElement.appendChild(doc.createTextNode("f1class"));
        filterElement.appendChild(filterClassElement);
        try
        {
            webXml.addFilter(filterElement);
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException ise)
        {
            // expected
        }
    }

    public void testAddOneFilterInitParam() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        webXml.addFilterInitParam("f1", "f1param1", "f1param1value");
        Iterator initParams = webXml.getFilterInitParamNames("f1");
        assertEquals("f1param1", initParams.next());
        assertTrue(!initParams.hasNext());
    }

    public void testAddMultipleFilterInitParams() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        webXml.addFilterInitParam("f1", "f1param1", "f1param1value");
        webXml.addFilterInitParam("f1", "f1param2", "f1param2value");
        webXml.addFilterInitParam("f1", "f1param3", "f1param3value");
        Iterator initParams = webXml.getFilterInitParamNames("f1");
        assertEquals("f1param1", initParams.next());
        assertEquals("f1param2", initParams.next());
        assertEquals("f1param3", initParams.next());
        assertTrue(!initParams.hasNext());
    }

    public void testRemoveFilterUndefined() throws Exception
    {
        String xml = "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Element filterElement = webXml.removeFilter("f1");
        assertNull(filterElement);
    }

    public void testRemoveFilterFromDocumentWithOneFilter() throws Exception
    {
        String xml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        webXml.removeFilter("f1");
        assertTrue(!webXml.hasFilter("f1"));
    }

    public void testRemoveFilterFromDocumentWithMoreFilters()
        throws Exception
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
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        webXml.removeFilter("f1");
        assertTrue(!webXml.hasFilter("f1"));
        assertTrue(webXml.hasFilter("f2"));
        assertTrue(webXml.hasFilter("f3"));
    }

    public void testHasServletWithNullName() throws Exception
    {
        String xml = "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        try
        {
            webXml.hasServlet(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // expected
        }
        
    }
    
    public void testHasServletWithOneServlet() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        assertTrue(webXml.hasServlet("s1"));
        assertTrue(!webXml.hasServlet("s2"));
    }

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
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        assertTrue(webXml.hasServlet("s1"));
        assertTrue(webXml.hasServlet("s2"));
        assertTrue(webXml.hasServlet("s3"));
        assertTrue(!webXml.hasServlet("s4"));
    }

    public void testGetServletElementWithOneServlet() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>".trim()
            + "    <servlet-name>s1</servlet-name>".trim()
            + "    <servlet-class>sclass1</servlet-class>".trim()
            + "  </servlet>".trim()
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Element servletElement = webXml.getServlet("s1");
        assertNotNull(servletElement);
        assertEquals("servlet", servletElement.getNodeName());
        assertEquals("servlet-name",
            servletElement.getFirstChild().getNodeName());
        assertEquals("s1",
            servletElement.getFirstChild().getFirstChild().getNodeValue());
        assertEquals("servlet-class",
            servletElement.getLastChild().getNodeName());
        assertEquals("sclass1",
            servletElement.getLastChild().getFirstChild().getNodeValue());
    }

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
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator servletNames = webXml.getServletNames();
        assertEquals("s1", servletNames.next());
        assertEquals("s2", servletNames.next());
        assertEquals("s3", servletNames.next());
        assertTrue(!servletNames.hasNext());
    }

    public void testGetServletMappingsWithOneMapping() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet-mapping>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <url-pattern>/s1mapping</url-pattern>"
            + "  </servlet-mapping>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator servletMappings = webXml.getServletMappings("s1");
        assertEquals("/s1mapping", servletMappings.next());
        assertTrue(!servletMappings.hasNext());
    }

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
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator servletMappings = webXml.getServletMappings("s1");
        assertEquals("/s1mapping1", servletMappings.next());
        assertEquals("/s1mapping2", servletMappings.next());
        assertEquals("/s1mapping3", servletMappings.next());
        assertTrue(!servletMappings.hasNext());
    }

    public void testAddServletToEmptyDocument() throws Exception
    {
        String xml = "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Element servletElement = doc.createElement("servlet");
        Element servletNameElement = doc.createElement("servlet-name");
        servletNameElement.appendChild(doc.createTextNode("s1"));
        servletElement.appendChild(servletNameElement);
        Element servletClassElement = doc.createElement("servlet-class");
        servletClassElement.appendChild(doc.createTextNode("s1class"));
        servletElement.appendChild(servletClassElement);
        webXml.addServlet(servletElement);
        assertTrue(webXml.hasServlet("s1"));
    }

    public void testAddServletToDocumentWithAnotherServlet() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Element servletElement = doc.createElement("servlet");
        Element servletNameElement = doc.createElement("servlet-name");
        servletNameElement.appendChild(doc.createTextNode("s2"));
        servletElement.appendChild(servletNameElement);
        Element servletClassElement = doc.createElement("servlet-class");
        servletClassElement.appendChild(doc.createTextNode("s2class"));
        servletElement.appendChild(servletClassElement);
        webXml.addServlet(servletElement);
        assertTrue(webXml.hasServlet("s1"));
        assertTrue(webXml.hasServlet("s2"));
    }

    public void testAddServletToDocumentWithTheSameServlet() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Element servletElement = doc.createElement("servlet");
        Element servletNameElement = doc.createElement("servlet-name");
        servletNameElement.appendChild(doc.createTextNode("s1"));
        servletElement.appendChild(servletNameElement);
        Element servletClassElement = doc.createElement("servlet-class");
        servletClassElement.appendChild(doc.createTextNode("s1class"));
        servletElement.appendChild(servletClassElement);
        try
        {
            webXml.addServlet(servletElement);
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException ise)
        {
            // expected
        }
    }

    public void testAddOneServletInitParam() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        webXml.addServletInitParam("s1", "s1param1", "s1param1value");
        Iterator initParams = webXml.getServletInitParamNames("s1");
        assertEquals("s1param1", initParams.next());
        assertTrue(!initParams.hasNext());
    }

    public void testAddMultipleServletInitParams() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        webXml.addServletInitParam("s1", "s1param1", "s1param1value");
        webXml.addServletInitParam("s1", "s1param2", "s1param2value");
        webXml.addServletInitParam("s1", "s1param3", "s1param3value");
        Iterator initParams = webXml.getServletInitParamNames("s1");
        assertEquals("s1param1", initParams.next());
        assertEquals("s1param2", initParams.next());
        assertEquals("s1param3", initParams.next());
        assertTrue(!initParams.hasNext());
    }

    public void testRemoveServletUndefined() throws Exception
    {
        String xml = "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Element servletElement = webXml.removeServlet("s1");
        assertNull(servletElement);
    }

    public void testRemoveServletFromDocumentWithOneServlet() throws Exception
    {
        String xml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        webXml.removeServlet("s1");
        assertTrue(!webXml.hasServlet("s1"));
    }

    public void testRemoveServletFromDocumentWithMoreServlets()
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
            + "    <servlet-class>sclass3</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        webXml.removeServlet("s1");
        assertTrue(!webXml.hasServlet("s1"));
        assertTrue(webXml.hasServlet("s2"));
        assertTrue(webXml.hasServlet("s3"));
    }

    public void testGetSecurityConstraintEmpty()
        throws Exception
    {
        String xml = "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator securityConstraints = webXml.getSecurityConstraints();
        assertTrue(!securityConstraints.hasNext());
    }

    public void testGetSingleSecurityConstraint()
        throws Exception
    {
        String xml = "<web-app>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>wr1</web-resource-name>"
            + "    </web-resource-collection>"
            + "  </security-constraint>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator securityConstraints = webXml.getSecurityConstraints();
        assertNotNull(securityConstraints.next());
        assertTrue(!securityConstraints.hasNext());
    }

    public void testGetMutlipleSecurityConstraints()
        throws Exception
    {
        String xml = "<web-app>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>wr1</web-resource-name>"
            + "    </web-resource-collection>"
            + "  </security-constraint>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>wr2</web-resource-name>"
            + "    </web-resource-collection>"
            + "  </security-constraint>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>wr3</web-resource-name>"
            + "    </web-resource-collection>"
            + "  </security-constraint>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator securityConstraints = webXml.getSecurityConstraints();
        assertNotNull(securityConstraints.next());
        assertNotNull(securityConstraints.next());
        assertNotNull(securityConstraints.next());
        assertTrue(!securityConstraints.hasNext());
    }

    public void testGetLoginConfigEmpty()
        throws Exception
    {
        String xml = "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        assertNull(webXml.getLoginConfig());
    }

    public void testGetLoginConfig()
        throws Exception
    {
        String xml = "<web-app><login-config/></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        assertNotNull(webXml.getLoginConfig());
    }

    public void testGetSecurityRoleEmpty()
        throws Exception
    {
        String xml = "<web-app></web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator securityRoles = webXml.getSecurityRoles();
        assertTrue(!securityRoles.hasNext());
    }

    public void testGetSingleSecurityRole()
        throws Exception
    {
        String xml = "<web-app>"
            + "  <security-role>"
            + "    <role-name>r1</role-name>"
            + "  </security-role>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator securityRoles = webXml.getSecurityRoles();
        assertNotNull(securityRoles.next());
        assertTrue(!securityRoles.hasNext());
    }

    public void testGetMutlipleSecurityRoles()
        throws Exception
    {
        String xml = "<web-app>"
            + "  <security-role>"
            + "    <role-name>r1</role-name>"
            + "  </security-role>"
            + "  <security-role>"
            + "    <role-name>r2</role-name>"
            + "  </security-role>"
            + "  <security-role>"
            + "    <role-name>r3</role-name>"
            + "  </security-role>"
            + "</web-app>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WebXml webXml = new WebXml(doc);
        Iterator securityRoles = webXml.getSecurityRoles();
        assertNotNull(securityRoles.next());
        assertNotNull(securityRoles.next());
        assertNotNull(securityRoles.next());
        assertTrue(!securityRoles.hasNext());
    }

}
