/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.cactus.internal;

import java.net.HttpURLConnection;
import java.util.StringTokenizer;

import org.apache.cactus.Cookie;
import org.apache.cactus.HttpSessionCookie;
import org.apache.cactus.ServletURL;
import org.apache.cactus.WebResponse;
import org.apache.cactus.internal.client.ClientException;
import org.apache.cactus.internal.client.WebResponseObjectFactory;
import org.apache.cactus.internal.client.connector.http.HttpClientConnectionHelper;
import org.apache.cactus.internal.configuration.WebConfiguration;
import org.apache.cactus.util.ChainedRuntimeException;

/**
 * Extends {@link BaseWebRequest} to add properties specific to the
 * Cactus Web Redirectors.
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
        
        HttpClientConnectionHelper helper = 
            new HttpClientConnectionHelper(
                ((WebConfiguration) getConfiguration()).getRedirectorURL(this));

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
