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

import java.io.InputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.cactus.client.authentication.AbstractAuthentication;
import org.apache.cactus.util.ChainedRuntimeException;
import org.apache.cactus.util.Configuration;

/**
 * Contains all HTTP request data for a test case but independently of
 * the fact that there is or there is not a Cactus redirector. It is the 
 * data that will be sent to the server side.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:Jason.Robertson@acs-inc.com">Jason Robertson</a>
 *
 * @version $Id$
 */
public class BaseWebRequest implements Request
{
    /**
     * GET Method identifier.
     */
    public static final String GET_METHOD = "GET";

    /**
     * POST Method identifier.
     */
    public static final String POST_METHOD = "POST";

    /**
     * Cactus configuration
     */
    private Configuration configuration;

    /**
     * The request parameters that need to be sent in the body (POST)
     */
    private Hashtable parametersPost = new Hashtable();

    /**
     * The request parameters that need to be sent in the URL (GET)
     */
    private Hashtable parametersGet = new Hashtable();

    /**
     * The Cookies
     */
    private Vector cookies = new Vector();

    /**
     * HTTP Headers.
     */
    private Hashtable headers = new Hashtable();

    /**
     * Binary data to send in the request body (if any)
     */
    private InputStream dataStream;

    /**
     * The content type to set in the http request
     */
    private String contentType = "application/x-www-form-urlencoded";

    /**
     * The Authentication Object that will configure the http request
     */
    private AbstractAuthentication authentication;

    /**
     * @param theConfiguration the Cactus configuration
     */
    public BaseWebRequest(Configuration theConfiguration)
    {
        this.configuration = theConfiguration;
    }

    /**
     * @return the Cactus configuration
     */
    protected Configuration getConfiguration()
    {
        return this.configuration;
    }

    /**
     * Sets the content type that will be set in the http request
     *
     * @param theContentType the content type
     */
    public void setContentType(String theContentType)
    {
        this.contentType = theContentType;
    }

    /**
     * @return the content type that will be set in the http request
     */
    public String getContentType()
    {
        return this.contentType;
    }

    /**
     * Allow the user to send arbitrary data in the request body
     *
     * @param theDataStream the stream on which the data are put by the user
     */
    public void setUserData(InputStream theDataStream)
    {
        this.dataStream = theDataStream;
    }

    /**
     * @return the data stream set up by the user
     */
    public InputStream getUserData()
    {
        return this.dataStream;
    }

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
    public void addParameter(String theName, String theValue, String theMethod)
    {
        Hashtable parameters;

        // Decide if the parameter is to be sent using in the url or not
        if (theMethod.equalsIgnoreCase(BaseWebRequest.POST_METHOD))
        {
            parameters = this.parametersPost;
        }
        else if (theMethod.equalsIgnoreCase(BaseWebRequest.GET_METHOD))
        {
            parameters = this.parametersGet;
        }
        else
        {
            throw new ChainedRuntimeException("The method need to be either "
                + "\"POST\" or \"GET\"");
        }

        // If there is already a parameter of the same name, add the
        // new value to the Vector. If not, create a Vector an add it to the
        // hashtable
        if (parameters.containsKey(theName))
        {
            Vector v = (Vector) parameters.get(theName);

            v.addElement(theValue);
        }
        else
        {
            Vector v = new Vector();

            v.addElement(theValue);
            parameters.put(theName, v);
        }
    }

    /**
     * Adds a parameter to the request. The parameter is added to the query
     * string of the URL.
     *
     * @param theName  the parameter's name
     * @param theValue the parameter's value
     *
     * @see #addParameter(String, String, String)
     */
    public void addParameter(String theName, String theValue)
    {
        addParameter(theName, theValue, BaseWebRequest.GET_METHOD);
    }

    /**
     * @return the parameter names that will be passed in the request body
     * (POST)
     */
    public Enumeration getParameterNamesPost()
    {
        return getParameterNames(this.parametersPost);
    }

    /**
     * @return the parameter names that will be passed in the URL (GET)
     */
    public Enumeration getParameterNamesGet()
    {
        return getParameterNames(this.parametersGet);
    }

