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
package org.apache.commons.cactus;

import java.util.*;

/**
 * Contains all HTTP request data for a test case. It is the data that
 * will be sent to the server redirector and that will be available to the test
 * methods through the <code>HttpServletRequest</code> object.
 * <br><br>
 * Namely, it is :
 * <ul>
 *   <li>Request parameters that the test case can retrieve using
 *       <code>HttpServletRequest.getParameters()</code>,</li>
 *   <li>Cookies that the test case can retrieve using
 *       <code>HttpServletRequest.getCookies()</code>,</li>
 *   <li>HTTP headers that the test case can retrieve using the
 *       <code>HttpServletRequest.getHeader(), getHeaders(),
 *       ...</code> APIs,</li>
 *   <li>URL data the the test case can retrieve using
 *       <code>HttpServletRequest.getRequestURI(), ...</code></li>
 *   <li>Whether you want the server redirector to automatically create a
 *       session for you or not,</li>
 *   <li>Whether you want the HTTP connection to the server redirector to
 *       use a POST or GET method. Default is POST</li>
 * </ul>
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class WebRequest
{
    /**
     * The request parameters.
     */
    private Hashtable parameters = new Hashtable();

    /**
     * GET Method identifier.
     */
    public static final String GET_METHOD = "GET";

    /**
     * POST Method identifier.
     */
    public static final String POST_METHOD = "POST";

    /**
     * The Cookies
     */
    private Hashtable cookies = new Hashtable();

    /**
     * HTTP Headers.
     */
    private Hashtable headers = new Hashtable();

    /**
     * The URL to simulate
     */
    private ServletURL url;

    /**
     * Automatic session creation flag (default is true).
     */
    private boolean isAutomaticSession = true;

    /**
     * The chosen method for posting data (GET or POST)
     */
    private String method = POST_METHOD;

    /**
     * @param theMethod the method to use to post data (GET or POST)
     */
    public void setMethod(String theMethod)
    {
        if (theMethod.equalsIgnoreCase(GET_METHOD)) {
            this.method = GET_METHOD;
        } else if (theMethod.equalsIgnoreCase(POST_METHOD)) {
            this.method = POST_METHOD;
        }
    }

    /**
     * @return the method to use for posting data to the server redirector.
     */
    public String getMethod()
    {
        return this.method;
    }

    /**
     * @param isAutomaticSession whether the redirector servlet will
     *        automatically create the HTTP session or not. Default is true.
     */
    public void setAutomaticSession(boolean isAutomaticSession)
    {
        this.isAutomaticSession = isAutomaticSession;
    }

    /**
     * @return true if session will be automatically created for the user or
     *         false otherwise.
     */
    public boolean getAutomaticSession()
    {
        return this.isAutomaticSession;
    }

    /**
     * Sets the simulated URL. A URL is of the form :<br>
     * <code><pre><b>
     * URL        = "http://" + serverName (including port) + requestURI ? queryString<br>
     * requestURI = contextPath + servletPath + pathInfo
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
    public void setURL(String theServerName, String theContextPath,
        String theServletPath, String thePathInfo, String theQueryString)
    {
        this.url = new ServletURL(theServerName, theContextPath,
            theServletPath, thePathInfo, theQueryString);

        // Now automatically add all HTTP parameters to the list of passed
        // parameters
        addQueryStringParameters(theQueryString);
    }

    /**
     * @return the simulated URL
     */
    public ServletURL getURL()
    {
        return this.url;
    }

    /**
     * Adds a parameter to the request. It is possible to add several times the
     * the same parameter name (the same as for the
     * <code>HttpServletRequest</code>).
     *
     * @param theName  the parameter's name
     * @param theValue the parameter's value
     */
    public void addParameter(String theName, String theValue)
    {
        // If there is already a parameter of the same name, add the
        // new value to the Vector. If not, create a Vector an add it to the
        // hashtable

        if (this.parameters.containsKey(theName)) {
            Vector v = (Vector)this.parameters.get(theName);
            v.addElement(theValue);
        } else {
            Vector v = new Vector();
            v.addElement(theValue);
            this.parameters.put(theName, v);
        }
    }

    /**
     * @return the parameter names
     */
    public Enumeration getParameterNames()
    {
        return this.parameters.keys();
    }

    /**
     * Returns the first value corresponding to this parameter's name.
     *
     * @param  theName the parameter's name
     * @return the first value corresponding to this parameter's name or null
     *         if not found
     */
    public String getParameter(String theName)
    {
        String[] values = getParameterValues(theName);

        if (values != null) {
            return values[0];
        }

        return null;
    }

    /**
     * Returns all the values associated with this parameter's name.
     *
     * @param  theName the parameter's name
     * @return the values corresponding to this parameter's name or null if not
     *         found
     */
    public String[] getParameterValues(String theName)
    {
        if (this.parameters.containsKey(theName)) {

            Vector v = (Vector)this.parameters.get(theName);

            Object[] objs = new Object[v.size()];
            v.copyInto(objs);

            String[] result = new String[objs.length];
            for (int i = 0; i < objs.length; i++) {
                result[i] = (String)objs[i];
            }

            return result;
        }

        return null;
    }

    /**
     * Adds a cookie to the request.
     *
     * @param theName  the cookie's name
     * @param theValue the cookie's value
     */
    public void addCookie(String theName, String theValue)
    {
        this.cookies.put(theName, theValue);
    }

    /**
     * @return the cookie names
     */
    public Enumeration getCookieNames()
    {
        return this.cookies.keys();
    }

    /**
     * @param  theName the cookie's name
     * @return the value corresponding to this cookie's name or null if not
     *         found
     */
    public String getCookieValue(String theName)
    {
        return (String)this.cookies.get(theName);
    }

    /**
     * Adds a header to the request. Supports adding several values for the
     * same header name.
     *
     * @param theName  the header's name
     * @param theValue the header's value
     */
    public void addHeader(String theName, String theValue)
    {
        // If there is already a header of the same name, add the
        // new header to the Vector. If not, create a Vector an add it to the
        // hashtable

        if (this.headers.containsKey(theName)) {
            Vector v = (Vector)this.headers.get(theName);
            v.addElement(theValue);
        } else {
            Vector v = new Vector();
            v.addElement(theValue);
            this.headers.put(theName, v);
        }
    }

    /**
     * @return the header names
     */
    public Enumeration getHeaderNames()
    {
        return this.headers.keys();
    }

    /**
     * Returns the first value corresponding to this header's name.
     *
     * @param  theName the header's name
     * @return the first value corresponding to this header's name or null if
     *         not found
     */
    public String getHeader(String theName)
    {
        String[] values = getHeaderValues(theName);

        if (values != null) {
            return values[0];
        }

        return null;
    }

    /**
     * Returns all the values associated with this header's name.
     *
     * @param  theName the header's name
     * @return the values corresponding to this header's name or null if not
     *         found
     */
    public String[] getHeaderValues(String theName)
    {
        if (this.headers.containsKey(theName)) {

            Vector v = (Vector)this.headers.get(theName);

            Object[] objs = new Object[v.size()];
            v.copyInto(objs);

            String[] result = new String[objs.length];
            for (int i = 0; i < objs.length; i++) {
                result[i] = (String)objs[i];
            }

            return result;
        }

        return null;
    }

    /**
     * Extract the HTTP parameters that might have been specified on the
     * query string and add them to the list of parameters to pass to the
     * servlet redirector.
     *
     * @param theQueryString the Query string in the URL to simulate, i.e. this
     *                       is the string that will be returned by the
     *                       <code>HttpServletResquest.getQueryString()</code>.
     *                       Can be null.
     */
    private void addQueryStringParameters(String theQueryString)
    {
        if (theQueryString == null) {
            return;
        }

        String nameValue = null;
        StringTokenizer tokenizer = new StringTokenizer(theQueryString, "&");
        int breakParam = -1;
        while (tokenizer.hasMoreTokens()) {
            nameValue = tokenizer.nextToken();
            breakParam = nameValue.indexOf("=");
            if (breakParam != -1) {
                addParameter(nameValue.substring(0, breakParam),
                    nameValue.substring(breakParam+1));
            } else {
                throw new RuntimeException("Bad QueryString [" + 
                    theQueryString + "] NameValue pair: [" + nameValue + "]");
            }
        }
    }

    /**
     * @return a string representation of the request
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("simulation URL = [" + getURL() + "], ");
        buffer.append("automatic session = [" + getAutomaticSession() + "], ");

        // Append cookies
        buffer.append("cookies = [");
        Enumeration cookies = getCookieNames();
        while (cookies.hasMoreElements()) {
            buffer.append("[");
            String cookieName = (String)cookies.nextElement();
            String cookieValue = getCookieValue(cookieName);
            buffer.append("[" + cookieName + "] = [" + cookieValue + "]");
            buffer.append("]");
        }
        buffer.append("], ");

        // Append headers
        buffer.append("headers = [");
        Enumeration headers = getHeaderNames();
        while (headers.hasMoreElements()) {
            buffer.append("[");
            String headerName = (String)headers.nextElement();
            String[] headerValues = getHeaderValues(headerName);
            buffer.append("[" + headerName + "] = [");
            for (int i = 0; i < headerValues.length - 1; i++) {
                buffer.append("[" + headerValues[i] + "], ");
            }
            buffer.append("[" + headerValues[headerValues.length - 1] + "]]");
            buffer.append("]");
        }
        buffer.append("], ");

        buffer.append("method = [" + getMethod() + "], ");


        // Append parameters
        buffer.append("parameters = [");
        Enumeration parameters = getParameterNames();
        while (parameters.hasMoreElements()) {
            buffer.append("[");
            String parameterName = (String)parameters.nextElement();
            String[] parameterValues = getParameterValues(parameterName);
            buffer.append("[" + parameterName + "] = [");
            for (int i = 0; i < parameterValues.length - 1; i++) {
                buffer.append("[" + parameterValues[i] + "], ");
            }
            buffer.append("[" + parameterValues[parameterValues.length - 1] +
                "]]");
            buffer.append("]");
        }
        buffer.append("]");

        return buffer.toString();
    }

}
