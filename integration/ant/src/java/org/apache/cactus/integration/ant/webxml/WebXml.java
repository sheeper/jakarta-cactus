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
        return document;
    }
    
    /**
     * Returns the servlet API version, one of {@link #SERVLET_VERSION_2_2} and 
     * {@link #SERVLET_VERSION_2_3}.
     * 
     * @return The servlet API version.
     */
    public String getVersion()
    {
        DocumentType docType = document.getDoctype();
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
     * @param theFilterElement The element representing the filter definition
     */
    public void addFilter(Element theFilterElement)
    {
        String filterName =
            getNestedText(theFilterElement, WebXmlElement.FILTER_NAME);
        if (filterName == null)
        {
            throw new IllegalArgumentException("Not a valid filter element");
        }
        if (hasFilter(filterName))
        {
            throw new IllegalStateException("Filter '" + filterName +
                "' already defined");
        }
        Node importedNode = document.importNode(theFilterElement, true);
        Node refNode = WebXmlElement.getNodeToInsertBefore(
            document, WebXmlElement.FILTER);
        rootElement.insertBefore(importedNode, refNode);
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
        Element initParamElement = WebXmlElement.createInitParam(
            document, theParamName, theParamValue);
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
        Element filterMappingElement = WebXmlElement.createFilterMapping(
            document, theFilterName, theUrlPattern);
        Node refNode = WebXmlElement.getNodeToInsertBefore(
            document, WebXmlElement.FILTER_MAPPING);
        rootElement.insertBefore(filterMappingElement, refNode);
    }
    
    /**
     * Removes a servlet filter from the descriptor.
     * 
     * @param theFilterName The name of the filter to remove
     * @return The removed element, or <code>null</code> if the specified filter
     *          was not defined
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
        NodeList filterElements =
            rootElement.getElementsByTagName(WebXmlElement.FILTER);
        for (int i = 0; i < filterElements.getLength(); i++)
        {
            Element filterElement = (Element) filterElements.item(i);
            if (theFilterName.equals(getNestedText(
                filterElement, WebXmlElement.FILTER_NAME)))
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
            NodeList initParamElements =
                filterElement.getElementsByTagName(WebXmlElement.INIT_PARAM);
            for (int i = 0; i < initParamElements.getLength(); i++)
            {
                Element initParamElement = (Element) initParamElements.item(i);
                String paramName = getNestedText(
                    initParamElement, WebXmlElement.PARAM_NAME);
                if (theParamName.equals(paramName))
                {
                    return getNestedText(
                        initParamElement, WebXmlElement.PARAM_VALUE);
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
            NodeList initParamElements =
                filterElement.getElementsByTagName(WebXmlElement.INIT_PARAM);
            for (int i = 0; i < initParamElements.getLength(); i++)
            {
                Element initParamElement = (Element) initParamElements.item(i);
                String paramName = getNestedText(
                    initParamElement, WebXmlElement.PARAM_NAME);
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
     * @param theFilterName The name of the servlet filter of which the mappings
     *         should be retrieved
     * @return An iterator over the ordered list of URL-patterns
     */
    public Iterator getFilterMappings(String theFilterName)
    {
        if (theFilterName == null)
        {
            throw new NullPointerException();
        }
        List filterMappings = new ArrayList();
        NodeList filterMappingElements =
            rootElement.getElementsByTagName(WebXmlElement.FILTER_MAPPING);
        for (int i = 0; i < filterMappingElements.getLength(); i++)
        {
            Element filterMappingElement = (Element)
                filterMappingElements.item(i);
            if (theFilterName.equals(getNestedText(
                filterMappingElement, WebXmlElement.FILTER_NAME)))
            {
                String urlPattern = getNestedText(
                    filterMappingElement, WebXmlElement.URL_PATTERN);
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
        NodeList filterElements =
            rootElement.getElementsByTagName(WebXmlElement.FILTER);
        for (int i = 0; i < filterElements.getLength(); i++)
        {
            Element filterElement = (Element) filterElements.item(i);
            String filterName =
                getNestedText(filterElement, WebXmlElement.FILTER_NAME);
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
     * @param theServletElement The element representing the servlet definition
     */
    public void addServlet(Element theServletElement)
    {
        String servletName =
            getNestedText(theServletElement, WebXmlElement.SERVLET_NAME);
        if (servletName == null)
        {
            throw new IllegalArgumentException("Not a valid servlet element");
        }
        if (hasServlet(servletName))
        {
            throw new IllegalStateException("Servlet '" + servletName +
                "' already defined");
        }
        Node importedNode = document.importNode(theServletElement, true);
        Node refNode = WebXmlElement.getNodeToInsertBefore(
            document, WebXmlElement.SERVLET);
        rootElement.insertBefore(importedNode, refNode);
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
        Element servletMappingElement = WebXmlElement.createServletMapping(
            document, theServletName, theUrlPattern);
        Node refNode = WebXmlElement.getNodeToInsertBefore(
            document, WebXmlElement.SERVLET_MAPPING);
        rootElement.insertBefore(servletMappingElement, refNode);
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
        Element initParamElement = WebXmlElement.createInitParam(
            document, theParamName, theParamValue);
        servletElement.appendChild(initParamElement);
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
        NodeList servletElements =
            rootElement.getElementsByTagName(WebXmlElement.SERVLET);
        for (int i = 0; i < servletElements.getLength(); i++)
        {
            Element servletElement = (Element) servletElements.item(i);
            if (theServletName.equals(getNestedText(
                servletElement, WebXmlElement.SERVLET_NAME)))
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
            NodeList initParamElements =
                servletElement.getElementsByTagName(WebXmlElement.INIT_PARAM);
            for (int i = 0; i < initParamElements.getLength(); i++)
            {
                Element initParamElement = (Element) initParamElements.item(i);
                String paramName = getNestedText(
                    initParamElement, WebXmlElement.PARAM_NAME);
                if (theParamName.equals(paramName))
                {
                    return getNestedText(
                        initParamElement, WebXmlElement.PARAM_VALUE);
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
            NodeList initParamElements =
                servletElement.getElementsByTagName(WebXmlElement.INIT_PARAM);
            for (int i = 0; i < initParamElements.getLength(); i++)
            {
                Element initParamElement = (Element) initParamElements.item(i);
                String paramName = getNestedText(
                    initParamElement, WebXmlElement.PARAM_NAME);
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
        NodeList servletMappingElements =
            rootElement.getElementsByTagName(WebXmlElement.SERVLET_MAPPING);
        for (int i = 0; i < servletMappingElements.getLength(); i++)
        {
            Element servletMappingElement = (Element)
                servletMappingElements.item(i);
            if (theServletName.equals(getNestedText(
                servletMappingElement, WebXmlElement.SERVLET_NAME)))
            {
                String urlPattern = getNestedText(
                    servletMappingElement, WebXmlElement.URL_PATTERN);
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
        NodeList servletElements =
            rootElement.getElementsByTagName(WebXmlElement.SERVLET);
        for (int i = 0; i < servletElements.getLength(); i++)
        {
            Element servletElement = (Element) servletElements.item(i);
            String servletName =
                getNestedText(servletElement, WebXmlElement.SERVLET_NAME);
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
    
    // Private Methods ---------------------------------------------------------
    
    /**
     * 
     * @param theElement The element of which the nested text should be
     *         returned
     * @param theTagName The name of the child element that contains the text
     * @return The text nested in the element
     */
    private String getNestedText(Element theElement, String theTagName)
    {
        NodeList nestedElements = theElement.getElementsByTagName(theTagName);
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