    /**
     * Returns all the values in the passed hashtable of parameters.
     *
     * @param theParameters the hashtable of parameters
     * @return the parameter names
     */
    private Enumeration getParameterNames(Hashtable theParameters)
    {
        return theParameters.keys();
    }

    /**
     * Returns the first value corresponding to this parameter's name (provided
     * this parameter is passed in the URL).
     *
     * @param theName the parameter's name
     * @return the first value corresponding to this parameter's name or null
     *         if not found in the list of parameters to be sent in the URL
     */
    public String getParameterGet(String theName)
    {
        String[] values = getParameterValuesGet(theName);

        if (values != null)
        {
            return values[0];
        }

        return null;
    }

    /**
     * Returns the first value corresponding to this parameter's name (provided
     * this parameter is passed in the request body - POST).
     *
     * @param theName the parameter's name
     * @return the first value corresponding to this parameter's name or null
     *         if not found in the list of parameters to be sent in the request
     *         body
     */
    public String getParameterPost(String theName)
    {
        String[] values = getParameterValuesPost(theName);

        if (values != null)
        {
            return values[0];
        }

        return null;
    }

    /**
     * Returns all the values corresponding to this parameter's name (provided
     * this parameter is passed in the URL).
     *
     * @param theName the parameter's name
     * @return the first value corresponding to this parameter's name or null
     *         if not found in the list of parameters to be sent in the URL
     */
    public String[] getParameterValuesGet(String theName)
    {
        return getParameterValues(theName, this.parametersGet);
    }

    /**
     * Returns all the values corresponding to this parameter's name (provided
     * this parameter is passed in the request body - POST).
     *
     * @param theName the parameter's name
     * @return the first value corresponding to this parameter's name or null
     *         if not found in the list of parameters to be sent in the request
     *         body
     */
    public String[] getParameterValuesPost(String theName)
    {
        return getParameterValues(theName, this.parametersPost);
    }

    /**
     * Returns all the values corresponding to this parameter's name in the
     * provided hashtable.
     *
     * @param theName the parameter's name
     * @param theParameters the hashtable containing the parameters
     * @return the first value corresponding to this parameter's name or null
     *         if not found in the passed hashtable
     */
    private String[] getParameterValues(String theName, Hashtable theParameters)
    {
        if (theParameters.containsKey(theName))
        {
            Vector v = (Vector) theParameters.get(theName);

            Object[] objs = new Object[v.size()];

            v.copyInto(objs);

            String[] result = new String[objs.length];

            for (int i = 0; i < objs.length; i++)
            {
                result[i] = (String) objs[i];
            }

            return result;
        }

        return null;
    }

    /**
     * Adds a cookie to the request. The cookie will be created with a
     * default localhost domain. Use the
     * <code>addCookie(String theDomain, String theName,
     * String theValue)</code> method or the
     * <code>addCookie(Cookie theCookie)</code> if you wish to specify a
     * domain.
     *
     * Note that the domain must match either the redirector host
     * (specified in <code>cactus.properties</code>) or the host set
     * using <code>setURL()</code>.
     *
     * @param theName the cookie's name
     * @param theValue the cookie's value
     */
    public void addCookie(String theName, String theValue)
    {
        addCookie("localhost", theName, theValue);
    }

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
    public void addCookie(String theDomain, String theName, String theValue)
    {
        addCookie(new Cookie(theDomain, theName, theValue));
    }

    /**
     * Adds a cookie to the request.
     *
     * Note that the domain must match either the redirector host
     * (specified in <code>cactus.properties</code>) or the host set
     * using <code>setURL()</code>.
     *
     * @param theCookie the cookie to add
     */
    public void addCookie(Cookie theCookie)
    {
        this.cookies.addElement(theCookie);
    }

