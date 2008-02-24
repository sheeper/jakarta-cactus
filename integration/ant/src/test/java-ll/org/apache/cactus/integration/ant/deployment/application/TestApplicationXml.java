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
package org.apache.cactus.integration.ant.deployment.application;

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
 * @version $Id: TestApplicationXml.java 239003 2004-05-31 20:05:27Z vmassol $
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
