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

import java.io.InputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.cactus.Cookie;
import org.apache.cactus.WebRequest;
import org.apache.cactus.client.authentication.Authentication;
import org.apache.cactus.internal.configuration.Configuration;
import org.apache.cactus.util.ChainedRuntimeException;

/**
 * Contains all HTTP request data for a test case but independently of
 * the fact that there is or there is not a Cactus redirector. It is the 
 * data that will be sent to the server side.
 *
 * @version $Id$
 */
public abstract class BaseWebRequest implements WebRequest
{
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
    private Authentication authentication;

    /**
     * Default constructor that requires that 
     * {@link #setConfiguration(Configuration)} be called before the methods
     * requiring a configuration object.
     * 
     */
    public BaseWebRequest()
    {
    }

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
     * @param theConfiguration the cactus configuration to assign to this 
     *        request
     */
    public void setConfiguration(Configuration theConfiguration)
    {
        this.configuration = theConfiguration;
    }

    /**
     * @see WebRequest#setContentType(String)
     */
    public void setContentType(String theContentType)
    {
        this.contentType = theContentType;
    }

    /**
     * @see WebRequest#getContentType()
     */
    public String getContentType()
    {
        return this.contentType;
    }

    /**
     * @see WebRequest#setUserData(InputStream)
     */
    public void setUserData(InputStream theDataStream)
    {
        this.dataStream = theDataStream;
    }

    /**
     * @see WebRequest#getUserData()
     */
    public InputStream getUserData()
    {
        return this.dataStream;
    }

    /**
     * @see WebRequest#addParameter(String, String, String)
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
     * @see WebRequest#addParameter(String, String)
     */
    public void addParameter(String theName, String theValue)
    {
        addParameter(theName, theValue, BaseWebRequest.GET_METHOD);
    }

    /**
     * @see WebRequest#getParameterNamesPost()
     */
    public Enumeration getParameterNamesPost()
    {
        return getParameterNames(this.parametersPost);
    }

    /**
     * @see WebRequest#getParameterNamesGet()
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
     * @see WebRequest#getParameterGet(String)
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
     * @see WebRequest#getParameterPost(String)
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
     * @see WebRequest#getParameterValuesGet(String)
     */
    public String[] getParameterValuesGet(String theName)
    {
        return getParameterValues(theName, this.parametersGet);
    }

    /**
     * @see WebRequest#getParameterValuesPost(String)
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
     * @see WebRequest#addCookie(String, String)
     */
    public void addCookie(String theName, String theValue)
    {
        addCookie("localhost", theName, theValue);
    }

    /**
     * @see WebRequest#addCookie(String, String, String)
     */
    public void addCookie(String theDomain, String theName, String theValue)
    {
        addCookie(new Cookie(theDomain, theName, theValue));
    }

    /**
     * @see WebRequest#addCookie(Cookie)
     */
    public void addCookie(Cookie theCookie)
    {
        if (theCookie == null)
        {
            throw new IllegalStateException("The cookie cannot be null");
        }
        this.cookies.addElement(theCookie);
    }

    /**
     * @see WebRequest#getCookies()
     */
    public Vector getCookies()
    {
        return this.cookies;
    }

    /**
     * @see WebRequest#addHeader(String, String)
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
     * @see WebRequest#getHeaderNames()
     */
    public Enumeration getHeaderNames()
    {
        return this.headers.keys();
    }

    /**
     * @see WebRequest#getHeader(String)
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
     * @see WebRequest#getHeaderValues(String)
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
     * @see WebRequest#setAuthentication(Authentication)
     */
    public void setAuthentication(Authentication theAuthentication)
    {
        this.authentication = theAuthentication;
    }

    /**
     * @see WebRequest#getAuthentication()
     */
    public Authentication getAuthentication()
    {
        return this.authentication;
    }
}