    /**
     * @return the cookies (vector of <code>Cookie</code> objects)
     */
    public Vector getCookies()
    {
        return this.cookies;
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
        // If the header is "Content-type", then call setContentType() instead.
        // This is to prevent the content type to be set twice.
        if (theName.equalsIgnoreCase("Content-type"))
        {
            setContentType(theValue);

            return;
        }

        // If there is already a header of the same name, add the
        // new header to the Vector. If not, create a Vector an add it to the
        // hashtable
        if (this.headers.containsKey(theName))
        {
            Vector v = (Vector) this.headers.get(theName);

            v.addElement(theValue);
        }
        else
        {
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

        if (values != null)
        {
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
        if (this.headers.containsKey(theName))
        {
            Vector v = (Vector) this.headers.get(theName);

            Object[] objs = new Object[v.size()];

            v.copyInto(objs);

            String[] result = new String[objs.length];

            for (int i = 0; i < objs.length; i++)
            {
                result[i] = (String) objs[i];
            }

            return result;
        }

        return null;
    }

    /**
     * @return a string representation of the request
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        // Append cookies
        buffer.append("cookies = [");
        buffer.append(toStringAppendCookies());
        buffer.append("], ");

        // Append headers
        buffer.append("headers = [");
        buffer.append(toStringAppendHeaders());
        buffer.append("], ");

        // Append parameters
        buffer.append("GET parameters = [");
        buffer.append(toStringAppendParametersGet());
        buffer.append("], ");
        buffer.append("POST parameters = [");
        buffer.append(toStringAppendParametersPost());
        buffer.append("]");

        return buffer.toString();
    }

    /**
     * @return a string representation of the headers
     */
    private String toStringAppendHeaders()
    {
        StringBuffer buffer = new StringBuffer();

        Enumeration headers = getHeaderNames();

        while (headers.hasMoreElements())
        {
            buffer.append("[");

            String headerName = (String) headers.nextElement();
            String[] headerValues = getHeaderValues(headerName);

            buffer.append("[" + headerName + "] = [");

            for (int i = 0; i < (headerValues.length - 1); i++)
            {
                buffer.append("[" + headerValues[i] + "], ");
            }

            buffer.append("[" + headerValues[headerValues.length - 1] + "]]");
            buffer.append("]");
        }

        return buffer.toString();
    }

    /**
     * @return a string representation of the cookies
     */
    private String toStringAppendCookies()
    {
        StringBuffer buffer = new StringBuffer();

        Enumeration cookies = getCookies().elements();

        while (cookies.hasMoreElements())
        {
            Cookie cookie = (Cookie) cookies.nextElement();

            buffer.append("[" + cookie + "]");
        }

        return buffer.toString();
    }

    /**
     * @return a string representation of the parameters to be added in the
     *         request body
     */
    private String toStringAppendParametersPost()
    {
        return toStringAppendParameters(this.parametersPost);
    }

    /**
     * @return a string representation of the parameters to be added in the
     *         URL
     */
    private String toStringAppendParametersGet()
    {
        return toStringAppendParameters(this.parametersGet);
    }

    /**
     * @param theParameters the HTTP parameters
     * @return a string representation of the HTTP parameters passed as
     *         parameters
     */
    private String toStringAppendParameters(Hashtable theParameters)
    {
        StringBuffer buffer = new StringBuffer();

        Enumeration parameters = getParameterNames(theParameters);

        while (parameters.hasMoreElements())
        {
            buffer.append("[");

            String parameterName = (String) parameters.nextElement();
            String[] parameterValues = getParameterValues(parameterName, 
                theParameters);

            buffer.append("[" + parameterName + "] = [");

            for (int i = 0; i < (parameterValues.length - 1); i++)
            {
                buffer.append("[" + parameterValues[i] + "], ");
            }

            buffer.append("[" + parameterValues[parameterValues.length - 1]
                + "]]");
            buffer.append("]");
        }

        return buffer.toString();
    }

    /**
     * Sets the authentication object that will configure the http request
     *
     * @param theAuthenticationObject the authentication object
     */
    public void setAuthentication(
        AbstractAuthentication theAuthenticationObject)
    {
        this.authentication = theAuthenticationObject;
        
        // Sets the Cactus configuration. It is performed here so that
        // Cactus users do not have to bother with setting it on the
        // Authentication object they create.
        this.authentication.setConfiguration(getConfiguration());
    }

    /**
     * @return the authentication object that will configure the http request
     */
    public AbstractAuthentication getAuthentication()
    {
        return this.authentication;
    }

}