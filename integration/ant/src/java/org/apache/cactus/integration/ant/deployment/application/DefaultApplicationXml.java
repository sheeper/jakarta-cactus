/* 
 * ========================================================================
 * 
 * Copyright 2003-2005 The Apache Software Foundation.
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
    
    /**
     * Specifies the order in which the top-level elements must appear in the
     * descriptor, according to the DTD.
     */
    private static final ApplicationXmlTag[] ELEMENT_ORDER = {
        ApplicationXmlTag.ICON,
        ApplicationXmlTag.DISPLAY_NAME,
        ApplicationXmlTag.DESCRIPTION,
        ApplicationXmlTag.MODULE,
        ApplicationXmlTag.SECURITY_ROLE
    };
    
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
    
    /**
     * @see ApplicationXml#addWebModule(String, String)
     */
    public void addWebModule(String theUri, String theContext)
    {
        Element moduleElement =
            this.document.createElement(ApplicationXmlTag.MODULE.getTagName());
        Element webElement = 
            this.document.createElement(ApplicationXmlTag.WEB.getTagName());
        webElement.appendChild(
            createNestedText(ApplicationXmlTag.WEB_URI, theUri));
        webElement.appendChild(
            createNestedText(ApplicationXmlTag.CONTEXT_ROOT, theContext));
        moduleElement.appendChild(webElement);
        addElement(ApplicationXmlTag.MODULE, moduleElement);
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
    
    /**
     * Creates an element that contains nested text.
     * 
     * @param theTag The tag to create an instance of
     * @param theText The text that should be nested in the element
     * @return The created DOM element
     */
    private Element createNestedText(ApplicationXmlTag theTag, String theText)
    {
        Element element = this.document.createElement(theTag.getTagName());
        element.appendChild(this.document.createTextNode(theText));
        return element;
    }
    
    /**
     * Adds an element of the specified tag to the descriptor.
     * 
     * @param theTag The descriptor tag
     * @param theElement The element to add
     */
    public final void addElement(ApplicationXmlTag theTag, Element theElement)
    {
        Node importedNode = this.document.importNode(theElement, true);
        Node refNode = getInsertionPointFor(theTag);
        this.rootElement.insertBefore(importedNode, refNode);
    }

    /**
     * Returns the node before which the specified tag should be inserted, or
     * <code>null</code> if the node should be inserted at the end of the 
     * descriptor.
     * 
     * @param theTag The tag that should be inserted
     * @return The node before which the tag can be inserted
     */
    private Node getInsertionPointFor(ApplicationXmlTag theTag)
    {
        for (int i = 0; i < ELEMENT_ORDER.length; i++)
        {
            if (ELEMENT_ORDER[i] == theTag)
            {
                for (int j = i + 1; j < ELEMENT_ORDER.length; j++)
                {
                    NodeList elements =
                        this.rootElement.getElementsByTagName(
                            ELEMENT_ORDER[j].getTagName());
                    if (elements.getLength() > 0)
                    {
                        Node result = elements.item(0);
                        Node previous = result.getPreviousSibling();
                        while ((previous != null)
                            && ((previous.getNodeType() == Node.COMMENT_NODE)
                             || (previous.getNodeType() == Node.TEXT_NODE)))
                        {
                            result = previous;
                            previous = result.getPreviousSibling();
                        }
                        return result;
                    }
                }
                break;
            }
        }
        return null;
    }
}
