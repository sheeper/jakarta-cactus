/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
package org.apache.cactus;

import javax.servlet.http.HttpServletRequest;

import org.apache.cactus.server.ServletUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simulate an HTTP URL by breaking it into its different parts :<br>
 * <code><pre><b>
 * URL = "http://" + serverName (including port) + requestURI ? queryString<br>
 * requestURI = contextPath + servletPath + pathInfo
 * </b></pre></code>
 * From the Servlet 2.2 specification :<br>
 * <code><pre><ul><li><b>Context Path</b>: The path prefix associated with the
 *   ServletContext that this servlet is a part of. If this context is the
 *   default context rooted at the base of the web server's URL namespace, this
 *   path will be an empty string. Otherwise, this path starts with a character
 *   but does not end with a character.</li>
 *   <li><b>Servlet Path</b>: The path section that directly corresponds to the
 *   mapping which activated this request. This path starts with a
 *   character.</li>
 *   <li><b>PathInfo</b>: The part of the request path that is not part of the
 *   Context Path or the Servlet Path.</li></ul></pre></code>
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ServletURL
{
    /**
     * Name of the parameter in the HTTP request that represents the protocol
     * (HTTP, HTTPS, etc) in the URL to simulate. The name is voluntarily long
     * so that it will not clash with a user-defined parameter.
     */
    public static final String URL_PROTOCOL_PARAM = "Cactus_URL_Protocol";

    /**
     * Name of the parameter in the HTTP request that represents the Server
     * name (+ port) in the URL to simulate. The name is voluntarily long so
     * that it will not clash with a user-defined parameter.
     */
    public static final String URL_SERVER_NAME_PARAM = "Cactus_URL_Server";

    /**
     * Name of the parameter in the HTTP request that represents the context
     * path in the URL to simulate. The name is voluntarily long so that it
     * will not clash with a user-defined parameter.
     */
    public static final String URL_CONTEXT_PATH_PARAM = 
        "Cactus_URL_ContextPath";

    /**
     * Name of the parameter in the HTTP request that represents the Servlet
     * Path in the URL to simulate. The name is voluntarily long so that it
     * will not clash with a user-defined parameter.
     */
    public static final String URL_SERVLET_PATH_PARAM = 
        "Cactus_URL_ServletPath";

    /**
     * Name of the parameter in the HTTP request that represents the Path Info
     * in the URL to simulate. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    public static final String URL_PATH_INFO_PARAM = "Cactus_URL_PathInfo";

    /**
     * Name of the parameter in the HTTP request that represents the Query
     * String in the URL to simulate. The name is voluntarily long so that it
     * will not clash with a user-defined parameter.
     */
    public static final String URL_QUERY_STRING_PARAM = 
        "Cactus_URL_QueryString";

    /**
     * Http protocol.
     */
    public static final String PROTOCOL_HTTP = "http";

    /**
     * Https protocol.
     */
    public static final String PROTOCOL_HTTPS = "https";

    /**
     * The logger
     */
    private static final Log LOGGER = LogFactory.getLog(ServletURL.class);

    /**
     * The server name to simulate (including port number)
     */
    private String serverName;

    /**
     * The context path to simulate
     */
    private String contextPath;

    /**
     * The servlet path to simulate
     */
    private String servletPath;

    /**
     * The Path Info to simulate
     */
    private String pathInfo;

    /**
     * The Query string
     */
    private String queryString;

    /**
     * The protocol to use. Default to HTTP.
     */
    private String protocol = PROTOCOL_HTTP;

    /**
     * Default constructor. Need to call the different setters to make this
     * a valid object.
     */
    public ServletURL()
    {
    }

    /**
     * Creates the URL to simulate.
     *
     * @param theProtocol   the protocol to simulate (either
     *                      <code>ServletURL.PROTOCOL_HTTP</code> or
     *                      <code>ServletURL.PROTOCOL_HTTPS</code>.
     * @param theServerName the server name (and port) in the URL to simulate,
     *                      i.e. this is the name that will be returned by the
     *                      <code>HttpServletRequest.getServerName()</code> and
     *                      <code>HttpServletRequest.getServerPort()</code>. Can
     *                      be null. If null, then the server name and port from
     *                      the Servlet Redirector will be returned.
     * @param theContextPath the webapp context path in the URL to simulate,
     *                      i.e. this is the name that will be returned by the
     *                      <code>HttpServletRequest.getContextPath()</code>.
     *                      Can be null. If null, then the context from the
     *                      Servlet Redirector will be returned.
     *                      Format: "/" + name or an empty string
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
    public ServletURL(String theProtocol, String theServerName, 
        String theContextPath, String theServletPath, String thePathInfo, 
        String theQueryString)
    {
        setProtocol(theProtocol);
        setServerName(theServerName);
        setContextPath(theContextPath);
        setServletPath(theServletPath);
        setPathInfo(thePathInfo);
        setQueryString(theQueryString);
    }

    /**
     * Creates the URL to simulate, using the default HTTP protocol.
     *
     * @param theServerName the server name (and port) in the URL to simulate,
     *                      i.e. this is the name that will be returned by the
     *                      <code>HttpServletRequest.getServerName()</code> and
     *                      <code>HttpServletRequest.getServerPort()</code>. Can
     *                      be null. If null, then the server name and port from
     *                      the Servlet Redirector will be returned.
     * @param theContextPath the webapp context path in the URL to simulate,
     *                      i.e. this is the name that will be returned by the
     *                      <code>HttpServletRequest.getContextPath()</code>.
     *                      Can be null. If null, then the context from the
     *                      Servlet Redirector will be returned.
     *                      Format: "/" + name or an empty string
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
    public ServletURL(String theServerName, String theContextPath, 
        String theServletPath, String thePathInfo, String theQueryString)
    {
        this(PROTOCOL_HTTP, theServerName, theContextPath, theServletPath, 
            thePathInfo, theQueryString);
    }

    /**
     * @return the protocol used to connect to the URL (HTTP, HTTPS, etc).
     */
    public String getProtocol()
    {
        return this.protocol;
    }

    /**
     * Sets the protocol to simulate (either
     * <code>ServletURL.PROTOCOL_HTTP</code> or
     * <code>ServletURL.PROTOCOL_HTTPS</code>. If parameter is null then
     * PROTOCOL_HTTP is assumed.
     *
     * @param theProtocol the protocol to simulate
     */
    public void setProtocol(String theProtocol)
    {
        // Only HTTP and HTTPS are currently supported.
        if ((!theProtocol.equals(PROTOCOL_HTTP))
            && (!theProtocol.equals(PROTOCOL_HTTPS)))
        {
            throw new RuntimeException("Invalid protocol [" + theProtocol
                + "]. Currently supported protocols are ["
                + PROTOCOL_HTTP + "] and ["
                + PROTOCOL_HTTPS + "].");
        }

        this.protocol = theProtocol;
    }

    /**
     * @return the simulated URL server name (including the port number)
     */
    public String getServerName()
    {
        return this.serverName;
    }

    /**
     * Sets the server name (and port) in the URL to simulate, ie this is the
     * name that will be returned by the
     * <code>HttpServletRequest.getServerName()</code> and
     * <code>HttpServletRequest.getServerPort()</code>. Does not need to be
     * set. If not set or null, then the server name and port from the Servlet
     * Redirector will be returned.
     *
     * @param theServerName the server name and port (ex:
     *        "jakarta.apache.org:80")
     */
    public void setServerName(String theServerName)
    {
        this.serverName = theServerName;
    }

    /**
     * @return the simulated URL server name (excluding the port number)
     */
    public String getHost()
    {
        String host = getServerName();

        if (host != null)
        {
            int pos = host.indexOf(":");

            if (pos > 0)
            {
                host = host.substring(0, pos);
            }
        }

        return host;
    }

    /**
     * @return the port number or -1 if none has been defined or it is a bad
     *         port
     */
    public int getPort()
    {
        int port = -1;

        if (getServerName() != null)
        {
            int pos = getServerName().indexOf(":");

            if (pos < 0)
            {
                return -1;
            }

            try
            {
                port = Integer.parseInt(getServerName().substring(pos + 1));
            }
            catch (NumberFormatException e)
            {
                port = -1;
            }
        }

        return port;
    }

    /**
     * @return the simulated URL context path
     */
    public String getContextPath()
    {
        return this.contextPath;
    }

    /**
     * Sets the webapp context path in the URL to simulate, ie this is the
     * name that will be returned by the
     * <code>HttpServletRequest.getContextPath()</code>. If not set, the
     * context from the Servlet Redirector will be returned. Format: "/" +
     * name or an empty string for the default context.
     *
     * @param theContextPath the context path to simulate
     */
    public void setContextPath(String theContextPath)
    {
        this.contextPath = theContextPath;
    }

    /**
     * @return the simulated URL servlet path
     */
    public String getServletPath()
    {
        return this.servletPath;
    }

    /**
     * Sets the servlet path in the URL to simulate, ie this is the name that
     * will be returned by the <code>HttpServletRequest.getServletPath()</code>.
     * If not set, the servlet path from the Servlet Redirector will be
     * returned. Format : "/" + name.
     *
     * @param theServletPath the servlet path to simulate
     */
    public void setServletPath(String theServletPath)
    {
        this.servletPath = theServletPath;
    }

    /**
     * @return the simulated URL path info
     */
    public String getPathInfo()
    {
        return this.pathInfo;
    }

    /**
     * Sets the path info in the URL to simulate, ie this is the name that will
     * be returned by the <code>HttpServletRequest.getPathInfo()</code>. If not
     * set, the path info from the Servlet Redirector will be returned.
     * Format : "/" + name.
     *
     * @param thePathInfo the path info to simulate
     */
    public void setPathInfo(String thePathInfo)
    {
        this.pathInfo = thePathInfo;
    }

    /**
     * @return the simulated Query String
     */
    public String getQueryString()
    {
        return this.queryString;
    }

    /**
     * Sets the Query string in the URL to simulate, ie this is the string that
     * will be returned by the
     * <code>HttpServletResquest.getQueryString()</code>. If not set, the
     * query string from the Servlet Redirector will be returned.
     *
     * @param theQueryString the query string to simulate
     */
    public void setQueryString(String theQueryString)
    {
        this.queryString = theQueryString;
    }

    /**
     * @return the path (contextPath + servletPath + pathInfo) or null if
     *         not set
     */
    public String getPath()
    {
        String path;

        path = (getContextPath() == null) ? "" : getContextPath();
        path += ((getServletPath() == null) ? "" : getServletPath());
        path += ((getPathInfo() == null) ? "" : getPathInfo());

        if (path.length() == 0)
        {
            path = null;
        }

        return path;
    }

    /**
     * Saves the current URL to a <code>WebRequest</code> object.
     *
     * @param theRequest the object to which the current URL should be saved to
     */
    public void saveToRequest(WebRequest theRequest)
    {
        // Note: All these pareameters are passed in the URL. This is to allow
        // the user to send whatever he wants in the request body. For example
        // a file, ...
        theRequest.addParameter(URL_PROTOCOL_PARAM, getProtocol(), 
            WebRequest.GET_METHOD);

        if (getServerName() != null)
        {
            theRequest.addParameter(URL_SERVER_NAME_PARAM, getServerName(), 
                WebRequest.GET_METHOD);
        }

        if (getContextPath() != null)
        {
            theRequest.addParameter(URL_CONTEXT_PATH_PARAM, getContextPath(), 
                WebRequest.GET_METHOD);
        }

        if (getServletPath() != null)
        {
            theRequest.addParameter(URL_SERVLET_PATH_PARAM, getServletPath(), 
                WebRequest.GET_METHOD);
        }

        if (getPathInfo() != null)
        {
            theRequest.addParameter(URL_PATH_INFO_PARAM, getPathInfo(), 
                WebRequest.GET_METHOD);
        }

        if (getQueryString() != null)
        {
            theRequest.addParameter(URL_QUERY_STRING_PARAM, getQueryString(), 
                WebRequest.GET_METHOD);
        }
    }

    /**
     * Creates a <code>ServletURL</code> object by loading it's values from the
     * HTTP request.
     *
     * @param theRequest the incoming HTTP request.
     * @return the <code>ServletURL</code> object unserialized from the HTTP
     *         request
     */
    public static ServletURL loadFromRequest(HttpServletRequest theRequest)
    {
        String qString = theRequest.getQueryString();

        ServletURL url = new ServletURL();

        String protocol = ServletUtil.getQueryStringParameter(qString, 
            URL_PROTOCOL_PARAM);

        if (protocol != null)
        {
            url.setProtocol(protocol);
        }

        String serverName = ServletUtil.getQueryStringParameter(qString, 
            URL_SERVER_NAME_PARAM);

        if (serverName != null)
        {
            url.setServerName(serverName);
        }

        String contextPath = ServletUtil.getQueryStringParameter(qString, 
            URL_CONTEXT_PATH_PARAM);

        if (contextPath != null)
        {
            url.setContextPath(contextPath);
        }

        String servletPath = ServletUtil.getQueryStringParameter(qString, 
            URL_SERVLET_PATH_PARAM);

        if (servletPath != null)
        {
            url.setServletPath(servletPath);
        }

        String pathInfo = ServletUtil.getQueryStringParameter(qString, 
            URL_PATH_INFO_PARAM);

        if (pathInfo != null)
        {
            url.setPathInfo(pathInfo);
        }

        String queryString = ServletUtil.getQueryStringParameter(qString, 
            URL_QUERY_STRING_PARAM);

        if (queryString != null)
        {
            url.setQueryString(queryString);
        }

        LOGGER.debug("URL = [" + url + "]");

        return url;
    }

    /**
     * @return a string representation
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("protocol = [" + getProtocol() + "], ");
        buffer.append("host name = [" + getHost() + "], ");
        buffer.append("port = [" + getPort() + "], ");
        buffer.append("context path = [" + getContextPath() + "], ");
        buffer.append("servlet path = [" + getServletPath() + "], ");
        buffer.append("path info = [" + getPathInfo() + "], ");
        buffer.append("query string = [" + getQueryString() + "]");

        return buffer.toString();
    }
}