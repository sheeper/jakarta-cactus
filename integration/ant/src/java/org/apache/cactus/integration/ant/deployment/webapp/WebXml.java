/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * @since Cactus 1.5
 * @version $Id$
 */
public class WebXml
{
    
    // Private Constants -------------------------------------------------------
    
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
    public final Document getDocument()
    {
        return this.document;
    }
    
    /**
     * Returns the servlet API version.
     * 
     * @return The version
     */
    public final WebXmlVersion getVersion()
    {
        DocumentType docType = this.document.getDoctype();
        if (docType != null)
        {
            return WebXmlVersion.valueOf(docType);
        }
        return null;
    }

    /**
     * Adds a new servlet filter to the descriptor.
     * 
     * @param theFilterName The name of the filter to add
     * @param theFilterClass The name of the class implementing the filter
     */
    public final void addFilter(String theFilterName, String theFilterClass)
    {
        if (theFilterName == null)
        {
            throw new NullPointerException();
        }
        if (hasFilter(theFilterName))
        {
            throw new IllegalStateException("Filter '" + theFilterName
                + "' already defined");
        }
        Element filterElement =
            this.document.createElement(WebXmlTag.FILTER.getTagName());
        filterElement.appendChild(
            createNestedText(WebXmlTag.FILTER_NAME, theFilterName));
        filterElement.appendChild(
            createNestedText(WebXmlTag.FILTER_CLASS, theFilterClass));
        addElement(WebXmlTag.FILTER, filterElement);
    }

    /**
     * Adds a new context-param element to the descriptor.
     * 
     * @param theContextParam The element representing the context-param 
     *        definition
     */
    public final void addContextParam(Element theContextParam)
    {
        checkElement(theContextParam, WebXmlTag.CONTEXT_PARAM);

        String paramName = 
            getNestedText(theContextParam, WebXmlTag.PARAM_NAME);
        if (paramName == null)
        {
            throw new IllegalArgumentException(
                "Not a valid context-param name element");
        }

        String paramValue = 
            getNestedText(theContextParam, WebXmlTag.PARAM_VALUE);
        if (paramValue == null)
        {
            throw new IllegalArgumentException(
                "Not a valid context-param value element");
        }

        if (hasContextParam(paramName))
        {
            throw new IllegalStateException("Context param '" + paramName
                + "' already defined");
        }
        addElement(WebXmlTag.CONTEXT_PARAM, theContextParam);
    }
    
