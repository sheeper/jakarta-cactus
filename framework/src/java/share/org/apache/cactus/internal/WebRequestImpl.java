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
package org.apache.cactus.internal;

import java.net.HttpURLConnection;
import java.util.StringTokenizer;

import org.apache.cactus.Cookie;
import org.apache.cactus.HttpSessionCookie;
import org.apache.cactus.RequestDirectives;
import org.apache.cactus.ServiceEnumeration;
import org.apache.cactus.ServletURL;
import org.apache.cactus.WebResponse;
import org.apache.cactus.client.WebResponseObjectFactory;
import org.apache.cactus.client.connector.http.ConnectionHelper;
import org.apache.cactus.client.connector.http.ConnectionHelperFactory;
import org.apache.cactus.configuration.WebConfiguration;
import org.apache.cactus.internal.client.ClientException;
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
public class WebRequestImpl extends BaseWebRequest
{
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
    public WebRequestImpl()
    {
    }

    /**
     * @param theConfiguration the Cactus configuration
     */
    public WebRequestImpl(WebConfiguration theConfiguration)
    {
        super(theConfiguration);
    }

    /**
     * @see org.apache.cactus.WebRequest#setRedirectorName(String)
     */
    public void setRedirectorName(String theRedirectorName)
    {
        this.redirectorName = theRedirectorName;
    }

    /**
     * @see org.apache.cactus.WebRequest#getRedirectorName()
     */
    public String getRedirectorName()
    {
        return this.redirectorName;
    }

    /**
     * @see org.apache.cactus.WebRequest#setAutomaticSession(boolean)
     */
    public void setAutomaticSession(boolean isAutomaticSession)
    {
        this.isAutomaticSession = isAutomaticSession;
    }

    /**
     * @see org.apache.cactus.WebRequest#getAutomaticSession()
     */
    public boolean getAutomaticSession()
    {
        return this.isAutomaticSession;
    }

    /**
     * @see org.apache.cactus.WebRequest#setURL(String, String, String, String, String)
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
     * @see org.apache.cactus.WebRequest#getURL()
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
     * @see org.apache.cactus.WebRequest#getSessionCookie()
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

        WebRequestImpl obtainSessionIdRequest = new WebRequestImpl(
            (WebConfiguration) getConfiguration());
            
        
        //Not sure whether I should be adding the service parameter to
        //this request (this) or to the obtainSessionIdRequest
        //seems obvious that it should be the obtainSessionIdRequest
        RequestDirectives directives = 
            new RequestDirectives(obtainSessionIdRequest);
        directives.setService(ServiceEnumeration.CREATE_SESSION_SERVICE);

        HttpURLConnection resultConnection;
        try
        {
            resultConnection =
                helper.connect(obtainSessionIdRequest, getConfiguration());
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
            response = (WebResponse) new WebResponseObjectFactory(
                resultConnection).getResponseObject(
                    WebResponse.class.getName(),
                    obtainSessionIdRequest);
        }
        catch (ClientException e)
        {
            throw new ChainedRuntimeException("Failed to connect to ["
                + ((WebConfiguration) getConfiguration()).getRedirectorURL(this)
                + "]", e);
        }

        Cookie cookie = response.getCookieIgnoreCase("jsessionid");

        // TODO: Add a constructor to the Cookie class that takes a Cookie
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
}
