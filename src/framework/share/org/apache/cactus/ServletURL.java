/*
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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
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
package org.apache.cactus;

import javax.servlet.http.HttpServletRequest;

import org.apache.cactus.server.ServletUtil;
import org.apache.cactus.util.log.Log;
import org.apache.cactus.util.log.LogService;

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
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ServletURL
{
    /**
     * Name of the parameter in the HTTP request that represents the Server name
     * in the URL to simulate. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    public final static String URL_SERVER_NAME_PARAM =
        "Cactus_URL_Server";

    /**
     * Name of the parameter in the HTTP request that represents the context
     * path in the URL to simulate. The name is voluntarily long so that it
     * will not clash with a user-defined parameter.
     */
    public final static String URL_CONTEXT_PATH_PARAM =
        "Cactus_URL_ContextPath";

    /**
     * Name of the parameter in the HTTP request that represents the Servlet
     * Path in the URL to simulate. The name is voluntarily long so that it
     * will not clash with a user-defined parameter.
     */
    public final static String URL_SERVLET_PATH_PARAM =
        "Cactus_URL_ServletPath";

    /**
     * Name of the parameter in the HTTP request that represents the Path Info
     * in the URL to simulate. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    public final static String URL_PATH_INFO_PARAM =
        "Cactus_URL_PathInfo";

    /**
     * Name of the parameter in the HTTP request that represents the Query
     * String in the URL to simulate. The name is voluntarily long so that it
     * will not clash with a user-defined parameter.
     */
    public final static String URL_QUERY_STRING_PARAM =
        "Cactus_URL_QueryString";

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
     * The logger
     */
    private static final Log LOGGER =
        LogService.getInstance().getLog(ServletURL.class.getName());

    /**
     * Creates the URL to simulate.
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
        this.serverName = theServerName;
        this.contextPath = theContextPath;
        this.servletPath = theServletPath;
        this.pathInfo = thePathInfo;
        this.queryString = theQueryString;
    }

    /**
     * @return the simulated URL server name (including the port number)
     */
    public String getServerName()
    {
        return this.serverName;
    }

    /**
     * @return the simulated URL server name (excluding the port number)
     */
    public String getHost()
    {
        String host = this.serverName;

        if (host != null) {
            int pos = host.indexOf(":");
            if (pos > 0) {
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

        if (this.serverName != null) {

            int pos = this.serverName.indexOf(":");

            if (pos < 0) {
                return -1;
            }

            try {
                port = Integer.parseInt(this.serverName.substring(pos + 1));
            } catch (NumberFormatException e) {
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
     * @return the simulated URL servlet path
     */
    public String getServletPath()
    {
        return this.servletPath;
    }

    /**
     * @return the simulated URL path info
     */
    public String getPathInfo()
    {
        return this.pathInfo;
    }

    /**
     * @return the simulated Query String
     */
    public String getQueryString()
    {
        return this.queryString;
    }

    /**
     * @return the path (contextPath + servletPath + pathInfo) or null if
     *         not set
     */
    public String getPath()
    {
        String path;

        path = getContextPath() == null ? "" : getContextPath();
        path += getServletPath() == null ? "" : getServletPath();
        path += getPathInfo() == null ? "" : getPathInfo();

        if (path.length() == 0) {
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

        if (getServerName() != null) {
            theRequest.addParameter(URL_SERVER_NAME_PARAM, getServerName(),
                WebRequest.GET_METHOD);
        }
        if (getContextPath() != null) {
            theRequest.addParameter(URL_CONTEXT_PATH_PARAM, getContextPath(),
                WebRequest.GET_METHOD);
        }
        if (getServletPath() != null) {
            theRequest.addParameter(URL_SERVLET_PATH_PARAM, getServletPath(),
                WebRequest.GET_METHOD);
        }
        if (getPathInfo() != null) {
            theRequest.addParameter(URL_PATH_INFO_PARAM, getPathInfo(),
                WebRequest.GET_METHOD);
        }
        if (getQueryString() != null) {
            theRequest.addParameter(URL_QUERY_STRING_PARAM, getQueryString(),
                WebRequest.GET_METHOD);
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
        String qString = theRequest.getQueryString();

        String serverName = ServletUtil.getQueryStringParameter(qString,
            URL_SERVER_NAME_PARAM);
        String contextPath = ServletUtil.getQueryStringParameter(qString,
            URL_CONTEXT_PATH_PARAM);
        String servletPath = ServletUtil.getQueryStringParameter(qString,
            URL_SERVLET_PATH_PARAM);
        String pathInfo = ServletUtil.getQueryStringParameter(qString,
            URL_PATH_INFO_PARAM);
        String queryString = ServletUtil.getQueryStringParameter(qString,
            URL_QUERY_STRING_PARAM);

        ServletURL url = new ServletURL(serverName, contextPath,
            servletPath, pathInfo, queryString);

        LOGGER.debug("URL = [" + url + "]");

        return url;
    }

    /**
     * @return a string representation
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("host name = [" + getHost() + "], ");
        buffer.append("port = [" + getPort() + "], ");
        buffer.append("context path = [" + getContextPath() + "], ");
        buffer.append("servlet path = [" + getServletPath() + "], ");
        buffer.append("path info = [" + getPathInfo() + "], ");
        buffer.append("query string = [" + getQueryString() + "]");

        return buffer.toString();
    }

}
