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
package org.apache.cactus.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.ProtocolException;
import java.net.URL;

import java.security.Permission;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;

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
 * @deprecated This class has been donated to the
 * <a href="http://jakarta.apache.org/commons/httpclient/">Jakarta 
 * Commons-HttpClient</a> project, and is now maintained there as 
 * <code>org.apache.commons.httpclient.util.HttpURLConnection</code>.
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

        for (int i = headers.length - 1; i >= 0; i--)
        {
            if (headers[i].getName().equalsIgnoreCase(theName))
            {
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
        // Note: HttpClient does not consider the returned Status Line as
        // a response header. However, getHeaderFieldKey(0) is supposed to 
        // return null. Hence the special case below ...
        
        if (theKeyPosition == 0)
        {
            return null;
        }

        // Note: I hope the header fields are kept in the correct order when
        // calling getRequestHeaders.
        Header[] headers = this.method.getResponseHeaders();
        
        if ((theKeyPosition < 0) || (theKeyPosition >= headers.length))
        {
            return null;
        }

        return headers[theKeyPosition - 1].getName();
    }

    /**
     * @see java.net.HttpURLConnection#getHeaderField(int)
     */
    public String getHeaderField(int thePosition)
    {
        // Note: HttpClient does not consider the returned Status Line as
        // a response header. However, getHeaderField(0) is supposed to 
        // return the status line. Hence the special case below ...
        
        if (thePosition == 0)
        {
            if (((HttpMethodBase) this.method).isHttp11())
            {
                return "HTTP/1.1 " + this.method.getStatusCode() 
                    + " " + this.method.getStatusText();
            }
            else
            {
                return "HTTP/1.0 " + this.method.getStatusCode() 
                    + " " + this.method.getStatusText();
            }
        }

        // Note: I hope the header fields are kept in the correct order when
        // calling getRequestHeaders.
        Header[] headers = this.method.getResponseHeaders();

        if ((thePosition < 0) || (thePosition >= headers.length))
        {
            return null;
        }

        return headers[thePosition - 1].getValue();
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
