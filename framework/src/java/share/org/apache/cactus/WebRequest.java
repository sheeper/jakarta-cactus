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

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.cactus.client.authentication.Authentication;

/**
 * Contains HTTP request data for a Cactus test case.
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public interface WebRequest extends Request
{
    /**
     * GET Method identifier.
     */
    String GET_METHOD = "GET";

    /**
     * POST Method identifier.
     */
    String POST_METHOD = "POST";

    /**
     * Sets the content type that will be set in the http request
     *
     * @param theContentType the content type
     */
    void setContentType(String theContentType);

    /**
     * @return the content type that will be set in the http request
     */
    String getContentType();

    /**
     * Allow the user to send arbitrary data in the request body
     *
     * @param theDataStream the stream on which the data are put by the user
     */
    void setUserData(InputStream theDataStream);

    /**
     * @return the data stream set up by the user
     */
    InputStream getUserData();

    /**
     * Adds a parameter to the request. It is possible to add several times the
     * the same parameter name, but with different value (the same as for the
     * <code>HttpServletRequest</code>).
     *
     * @param theName the parameter's name
     * @param theValue the parameter's value
     * @param theMethod GET_METHOD or POST_METHOD. If GET_METHOD then the
     *        parameter will be sent in the query string of the URL. If
     *        POST_METHOD, it will be sent as a parameter in the request body.
     */
    void addParameter(String theName, String theValue, String theMethod);

    /**
     * Adds a parameter to the request. The parameter is added to the query
     * string of the URL.
     *
     * @param theName  the parameter's name
     * @param theValue the parameter's value
     *
     * @see #addParameter(String, String, String)
     */
    void addParameter(String theName, String theValue);

    /**
     * @return the parameter names that will be passed in the request body
     *         (POST)
     */
    Enumeration getParameterNamesPost();

    /**
     * @return the parameter names that will be passed in the URL (GET)
     */
    Enumeration getParameterNamesGet();

    /**
     * Returns the first value corresponding to this parameter's name (provided
     * this parameter is passed in the URL).
     *
     * @param theName the parameter's name
     * @return the first value corresponding to this parameter's name or null
     *         if not found in the list of parameters to be sent in the URL
     */
    String getParameterGet(String theName);

    /**
     * Returns the first value corresponding to this parameter's name (provided
     * this parameter is passed in the request body - POST).
     *
     * @param theName the parameter's name
     * @return the first value corresponding to this parameter's name or null
     *         if not found in the list of parameters to be sent in the request
     *         body
     */
    String getParameterPost(String theName);

    /**
     * Returns all the values corresponding to this parameter's name (provided
     * this parameter is passed in the URL).
     *
     * @param theName the parameter's name
     * @return the first value corresponding to this parameter's name or null
     *         if not found in the list of parameters to be sent in the URL
     */
    String[] getParameterValuesGet(String theName);

    /**
     * Returns all the values corresponding to this parameter's name (provided
     * this parameter is passed in the request body - POST).
     *
     * @param theName the parameter's name
     * @return the first value corresponding to this parameter's name or null
     *         if not found in the list of parameters to be sent in the request
     *         body
     */
    String[] getParameterValuesPost(String theName);

    /**
     * Adds a cookie to the request. The cookie will be created with a
     * default localhost domain. If you need to specify a domain for the cookie,
     * use the {@link #addCookie(String, String, String)} method or the method
     * {@link #addCookie(Cookie)}.
     *
     * @param theName the cookie's name
     * @param theValue the cookie's value
     */
    void addCookie(String theName, String theValue);

    /**
     * Adds a cookie to the request. The cookie will be created with the
     * domain passed as parameter (i.e. the cookie will get sent only to
     * requests to that domain).
     *
     * Note that the domain must match either the redirector host
     * (specified in <code>cactus.properties</code>) or the host set
     * using <code>setURL()</code>.
     *
     * @param theDomain the cookie domain
     * @param theName the cookie name
     * @param theValue the cookie value
     */
    void addCookie(String theDomain, String theName, String theValue);

    /**
     * Adds a cookie to the request.
     *
     * Note that the domain must match either the redirector host
     * (specified in <code>cactus.properties</code>) or the host set
     * using <code>setURL()</code>.
     *
     * @param theCookie the cookie to add
     */
    void addCookie(Cookie theCookie);

    /**
     * @return the cookies (vector of <code>Cookie</code> objects)
     */
    Vector getCookies();

    /**
     * Adds a header to the request. Supports adding several values for the
     * same header name.
     *
     * @param theName  the header's name
     * @param theValue the header's value
     */
    void addHeader(String theName, String theValue);

    /**
     * @return the header names
     */
    Enumeration getHeaderNames();

    /**
     * Returns the first value corresponding to this header's name.
     *
     * @param  theName the header's name
     * @return the first value corresponding to this header's name or null if
     *         not found
     */
    String getHeader(String theName);

    /**
     * Returns all the values associated with this header's name.
     *
     * @param  theName the header's name
     * @return the values corresponding to this header's name or null if not
     *         found
     */
    String[] getHeaderValues(String theName);

    /**
     * Sets the authentication object that will configure the http request
     *
     * @param theAuthentication the authentication object
     */
    void setAuthentication(Authentication theAuthentication);

    /**
     * @return the authentication that will configure the http request
     */
    Authentication getAuthentication();

    /**
     * Override the redirector Name defined in <code>cactus.properties</code>.
     * This is useful to define a per test case Name (for example, if some
     * test case need to have authentication turned on and not other tests,
     * etc).
     *
     * @param theRedirectorName the new redirector Name to use
     */
    void setRedirectorName(String theRedirectorName);

    /**
     * @return the overriden redirector Name or null if none has been defined
     */
    String getRedirectorName();

    /**
     * @param isAutomaticSession whether the redirector servlet will
     *        automatically create the HTTP session or not. Default is true.
     */
    void setAutomaticSession(boolean isAutomaticSession);

    /**
     * @return true if session will be automatically created for the user or
     *         false otherwise.
     */
    boolean getAutomaticSession();

    /**
     * Sets the simulated URL. A URL is of the form :<br>
     * <code><pre><b>
     * URL = "http://" + serverName (including port) + requestURI ? queryString
     * <br>requestURI = contextPath + servletPath + pathInfo
     * </b></pre></code>
     * From the Servlet 2.2 specification :<br>
     * <code><pre><ul>
     * <li><b>Context Path</b>: The path prefix associated with the
     *   ServletContext that this servlet is a part of. If this context is the
     *   default context rooted at the base of the web server's URL namespace,
     *   this path will be an empty string. Otherwise, this path starts with a
     *   character but does not end with a character.</li>
     * <li><b>Servlet Path</b>: The path section that directly corresponds to
     *   the mapping which activated this request. This path starts with a
     *   character.</li>
     * <li><b>PathInfo</b>: The part of the request path that is not part of the
     *   Context Path or the Servlet Path.</li>
     * </ul></pre></code>
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
    void setURL(String theServerName, String theContextPath, 
        String theServletPath, String thePathInfo, String theQueryString);

    /**
     * @return the simulated URL
     */
    ServletURL getURL();

    /**
     * Gets an HTTP session id by calling the server side and retrieving
     * the jsessionid cookie in the HTTP response. This is achieved by
     * calling the Cactus redirector used by the current test case.
     * 
     * @return the HTTP session id as a <code>HttpSessionCookie</code> object
     */
    HttpSessionCookie getSessionCookie();
}
