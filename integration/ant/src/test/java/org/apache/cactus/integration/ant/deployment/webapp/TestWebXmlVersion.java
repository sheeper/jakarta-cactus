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
package org.apache.cactus.integration.ant.deployment.webapp;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.codehaus.cargo.module.webapp.WebXmlVersion;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;

/**
 * Unit tests for {@link WebXmlVersion}.
 *
 * @version $Id: TestWebXmlVersion.java 239003 2004-05-31 20:05:27Z vmassol $
 */
public final class TestWebXmlVersion extends TestCase
{
    /**
     * The DOM implementation.
     */
    private DOMImplementation domImpl;

    /**
     * {@inheritDoc}
     * @see TestCase#setUp
     */
    public void setUp() throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);

        this.domImpl = factory.newDocumentBuilder().getDOMImplementation();
    }

    /**
     * Verifies that comparing version 2.2 to version 2.2 yields zero.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testCompare22To22() throws Exception
    {
        assertTrue(WebXmlVersion.V2_2.compareTo(WebXmlVersion.V2_2) == 0);
    }

    /**
     * Verifies that comparing version 2.2 to version 2.3 yields a negative
     * value.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testCompare22To23() throws Exception
    {
        assertTrue(WebXmlVersion.V2_2.compareTo(WebXmlVersion.V2_3) < 0);
    }

    /**
     * Verifies that comparing version 2.3 to version 2.3 yields zero.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testCompare23To23() throws Exception
    {
        assertTrue(WebXmlVersion.V2_3.compareTo(WebXmlVersion.V2_3) == 0);
    }

    /**
     * Verifies that comparing version 2.2 to version 2.3 yields a negative
     * value.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testCompare23To22() throws Exception
    {
        assertTrue(WebXmlVersion.V2_3.compareTo(WebXmlVersion.V2_2) > 0);
    }

    /**
     * Verifies that calling WebXmlVersion.valueOf(null) throws a
     * NullPointerException.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testValueOfNull() throws Exception
    {
        try
        {
            WebXmlVersion.valueOf((DocumentType) null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException expected)
        {
            // expected
        }
    }

    /**
     * Verifies that calling WebXmlVersion.valueOf() with a unknown document
     * type returns null.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testValueOfUnknownDocType() throws Exception
    {
        DocumentType docType = domImpl.createDocumentType("web-app",
            "foo", "bar");
        assertNull(WebXmlVersion.valueOf(docType));
    }

    /**
     * Verifies that calling WebXmlVersion.valueOf() with a web-app 2.2 document
     * type returns the correct instance.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testValueOfDocType22() throws Exception
    {
        DocumentType docType = domImpl.createDocumentType("web-app",
            WebXmlVersion.V2_2.getPublicId(), WebXmlVersion.V2_2.getSystemId());
        assertEquals(WebXmlVersion.V2_2, WebXmlVersion.valueOf(docType));
    }

    /**
     * Verifies that calling WebXmlVersion.valueOf() with a web-app 2.3 document
     * type returns the correct instance.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testValueOfDocType23() throws Exception
    {
        DocumentType docType = domImpl.createDocumentType("web-app",
            WebXmlVersion.V2_3.getPublicId(), WebXmlVersion.V2_3.getSystemId());
        assertEquals(WebXmlVersion.V2_3, WebXmlVersion.valueOf(docType));
    }

}