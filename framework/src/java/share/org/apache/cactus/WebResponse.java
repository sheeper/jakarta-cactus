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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import java.net.HttpURLConnection;

import java.util.Vector;

import org.apache.cactus.util.ChainedRuntimeException;
import org.apache.cactus.util.CookieUtil;
import org.apache.cactus.util.IoUtil;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default web response implementation that provides a minimal
 * API for asserting returned output stream from the server side. For more
 * complex assertions, use an <code>com.meterware.httpunit.WebResponse</code>
 * instead as parameter of your <code>endXXX()</code> methods.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class WebResponse
{
    /**
     * The logger
     */
    private static final Log LOGGER = LogFactory.getLog(WebResponse.class);

    /**
     * The connection object that was used to call the URL
     */
    private HttpURLConnection connection;

    /**
     * The request data that were used to open the connection to the server.
     */
    private WebRequest request;

    /**
     * Save the response content for repeatable reads.
     */
    private String content;

    /**
     * @param theRequest the request data that were used to open the
     *        connection to the server.
     * @param theConnection the original <code>HttpURLConnection</code> used
     *        to call the URL
     */
    public WebResponse(WebRequest theRequest, HttpURLConnection theConnection)
    {
        this.request = theRequest;
        this.connection = theConnection;
    }

    /**
     * @return the original <code>HttpURLConnection</code> used to call the
     *         URL
     */
    public HttpURLConnection getConnection()
    {
        return this.connection;
    }

    /**
     * @return the request data the were used to open the connection to the
     *         server
     */
    public WebRequest getWebRequest()
    {
        return this.request;
    }

    /**
     * @return the text of the response (excluding headers) as a string.
     */
    public String getText()
    {
        // Get the text from the save content if content has already been
        // read.
        if (this.content == null)
        {
            try
            {
                this.content = IoUtil.getText(this.connection.getInputStream());
            }
            catch (IOException e)
            {
                throw new ChainedRuntimeException(e);
            }
        }

        return this.content;
    }

    /**
     * @return the text of the response (excluding headers) as an array of
     *         strings (each string is a separate line from the output stream).
     */
    public String[] getTextAsArray()
    {
        Vector lines = new Vector();

        try
        {
            // Read content first
            if (this.content == null)
            {
                getText();
            }

            BufferedReader input = new BufferedReader(
                new StringReader(this.content));
            String str;

            while (null != (str = input.readLine()))
            {
                lines.addElement(str);
            }

            input.close();
        }
        catch (IOException e)
        {
            throw new ChainedRuntimeException(e);
        }

        // Dummy variable to explicitely tell the object type to copy.
        String[] dummy = new String[lines.size()];

        return (String[]) (lines.toArray(dummy));
    }

    /**
     * @return a buffered input stream for reading the response data.
     **/
    public InputStream getInputStream()
    {
        try
        {
            return this.connection.getInputStream();
        }
        catch (IOException e)
        {
            throw new ChainedRuntimeException(e);
        }
    }

    /**
     * Return the first cookie found that has the specified name or null
     * if not found.
     *
     * @param theName the cookie name to find
     * @return the cookie or null if not found
     */
    public Cookie getCookie(String theName)
    {
        Cookie result = null;

        Cookie[] cookies = getCookies();

        for (int i = 0; i < cookies.length; i++)
        {
            if (cookies[i].getName().equals(theName))
            {
                result = cookies[i];

                break;
            }
        }

        return result;
    }

    /**
     * Return the first cookie found that has the specified name or null
     * if not found. The name is case-insensitive.
     *
     * @param theName the cookie name to find (case-insensitive)
     * @return the cookie or null if not found
     * @since 1.5
     */
    public Cookie getCookieIgnoreCase(String theName)
    {
        Cookie result = null;

        Cookie[] cookies = getCookies();

        for (int i = 0; i < cookies.length; i++)
        {
            if (cookies[i].getName().equalsIgnoreCase(theName))
            {
                result = cookies[i];

                break;
            }
        }

        return result;
    }

    /**
     * @return the cookies returned by the server
     */
    public Cookie[] getCookies()
    {
        Cookie[] returnCookies = null;

        // There can be several headers named "Set-Cookie", so loop through
        // all the headers, looking for cookies
        String headerName = this.connection.getHeaderFieldKey(0);
        String headerValue = this.connection.getHeaderField(0);

        Vector cookieVector = new Vector();
        CookieSpec cookieSpec = CookiePolicy.getDefaultSpec();

        for (int i = 1; (headerName != null) || (headerValue != null); i++)
        {
            LOGGER.debug("Header name  = [" + headerName + "]");
            LOGGER.debug("Header value = [" + headerValue + "]");

            if ((headerName != null)
                && (headerName.toLowerCase().equals("set-cookie") 
                || headerName.toLowerCase().equals("set-cookie2")))
            {
                // Parse the cookie definition
                org.apache.commons.httpclient.Cookie[] cookies;
                try
                {
                    cookies = cookieSpec.parse(
                        CookieUtil.getCookieDomain(getWebRequest(), 
                            getConnection().getURL().getHost()), 
                        CookieUtil.getCookiePort(getWebRequest(), 
                            getConnection().getURL().getPort()), 
                        CookieUtil.getCookiePath(getWebRequest(), 
                            getConnection().getURL().getFile()),
                        false, new Header(headerName, headerValue));
                }
                catch (HttpException e)
                {
                    throw new ChainedRuntimeException(
                        "Error parsing cookies", e);
                }

                // Transform the HttpClient cookies into Cactus cookies and
                // add them to the cookieVector vector
                for (int j = 0; j < cookies.length; j++)
                {
                    Cookie cookie = new Cookie(cookies[j].getDomain(), 
                        cookies[j].getName(), cookies[j].getValue());

                    cookie.setComment(cookies[j].getComment());
                    cookie.setExpiryDate(cookies[j].getExpiryDate());
                    cookie.setPath(cookies[j].getPath());
                    cookie.setSecure(cookies[j].getSecure());

                    cookieVector.addElement(cookie);
                }
            }

            headerName = this.connection.getHeaderFieldKey(i);
            headerValue = this.connection.getHeaderField(i);
        }

        returnCookies = new Cookie[cookieVector.size()];
        cookieVector.copyInto(returnCookies);

        return returnCookies;
    }

    /**
     * Returns the status code returned by the server.
     * 
     * @return The status code
     * @since 1.5
     */
    public int getStatusCode()
    {
        try
        {
            return this.connection.getResponseCode();
        }
        catch (IOException e)
        {
            throw new ChainedRuntimeException(e);
        }
    }

}