    /**
     * Adds a new servlet filter to the descriptor.
     * 
     * @param theFilter The element representing the filter definition
     */
    public final void addFilter(Element theFilter)
    {
        checkElement(theFilter, WebXmlTag.FILTER);
        String filterName = getNestedText(theFilter, WebXmlTag.FILTER_NAME);
        if (filterName == null)
        {
            throw new IllegalArgumentException("Not a valid filter element");
        }
        if (hasFilter(filterName))
        {
            throw new IllegalStateException("Filter '" + filterName
                + "' already defined");
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
    public final void addFilterInitParam(String theFilterName,
        String theParamName, String theParamValue)
    {
        Element filterElement = getFilter(theFilterName);
        if (filterElement == null)
        {
            throw new IllegalStateException("Filter '" + theFilterName
                + "' not defined");
        }
        addInitParam(filterElement, theParamName, theParamValue);
    }
    
    /**
     * Adds a filter mapping to the descriptor.
     * 
     * @param theFilterName The name of the filter
     * @param theUrlPattern The URL pattern the filter should be mapped to
     */
    public final void addFilterMapping(String theFilterName,
        String theUrlPattern)
    {
        if (!hasFilter(theFilterName))
        {
            throw new IllegalStateException("Filter '" + theFilterName
                + "' not defined");
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
     * Returns the element that contains the definition of a specific servlet
     * filter, or <code>null</code> if a filter of the specified name is not
     * defined in the descriptor.
     * 
     * @param theFilterName The name of the servlet filter
     * @return The DOM element representing the filter definition
     */
    public final Element getFilter(String theFilterName)
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
     * Returns the element that contains the definition of a specific context
     * param, or <code>null</code> if a context param of the specified name 
     * is not defined in the descriptor.
     * 
     * @param theParamName The context param name
     * @return The DOM element representing the context param definition
     */
    public final Element getContextParam(String theParamName)
    {
        if (theParamName == null)
        {
            throw new NullPointerException();
        }
        Iterator contextParamElements = getElements(WebXmlTag.CONTEXT_PARAM);
        while (contextParamElements.hasNext())
        {
            Element contextParamElement = (Element) contextParamElements.next();
            if (theParamName.equals(getNestedText(
                    contextParamElement, WebXmlTag.PARAM_NAME)))
            {
                return contextParamElement;
            }
        }
        return null;
    }

    /**
     * @param theContextParam the context param element from which to extract 
     *        the name
     * @return the name of the passed context param element
     */
    public final String getContextParamName(Element theContextParam)
    {
        return getNestedText(theContextParam, WebXmlTag.PARAM_NAME);
    }
    
    /**
     * Returns a list of names of filters that are mapped to the specified
     * class.
     * 
     * @param theClassName The fully qualified name of the filter class
     * @return An iterator over the names of the filters mapped to the class
     */
    public final Iterator getFilterNamesForClass(String theClassName)
    {
        if (theClassName == null)
        {
            throw new NullPointerException();
        }
        Iterator filterElements = getElements(WebXmlTag.FILTER);
        List filterNames = new ArrayList();
        while (filterElements.hasNext())
        {
            Element filterElement = (Element) filterElements.next();
            if (theClassName.equals(getNestedText(
                filterElement, WebXmlTag.FILTER_CLASS)))
            {
                filterNames.add(getNestedText(
                    filterElement, WebXmlTag.FILTER_NAME));
            }
        }
        return filterNames.iterator();
    }
    
    /**
     * Returns the value of an initialization parameter of the specified filter.
     * 
     * @param theFilterName The name of the servlet filter
     * @param theParamName The name of the initialization parameter
     * @return The parameter value
     */
    public final String getFilterInitParam(String theFilterName,
        String theParamName)
    {
        return getInitParam(getFilter(theFilterName), theParamName);
    }
    
    /**
     * Returns the names of the initialization parameters of the specified 
     * servlet filter.
     * 
     * @param theFilterName The name of the servlet filter of which the
     *         parameter names should be retrieved
     * @return An iterator over the ordered list of parameter names
     */
    public final Iterator getFilterInitParamNames(String theFilterName)
    {
        return getInitParamNames(getFilter(theFilterName));
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
    public final Iterator getFilterMappings(String theFilterName)
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
    public final Iterator getFilterNames()
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
     * Returns whether a context param by the specified name is defined in the 
     * deployment descriptor.
     * 
     * @param theParamName The name of the context param
     * @return <code>true</code> if the context param is defined,
     *         <code>false</code> otherwise
     */
    public final boolean hasContextParam(String theParamName)
    {
        return (getContextParam(theParamName) != null);
    }
    
    /**
     * Returns whether a servlet filter by the specified name is defined in the 
     * deployment descriptor.
     * 
     * @param theFilterName The name of the filter
     * @return <code>true</code> if the filter is defined, <code>false</code>
     *          otherwise
     */
    public final boolean hasFilter(String theFilterName)
    {
        return (getFilter(theFilterName) != null);
    }
    
    /**
     * Adds a mapped JSP file to the descriptor.
     * 
     * @param theServletName The name of the servlet to add
     * @param theJspFile The path to the JSP file relative to the root of the
     *        web application
     */
    public final void addJspFile(String theServletName, String theJspFile)
    {
        if (theServletName == null)
        {
            throw new NullPointerException();
        }
        if (hasFilter(theServletName))
        {
            throw new IllegalStateException("Servlet '" + theServletName
                + "' already defined");
        }
        Element servletElement =
            this.document.createElement(WebXmlTag.SERVLET.getTagName());
        servletElement.appendChild(
            createNestedText(WebXmlTag.SERVLET_NAME, theServletName));
        servletElement.appendChild(
            createNestedText(WebXmlTag.JSP_FILE, theJspFile));
        addElement(WebXmlTag.SERVLET, servletElement);
    }
    
    /**
     * Adds a new servlet to the descriptor.
     * 
     * @param theServletName The name of the servlet to add
     * @param theServletClass The name of the class implementing the servlet
     */
    public final void addServlet(String theServletName, String theServletClass)
    {
        if (theServletName == null)
        {
            throw new NullPointerException();
        }
        if (hasServlet(theServletName))
        {
            throw new IllegalStateException("Servlet '" + theServletName
                + "' already defined");
        }
        Element servletElement =
            this.document.createElement(WebXmlTag.SERVLET.getTagName());
        servletElement.appendChild(
            createNestedText(WebXmlTag.SERVLET_NAME, theServletName));
        servletElement.appendChild(
            createNestedText(WebXmlTag.SERVLET_CLASS, theServletClass));
        addElement(WebXmlTag.SERVLET, servletElement);
    }
    
    /**
     * Adds a new servlet to the descriptor.
     * 
     * @param theServlet The element representing the servlet definition
     */
    public final void addServlet(Element theServlet)
    {
        checkElement(theServlet, WebXmlTag.SERVLET);
        String servletName = getNestedText(theServlet, WebXmlTag.SERVLET_NAME);
        if (servletName == null)
        {
            throw new IllegalArgumentException("Not a valid servlet element");
        }
        if (hasServlet(servletName))
        {
            throw new IllegalStateException("Servlet '" + servletName
                + "' already defined");
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
    public final void addServletInitParam(String theServletName,
        String theParamName, String theParamValue)
    {
        Element servletElement = getServlet(theServletName);
        if (servletElement == null)
        {
            throw new IllegalStateException("Servlet '" + theServletName
                + "' not defined");
        }
        addInitParam(servletElement, theParamName, theParamValue);
    }
    
    /**
     * Adds a servlet mapping to the descriptor.
     * 
     * @param theServletName The name of the servlet
     * @param theUrlPattern The URL pattern the servlet should be mapped to
     */
    public final void addServletMapping(String theServletName,
        String theUrlPattern)
    {
        if (!hasServlet(theServletName))
        {
            throw new IllegalStateException("Servlet '" + theServletName
                + "' not defined");
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
     * Returns the element that contains the definition of a specific servlet, 
     * or <code>null</code> if a servlet of the specified name is not defined
     * in the descriptor.
     * 
     * @param theServletName The name of the servlet
     * @return The DOM element representing the servlet definition
     */
    public final Element getServlet(String theServletName)
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
    public final String getServletInitParam(String theServletName,
        String theParamName)
    {
        return getInitParam(getServlet(theServletName), theParamName);
    }
    
    /**
     * Returns the names of the initialization parameters of the specified 
     * servlet.
     * 
     * @param theServletName The name of the servlet of which the parameter
     *         names should be retrieved
     * @return An iterator over the ordered list of parameter names
     */
    public final Iterator getServletInitParamNames(String theServletName)
    {
        return getInitParamNames(getServlet(theServletName));
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
    public final Iterator getServletMappings(String theServletName)
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
    public final Iterator getServletNames()
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
     * Returns a list of names of servlets that are mapped to the specified
     * class.
     * 
     * @param theClassName The fully qualified name of the servlet class
     * @return An iterator over the names of the servlets mapped to the class
     */
    public final Iterator getServletNamesForClass(String theClassName)
    {
        if (theClassName == null)
        {
            throw new NullPointerException();
        }
        Iterator servletElements = getElements(WebXmlTag.SERVLET);
        List servletNames = new ArrayList();
        while (servletElements.hasNext())
        {
            Element servletElement = (Element) servletElements.next();
            if (theClassName.equals(getNestedText(
                servletElement, WebXmlTag.SERVLET_CLASS)))
            {
                servletNames.add(getNestedText(
                    servletElement, WebXmlTag.SERVLET_NAME));
            }
        }
        return servletNames.iterator();
    }
    
    /**
     * Returns a list of names of servlets that are mapped to the specified
     * JSP file.
     * 
     * @param theJspFile The path to the JSP file, relative to the root of the
     *        web-application
     * @return An iterator over the names of the servlets mapped to the JSP file
     */
    public final Iterator getServletNamesForJspFile(String theJspFile)
    {
        if (theJspFile == null)
        {
            throw new NullPointerException();
        }
        Iterator servletElements = getElements(WebXmlTag.SERVLET);
        List servletNames = new ArrayList();
        while (servletElements.hasNext())
        {
            Element servletElement = (Element) servletElements.next();
            if (theJspFile.equals(getNestedText(
                servletElement, WebXmlTag.JSP_FILE)))
            {
                servletNames.add(getNestedText(
                    servletElement, WebXmlTag.SERVLET_NAME));
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
    public final boolean hasServlet(String theServletName)
    {
        return (getServlet(theServletName) != null);
    }
    
    /**
     * Creates and adds a security-constraint to the descriptor.
     * 
     * @param theWebResourceName The name of the web resource collection to
     *        protect
     * @param theUrlPattern The URL pattern to apply the constraint to
     * @param theRoles The list of authorized roles
     */
    public final void addSecurityConstraint(String theWebResourceName,
        String theUrlPattern, List theRoles)
    {
        if ((theWebResourceName == null) || (theUrlPattern == null)
         || (theRoles == null))
        {
            throw new NullPointerException();
        }
        if (hasSecurityConstraint(theUrlPattern))
        {
            throw new IllegalStateException("Security constraint for URL "
                + "pattern " + theUrlPattern + " already defined");
        }
        Element securityConstraintElement =
            this.document.createElement(
                WebXmlTag.SECURITY_CONSTRAINT.getTagName());
        Element webResourceCollectionElement =
            this.document.createElement(
                WebXmlTag.WEB_RESOURCE_COLLECTION.getTagName());
        webResourceCollectionElement.appendChild(
            createNestedText(WebXmlTag.WEB_RESOURCE_NAME, theWebResourceName));
        webResourceCollectionElement.appendChild(
            createNestedText(WebXmlTag.URL_PATTERN, theUrlPattern));
        securityConstraintElement.appendChild(webResourceCollectionElement);
        Element authConstraintElement =
            this.document.createElement(WebXmlTag.AUTH_CONSTRAINT.getTagName());
        for (Iterator i = theRoles.iterator(); i.hasNext();)
        {
            authConstraintElement.appendChild(
                createNestedText(WebXmlTag.ROLE_NAME, (String) i.next()));
        }
        securityConstraintElement.appendChild(authConstraintElement);
        addElement(WebXmlTag.SECURITY_CONSTRAINT, securityConstraintElement);
    }

    /**
     * Returns the element that contains the security constraint defined for the
     * specified URL pattern.
     * 
     * @param theUrlPattern The URL pattern
     * @return The DOM element representing the security constraint
     */
    public final Element getSecurityConstraint(String theUrlPattern)
    {
        if (theUrlPattern == null)
        {
            throw new NullPointerException();
        }
        Iterator securityConstraintElements =
            getElements(WebXmlTag.SECURITY_CONSTRAINT);
        while (securityConstraintElements.hasNext())
        {
            Element securityConstraintElement = (Element)
                securityConstraintElements.next();
            Iterator webResourceCollectionElements = 
                getNestedElements(securityConstraintElement,
                    WebXmlTag.WEB_RESOURCE_COLLECTION);
            if (webResourceCollectionElements.hasNext())
            {
                Element webResourceCollectionElement = (Element)
                    webResourceCollectionElements.next();
                if (theUrlPattern.equals(getNestedText(
                    webResourceCollectionElement, WebXmlTag.URL_PATTERN)))
                {
                    return securityConstraintElement;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns whether a security constraint has been mapped to the specified
     * URL pattern.
     * 
     * @param theUrlPattern The URL patterm
     * @return <code>true</code> if a security constraint is defined,
     *         <code>false</code> otherwise
     */
    public final boolean hasSecurityConstraint(String theUrlPattern)
    {
        return (getSecurityConstraint(theUrlPattern) != null);
    }
    
    /**
     * Returns whether the descriptor has a login configuration.
     * 
     * @return <code>true</code> if a login config is defined,
     *         <code>false</code> otherwise
     */
    public final boolean hasLoginConfig()
    {
        return (getLoginConfig() != null);
    }

    /**
     * Returns whether the descriptor has a login configuration.
     * 
     * @return <code>true</code> if a login config is defined,
     *         <code>false</code> otherwise
     */
    public final Element getLoginConfig()
    {
        Iterator loginConfigElements = getElements(WebXmlTag.LOGIN_CONFIG);
        if (loginConfigElements.hasNext())
        {
            return (Element) loginConfigElements.next();
        }
        return null;
    }

    /**
     * Returns the authorization method defined by the login configuration.
     * 
     * @return The authorization method
     */
    public final String getLoginConfigAuthMethod()
    {
        return getNestedText(getLoginConfig(), WebXmlTag.AUTH_METHOD);
    }

    /**
     * Sets the login configuration.
     * 
     * @param theAuthMethod The authentication method (for example, BASIC)
     * @param theRealmName The name of the realm
     */
    public final void setLoginConfig(String theAuthMethod, String theRealmName)
    {
        if ((theRealmName == null) || (theAuthMethod == null))
        {
            throw new NullPointerException();
        }
        Element loginConfigElement =
            document.createElement(WebXmlTag.LOGIN_CONFIG.getTagName());
        loginConfigElement.appendChild(
            createNestedText(WebXmlTag.AUTH_METHOD, theAuthMethod));
        loginConfigElement.appendChild(
            createNestedText(WebXmlTag.REALM_NAME, theRealmName));
        replaceElement(WebXmlTag.LOGIN_CONFIG, loginConfigElement);
    }

    /**
     * Adds a new security role to the descriptor.
     * 
     * @param theRoleName The name of the role to add
     */
    public final void addSecurityRole(String theRoleName)
    {
        if (theRoleName == null)
        {
            throw new NullPointerException();
        }
        if (hasSecurityRole(theRoleName))
        {
            throw new IllegalStateException("Security role '" + theRoleName
                + "' already defined");
        }
        Element securityRoleElement =
            this.document.createElement(WebXmlTag.SECURITY_ROLE.getTagName());
        securityRoleElement.appendChild(
            createNestedText(WebXmlTag.ROLE_NAME, theRoleName));
        addElement(WebXmlTag.SECURITY_ROLE, securityRoleElement);
    }
    
    /**
     * Returns the element that contains the specified security role, or
     * <code>null</code> if the role is not defined in the descriptor.
     * 
     * @param theRoleName The name of the role
     * @return The DOM element representing the security role
     */
    public final Element getSecurityRole(String theRoleName)
    {
        if (theRoleName == null)
        {
            throw new NullPointerException();
        }
        Iterator securityRoleElements = getElements(WebXmlTag.SECURITY_ROLE);
        while (securityRoleElements.hasNext())
        {
            Element securityRoleElement = (Element) securityRoleElements.next();
            if (theRoleName.equals(getNestedText(
                securityRoleElement, WebXmlTag.ROLE_NAME)))
            {
                return securityRoleElement;
            }
        }
        return null;
    }
    
    /**
     * Returns a list of the security role names defined in the deployment 
     * descriptor
     * 
     * @return An iterator over the list of security role names, or an empty
     *         iterator if no security roles are defined in the descriptor
     */
    public final Iterator getSecurityRoleNames()
    {
        List securityRoleNames = new ArrayList();
        Iterator securityRoleElements = getElements(WebXmlTag.SECURITY_ROLE);
        while (securityRoleElements.hasNext())
        {
            Element securityRoleElement = (Element) securityRoleElements.next();
            String securityRoleName =
                getNestedText(securityRoleElement, WebXmlTag.ROLE_NAME);
            if (securityRoleName != null)
            {
                securityRoleNames.add(securityRoleName);
            }
        }
        return securityRoleNames.iterator();
    }
    
    /**
     * Returns whether a specific security role has been defined.
     * 
     * @param theRoleName The name of the role
     * @return <code>true</code> if the security role is defined,
     *         <code>false</code> otherwise
     */
    public final boolean hasSecurityRole(String theRoleName)
    {
        return (getSecurityRole(theRoleName) != null);
    }
    
    /**
     * Returns an iterator over the elements that match the specified tag.
     * 
     * @param theTag The descriptor tag of which the elements should be
     *         returned
     * @return An iterator over the elements matching the tag, in the order 
     *          they occur in the descriptor
     */
    public final Iterator getElements(WebXmlTag theTag)
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
    public final void addElement(WebXmlTag theTag, Element theElement)
    {
        checkElement(theElement, theTag);
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
    public final void replaceElement(WebXmlTag theTag, Element theElement)
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
     * Adds an initialization parameter to the specified filter or servlet.
     * 
     * @param theElement The filter or servlet element to which the
     *         initialization parameter should be added
     * @param theParamName The name of the parameter
     * @param theParamValue The parameter value
     */
    private void addInitParam(Element theElement, String theParamName,
        String theParamValue)
    {
        Element initParamElement =
            this.document.createElement(WebXmlTag.INIT_PARAM.getTagName());
        initParamElement.appendChild(
            createNestedText(WebXmlTag.PARAM_NAME, theParamName));
        initParamElement.appendChild(
            createNestedText(WebXmlTag.PARAM_VALUE, theParamValue));
        Iterator loadOnStartupElements = getNestedElements(theElement,
            WebXmlTag.LOAD_ON_STARTUP);
        if (loadOnStartupElements.hasNext())
        {
            theElement.insertBefore(initParamElement,
                (Element) loadOnStartupElements.next());
        }
        else
        {
            theElement.appendChild(initParamElement);
        }
    }
    
    /**
     * Checks an element whether its name matches the specified name.
     * 
     * @param theElement The element to check
     * @param theExpectedTag The expected tag name
     * @throws IllegalArgumentException If the element name doesn't match
     */
    private void checkElement(Element theElement, WebXmlTag theExpectedTag)
        throws IllegalArgumentException
    {
        if (!theExpectedTag.getTagName().equals(theElement.getNodeName()))
        {
            throw new IllegalArgumentException("Not a '" + theExpectedTag
                + "' element");
        }
    }
    
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
        WebXmlTag theTag)
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
     * Returns the value of an initialization parameter of the specified filter
     * or servlet.
     * 
     * @param theElement The filter or servlet element that contains the
     *         initialization parameters
     * @param theParamName The name of the initialization parameter
     * @return The parameter value
     */
    private String getInitParam(Element theElement, String theParamName)
    {
        if (theElement != null)
        {
            NodeList initParamElements =
                theElement.getElementsByTagName(
                    WebXmlTag.INIT_PARAM.getTagName());
            for (int i = 0; i < initParamElements.getLength(); i++)
            {
                Element initParamElement = (Element) initParamElements.item(i);
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
     * filter or servlet.
     * 
     * @param theElement The filter or servlet element that contains the
     *         initialization parameters
     * @return An iterator over the ordered list of parameter names
     */
    private Iterator getInitParamNames(Element theElement)
    {
        List initParamNames = new ArrayList();
        if (theElement != null)
        {
            NodeList initParamElements =
                theElement.getElementsByTagName(
                    WebXmlTag.INIT_PARAM.getTagName());
            for (int i = 0; i < initParamElements.getLength(); i++)
            {
                Element initParamElement = (Element) initParamElements.item(i);
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
     * Returns the node before which the specified tag should be inserted, or
     * <code>null</code> if the node should be inserted at the end of the 
     * descriptor.
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
    
    /**
     * Returns the text nested inside a child element of the specified element.
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