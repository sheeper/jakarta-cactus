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
package org.apache.cactus.integration.ant.applicationxml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Encapsulates the DOM representation of a enterprise application descriptor 
 * (<code>application.xml</code>) to provide convenience methods for easy access
 * and manipulation.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public class ApplicationXml
{
    
    // Instance Variables ------------------------------------------------------
    
    /**
     * The DOM representation of the deployment descriptor.
     */
    private final Document document;
    
    /**
     * The root element of the descriptor.
     */
    private final Element rootElement;
    
    // Constructors ------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @param theDocument The DOM document representing the parsed deployment
     *         descriptor
     */
    public ApplicationXml(Document theDocument)
    {
        this.document = theDocument;
        this.rootElement = theDocument.getDocumentElement();
    }
    
    // Public Methods ----------------------------------------------------------
    
    /**
     * Returns the DOM document representing the deployment descriptor. The 
     * document will contain any modifications made through this instance.
     * 
     * @return The document representing the deploy descriptor
     */
    public final Document getDocument()
    {
        return this.document;
    }

    /**
     * Returns the element that contains the definition of a specific web
     * module, or <code>null</code> if a web module with the specified web-uri
     * is not defined.
     * 
     * @param theWebUri The uri of the web module
     * @return The DOM element representing the filter definition
     */
    public final Element getWebModule(String theWebUri)
    {
        if (theWebUri == null)
        {
            throw new NullPointerException();
        }
        Iterator moduleElements = getElements(ApplicationXmlTag.MODULE);
        while (moduleElements.hasNext())
        {
            Element moduleElement = (Element) moduleElements.next();
            Iterator webElements =
                getNestedElements(moduleElement, ApplicationXmlTag.WEB);
            if (webElements.hasNext())
            {
                Element webElement = (Element) webElements.next(); 
                if (theWebUri.equals(getNestedText(
                    webElement, ApplicationXmlTag.WEB_URI)))
                {
                    return webElement;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the context root of the the specified web module.
     * 
     * @param theWebUri The uri of the web module
     * @return The context root of the web module
     */
    public final String getWebModuleContextRoot(String theWebUri)
    {
        Element webModuleElement = getWebModule(theWebUri);
        if (webModuleElement == null)
        {
            throw new IllegalArgumentException("Web Module '" + theWebUri
                + "' not defined");
        }
        return getNestedText(webModuleElement, ApplicationXmlTag.CONTEXT_ROOT);
    }

    /**
     * Returns an iterator over the URIs of the web modules defined in the 
     * descriptor.
     * 
     * @return An iterator over the URIs of the web modules
     */
    public final Iterator getWebModuleUris()
    {
        List webUris = new ArrayList();
        Iterator moduleElements = getElements(ApplicationXmlTag.MODULE);
        while (moduleElements.hasNext())
        {
            Element moduleElement = (Element) moduleElements.next();
            Iterator webElements =
                getNestedElements(moduleElement, ApplicationXmlTag.WEB);
            if (webElements.hasNext())
            {
                Element webElement = (Element) webElements.next(); 
                String webUri =
                    getNestedText(webElement, ApplicationXmlTag.WEB_URI);
                if (webUri != null)
                {
                    webUris.add(webUri);
                }
            }
        }
        return webUris.iterator();
    }

    /**
     * Returns an iterator over the elements that match the specified tag.
     * 
     * @param theTag The descriptor tag of which the elements should be
     *        returned
     * @return An iterator over the elements matching the tag, in the order 
     *         they occur in the descriptor
     */
    public final Iterator getElements(ApplicationXmlTag theTag)
    {
        List elements = new ArrayList();
        NodeList nodeList =
            this.rootElement.getElementsByTagName(theTag.getTagName()); 
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            elements.add(nodeList.item(i));
        }
        return elements.iterator();
    }
    
    // Private Methods ---------------------------------------------------------

    /**
     * Returns an iterator over the child elements of the specified element that
     * match the specified tag.
     *  
     * @param theParent The element of which the nested elements should be
     *        retrieved
     * @param theTag The descriptor tag of which the elements should be
     *        returned
     * @return An iterator over the elements matching the tag, in the order 
     *         they occur in the descriptor
     */
    private Iterator getNestedElements(Element theParent,
        ApplicationXmlTag theTag)
    {
        List elements = new ArrayList();
        NodeList nodeList = theParent.getElementsByTagName(theTag.getTagName());
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            elements.add(nodeList.item(i));
        }
        return elements.iterator();
    }

    /**
     * Returns the text nested inside a child element of the specified element.
     * 
     * @param theElement The element of which the nested text should be
     *         returned
     * @param theTag The descriptor tag in which the text is nested
     * @return The text nested in the element
     */
    private String getNestedText(Element theElement,
        ApplicationXmlTag theTag)
    {
        NodeList nestedElements =
            theElement.getElementsByTagName(theTag.getTagName());
        if (nestedElements.getLength() > 0)
        {
            Node nestedText = nestedElements.item(0).getFirstChild();
            if (nestedText != null)
            {
                return nestedText.getNodeValue();
            }
        }
        return null;
    }
    
}
