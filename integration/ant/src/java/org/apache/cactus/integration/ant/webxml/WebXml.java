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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Encapsulates the DOM representation of a web deployment descriptor 
 * <code>web.xml</code> to provide convenience methods for easy access and 
 * manipulation.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class WebXml
{
    
    // Public Constants --------------------------------------------------------
    
    /**
     * Servlet API version 2.2.
     */
    public static final String SERVLET_VERSION_2_2 = "2.2";
    
    /**
     * Servlet API version 2.3.
     */
    public static final String SERVLET_VERSION_2_3 = "2.3";
    
    // Private Constants -------------------------------------------------------
    
    /**
     * Public ID of the web-app DTD of Servlet API version 2.2.
     */
    private static final String WEB_APP_2_2_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    
    /**
     * Public ID of the web-app DTD of Servlet API version 2.3.
     */
    private static final String WEB_APP_2_3_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    
    /**
     * Specifies the order in which the top-level elements must appear in the
     * descriptor, according to the DTD.
     */
    private static final WebXmlTag[] ELEMENT_ORDER = {
        WebXmlTag.ICON,
        WebXmlTag.DISPLAY_NAME,
        WebXmlTag.DESCRIPTION,
        WebXmlTag.DISTRIBUTABLE,
        WebXmlTag.FILTER,
        WebXmlTag.FILTER_MAPPING,
        WebXmlTag.LISTENER,
        WebXmlTag.SERVLET,
        WebXmlTag.SERVLET_MAPPING,
        WebXmlTag.SESSION_CONFIG,
        WebXmlTag.MIME_MAPPING,
        WebXmlTag.WELCOME_FILE_LIST,
        WebXmlTag.ERROR_PAGE,
        WebXmlTag.TAGLIB,
        WebXmlTag.RESOURCE_ENV_REF,
        WebXmlTag.RESOURCE_REF,
        WebXmlTag.SECURITY_CONSTRAINT,
        WebXmlTag.LOGIN_CONFIG,
        WebXmlTag.SECURITY_ROLE,
        WebXmlTag.ENV_ENTRY,
        WebXmlTag.EJB_REF,
        WebXmlTag.EJB_LOCAL_REF,
    };
    
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
    public WebXml(Document theDocument)
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
    public Document getDocument()
    {
        return this.document;
    }
    
    /**
     * Returns the servlet API version, one of {@link #SERVLET_VERSION_2_2} and 
     * {@link #SERVLET_VERSION_2_3}.
     * 
     * @return The servlet API version.
     */
    public String getVersion()
    {
        DocumentType docType = this.document.getDoctype();
        if (docType != null)
        {
            String publicId = docType.getPublicId();
            if (WEB_APP_2_2_PUBLIC_ID.equals(publicId))
            {
                return SERVLET_VERSION_2_2;
            }
            else if (WEB_APP_2_3_PUBLIC_ID.equals(publicId))
            {
                return SERVLET_VERSION_2_3;
            }
        }
        return null;
    }

    /**
     * Adds a new servlet filter to the descriptor.
     * 
     * @param theFilter The element representing the filter definition
     */
    public void addFilter(Element theFilter)
    {
        checkElement(theFilter, WebXmlTag.FILTER);
        String filterName = getNestedText(theFilter, WebXmlTag.FILTER_NAME);
        if (filterName == null)
        {
            throw new IllegalArgumentException("Not a valid filter element");
        }
        if (hasFilter(filterName))
        {
            throw new IllegalStateException("Filter '" + filterName +
                "' already defined");
        }
        addElement(WebXmlTag.FILTER, theFilter);
    }
    
    /**
     * Adds an initialization parameter to the specified filter.
     * 
     * @param theFilterName The name of the filter
     * @param theParamName The name of the parameter
     * @param theParamValue The parameter value
     */
    public void addFilterInitParam(String theFilterName, String theParamName,
        String theParamValue)
    {
        Element filterElement = getFilter(theFilterName);
        if (filterElement == null)
        {
            throw new IllegalStateException("Filter '" + theFilterName +
                "' not defined");
        }
		Element initParamElement =
		    this.document.createElement(WebXmlTag.INIT_PARAM.getTagName());
		initParamElement.appendChild(
		    createNestedText(WebXmlTag.PARAM_NAME, theParamName));
		initParamElement.appendChild(
		    createNestedText(WebXmlTag.PARAM_VALUE, theParamValue));
        filterElement.appendChild(initParamElement);
    }
    
    /**
     * Adds a filter mapping to the descriptor.
     * 
     * @param theFilterName The name of the filter
     * @param theUrlPattern The URL pattern the filter should be mapped to
     */
    public void addFilterMapping(String theFilterName, String theUrlPattern)
    {
        if (!hasFilter(theFilterName))
        {
            throw new IllegalStateException("Filter '" + theFilterName +
                "' not defined");
        }
		Element filterMappingElement =
		    this.document.createElement(WebXmlTag.FILTER_MAPPING.getTagName());
		filterMappingElement.appendChild(
		    createNestedText(WebXmlTag.FILTER_NAME, theFilterName));
		filterMappingElement.appendChild(
		    createNestedText(WebXmlTag.URL_PATTERN,  theUrlPattern));
        addElement(WebXmlTag.FILTER_MAPPING, filterMappingElement);
    }
    
    /**
     * Removes a servlet filter from the descriptor.
     * 
     * @param theFilterName The name of the filter to remove
     * @return The removed element, or <code>null</code> if the specified
     *          filter was not defined
     */
    public Element removeFilter(String theFilterName)
    {
        Element filterElement = getFilter(theFilterName);
        if (filterElement != null)
        {
            filterElement.getParentNode().removeChild(filterElement);
        }
        return filterElement;
    }
    
    /**
     * Returns the element that contains the definition of a specific servlet
     * filter, or <code>null</code> if a filter of the specified name is not
     * defined in the descriptor.
     * 
     * @param theFilterName The name of the servlet filter
     * @return The DOM element representing the filter definition
     */
    public Element getFilter(String theFilterName)
    {
        if (theFilterName == null)
        {
            throw new NullPointerException();
        }
        Iterator filterElements = getElements(WebXmlTag.FILTER);
        while (filterElements.hasNext())
        {
            Element filterElement = (Element) filterElements.next();
            if (theFilterName.equals(getNestedText(
                filterElement, WebXmlTag.FILTER_NAME)))
            {
                return filterElement;
            }
        }
        return null;
    }
    
    /**
     * Returns the value of an initialization parameter of the specified filter.
     * 
     * @param theFilterName The name of the servlet filter
     * @param theParamName The name of the initialization parameter
     * @return The parameter value
     */
    public String getFilterInitParam(String theFilterName, String theParamName)
    {
        Element filterElement = getFilter(theFilterName);
        if (filterElement != null)
        {
            Iterator initParamElements = getElements(WebXmlTag.INIT_PARAM);
            while (initParamElements.hasNext())
            {
                Element initParamElement = (Element) initParamElements.next();
                String paramName = getNestedText(
                    initParamElement, WebXmlTag.PARAM_NAME);
                if (theParamName.equals(paramName))
                {
                    return getNestedText(
                        initParamElement, WebXmlTag.PARAM_VALUE);
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the names of the initialization parameters of the specified 
     * servlet filter.
     * 
     * @param theFilterName The name of the servlet filter of which the
     *         parameter names should be retrieved
     * @return An iterator over the ordered list of parameter names
     */
    public Iterator getFilterInitParamNames(String theFilterName)
    {
        List initParamNames = new ArrayList();
        Element filterElement = getFilter(theFilterName);
        if (filterElement != null)
        {
            Iterator initParamElements = getElements(WebXmlTag.INIT_PARAM);
            while (initParamElements.hasNext())
            {
                Element initParamElement = (Element) initParamElements.next();
                String paramName = getNestedText(
                    initParamElement, WebXmlTag.PARAM_NAME);
                if (paramName != null)
                {
                    initParamNames.add(paramName);
                }
            }
        }
        return initParamNames.iterator();
    }
    
    /**
     * Returns the URL-patterns that the specified filter is mapped to in an
     * ordered list. If there are no mappings for the specified filter, an
     * iterator over an empty list is returned.
     * 
     * @param theFilterName The name of the servlet filter of which the 
     *         mappings should be retrieved
     * @return An iterator over the ordered list of URL-patterns
     */
    public Iterator getFilterMappings(String theFilterName)
    {
        if (theFilterName == null)
        {
            throw new NullPointerException();
        }
        List filterMappings = new ArrayList();
        Iterator filterMappingElements = getElements(WebXmlTag.FILTER_MAPPING);
        while (filterMappingElements.hasNext())
        {
            Element filterMappingElement = (Element)
                filterMappingElements.next();
            if (theFilterName.equals(getNestedText(
                filterMappingElement, WebXmlTag.FILTER_NAME)))
            {
                String urlPattern = getNestedText(
                    filterMappingElement, WebXmlTag.URL_PATTERN);
                if (urlPattern != null)
                {
                    filterMappings.add(urlPattern);
                }
            }
        }
        return filterMappings.iterator();
    }
    
    /**
     * Returns the names of all filters defined in the deployment descriptor.
     * The names are returned as an iterator over an ordered list.
     * 
     * @return The filter names
     */
    public Iterator getFilterNames()
    {
        List filterNames = new ArrayList();
        Iterator filterElements = getElements(WebXmlTag.FILTER);
        while (filterElements.hasNext())
        {
            Element filterElement = (Element) filterElements.next();
            String filterName =
                getNestedText(filterElement, WebXmlTag.FILTER_NAME);
            if (filterName != null)
            {
                filterNames.add(filterName);
            }
        }
        return filterNames.iterator();
    }
    
    /**
     * Returns whether a servlet filter by the specified name is defined in the 
     * deployment descriptor.
     * 
     * @param theFilterName The name of the filter
     * @return <code>true</code> if the filter is defined, <code>false</code>
     *          otherwise
     */
    public boolean hasFilter(String theFilterName)
    {
        return (getFilter(theFilterName) != null);
    }
    
    /**
     * Adds a new servlet to the descriptor.
     * 
     * @param theServlet The element representing the servlet definition
     */
    public void addServlet(Element theServlet)
    {
        checkElement(theServlet, WebXmlTag.SERVLET);
        String servletName = getNestedText(theServlet, WebXmlTag.SERVLET_NAME);
        if (servletName == null)
        {
            throw new IllegalArgumentException("Not a valid servlet element");
        }
        if (hasServlet(servletName))
        {
            throw new IllegalStateException("Servlet '" + servletName +
                "' already defined");
        }
        addElement(WebXmlTag.SERVLET, theServlet);
    }
    
    /**
     * Adds an initialization parameter to the specified servlet.
     * 
     * @param theServletName The name of the filter
     * @param theParamName The name of the parameter
     * @param theParamValue The parameter value
     */
    public void addServletInitParam(String theServletName, String theParamName,
        String theParamValue)
    {
        Element servletElement = getServlet(theServletName);
        if (servletElement == null)
        {
            throw new IllegalStateException("Servlet '" + theServletName +
                "' not defined");
        }
		Element initParamElement =
		    this.document.createElement(WebXmlTag.INIT_PARAM.getTagName());
		initParamElement.appendChild(
		    createNestedText(WebXmlTag.PARAM_NAME, theParamName));
		initParamElement.appendChild(
		    createNestedText(WebXmlTag.PARAM_VALUE, theParamValue));
        servletElement.appendChild(initParamElement);
    }
    
    /**
     * Adds a servlet mapping to the descriptor.
     * 
     * @param theServletName The name of the servlet
     * @param theUrlPattern The URL pattern the servlet should be mapped to
     */
    public void addServletMapping(String theServletName, String theUrlPattern)
    {
        if (!hasServlet(theServletName))
        {
            throw new IllegalStateException("Servlet '" + theServletName +
                "' not defined");
        }
		Element servletMappingElement =
		    this.document.createElement(WebXmlTag.SERVLET_MAPPING.getTagName());
		servletMappingElement.appendChild(
		    createNestedText(WebXmlTag.SERVLET_NAME, theServletName));
		servletMappingElement.appendChild(
		    createNestedText(WebXmlTag.URL_PATTERN, theUrlPattern));
        addElement(WebXmlTag.SERVLET_MAPPING, servletMappingElement);
    }
    
    /**
     * Removes a servlet from the descriptor.
     * 
     * @param theServletName The name of the servlet to remove
     * @return The removed element, or <code>null</code> if the servlet was not
     *          defined
     */
    public Element removeServlet(String theServletName)
    {
        Element servletElement = getServlet(theServletName);
        if (servletElement != null)
        {
            servletElement.getParentNode().removeChild(servletElement);
        }
        return servletElement;
    }
    
    /**
     * Returns the element that contains the definition of a specific servlet, 
     * or <code>null</code> if a servlet of the specified name is not defined
     * in the descriptor.
     * 
     * @param theServletName The name of the servlet
     * @return The DOM element representing the servlet definition
     */
    public Element getServlet(String theServletName)
    {
        if (theServletName == null)
        {
            throw new NullPointerException();
        }
        Iterator servletElements = getElements(WebXmlTag.SERVLET);
        while (servletElements.hasNext())
        {
            Element servletElement = (Element) servletElements.next();
            if (theServletName.equals(getNestedText(
                servletElement, WebXmlTag.SERVLET_NAME)))
            {
                return servletElement;
            }
        }
        return null;
    }
    
    /**
     * Returns the value of an initialization parameter of the specified
     * servlet.
     * 
     * @param theServletName The name of the servlet
     * @param theParamName The name of the initialization parameter
     * @return The parameter value
     */
    public String getServletInitParam(String theServletName,
        String theParamName)
    {
        Element servletElement = getServlet(theServletName);
        if (servletElement != null)
        {
            Iterator initParamElements = getElements(WebXmlTag.INIT_PARAM);
            while (initParamElements.hasNext())
            {
                Element initParamElement = (Element) initParamElements.next();
                String paramName = getNestedText(
                    initParamElement, WebXmlTag.PARAM_NAME);
                if (theParamName.equals(paramName))
                {
                    return getNestedText(
                        initParamElement, WebXmlTag.PARAM_VALUE);
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the names of the initialization parameters of the specified 
     * servlet.
     * 
     * @param theServletName The name of the servlet of which the parameter
     *         names should be retrieved
     * @return An iterator over the ordered list of parameter names
     */
    public Iterator getServletInitParamNames(String theServletName)
    {
        List initParamNames = new ArrayList();
        Element servletElement = getServlet(theServletName);
        if (servletElement != null)
        {
            Iterator initParamElements = getElements(WebXmlTag.INIT_PARAM);
            while (initParamElements.hasNext())
            {
                Element initParamElement = (Element) initParamElements.next();
                String paramName = getNestedText(
                    initParamElement, WebXmlTag.PARAM_NAME);
                if (paramName != null)
                {
                    initParamNames.add(paramName);
                }
            }
        }
        return initParamNames.iterator();
    }
    
    /**
     * Returns the URL-patterns that the specified servlet is mapped to in an
     * ordered list. If there are no mappings for the specified servlet, an
     * iterator over an empty list is returned.
     * 
     * @param theServletName The name of the servlet of which the mappings
     *         should be retrieved
     * @return An iterator over the ordered list of URL-patterns
     */
    public Iterator getServletMappings(String theServletName)
    {
        if (theServletName == null)
        {
            throw new NullPointerException();
        }
        List servletMappings = new ArrayList();
        Iterator servletMappingElements =
            getElements(WebXmlTag.SERVLET_MAPPING);
        while (servletMappingElements.hasNext())
        {
            Element servletMappingElement = (Element)
                servletMappingElements.next();
            if (theServletName.equals(getNestedText(
                servletMappingElement, WebXmlTag.SERVLET_NAME)))
            {
                String urlPattern = getNestedText(
                    servletMappingElement, WebXmlTag.URL_PATTERN);
                if (urlPattern != null)
                {
                    servletMappings.add(urlPattern);
                }
            }
        }
        return servletMappings.iterator();
    }
    
    /**
     * Returns the names of all servlets defined in the deployment descriptor.
     * The names are returned as an iterator over an ordered list.
     * 
     * @return The servlet names
     */
    public Iterator getServletNames()
    {
        List servletNames = new ArrayList();
        Iterator servletElements = getElements(WebXmlTag.SERVLET);
        while (servletElements.hasNext())
        {
            Element servletElement = (Element) servletElements.next();
            String servletName =
                getNestedText(servletElement, WebXmlTag.SERVLET_NAME);
            if (servletName != null)
            {
                servletNames.add(servletName);
            }
        }
        return servletNames.iterator();
    }
    
    /**
     * Returns whether a servlet by the specified name is defined in the 
     * deployment descriptor.
     * 
     * @param theServletName The name of the servlet
     * @return <code>true</code> if the servlet is defined, <code>false</code>
     *          otherwise
     */
    public boolean hasServlet(String theServletName)
    {
        return (getServlet(theServletName) != null);
    }
    
    /**
     * Returns an iterator over the elements that match the specified tag.
     * 
     * @param theTag The descriptor tag of which the elements should be
     *         returned
     * @return An iterator over the elements matching the tag, in the order 
     *          they occur in the descriptor
     */
    public Iterator getElements(WebXmlTag theTag)
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
     * Adds an element of the specified tag to the descriptor.
     * 
     * @param theTag The descriptor tag
     * @param theElement The element to add
     */
    public void addElement(WebXmlTag theTag, Element theElement)
    {
        if (!theTag.getTagName().equals(theElement.getNodeName()))
        {
            throw new IllegalArgumentException("Not a '" + theTag
                + "' element");
        }
        if (!theTag.isMultipleAllowed() && getElements(theTag).hasNext())
        {
            throw new IllegalStateException("The tag '" + theTag
                + "' may not occur more than once in the descriptor");
        }
        Node importedNode = this.document.importNode(theElement, true);
        Node refNode = getInsertionPointFor(theTag);
        this.rootElement.insertBefore(importedNode, refNode);
    }
    
    /**
     * Replaces all elements of the specified tag with the provided element.
     * 
     * @param theTag The descriptor tag
     * @param theElement The element to replace the current elements with
     */
    public void replaceElement(WebXmlTag theTag, Element theElement)
    {
        Iterator elements = getElements(theTag);
        while (elements.hasNext())
        {
            Element element = (Element) elements.next();
            element.getParentNode().removeChild(element);
        }
        addElement(theTag, theElement);
    }
    
    // Private Methods ---------------------------------------------------------
    
    /**
     * Checks an element whether its name matches the specified name.
     * 
     * @param theElement The element to check
     * @param theExpectedTag The expected tag name
     * @throws IllegalArgumentException If the element name doesn't match
     */
    private void checkElement(Element theElement, WebXmlTag theExpectedTag)
    {
        if (!theExpectedTag.getTagName().equals(theElement.getNodeName()))
        {
            throw new IllegalArgumentException("Not a '" + theExpectedTag
                + "' element");
        }
    }
    
    /**
     * Creates an element that contains nested text.
     * 
     * @param theTag The tag to create an instance of
     * @param theText The text that should be nested in the element
     * @return The created DOM element
     */
    private Element createNestedText(WebXmlTag theTag, String theText)
    {
        Element element = this.document.createElement(theTag.getTagName());
        element.appendChild(this.document.createTextNode(theText));
        return element;
    }
    
    /**
     * Returns the node before which the specified tag should be inserted, or
     * <code>null</code> if the node should be inserted at the end of the 
     * descriptor.
     * 
     * FIXME: need more unit tests for this routine, as it is quite fragile
     *  
     * @param theTag The tag that should be inserted
     * @return The node before which the tag can be inserted
     */
    private Node getInsertionPointFor(WebXmlTag theTag)
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
                        while ((previous != null) &&
                            ((previous.getNodeType() == Node.COMMENT_NODE) ||
                             (previous.getNodeType() == Node.TEXT_NODE)))
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
    
    /**
     * 
     * @param theElement The element of which the nested text should be
     *         returned
     * @param theTag The descriptor tag in which the text is nested
     * @return The text nested in the element
     */
    private String getNestedText(Element theElement,
        WebXmlTag theTag)
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
