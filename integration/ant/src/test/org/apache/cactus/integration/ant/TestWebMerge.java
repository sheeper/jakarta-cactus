/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

/**
 * Unit tests for {@link WebMerge}.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestWebMerge extends TestCase
{
    /**
     * @see TestCase#TestCase(String)
     */
    public TestWebMerge(String theTestName)
    {
        super(theTestName);        
    }

    private Document createOriginalDocument() throws Exception
    {
        String xml = 
              "<web-app>".trim()
            + "  <servlet>".trim()
            + "    <servlet-name>s1</servlet-name>".trim()
            + "    <servlet-class>sclass1</servlet-class>".trim()
            + "  </servlet>".trim()
            + "  <servlet>".trim()
            + "    <servlet-name>s2</servlet-name>".trim()
            + "    <servlet-class>sclass2</servlet-class>".trim()
            + "  </servlet>".trim()
            + "  <security-role>".trim()
            + "    <description>Test role</description>".trim()
            + "    <role-name>test</role-name>".trim()
            + "  </security-role>".trim()
            + "  <security-constraint/>".trim()
            + "</web-app>".trim();
        WebMerge webMerge = new WebMerge();
        return webMerge.parse(new ByteArrayInputStream(xml.getBytes())); 
    }

    private Document createOverrideDocument() throws Exception
    {
        String xml = 
              "<web-app>".trim()
            + "  <filter>".trim()
            + "    <filter-name>f1</filter-name>".trim()
            + "    <filter-class>fclass1</filter-class>".trim()
            + "  </filter>".trim()
            + "  <servlet>".trim()
            + "    <servlet-name>s3</servlet-name>".trim()
            + "    <servlet-class>sclass3</servlet-class>".trim()
            + "  </servlet>".trim()
            + "  <servlet>".trim()
            + "    <servlet-name>s4</servlet-name>".trim()
            + "    <servlet-class>sclass4</servlet-class>".trim()
            + "  </servlet>".trim()
            + "  <login-config>".trim()
            + "    <auth-method>BASIC</auth-method>".trim()
            + "    <realm-name>Test</realm-name>".trim()
            + "  </login-config>".trim()
            + "  <security-constraint/>".trim()
            + "</web-app>".trim();

        WebMerge webMerge = new WebMerge();
        return webMerge.parse(new ByteArrayInputStream(xml.getBytes())); 
    }

    private Document createEmptyDocument()
    {
        DocumentImpl doc = new DocumentImpl();
        doc.appendChild(doc.createElement("web-app"));
        return doc;
    }        

    private void verifyMergedDocument(Node theCurrent)
    {
        assertEquals("servlet", theCurrent.getNodeName());
        assertEquals("s3", 
            theCurrent.getFirstChild().getFirstChild().getNodeValue());

        theCurrent = theCurrent.getNextSibling();
        assertEquals("servlet", theCurrent.getNodeName());
        assertEquals("s4", 
            theCurrent.getFirstChild().getFirstChild().getNodeValue());

        theCurrent = theCurrent.getNextSibling();
        assertEquals("servlet", theCurrent.getNodeName());
        assertEquals("s1",
            theCurrent.getFirstChild().getFirstChild().getNodeValue());

        theCurrent = theCurrent.getNextSibling();
        assertEquals("servlet", theCurrent.getNodeName());
        assertEquals("s2", 
            theCurrent.getFirstChild().getFirstChild().getNodeValue());
    }

    public void testInsertBeforeWithNodeListOk() throws Exception
    {
        Document originalDoc = createOriginalDocument();
        Document overrideDoc = createOverrideDocument();
                
        WebMerge webMerge = new WebMerge();

        NodeList nl = overrideDoc.getElementsByTagName("servlet");        

        // Points to the first <servlet> Node
        Node node = originalDoc.getDocumentElement().getFirstChild();

        webMerge.insertBefore(nl, node);

        Node current = originalDoc.getElementsByTagName("servlet").item(0);
        verifyMergedDocument(current);
    }

    public void testInsertTagServletElementsExist() throws Exception
    {
        Document originalDoc = createOriginalDocument();
        Document overrideDoc = createOverrideDocument();
                
        WebMerge webMerge = new WebMerge();
        Node insertPointNode = 
            originalDoc.getDocumentElement().getFirstChild();
        webMerge.insertTag("servlet", insertPointNode, originalDoc, 
            overrideDoc);

        Node current = originalDoc.getElementsByTagName("servlet").item(0);
        assertEquals("servlet", current.getNodeName());
        assertEquals("s3", 
            current.getFirstChild().getFirstChild().getNodeValue());

        current = current.getNextSibling();
        assertEquals("servlet", current.getNodeName());
        assertEquals("s4", 
            current.getFirstChild().getFirstChild().getNodeValue());

        current = current.getNextSibling();
        assertEquals("servlet", current.getNodeName());
        assertEquals("s1", 
            current.getFirstChild().getFirstChild().getNodeValue());

        current = current.getNextSibling();
        assertEquals("servlet", current.getNodeName());
        assertEquals("s2", 
            current.getFirstChild().getFirstChild().getNodeValue());
    }

    public void testInsertTagServletElementNotExistInOverride()
        throws Exception
    {
        Document originalDoc = createOriginalDocument();
        Document overrideDoc = createEmptyDocument();
                
        WebMerge webMerge = new WebMerge();
        Node insertPointNode = 
            originalDoc.getDocumentElement().getFirstChild();
        webMerge.insertTag("servlet", insertPointNode, originalDoc, 
            overrideDoc);

        Node current = originalDoc.getElementsByTagName("servlet").item(0);
        assertEquals("servlet", current.getNodeName());
        assertEquals("s1", 
            current.getFirstChild().getFirstChild().getNodeValue());

        current = current.getNextSibling();
        assertEquals("servlet", current.getNodeName());
        assertEquals("s2", 
            current.getFirstChild().getFirstChild().getNodeValue());
    }

    public void testInsertTagServletElementNotExistInOriginal()
        throws Exception
    {
        Document originalDoc = createEmptyDocument();
        originalDoc.getDocumentElement().appendChild(
            originalDoc.createElement("servlet-mapping"));
        Document overrideDoc = createOverrideDocument();
                
        WebMerge webMerge = new WebMerge();
        Node insertPointNode = 
            originalDoc.getDocumentElement().getFirstChild();
        webMerge.insertTag("servlet", insertPointNode, originalDoc, 
            overrideDoc);

        Node current = originalDoc.getElementsByTagName("servlet").item(0);
        assertEquals("servlet", current.getNodeName());
        assertEquals("s3", 
            current.getFirstChild().getFirstChild().getNodeValue());

        current = current.getNextSibling();
        assertEquals("servlet", current.getNodeName());
        assertEquals("s4", 
            current.getFirstChild().getFirstChild().getNodeValue());
    }

    public void testMergeOk() throws Exception
    {
        Document originalDoc = createOriginalDocument();
        Document overrideDoc = createOverrideDocument();

        WebMerge webMerge = new WebMerge();
        Document resultDoc = webMerge.merge(originalDoc, overrideDoc);                

        Node current = resultDoc.getElementsByTagName("servlet").item(0);
        verifyMergedDocument(current);
    }

    public void testMergeUniqueElements() throws Exception
    {
        Document originalDoc = createOriginalDocument();
        Document overrideDoc = createOverrideDocument();

        WebMerge webMerge = new WebMerge();
        Document resultDoc = webMerge.merge(originalDoc, overrideDoc);                

        NodeList nl = 
            resultDoc.getElementsByTagName("security-constraint");
        assertEquals(1, nl.getLength());
    }

}
