/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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
package org.apache.cactus;

import javax.servlet.http.HttpServletRequest;

import org.apache.cactus.server.ServletUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simulate an HTTP URL by breaking it into its different parts.
 * <br><code><pre><b>
 * URL = "http://" + serverName (including port) + requestURI ? queryString<br>
 * requestURI = contextPath + servletPath + pathInfo
 * </b></pre></code>
 * From the Servlet 2.2 specification :<br>
 * <code><pre><ul><li><b>Context Path</b>: The path prefix associated with the
 *   ServletContext that this servlet is a part of. If this context is the
 *   default context rooted at the base of the web server's URL namespace, this
 *   path will be an empty string. Otherwise, this path starts with a "/"
 *   character but does not end with a "/" character.</li>
 * <li><b>Servlet Path</b>: The path section that directly corresponds to the
 *   mapping which activated this request. This path starts with a "/"
 *   character.</li>
 * <li><b>PathInfo</b>: The part of the request path that is not part of the
 *   Context Path or the Servlet Path.</li></ul></pre></code>
 * From the Servlet 2.3 specification :<br>
 * <code><pre><ul><li><b>Context Path</b>: The path prefix associated with the
 *   ServletContext that this servlet is a part of. If this context is the
 *   default context rooted at the base of the web server's URL namespace, this
 *   path will be an empty string. Otherwise, this path starts with a "/"
 *   character but does not end with a "/" character.</li>
 * <li><b>Servlet Path</b>: The path section that directly corresponds to the
 *   mapping which activated this request. This path starts with a "/"
 *   character <b>except in the case where the request is matched with the 
 *   "/*" pattern, in which case it is the empty string</b>.</li>
 * <li><b>PathInfo</b>: The part of the request path that is not part of the
 *   Context Path or the Servlet Path. <b>It is either null if there is no 
 *   extra path, or is a string with a leading "/"</b>.</li></ul></pre></code>
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
     * Default port of the HTTP protocol.
     */
    private static final int DEFAULT_PORT_HTTP = 80;

    /**
     * Default port of HTTP over SSL.
     */
    private static final int DEFAULT_PORT_HTTPS = 443;

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
     *                      Servlet Redirector will be used.
     *                      Format: "/" + name or an empty string for the 
     *                      default context. Must not end with a "/" character.
     * @param theServletPath the servlet path in the URL to simulate,
     *                      i.e. this is the name that will be returned by the
     *                      <code>HttpServletRequest.getServletPath()</code>.
     *                      Can be null. If null, then the servlet path from 
     *                      the Servlet Redirector will be used.
     *                      Format : "/" + name or an empty string.
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
     *                      Servlet Redirector will be used.
     *                      Format: "/" + name or an empty string for the 
     *                      default context. Must not end with a "/" character.
     * @param theServletPath the servlet path in the URL to simulate,
     *                      i.e. this is the name that will be returned by the
     *                      <code>HttpServletRequest.getServletPath()</code>.
     *                      Can be null. If null, then the servlet path from 
     *                      the Servlet Redirector will be used.
     *                      Format : "/" + name or an empty string.
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
     * Returns the host name.
     * 
     * <p>
     *   The host name is extracted from the specified server name (as in 
     *   <code><strong>jakarta.apache.org</strong>:80</code>). If the server
     *   name has not been set, this method will return <code>null</code>.
     * </p>
     * 
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
     * Returns the port.
     * 
     * <p>
     *   The port is extracted from the specified server name (as in 
     *   <code>jakarta.apache.org:<strong>80</strong></code>). If the server
     *   name doesn't contain a port number, the default port number is returned
     *   (80 for HTTP, 443 for HTTP over SSL). If a port number is specified but
     *   illegal, or the server name has not been set, this method will return
     *   -1.
     * </p>
     * 
     * @return the simulated port number or -1 if an illegal port has been
     *          specified
     */
    public int getPort()
    {
        int port = -1;

        if (getServerName() != null)
        {
            int pos = getServerName().indexOf(":");

            if (pos < 0)
            {
                // the server name doesn't contain a port specification, so use
                // the default port for the protocol
                port = getDefaultPort();
            }
            else
            {
                // parse the port encoded in the server name
                try
                {
                    port = Integer.parseInt(getServerName().substring(pos + 1));
                    if (port < 0)
                    {
                        port = -1;
                    }
                }
                catch (NumberFormatException e)
                {
                    port = -1;
                }
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
     * name or an empty string for the default context. If not an empty
     * string the last character must not be "/".
     *
     * @param theContextPath the context path to simulate
     */
    public void setContextPath(String theContextPath)
    {
        if ((theContextPath != null) && (theContextPath.length() > 0))
        {
            if (!theContextPath.startsWith("/"))
            {
                throw new IllegalArgumentException("The Context Path must"
                    + " start with a \"/\" character.");
            }
            if (theContextPath.endsWith("/"))
            {
                throw new IllegalArgumentException("The Context Path must not"
                    + " end with a \"/\" character.");                
            }
        }

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
     * If null then the servlet path from the Servlet Redirector will be
     * returned. Format : "/" + name or an empty string.
     *
     * @param theServletPath the servlet path to simulate
     */
    public void setServletPath(String theServletPath)
    {
        if ((theServletPath != null) && (theServletPath.length() > 0))
        {
            if (!theServletPath.startsWith("/"))
            {
                throw new IllegalArgumentException("The Servlet Path must"
                    + " start with a \"/\" character.");
            }            
        }

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
     * be returned by the <code>HttpServletRequest.getPathInfo()</code>. 
     * If null then no path info will be set (and the Path Info from the 
     * Servlet Redirector will <b>not</b> be used). 
     * Format : "/" + name.
     *
     * @param thePathInfo the path info to simulate
     */
    public void setPathInfo(String thePathInfo)
    {
        if ((thePathInfo != null) && (thePathInfo.length() == 0))
        { 
            throw new IllegalArgumentException("The Path Info must"
                + " not be an empty string. Use null if you don't"
                + " want to have a path info.");
        }
        else if (thePathInfo != null)
        {
            if (!thePathInfo.startsWith("/"))
            {
                throw new IllegalArgumentException("The Path Info must"
                    + " start with a \"/\" character.");
            }            
        }

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
        boolean isDefined = false;

        ServletURL url = new ServletURL();

        String protocol = ServletUtil.getQueryStringParameter(qString, 
            URL_PROTOCOL_PARAM);

        if (protocol != null)
        {
            isDefined = true;
            url.setProtocol(protocol);
        }

        String serverName = ServletUtil.getQueryStringParameter(qString, 
            URL_SERVER_NAME_PARAM);

        if (serverName != null)
        {
            isDefined = true;
            url.setServerName(serverName);
        }

        String contextPath = ServletUtil.getQueryStringParameter(qString, 
            URL_CONTEXT_PATH_PARAM);

        if (contextPath != null)
        {
            isDefined = true;
            url.setContextPath(contextPath);
        }

        String servletPath = ServletUtil.getQueryStringParameter(qString, 
            URL_SERVLET_PATH_PARAM);

        if (servletPath != null)
        {
            isDefined = true;
            url.setServletPath(servletPath);
        }

        String pathInfo = ServletUtil.getQueryStringParameter(qString, 
            URL_PATH_INFO_PARAM);

        if (pathInfo != null)
        {
            isDefined = true;
            url.setPathInfo(pathInfo);
        }

        String queryString = ServletUtil.getQueryStringParameter(qString, 
            URL_QUERY_STRING_PARAM);

        if (queryString != null)
        {
            isDefined = true;
            url.setQueryString(queryString);
        }

        if (!isDefined)
        {
            LOGGER.debug("Undefined simulation URL");
            url = null;
        }
        else
        {
            LOGGER.debug("Simulation URL = [" + url + "]");
        }

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

    /**
     * Returns the default port for the protocol.
     * 
     * @return the default port (80 for HTTP, 443 for HTTP over SSL)
     */
    private int getDefaultPort()
    {
        if (PROTOCOL_HTTPS.equals(getProtocol()))
        {
            return DEFAULT_PORT_HTTPS;
        }
        else
        {
            return DEFAULT_PORT_HTTP;
        }
    }

}
