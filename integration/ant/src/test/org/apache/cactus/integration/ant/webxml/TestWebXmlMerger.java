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
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.w3c.dom.Document;

/**
 * Unit tests for {@link WebXmlMerger}.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class TestWebXmlMerger extends TestCase
{
    private DocumentBuilderFactory factory;

	private DocumentBuilder builder;

	/**
     * @see TestCase#TestCase(String)
     */
    public TestWebXmlMerger(String theTestName)
    {
        super(theTestName);        
    }
    
    /**
     * @see TestCase#setUp
     */
    public void setUp() throws ParserConfigurationException
    {
		factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);

		builder = factory.newDocumentBuilder();
    }

    public void testMergeOneFilterIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(srcWebXml.hasFilter("f1"));
    }

    public void testMergeOneFilterIntoDocumentWithAnotherFilter()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f2</filter-name>"
            + "    <filter-class>fclass2</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(srcWebXml.hasFilter("f1"));
        assertTrue(srcWebXml.hasFilter("f2"));
    }

    public void testMergeOneFilterIntoDocumentWithSameFilter()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(srcWebXml.hasFilter("f1"));
    }

    public void testMergeOneFilterIntoDocumentWithSameFilterAndParam()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "    <init-param>"
            + "      <param-name>f1param1</param-name>"
            + "      <param-value>f1param1value</param-value>"
            + "    </init-param>"
            + "  </filter>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(srcWebXml.hasFilter("f1"));
        Iterator initParams = srcWebXml.getFilterInitParamNames("f1");
        assertEquals("f1param1", initParams.next());
        assertTrue(!initParams.hasNext());
    }

    public void testMergeOneFilterIntoDocumentWithMultipleFilters()
        throws Exception
    {
        String srcXml = "<web-app>"
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
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f4</filter-name>"
            + "    <filter-class>fclass4</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        Iterator filterNames = srcWebXml.getFilterNames();
        assertEquals("f1", filterNames.next());
        assertEquals("f2", filterNames.next());
        assertEquals("f3", filterNames.next());
        assertEquals("f4", filterNames.next());
        assertTrue(!filterNames.hasNext());
    }

    public void testMergeMultipleFiltersIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
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
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        Iterator filterNames = srcWebXml.getFilterNames();
        assertEquals("f1", filterNames.next());
        assertEquals("f2", filterNames.next());
        assertEquals("f3", filterNames.next());
        assertTrue(!filterNames.hasNext());
    }

    public void testMergeOneFilterWithOneMappingIntoEmptyDocument()
        throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping1</url-pattern>"
            + "  </filter-mapping>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(srcWebXml.hasFilter("f1"));
        Iterator filterMappings = srcWebXml.getFilterMappings("f1");
        assertEquals("/f1mapping1", filterMappings.next());
        assertTrue(!filterMappings.hasNext());
    }

    public void testMergeOneFilterWithMultipleMappingsIntoEmptyDocument()
        throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
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
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(srcWebXml.hasFilter("f1"));
        Iterator filterMappings = srcWebXml.getFilterMappings("f1");
        assertEquals("/f1mapping1", filterMappings.next());
        assertEquals("/f1mapping2", filterMappings.next());
        assertEquals("/f1mapping3", filterMappings.next());
        assertTrue(!filterMappings.hasNext());
    }

    public void testMergeOneServletIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        assertTrue(srcWebXml.hasServlet("s1"));
    }

    public void testMergeOneServletIntoDocumentWithAnotherServlet()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s2</servlet-name>"
            + "    <servlet-class>sclass2</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        assertTrue(srcWebXml.hasServlet("s1"));
        assertTrue(srcWebXml.hasServlet("s2"));
    }

    public void testMergeOneServletIntoDocumentWithSameServlet()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        assertTrue(srcWebXml.hasServlet("s1"));
    }

    public void testMergeOneServletIntoDocumentWithSameServletAndParam()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "    <init-param>"
            + "      <param-name>s1param1</param-name>"
            + "      <param-value>s1param1value</param-value>"
            + "    </init-param>"
            + "  </servlet>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        assertTrue(srcWebXml.hasServlet("s1"));
        Iterator initParams = srcWebXml.getServletInitParamNames("s1");
        assertEquals("s1param1", initParams.next());
        assertTrue(!initParams.hasNext());
        assertEquals("s1param1value",
            srcWebXml.getServletInitParam("s1", "s1param1"));
    }

    public void testMergeOneServletIntoDocumentWithMultipleServlets()
        throws Exception
    {
        String srcXml = "<web-app>"
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
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s4</servlet-name>"
            + "    <servlet-class>sclass4</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        Iterator servletNames = srcWebXml.getServletNames();
        assertEquals("s1", servletNames.next());
        assertEquals("s2", servletNames.next());
        assertEquals("s3", servletNames.next());
        assertEquals("s4", servletNames.next());
        assertTrue(!servletNames.hasNext());
    }

    public void testMergeMultipleServletsIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
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
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        Iterator servletNames = srcWebXml.getServletNames();
        assertEquals("s1", servletNames.next());
        assertEquals("s2", servletNames.next());
        assertEquals("s3", servletNames.next());
        assertTrue(!servletNames.hasNext());
    }

    public void testMergeOneServletWithOneMappingIntoEmptyDocument()
        throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "  <servlet-mapping>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <url-pattern>/s1mapping1</url-pattern>"
            + "  </servlet-mapping>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        assertTrue(srcWebXml.hasServlet("s1"));
        Iterator servletMappings = srcWebXml.getServletMappings("s1");
        assertEquals("/s1mapping1", servletMappings.next());
        assertTrue(!servletMappings.hasNext());
    }

    public void testMergeOneServletWithMultipleMappingsIntoEmptyDocument()
        throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
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
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        assertTrue(srcWebXml.hasServlet("s1"));
        Iterator servletMappings = srcWebXml.getServletMappings("s1");
        assertEquals("/s1mapping1", servletMappings.next());
        assertEquals("/s1mapping2", servletMappings.next());
        assertEquals("/s1mapping3", servletMappings.next());
        assertTrue(!servletMappings.hasNext());
    }

}
