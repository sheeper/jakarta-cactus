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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Unit tests for {@link ApplicationXml}.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public final class TestApplicationXml extends TestCase
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
     * Tests whether the construction of a ApplicationXml object with a
     * <code>null</code> parameter for the DOM document throws a
     * <code>NullPointerException</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testConstructionWithNullDocument() throws Exception
    {
        try
        {
            new DefaultApplicationXml(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // expected
        }
        
    }
    
    /**
     * Verifies that the method <code>getWebModuleUris()</code> returns an empty
     * iterator for a descriptor with no web module definitions.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetWebModuleUrisWithEmptyDocument() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <java>javaclient.jar</java>"
            + "  </module>"
            + "</application>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        ApplicationXml applicationXml = new DefaultApplicationXml(doc);
        Iterator webUris = applicationXml.getWebModuleUris();
        assertTrue("No web modules defined", !webUris.hasNext());
    }
    
    /**
     * Verifies that the method <code>getWebModuleUris()</code> returns an 
     * iterator with the correct web-uri for a descriptor with a single web
     * module definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetWebModuleUrisWithSingleWebModule() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule.jar</web-uri>"
            + "      <context-root>/webmodule</context-root>"
            + "    </web>"
            + "  </module>"
            + "</application>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        ApplicationXml applicationXml = new DefaultApplicationXml(doc);
        Iterator webUris = applicationXml.getWebModuleUris();
        assertEquals("webmodule.jar", webUris.next());
        assertTrue(!webUris.hasNext());
    }
    
    /**
     * Verifies that the method <code>getWebModuleUris()</code> returns an 
     * iterator with the correct web-uris for a descriptor with multiple web
     * module definitions.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetWebModuleUrisWithMultipleWebModules() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule1.jar</web-uri>"
            + "      <context-root>/webmodule1</context-root>"
            + "    </web>"
            + "  </module>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule2.jar</web-uri>"
            + "      <context-root>/webmodule2</context-root>"
            + "    </web>"
            + "  </module>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule3.jar</web-uri>"
            + "      <context-root>/webmodule3</context-root>"
            + "    </web>"
            + "  </module>"
            + "</application>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        ApplicationXml applicationXml = new DefaultApplicationXml(doc);
        Iterator webUris = applicationXml.getWebModuleUris();
        assertEquals("webmodule1.jar", webUris.next());
        assertEquals("webmodule2.jar", webUris.next());
        assertEquals("webmodule3.jar", webUris.next());
        assertTrue(!webUris.hasNext());
    }
    
    /**
     * Verifies that the method <code>getWebModuleContextRoot()</code> throws an
     * <code>IllegalARgumentException</code> when the specified web module is
     * not defined.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetWebModuleContextRootUndefined() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <java>javaclient.jar</java>"
            + "  </module>"
            + "</application>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        ApplicationXml applicationXml = new DefaultApplicationXml(doc);
        try
        {
            applicationXml.getWebModuleContextRoot("webmodule.jar");
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException expected)
        {
            // expected
        }
    }

    /**
     * Verifies that the method <code>getWebModuleContextRoot()</code> returns
     * an the correct context root for a descriptor with a single web module.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetWebModuleContextRootSingleWebModule() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule.jar</web-uri>"
            + "      <context-root>/webmodule</context-root>"
            + "    </web>"
            + "  </module>"
            + "</application>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        ApplicationXml applicationXml = new DefaultApplicationXml(doc);
        assertEquals("/webmodule",
            applicationXml.getWebModuleContextRoot("webmodule.jar"));
    }

    /**
     * Verifies that the method <code>getWebModuleContextRoot()</code> returns
     * an the correct context roots for a descriptor with multiple web modules.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetWebModuleContextRootMultipleWebModules() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule1.jar</web-uri>"
            + "      <context-root>/webmodule1</context-root>"
            + "    </web>"
            + "  </module>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule2.jar</web-uri>"
            + "      <context-root>/webmodule2</context-root>"
            + "    </web>"
            + "  </module>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule3.jar</web-uri>"
            + "      <context-root>/webmodule3</context-root>"
            + "    </web>"
            + "  </module>"
            + "</application>";
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        ApplicationXml applicationXml = new DefaultApplicationXml(doc);
        assertEquals("/webmodule1",
            applicationXml.getWebModuleContextRoot("webmodule1.jar"));
        assertEquals("/webmodule2",
            applicationXml.getWebModuleContextRoot("webmodule2.jar"));
        assertEquals("/webmodule3",
            applicationXml.getWebModuleContextRoot("webmodule3.jar"));
    }

}
