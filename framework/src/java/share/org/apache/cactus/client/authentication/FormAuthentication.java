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
package org.apache.cactus.client.authentication;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.cactus.WebRequest;
import org.apache.cactus.client.connector.http.ConnectionHelper;
import org.apache.cactus.client.connector.http.ConnectionHelperFactory;
import org.apache.cactus.configuration.Configuration;
import org.apache.cactus.configuration.WebConfiguration;
import org.apache.cactus.util.ChainedRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Form-based authentication implementation. An instance of this class
 * can be reused across several tests as it caches the session cookie.
 * Thus the first time it is used to authenticate the user, it calls
 * the security URL (which is by default the context URL prepended by
 * "j_security_check"), caches the returned session cookie and adds the
 * cookie for the next request. The second time it is called, it simply
 * addes the session cookie for the next request.
 * 
 * @author <a href="mailto:Jason.Robertson@acs-inc.com">Jason Robertson</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @since 1.5
 *
 * @version $Id: $
 */
public class FormAuthentication extends AbstractAuthentication
{
    /**
     * The logger.
     */
    private static final Log LOGGER = 
        LogFactory.getLog(FormAuthentication.class);

    /**
     * The URL to use when attempting to log in, if for whatever reason 
     * the default URL is incorrect.
     */
    private URL securityCheckURL = null;

    /**
     * We store the session cookie name because of case issues. We need
     * to be able to send exactly the same one as was sent back by the
     * server.
     */
    private String sessionIdCookieName = null;

    /**
     * We store the session id cookie so that this instance can
     * be reused for another test.
     */
    private String sessionId = null;

    /**
     * {@link WebRequest} object that will be used to connect to the
     * security URL. 
     */
    private WebRequest securityRequest = new WebRequest();
      
    /**
     * @param theName user name of the Credential
     * @param thePassword user password of the Credential
     */
    public FormAuthentication(String theName, String thePassword)
    {
        super(theName, thePassword);
    }
    
    /**
     * @see AbstractAuthentication#validateName(String)
     */
    protected void validateName(String theName)
    {
        // Nothing to do here...
    }
    
    /**
     * @see AbstractAuthentication#validatePassword(String)
     */
    protected void validatePassword(String thePassword)
    {
        // Nothing to do here...
    }

    /**
     * @see AbstractAuthentication#configure(WebRequest, Configuration)
     */
    public void configure(WebRequest theRequest,
        Configuration theConfiguration)
    {
        // Only authenticate the first time this instance is used.
        if (this.sessionId == null)
        {
           authenticate(theRequest, theConfiguration);
        }

        // Sets the session id cookie for the next request.
        if (this.sessionId != null)
        {
            theRequest.addCookie(this.sessionIdCookieName, this.sessionId);
        }
    }

    /**
     * @return the {@link WebRequest} that will be used to connect to the
     * security URL. It can be used to add additional HTTP parameters such
     * as proprietary ones required by some containers.
     */
    public WebRequest getSecurityRequest()
    {
        return this.securityRequest;
    }
    
    /**
     * This sets the URL to use when attempting to log in. This method is used
     * if for whatever reason the default URL is incorrect.
     *
     * @param theUrl A URL to use to attempt to login.
     */
    public void setSecurityCheckURL(URL theUrl)
    {
       this.securityCheckURL = theUrl;
    }
    
    /**
     * This returns the URL to use when attempting to log in. By default, it's
     * the context URL defined in the Cactus configuration with  
     * "/j_security_check" appended. 
     *
     * @param theConfiguration the Cactus configuration
     * @return the URL that is being used to attempt to login.
     */
    public URL getSecurityCheckURL(Configuration theConfiguration)
    {
        if (this.securityCheckURL == null)
        {
            // Configure default
            String stringUrl = 
                ((WebConfiguration) theConfiguration).getContextURL()
                + "/j_security_check";

            try
            {
                this.securityCheckURL = new URL(stringUrl);
            }
            catch (MalformedURLException e)
            {
                throw new ChainedRuntimeException(
                    "Unable to create default Security Check URL [" 
                    + stringUrl + "]");
            }
        }

        LOGGER.debug("Using security check URL [" + this.securityCheckURL
            + "]");

        return securityCheckURL;
    }

    /**
     * Authenticate the principal by calling the security URL.
     * 
     * @param theRequest the web request used to connect to the Redirector
     * @param theConfiguration the Cactus configuration
     */    
    public void authenticate(WebRequest theRequest, 
        Configuration theConfiguration)
    {
        //Note: This method needs refactoring. It is too complex.
        
        try
        {
            // Create a helper that will connect to a restricted resource.

            String resource = ((WebConfiguration) theConfiguration).
                getRedirectorURL(theRequest);
    
            ConnectionHelper helper = 
                ConnectionHelperFactory.getConnectionHelper(resource, 
                theConfiguration);

            // Make the connection using a default web request.
            HttpURLConnection connection = helper.connect(
                new WebRequest((WebConfiguration) theConfiguration), 
                theConfiguration);

            // Clean any existing session ID.
            sessionId = null;
            
            // Check (possible multiple) cookies for a JSESSIONID.
            int i = 1;
            String key = connection.getHeaderFieldKey(i);
            while (key != null)
            {
                if (key.equalsIgnoreCase("set-cookie"))
                {
                    // Cookie is in the form:
                    // "NAME=VALUE; expires=DATE; path=PATH;
                    //  domain=DOMAIN_NAME; secure"
                    // The only thing we care about is finding a cookie with 
                    // the name "JSESSIONID" and caching the value.
                    
                    String cookiestr = connection.getHeaderField(i);
                    String nameValue = cookiestr.substring(0, 
                        cookiestr.indexOf(";"));
                    int equalsChar = nameValue.indexOf("=");
                    String name = nameValue.substring(0, equalsChar);

                    if (name.equalsIgnoreCase("JSESSIONID"))
                    {
                        // We must set a cookie with the exact same name as the
                        // one given to us, so to preserve any capitalization
                        // issues, cache the exact cookie name.
                        sessionIdCookieName = name;
                        sessionId = nameValue.substring(equalsChar + 1);
                        break;
                    }
                }
                key = connection.getHeaderFieldKey(++i);
            }

            // Create a helper that will connect to the security check URL.
            helper = ConnectionHelperFactory.getConnectionHelper(
                getSecurityCheckURL(theConfiguration).toString(), 
                (WebConfiguration) theConfiguration);
                
            // Configure a web request with the JSESSIONID cookie, 
            // the username and the password.          
            WebRequest request = getSecurityRequest();
            request.setConfiguration(theConfiguration);
            request.addCookie(sessionIdCookieName, sessionId);
            request.addParameter("j_username", getName(), 
                WebRequest.POST_METHOD);
            request.addParameter("j_password", getPassword(), 
                WebRequest.POST_METHOD);
            
            // Make the connection using the configured web request.
            connection = helper.connect(request, theConfiguration);
        
            // If we get back a response code of 302, it means we were 
            // redirected to the context root after successfully logging in.
            // If we receive anything else, we didn't log in correctly.
            if (connection.getResponseCode() != 302)
            {
                throw new ChainedRuntimeException("Unable to login, "
                    + "probably due to bad username/password. Received a ["
                    + connection.getResponseCode() + "] response code and "
                    + "was expecting a [302]");
            }
        }
        catch (Throwable e)
        {
            throw new ChainedRuntimeException("Failed to authenticate "
                + "the principal", e);
        }
    }
    
}
