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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import java.net.HttpURLConnection;

import java.util.Vector;

import org.apache.cactus.util.ChainedRuntimeException;
import org.apache.cactus.util.IoUtil;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
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
                    cookies = org.apache.commons.httpclient.Cookie.parse(
                        Cookie.getCookieDomain(getWebRequest(), 
                            getConnection().getURL().getHost()), 
                        Cookie.getCookiePort(getWebRequest(), 
                            getConnection().getURL().getPort()), 
                        Cookie.getCookiePath(getWebRequest(), 
                            getConnection().getURL().getFile()), 
                        new Header(headerName, headerValue));
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
}