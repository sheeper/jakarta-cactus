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

/**
 * Represents the various top-level tags in a web deployment descriptor as a 
 * typesafe enumeration.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public final class WebXmlTag
{
    
    // Public Constants --------------------------------------------------------
    
    /**
     * Element name 'icon'.
     */
    public static final WebXmlTag ICON =
        new WebXmlTag("icon", false);
    
    /**
     * Element name 'display-name'.
     */
    public static final WebXmlTag DISPLAY_NAME =
        new WebXmlTag("display-name", false);
    
    /**
     * Element name 'description'.
     */
    public static final WebXmlTag DESCRIPTION =
        new WebXmlTag("description", false);
    
    /**
     * Element name 'distributable'.
     */
    public static final WebXmlTag DISTRIBUTABLE =
        new WebXmlTag("distributable", false);
    
    /**
     * Element name 'context-param'.
     */
    public static final WebXmlTag CONTEXT_PARAM =
        new WebXmlTag("context-param");
    
    /**
     * Element name 'param-name'.
     */
    public static final WebXmlTag PARAM_NAME =
        new WebXmlTag("param-name");
    
    /**
     * Element name 'param-value'.
     */
    public static final WebXmlTag PARAM_VALUE =
        new WebXmlTag("param-value");
    
    /**
     * Element name 'filter'.
     */
    public static final WebXmlTag FILTER =
        new WebXmlTag("filter");
    
    /**
     * Element name 'filter-name'.
     */
    public static final WebXmlTag FILTER_NAME =
        new WebXmlTag("filter-name");
    
    /**
     * Element name 'filter-class'.
     */
    public static final WebXmlTag FILTER_CLASS =
        new WebXmlTag("filter-class");
    
    /**
     * Element name 'filter-mapping'.
     */
    public static final WebXmlTag FILTER_MAPPING =
        new WebXmlTag("filter-mapping");
    
    /**
     * Element name 'init-param'.
     */
    public static final WebXmlTag INIT_PARAM =
        new WebXmlTag("init-param");
    
    /**
     * Element name 'listener'.
     */
    public static final WebXmlTag LISTENER =
        new WebXmlTag("listener");
    
    /**
     * Element name 'servlet'.
     */
    public static final WebXmlTag SERVLET =
        new WebXmlTag("servlet");
    
    /**
     * Element name 'servlet-name'.
     */
    public static final WebXmlTag SERVLET_NAME =
        new WebXmlTag("servlet-name");
    
    /**
     * Element name 'jsp-file'.
     */
    public static final WebXmlTag JSP_FILE =
        new WebXmlTag("jsp-file");
    
    /**
     * Element name 'servlet-class'.
     */
    public static final WebXmlTag SERVLET_CLASS =
        new WebXmlTag("servlet-class");
    
    /**
     * Element name 'servlet-mapping'.
     */
    public static final WebXmlTag SERVLET_MAPPING =
        new WebXmlTag("servlet-mapping");
    
    /**
     * Element name 'url-pattern'.
     */
    public static final WebXmlTag URL_PATTERN =
        new WebXmlTag("url-pattern");
    
    /**
     * Element name 'session-config'.
     */
    public static final WebXmlTag SESSION_CONFIG =
        new WebXmlTag("session-config", false);
    
    /**
     * Element name 'mime-mapping'.
     */
    public static final WebXmlTag MIME_MAPPING =
        new WebXmlTag("mime-mapping");
    
    /**
     * Element name 'welcome-file-list'.
     */
    public static final WebXmlTag WELCOME_FILE_LIST =
        new WebXmlTag("welcome-file-list", false);
    
    /**
     * Element name 'error-page'.
     */
    public static final WebXmlTag ERROR_PAGE =
        new WebXmlTag("error-page");
    
    /**
     * Element name 'taglib'.
     */
    public static final WebXmlTag TAGLIB =
        new WebXmlTag("taglib");
    
    /**
     * Element name 'resource-env-ref'.
     */
    public static final WebXmlTag RESOURCE_ENV_REF =
        new WebXmlTag("resource-env-ref");
    
    /**
     * Element name 'resource-ref'.
     */
    public static final WebXmlTag RESOURCE_REF =
        new WebXmlTag("resource-ref");
    
    /**
     * Element name 'security-constraint'.
     */
    public static final WebXmlTag SECURITY_CONSTRAINT =
        new WebXmlTag("security-constraint");
    
    /**
     * Element name 'web-resource-collection'.
     */
    public static final WebXmlTag WEB_RESOURCE_COLLECTION =
        new WebXmlTag("web-resource-collection");
    
    /**
     * Element name 'web-resource-name'.
     */
    public static final WebXmlTag WEB_RESOURCE_NAME =
        new WebXmlTag("web-resource-name");
    
    /**
     * Element name 'auth-constraint'.
     */
    public static final WebXmlTag AUTH_CONSTRAINT =
        new WebXmlTag("auth-constraint");
    
    /**
     * Element name 'login-config'.
     */
    public static final WebXmlTag LOGIN_CONFIG =
        new WebXmlTag("login-config", false);
    
    /**
     * Element name 'auth-method'.
     */
    public static final WebXmlTag AUTH_METHOD =
        new WebXmlTag("auth-method");

    /**
     * Element name 'realm-name'.
     */
    public static final WebXmlTag REALM_NAME =
        new WebXmlTag("realm-name");

    /**
     * Element name 'security-role'.
     */
    public static final WebXmlTag SECURITY_ROLE =
        new WebXmlTag("security-role");
    
    /**
     * Element name 'role-name'.
     */
    public static final WebXmlTag ROLE_NAME =
        new WebXmlTag("role-name");
    
    /**
     * Element name 'env-entry'.
     */
    public static final WebXmlTag ENV_ENTRY =
        new WebXmlTag("env-entry");
    
    /**
     * Element name 'ejb-ref'.
     */
    public static final WebXmlTag EJB_REF =
        new WebXmlTag("ejb-ref");
    
    /**
     * Element name 'ejb-local-ref'.
     */
    public static final WebXmlTag EJB_LOCAL_REF =
        new WebXmlTag("ejb-local-ref");
    
    // Instance Variables ------------------------------------------------------
    
    /**
     * The tag name,
     */
    private String tagName;
    
    /**
     * Whether multiple occurrences of the tag in the descriptor are allowed.
     */
    private boolean multipleAllowed;
    
    // Constructors ------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @param theTagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in
     *         the descriptor
     */
    private WebXmlTag(String theTagName, boolean isMultipleAllowed)
    {
        this.tagName = theTagName;
        this.multipleAllowed = isMultipleAllowed;
    }

    /**
     * Constructor.
     * 
     * @param theTagName The tag name of the element
     */
    private WebXmlTag(String theTagName)
    {
        this(theTagName, true);
    }

    // Public Methods ----------------------------------------------------------
    
    /**
     * @see java.lang.Object#toString
     */
    public final boolean equals(Object theOther)
    {
        return super.equals(theOther);
    }
    
    /**
     * @see java.lang.Object#hashCode
     */
    public final int hashCode()
    {
        return super.hashCode();
    }
    
    /**
     * Returns the tag name.
     * 
     * @return The tag name
     */
    public final String getTagName()
    {
        return this.tagName;
    }
    
    /**
     * Returns whether the tag may occur multiple times in the descriptor.
     * 
     * @return Whether multiple occurrences are allowed
     */
    public final boolean isMultipleAllowed()
    {
        return this.multipleAllowed;
    }
    
    /**
     * @see java.lang.Object#toString
     */
    public final String toString()
    {
        return getTagName();
    }
    
}
