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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class that knows about the names and order of the top-level elements in a web
 * deployment descriptor.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class WebXmlElement
{
    
    // Public Constants --------------------------------------------------------
    
    /**
     * Element name 'web-app'.
     */
    public static final String WEB_APP = "web-app";
    
    /**
     * Element name 'icon'.
     */
    public static final String ICON = "web-app";
    
    /**
     * Element name 'display-name'.
     */
    public static final String DISPLAY_NAME = "display-name";
    
    /**
     * Element name 'description'.
     */
    public static final String DESCRIPTION = "description";
    
    /**
     * Element name 'distributable'.
     */
    public static final String DISTRIBUTABLE = "distributable";
    
    /**
     * Element name 'context-param'.
     */
    public static final String CONTEXT_PARAM = "context-param";
    
    /**
     * Element name 'param-name'.
     */
    public static final String PARAM_NAME = "param-name";
    
    /**
     * Element name 'param-value'.
     */
    public static final String PARAM_VALUE = "param-value";
    
    /**
     * Element name 'filter'.
     */
    public static final String FILTER = "filter";
    
    /**
     * Element name 'filter-name'.
     */
    public static final String FILTER_NAME = "filter-name";
    
    /**
     * Element name 'filter-class'.
     */
    public static final String FILTER_CLASS = "filter-class";
    
    /**
     * Element name 'filter-mapping'.
     */
    public static final String FILTER_MAPPING = "filter-mapping";
    
    /**
     * Element name 'init-param'.
     */
    public static final String INIT_PARAM = "init-param";
    
    /**
     * Element name 'listener'.
     */
    public static final String LISTENER = "listener";
    
    /**
     * Element name 'servlet',
     */
    public static final String SERVLET = "servlet";
    
    /**
     * Element name 'servlet-name',
     */
    public static final String SERVLET_NAME = "servlet-name";
    
    /**
     * Element name 'jsp-file'.
     */
    public static final String JSP_FILE = "jsp-file";
    
    /**
     * Element name 'servlet-class'.
     */
    public static final String SERVLET_CLASS = "servlet-class";
    
    /**
     * Element name 'servlet-mapping',
     */
    public static final String SERVLET_MAPPING = "servlet-mapping";
    
    /**
     * Element name 'url-pattern',
     */
    public static final String URL_PATTERN = "url-pattern";
    
    /**
     * Element name 'session-config',
     */
    public static final String SESSION_CONFIG = "session-config";
    
    /**
     * Element name 'mime-mapping',
     */
    public static final String MIME_MAPPING = "mime-mapping";
    
    /**
     * Element name 'welcome-file-list',
     */
    public static final String WELCOME_FILE_LIST = "welcome-file-list";
    
    /**
     * Element name 'error-page',
     */
    public static final String ERROR_PAGE = "error-page";
    
    /**
     * Element name 'taglib',
     */
    public static final String TAGLIB = "taglib";
    
    /**
     * Element name 'resource-env-ref',
     */
    public static final String RESOURCE_ENV_REF = "resource-env-ref";
    
    /**
     * Element name 'resource-ref',
     */
    public static final String RESOURCE_REF = "resource-ref";
    
    /**
     * Element name 'security-constraint',
     */
    public static final String SECURITY_CONSTRAINT = "security-constraint";
    
    /**
     * Element name 'login-config',
     */
    public static final String LOGIN_CONFIG = "login-config";
    
    /**
     * Element name 'security-role',
     */
    public static final String SECURITY_ROLE = "security-role";
    
    /**
     * Element name 'env-entry',
     */
    public static final String ENV_ENTRY = "env-entry";
    
    /**
     * Element name 'ejb-ref',
     */
    public static final String EJB_REF = "ejb-ref";
    
    /**
     * Element name 'ejb-local-ref',
     */
    public static final String EJB_LOCAL_REF = "ejb-local-ref";
    
    // Private Constants -------------------------------------------------------
    
    /**
     * Specifies the order in which the top-level elements must appear in the
     * descriptor, according to the DTD.
     */
    private static final String[] ELEMENT_ORDER = {
        WebXmlElement.ICON,
        WebXmlElement.DISPLAY_NAME,
        WebXmlElement.DESCRIPTION,
        WebXmlElement.DISTRIBUTABLE,
        WebXmlElement.FILTER,
        WebXmlElement.FILTER_MAPPING,
        WebXmlElement.LISTENER,
        WebXmlElement.SERVLET,
        WebXmlElement.SERVLET_MAPPING,
        WebXmlElement.SESSION_CONFIG,
        WebXmlElement.MIME_MAPPING,
        WebXmlElement.WELCOME_FILE_LIST,
        WebXmlElement.ERROR_PAGE,
        WebXmlElement.TAGLIB,
        WebXmlElement.RESOURCE_ENV_REF,
        WebXmlElement.RESOURCE_REF,
        WebXmlElement.SECURITY_CONSTRAINT,
        WebXmlElement.LOGIN_CONFIG,
        WebXmlElement.SECURITY_ROLE,
        WebXmlElement.ENV_ENTRY,
        WebXmlElement.EJB_REF,
        WebXmlElement.EJB_LOCAL_REF,
    };
    
    // Public Static Methods ---------------------------------------------------
    
    /**
     * Creates a filter-mapping element initialized with the filter name and
     * class.
     * 
     * @param theDocument The document for which the element should be created
     * @param theFilterName The name of the filter
     * @param theUrlPattern The URL-pattern to map the filter to
     * @return A DOM element representing the filter definition
     */
    public static Element createFilterMapping(Document theDocument,
        String theFilterName, String theUrlPattern)
    {
        Element filterMappingElement =
            theDocument.createElement(WebXmlElement.FILTER_MAPPING);
        filterMappingElement.appendChild(
            createNestedText(theDocument, FILTER_NAME, theFilterName));
        filterMappingElement.appendChild(
            createNestedText(theDocument, URL_PATTERN, theUrlPattern));
        return filterMappingElement;
    }
    
    /**
     * Creates an init-param element initialized with the parameter name and
     * value.
     * 
     * @param theDocument The document for which the element should be created
     * @param theParamName The name of the parameter
     * @param theParamValue The parameter value
     * @return A DOM element representing the parameter definition
     */
    public static Element createInitParam(Document theDocument,
        String theParamName, String theParamValue)
    {
        Element initParamElement =
            theDocument.createElement(WebXmlElement.INIT_PARAM);
        initParamElement.appendChild(
            createNestedText(theDocument, PARAM_NAME, theParamName));
        initParamElement.appendChild(
            createNestedText(theDocument, PARAM_VALUE, theParamValue));
        return initParamElement;
    }
    
    /**
     * Creates a servlet-mapping element initialized with the servlet name and
     * class.
     * 
     * @param theDocument The document for which the element should be created
     * @param theServletName The name of the servlet
     * @param theUrlPattern The URL-pattern to map the servlet to
     * @return A DOM element representing the servlet definition
     */
    public static Element createServletMapping(Document theDocument,
        String theServletName, String theUrlPattern)
    {
        Element servletMappingElement =
            theDocument.createElement(WebXmlElement.SERVLET_MAPPING);
        servletMappingElement.appendChild(
            createNestedText(theDocument, SERVLET_NAME, theServletName));
        servletMappingElement.appendChild(
            createNestedText(theDocument, URL_PATTERN, theUrlPattern));
        return servletMappingElement;
    }
    
    /**
     * Finds and returns the node before which a new element of the specified 
     * name should be inserted.
     * 
     * @param theDocument The DOM representation of the descriptor
     * @param theTagName The name of the element that is to be inserted
     * @return The node before which the element should be inserted, or
     *          <code>null</code> if the element should be appended to the end
     */
    public static Node getNodeToInsertBefore(Document theDocument,
        String theTagName)
    {
        Element root = theDocument.getDocumentElement();
        for (int i = 0; i < ELEMENT_ORDER.length; i++)
        {
            if (ELEMENT_ORDER[i].equals(theTagName))
            {
                for (int j = i + 1; j < ELEMENT_ORDER.length; j++)
                {
                    NodeList elements =
                        root.getElementsByTagName(ELEMENT_ORDER[j]);
                    if (elements.getLength() > 0)
                    {
                        return elements.item(0);
                    }
                }
                break;
            }
        }
        return null;
    }
    
    // Private Static Methods --------------------------------------------------
    
    /**
     * Creates an element that contains nested text.
     * 
     * @param theDocument The document for which the element should be created
     * @param theTagName The name of the element to create
     * @param theText The text that should be nested in the element
     * @return The DOM element
     */
    private static Element createNestedText(Document theDocument, 
        String theTagName, String theText)
    {
        Element element = theDocument.createElement(theTagName);
        element.appendChild(theDocument.createTextNode(theText));
        return element;
    }
    
}
