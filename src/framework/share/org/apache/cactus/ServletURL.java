/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */
package org.apache.commons.cactus;

import java.util.*;
import java.io.*;
import java.security.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import junit.framework.*;

/**
 * Simulate an HTTP URL by breaking it into its different parts :<br>
 * <code><pre><b>
 * URL        = "http://" + serverName (including port) + requestURI ? queryString<br>
 * requestURI = contextPath + servletPath + pathInfo
 * </b></pre></code>
 * From the Servlet 2.2 specification :<br>
 * <code><pre><ul><li><b>Context Path</b>: The path prefix associated with the
 *   ServletContext that this servlet is a part of. If this context is the
 *   default context rooted at the base of the web server's URL namespace, this
 *   path will be an empty string. Otherwise, this path starts with a character
 *   but does not end with a character.</li>
 *   <li><b>Servlet Path</b>: The path section that directly corresponds to the
 *   mapping which activated this request. This path starts with a character.</li>
 *   <li><b>PathInfo</b>: The part of the request path that is not part of the
 *   Context Path or the Servlet Path.</li></ul></pre></code>
 *
 * @version @version@
 */
public class ServletURL
{
    /**
     * Name of the parameter in the HTTP request that represents the Server name
     * in the URL to simulate. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    public final static String URL_SERVER_NAME_PARAM = "ServletTestCase_URL_Server";

    /**
     * Name of the parameter in the HTTP request that represents the context path
     * in the URL to simulate. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    public final static String URL_CONTEXT_PATH_PARAM = "ServletTestCase_URL_ContextPath";

    /**
     * Name of the parameter in the HTTP request that represents the Servlet
     * Path in the URL to simulate. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    public final static String URL_SERVLET_PATH_PARAM = "ServletTestCase_URL_ServletPath";

    /**
     * Name of the parameter in the HTTP request that represents the Path Info
     * in the URL to simulate. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    public final static String URL_PATH_INFO_PARAM = "ServletTestCase_URL_PathInfo";

    /**
     * Name of the parameter in the HTTP request that represents the Query String
     * in the URL to simulate. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    public final static String URL_QUERY_STRING_PARAM = "ServletTestCase_URL_QueryString";

    /**
     * The server name to simulate (including port number)
     */
    private String m_URL_ServerName;

    /**
     * The context path to simulate
     */
    private String m_URL_ContextPath;

    /**
     * The servlet path to simulate
     */
    private String m_URL_ServletPath;

    /**
     * The Path Info to simulate
     */
    private String m_URL_PathInfo;

    /**
     * The Query string
     */
    private String m_URL_QueryString;

    /**
     * The full URL (useful later because we can benefit from the all
     * methods of the <code>URL</code> class.
     */
    private URL m_FullURL;

    /**
     * Creates the URL to simulate.
     *
     * @param theServerName the server name (and port) in the URL to simulate,
     *                      i.e. this is the name that will be returned by the
     *                      <code>HttpServletRequest.getServerName()</code> and
     *                      <code>HttpServletRequest.getServerPort()</code>.
     * @param theContextPath the webapp context path in the URL to simulate,
     *                      i.e. this is the name that will be returned by the
     *                      <code>HttpServletRequest.getContextPath()</code>.
     *                      Can be null. Format: "/" + name or an empty string
     *                      for the default context.
     * @param theServletPath the servlet path in the URL to simulate,
     *                      i.e. this is the name that will be returned by the
     *                      <code>HttpServletRequest.getServletPath()</code>.
     *                      Can be null. Format : "/" + name.
     * @param thePathInfo   the path info in the URL to simulate, i.e. this is
     *                      the name that will be returned by the
     *                      <code>HttpServletRequest.getPathInfo()</code>. Can
     *                      be null. Format : "/" + name.
     * @param theQueryString the Query string in the URL to simulate, i.e. this
     *                       is the string that will be returned by the
     *                       <code>HttpServletResquest.getQueryString()</code>.
     *                       Can be null.
     */
    public ServletURL(String theServerName, String theContextPath, String theServletPath,
        String thePathInfo, String theQueryString)
    {
        if (theServerName == null) {
            throw new AssertionFailedError("Bad URL. The server name cannot be null");
        }

        m_URL_ServerName = theServerName;
        m_URL_ContextPath = (theContextPath  == null) ? "" : theContextPath;
        m_URL_ServletPath = theServletPath;
        m_URL_PathInfo = thePathInfo;
        m_URL_QueryString = theQueryString;

        // create a full URL
        String fullURL = "http://" + m_URL_ServerName;
        if (m_URL_ContextPath.length() != 0) {
            fullURL = fullURL + m_URL_ContextPath;
        }
        if ((m_URL_ServletPath != null) && (m_URL_ServletPath.length() != 0)) {
            fullURL = fullURL + m_URL_ServletPath;
        }
        if ((m_URL_PathInfo != null) && (m_URL_PathInfo.length() != 0)) {
            fullURL = fullURL + m_URL_PathInfo;
        }

        try {
            m_FullURL = new URL(fullURL);
        } catch (MalformedURLException e) {
            throw new AssertionFailedError("Bad URL [" + fullURL + "]");
        }

    }

    /**
     * @return the full URL as a <code>URL</code> object.
     */
    public URL getURL()
    {
        return m_FullURL;
    }

    /**
     * @return the simulated URL server name (including the port number)
     */
    public String getServerName()
    {
        return m_URL_ServerName;
    }

    /**
     * @return the simulated URL context path
     */
    public String getContextPath()
    {
        return m_URL_ContextPath;
    }

    /**
     * @return the simulated URL servlet path
     */
    public String getServletPath()
    {
        return m_URL_ServletPath;
    }

    /**
     * @return the simulated URL path info
     */
    public String getPathInfo()
    {
        return m_URL_PathInfo;
    }

    /**
     * @return the simulated Query String
     */
    public String getQueryString()
    {
        return m_URL_QueryString;
    }

    /**
     * Saves the current URL to a <code>ServletTestRequest</code> object.
     *
     * @param theRequest the object to which the current URL should be saved to
     */
    public void saveToRequest(ServletTestRequest theRequest)
    {
        if (m_URL_ServerName != null) {
            theRequest.addParameter(URL_SERVER_NAME_PARAM, getServerName());
        }
        if (m_URL_ContextPath != null) {
            theRequest.addParameter(URL_CONTEXT_PATH_PARAM, getContextPath());
        }
        if (m_URL_ServletPath != null) {
            theRequest.addParameter(URL_SERVLET_PATH_PARAM, getServletPath());
        }
        if (m_URL_PathInfo != null) {
            theRequest.addParameter(URL_PATH_INFO_PARAM, getPathInfo());
        }
        if (m_URL_QueryString != null) {
            theRequest.addParameter(URL_QUERY_STRING_PARAM, getQueryString());
        }
    }

    /**
     * Creates a <code>ServletURL</code> object by loading it's values from the
     * HTTP request.
     *
     * @param theRequest the incoming HTTP request.
     */
    public static ServletURL loadFromRequest(HttpServletRequest theRequest)
    {
        String serverName = theRequest.getParameter(URL_SERVER_NAME_PARAM);
        String contextPath = theRequest.getParameter(URL_CONTEXT_PATH_PARAM);
        String servletPath = theRequest.getParameter(URL_SERVLET_PATH_PARAM);
        String pathInfo = theRequest.getParameter(URL_PATH_INFO_PARAM);
        String queryString = theRequest.getParameter(URL_QUERY_STRING_PARAM);

        if (serverName != null) {
            return new ServletURL(serverName, contextPath, servletPath, pathInfo, queryString);
        }

        return null;
    }

}
