/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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

import java.net.HttpURLConnection;
import java.util.StringTokenizer;

import org.apache.cactus.client.ClientException;
import org.apache.cactus.client.WebResponseObjectFactory;
import org.apache.cactus.client.connector.http.ConnectionHelper;
import org.apache.cactus.client.connector.http.ConnectionHelperFactory;
import org.apache.cactus.configuration.WebConfiguration;
import org.apache.cactus.util.ChainedRuntimeException;

/**
 * Extends {@link BaseWebRequest} to add properties specific to the
 * Cactus Web Redirectors.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:Jason.Robertson@acs-inc.com">Jason Robertson</a>
 *
 * @version $Id$
 */
public class WebRequest extends BaseWebRequest
{
    /**
     * The unique identifier for this test case.
     */
    private String uniqueId;
    
    /**
     * The URL to simulate
     */
    private ServletURL url;

    /**
     * Automatic session creation flag (default is true).
     */
    private boolean isAutomaticSession = true;

    /**
     * Redirector Name. This is to let the user the possibility to override
     * the default Redirector Name specified in <code>cactus.properties</code>.
     */
    private String redirectorName;

    /**
     * Default constructor that requires that 
     * {@link #setConfiguration(Configuration)} be called before the methods
     * requiring a configuration object.
     * 
     */
    public WebRequest()
    {
    }

    /**
     * @param theConfiguration the Cactus configuration
     */
    public WebRequest(WebConfiguration theConfiguration)
    {
        super(theConfiguration);
    }

    /**
     * Override the redirector Name defined in <code>cactus.properties</code>.
     * This is useful to define a per test case Name (for example, if some
     * test case need to have authentication turned on and not other tests,
     * etc).
     *
     * @param theRedirectorName the new redirector Name to use
     */
    public void setRedirectorName(String theRedirectorName)
    {
        this.redirectorName = theRedirectorName;
    }

    /**
     * @return the overriden redirector Name or null if none has been defined
     */
    public String getRedirectorName()
    {
        return this.redirectorName;
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
     * @return a string representation of the request
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("simulation URL = [" + getURL() + "], ");
        buffer.append("automatic session = [" + getAutomaticSession() + "], ");

        buffer.append(super.toString());
        
        return buffer.toString();
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
        if (theQueryString == null)
        {
            return;
        }

        String nameValue = null;
        StringTokenizer tokenizer = new StringTokenizer(theQueryString, "&");
        int breakParam = -1;

        while (tokenizer.hasMoreTokens())
        {
            nameValue = tokenizer.nextToken();
            breakParam = nameValue.indexOf("=");

            if (breakParam != -1)
            {
                addParameter(nameValue.substring(0, breakParam), 
                    nameValue.substring(breakParam + 1));
            }
            else
            {
                throw new RuntimeException("Bad QueryString [" + theQueryString
                    + "] NameValue pair: [" + nameValue + "]");
            }
        }
    }
    
    /**
     * Gets an HTTP session id by calling the server side and retrieving
     * the jsessionid cookie in the HTTP response. This is achieved by
     * calling the Cactus redirector used by the current test case.
     * 
     * @return the HTTP session id as a <code>HttpSessionCookie</code> object
     */
    public HttpSessionCookie getSessionCookie()
    {
        if (getConfiguration() == null)
        {
            throw new ChainedRuntimeException("setConfiguration() should have "
                + "been called prior to calling getSessionCookie()");
        }
        
        ConnectionHelper helper = ConnectionHelperFactory.getConnectionHelper(
            ((WebConfiguration) getConfiguration()).getRedirectorURL(this), 
            getConfiguration());

        WebRequest request = new WebRequest(
            (WebConfiguration) getConfiguration());
        addCactusCommand(HttpServiceDefinition.SERVICE_NAME_PARAM, 
            ServiceEnumeration.CREATE_SESSION_SERVICE.toString());

        HttpURLConnection resultConnection;
        try
        {
            resultConnection = helper.connect(request, getConfiguration());
        }
        catch (Throwable e)
        {
            throw new ChainedRuntimeException("Failed to connect to ["
                + ((WebConfiguration) getConfiguration()).getRedirectorURL(this)
                + "]", e);
        }

        WebResponse response;
        try
        {
            response = (WebResponse) new WebResponseObjectFactory().
                getResponseObject(WebResponse.class.getName(), request, 
                resultConnection);
        }
        catch (ClientException e)
        {
            throw new ChainedRuntimeException("Failed to connect to ["
                + ((WebConfiguration) getConfiguration()).getRedirectorURL(this)
                + "]", e);
        }

        Cookie cookie = response.getCookieIgnoreCase("jsessionid");

        // FIXME: Add a constructor to the Cookie class that takes a Cookie
        // as parameter.

        HttpSessionCookie sessionCookie = null;

        if (cookie != null)                
        {
            sessionCookie = new HttpSessionCookie(cookie.getDomain(), 
                cookie.getName(), cookie.getValue());
            sessionCookie.setComment(cookie.getComment());
            sessionCookie.setExpiryDate(cookie.getExpiryDate());
            sessionCookie.setPath(cookie.getPath());
            sessionCookie.setSecure(cookie.isSecure());
        }
                
        return sessionCookie;
    }

    /**
     * Adds a cactus-specific command to the URL
     * The URL is used to allow the user to send whatever he wants
     * in the request body. For example a file, ...
     * 
     * @param theCommandName The name of the command to add--must start with 
     *                       "Cactus_"
     * @param theCommandValue Value of the command
     */
    public void addCactusCommand(String theCommandName, String theCommandValue)
    {
        if (!theCommandName.startsWith(HttpServiceDefinition.COMMAND_PREFIX))
        {
            throw new IllegalArgumentException("Cactus commands must begin"
                + " with" + HttpServiceDefinition.COMMAND_PREFIX + ". The"
                + " offending command was [" + theCommandName + "]");
        }
        addParameter(theCommandName, theCommandValue, GET_METHOD);
    }

    /**
     * Sets the unique id of the test case. Also adds
     * a cactus command consisting of the id
     * to the actual HTTP request.
     * @param theUniqueId new uniqueId for the test case associated
     *        with this request
     */
    public void setUniqueId(String theUniqueId)
    {
        if (this.uniqueId != null)
        {
            throw new IllegalStateException("uniqueId already set!");
        }
        this.uniqueId = theUniqueId;
        addCactusCommand(HttpServiceDefinition.TEST_ID, theUniqueId);
    }

    /**
     * @return Gets the unique id of the test case
     */
    public String getUniqueId()
    {
        return uniqueId;
    }
}
