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


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;

/**
 * Unit tests for {@link WebXmlVersion}.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public final class TestWebXmlVersion extends TestCase
{
    /**
     * The DOM implementation
     */
    private DOMImplementation domImpl;

    /**
     * @see TestCase#TestCase(String)
     */
    public TestWebXmlVersion(String theTestName)
    {
        super(theTestName);        
    }
    
    /**
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
