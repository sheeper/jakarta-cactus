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
package org.apache.cactus.integration.ant.deployment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Encapsulates the DOM representation of an EAR descriptor 
 * (<code>application.xml</code>) to provide convenience methods for easy 
 * access and manipulation.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public class DefaultApplicationXml implements ApplicationXml
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
    public DefaultApplicationXml(Document theDocument)
    {
        this.document = theDocument;
        this.rootElement = theDocument.getDocumentElement();
    }
    
    // Public Methods ----------------------------------------------------------
    
    /**
     * @see ApplicationXml#getDocument()
     */
    public final Document getDocument()
    {
        return this.document;
    }

    /**
     * @see ApplicationXml#getVersion()
     */
    public final ApplicationXmlVersion getVersion()
    {
        DocumentType docType = this.document.getDoctype();
        if (docType != null)
        {
            return ApplicationXmlVersion.valueOf(docType);
        }
        return null;
    }

    /**
     * @see ApplicationXml#getWebModule(String)
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
     * @see ApplicationXml#getWebModuleContextRoot(String)
     */
    public final String getWebModuleContextRoot(String theWebUri)
    {
        Element webModuleElement = getWebModule(theWebUri);
        if (webModuleElement == null)
        {
            throw new IllegalArgumentException("Web module [" + theWebUri
                + "] is not defined");
        }
        return getNestedText(webModuleElement, ApplicationXmlTag.CONTEXT_ROOT);
    }

    /**
     * @see ApplicationXml#getWebModuleUris()
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
     * @see ApplicationXml#getElements(ApplicationXmlTag)
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
