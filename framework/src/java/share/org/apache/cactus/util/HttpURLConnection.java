/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
package org.apache.cactus.util;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.Header;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.ProtocolException;
import java.security.Permission;

/**
 * Provides a <code>HttpURLConnection</code> wrapper around HttpClient
 * <code>HttpMethod</code>. This allows existing code to easily switch to
 * HttpClieht without breaking existing interfaces using the JDK
 * <code>HttpURLConnection<code>.
 *
 * Note: It is a best try effort as different version of the JDK have different
 * behaviours for <code>HttpURLConnection</code> (And I'm not even including
 * the numerous <code>HttpURLConnection</code> bugs!).
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class HttpURLConnection extends java.net.HttpURLConnection
{
    /**
     * The <code>HttpMethod</code> object that was used to connect to the
     * HTTP server. It contains all the returned data.
     */
    private HttpMethod method;

    /**
     * The URL to which we are connected
     */
    private URL url;

    /**
     * Creates an <code>HttpURLConnection</code> from a
     * <code>HttpMethod</code>.
     *
     * @param theMethod the theMethod that was used to connect to the HTTP
     *        server and which contains the returned data.
     * @param theURL the URL to which we are connected (includes query string)
     */
    public HttpURLConnection(HttpMethod theMethod, URL theURL)
    {
        super(theURL);
        this.method = theMethod;
        this.url = theURL;
    }

    /**
     * @see java.net.HttpURLConnection#HttpURLConnection(URL)
     */
    protected HttpURLConnection(URL theURL)
    {
        super(theURL);
        throw new RuntimeException("An HTTP URL connection can only be "
            + "constructed from a HttpMethod class");
    }

    /**
     * @see java.net.HttpURLConnection#getInputStream()
     */
    public InputStream getInputStream() throws IOException
    {
        return this.method.getResponseBodyAsStream();
    }

    /**
     * @see java.net.HttpURLConnection#getErrorStream()
     */
    public InputStream getErrorStream()
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#disconnect()
     */
    public void disconnect()
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#connect()
     */
    public void connect() throws IOException
    {
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * @see java.net.HttpURLConnection#usingProxy()
     */
    public boolean usingProxy()
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#getRequestMethod()
     */
    public String getRequestMethod()
    {
        return this.method.getName();
    }

    /**
     * @see java.net.HttpURLConnection#getResponseCode()
     */
    public int getResponseCode() throws IOException
    {
        return this.method.getStatusCode();
    }

    /**
     * @see java.net.HttpURLConnection#getResponseMessage()
     */
    public String getResponseMessage() throws IOException
    {
        return this.method.getStatusText();
    }

    /**
     * @see java.net.HttpURLConnection#getHeaderField(String)
     */
    public String getHeaderField(String theName)
    {
        // Note: Return the last matching header in the Header[] array, as in
        // the JDK implementation.
        Header[] headers = this.method.getResponseHeaders();
        for (int i = headers.length - 1; i >= 0; i--) {
            if (headers[i].getName().equalsIgnoreCase(theName)) {
                return headers[i].getValue();
            }
        }

        return null;
    }

    /**
     * @see java.net.HttpURLConnection#getHeaderFieldKey(int)
     */
    public String getHeaderFieldKey(int theKeyPosition)
    {
        // Note: I hope the header fields are kept in the correct order when
        // calling getRequestHeaders.

        Header[] headers = this.method.getResponseHeaders();
        if (theKeyPosition < 0 || theKeyPosition >= headers.length) {
            return null;
        }

        return headers[theKeyPosition].getName();
    }

    /**
     * @see java.net.HttpURLConnection#getHeaderField(int)
     */
    public String getHeaderField(int thePosition)
    {
        // Note: I hope the header fields are kept in the correct order when
        // calling getRequestHeaders.

        Header[] headers = this.method.getResponseHeaders();
        if (thePosition < 0 || thePosition >= headers.length) {
            return null;
        }

        return headers[thePosition].getValue();
    }

    /**
     * @see java.net.HttpURLConnection#getURL()
     */
    public URL getURL()
    {
        return this.url;
    }

    // Note: We don't implement the following methods so that they default to
    // the JDK implementation. They will all call
    // <code>getHeaderField(String)</code> which we have overridden.

    // java.net.HttpURLConnection#getHeaderFieldDate(String, long)
    // java.net.HttpURLConnection#getContentLength()
    // java.net.HttpURLConnection#getContentType()
    // java.net.HttpURLConnection#getContentEncoding()
    // java.net.HttpURLConnection#getDate()
    // java.net.HttpURLConnection#getHeaderFieldInt(String, int)
    // java.net.HttpURLConnection#getExpiration()
    // java.net.HttpURLConnection#getLastModified()

    /**
     * @see java.net.HttpURLConnection#setInstanceFollowRedirects(boolean)
     */
    public void setInstanceFollowRedirects(boolean isFollowingRedirects)
    {
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * @see java.net.HttpURLConnection#getInstanceFollowRedirects()
     */
    public boolean getInstanceFollowRedirects()
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#setRequestMethod(String)
     */
    public void setRequestMethod(String theMethod) throws ProtocolException
    {
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * @see java.net.HttpURLConnection#getPermission()
     */
    public Permission getPermission() throws IOException
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#getContent()
     */
    public Object getContent() throws IOException
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#getContent(Class[])
     */
    public Object getContent(Class[] theClasses) throws IOException
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException
    {
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * @see java.net.HttpURLConnection#setDoInput(boolean)
     */
    public void setDoInput(boolean isInput)
    {
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * @see java.net.HttpURLConnection#getDoInput()
     */
    public boolean getDoInput()
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#setDoOutput(boolean)
     */
    public void setDoOutput(boolean isOutput)
    {
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * @see java.net.HttpURLConnection#getDoOutput()
     */
    public boolean getDoOutput()
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#setAllowUserInteraction(boolean)
     */
    public void setAllowUserInteraction(boolean isAllowInteraction)
    {
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * @see java.net.HttpURLConnection#getAllowUserInteraction()
     */
    public boolean getAllowUserInteraction()
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#setUseCaches(boolean)
     */
    public void setUseCaches(boolean isUsingCaches)
    {
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * @see java.net.HttpURLConnection#getUseCaches()
     */
    public boolean getUseCaches()
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#setIfModifiedSince(long)
     */
    public void setIfModifiedSince(long theModificationDate)
    {
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * @see java.net.HttpURLConnection#getIfModifiedSince()
     */
    public long getIfModifiedSince()
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#getDefaultUseCaches()
     */
    public boolean getDefaultUseCaches()
    {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @see java.net.HttpURLConnection#setDefaultUseCaches(boolean)
     */
    public void setDefaultUseCaches(boolean isUsingCaches)
    {
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * @see java.net.HttpURLConnection#setRequestProperty(String, String)
     */
    public void setRequestProperty(String theKey, String theValue)
    {
        throw new RuntimeException("This class can only be used with already"
            + "retrieved data");
    }

    /**
     * @see java.net.HttpURLConnection#getRequestProperty(String)
     */
    public String getRequestProperty(String theKey)
    {
        throw new RuntimeException("Not implemented yet");
    }

}
