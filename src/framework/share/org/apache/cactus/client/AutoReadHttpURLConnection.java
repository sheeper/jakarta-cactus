/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */
package org.apache.commons.cactus.client;

import java.io.*;
import java.net.*;
import java.security.*;

/**
 * Wrapper class for the real <code>HttpURLConnection</code> to the test servlet
 * that reads the complete input stream into an internal buffer on
 * the first call to getInputStream(). This is to ensure that the test servlet
 * is not blocked on i/o when the test caller asks for the results.
 * <p>
 * The wrapper returns the buffered input stream from getInputStream and
 * delegates the rest of the calls.
 * <p>
 * This class is final so we don't have to provide access to protected instance
 * variables and methods of the wrapped connection.
 *
 * @version @version@
 */
final class AutoReadHttpURLConnection extends HttpURLConnection
{
    /**
     * Default size of array for copying data, not sure what a good size is.
     */
    static final int DEFAULT_CHUNK_SIZE = 16384;

    /**
     * Some magic keyword that is prepended to the servlet output stream in
     * order to never have an empty stream returned to the client side. This is
     * needed because this class will try to read all the returned data and
     * if there is none will block ...
     */
    static final byte[] MAGIC_KEYWORD = "C*&()C$$".getBytes();

    /**
     * Size of array for copying data, Allow reset for testing copy loop.
     */
    int chunkSize = DEFAULT_CHUNK_SIZE;

    /**
     * The wrapped connection.
     */
    HttpURLConnection delegate;

    /**
     * The read input stream.
     */
    InputStream streamBuffer;

    AutoReadHttpURLConnection(HttpURLConnection conn)
    {
        super(null);
        delegate = conn;
    }

    /**
     * Returns an input stream containing the fully read contents of
     * the wrapped connection's input stream
     *
     * @return the input stream
     */
    public synchronized InputStream getInputStream() throws IOException
    {
        if (streamBuffer == null) {
            streamBuffer = bufferInputStream(delegate.getInputStream());
        }
        return streamBuffer;
    }

    InputStream bufferInputStream(InputStream is) throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream(chunkSize);
        copy(is, os);
        ByteArrayInputStream bais = null;

        // Remove the magic keyword if it exists. On most cases the magic
        // keyword will have been prepended. However, in some cases (like
        // when the test class does a forward()) the redirector servlet will
        // not have been able to prepend it.

        byte[] buffer = os.toByteArray();
        boolean foundMagic = true;
        for (int i = 1; i < MAGIC_KEYWORD.length - 1; i++) {
            if (buffer[buffer.length - i] != MAGIC_KEYWORD[MAGIC_KEYWORD.length - i]) {
                foundMagic = false;
                break;
            }
        }
        if (foundMagic) {
            bais = new ByteArrayInputStream(buffer, 0, buffer.length - MAGIC_KEYWORD.length);
        } else {
            bais = new ByteArrayInputStream(buffer);
        }

        return bais;        
    }

    void copy(InputStream is, OutputStream os) throws IOException
    {
        byte[] buf = new byte[chunkSize];
        int count;

        while( -1 != (count = is.read(buf)) ) {
            os.write(buf, 0, count);
        }
    }

    // Delegated methods

    public void connect() throws IOException
    {
        delegate.connect();
    }

    public boolean getAllowUserInteraction()
    {
        return delegate.getAllowUserInteraction();
    }

    public Object getContent() throws IOException 
    {
        return delegate.getContent();
    }

    public String getContentEncoding()
    {
        return delegate.getContentEncoding();
    }

    public int getContentLength()
    {
        return delegate.getContentLength();
    }

    public String getContentType()
    {
        return delegate.getContentType();
    }

    public long getDate()
    {
        return delegate.getDate();
    }

    public boolean getDefaultUseCaches()
    {
      return delegate.getDefaultUseCaches();
    }

    public boolean getDoInput()
    {
        return delegate.getDoInput();
    }

    public boolean getDoOutput()
    {
        return delegate.getDoOutput();
    }

    public long getExpiration()
    {
        return delegate.getExpiration();
    }

    public String getHeaderField(int a0)
    {
        return delegate.getHeaderField(a0);
    }

    public String getHeaderField(String a0)
    {
        return delegate.getHeaderField(a0);
    }

    public long getHeaderFieldDate(String a0, long a1)
    {
        return delegate.getHeaderFieldDate(a0, a1);
    }

    public int getHeaderFieldInt(String a0, int a1)
    {
        return delegate.getHeaderFieldInt(a0, a1);
    }

    public String getHeaderFieldKey(int a0)
    {
        return delegate.getHeaderFieldKey(a0);
    }

    public long getIfModifiedSince()
    {
        return delegate.getIfModifiedSince();
    }

    public long getLastModified()
    {
        return delegate.getLastModified();
    }

    public OutputStream getOutputStream() throws IOException
    {
        return delegate.getOutputStream();
    }

    public Permission getPermission() throws IOException
    {
        return delegate.getPermission();
    }

    public String getRequestProperty(String a0)
    {
        return delegate.getRequestProperty(a0);
    }

    public URL getURL()
    {
        return delegate.getURL();
    }

    public boolean getUseCaches()
    {
        return delegate.getUseCaches();
    }

    public void setAllowUserInteraction(boolean a0)
    {
        delegate.setAllowUserInteraction(a0);
    }

    public void setDefaultUseCaches(boolean a0)
    {
        delegate.setDefaultUseCaches(a0);
    }

    public void setDoInput(boolean a0)
    {
        delegate.setDoInput(a0);
    }

    public void setDoOutput(boolean a0)
    {
        delegate.setDoOutput(a0);
    }

    public void setIfModifiedSince(long a0)
    {
        delegate.setIfModifiedSince(a0);
    }

    public void setRequestProperty(String a0, String a1)
    {
        delegate.setRequestProperty(a0, a1);
    }

    public void setUseCaches(boolean a0)
    {
        delegate.setUseCaches(a0);
    }

    public String toString()
    {
        return delegate.toString();
    }

    public void disconnect()
    {
        delegate.disconnect();
    }

    public InputStream getErrorStream()
    {
        return delegate.getErrorStream();
    }

    public String getRequestMethod()
    {
        return delegate.getRequestMethod();
    }

    public int getResponseCode() throws IOException
    {
        return delegate.getResponseCode();
    }

    public String getResponseMessage() throws IOException
    {
        return delegate.getResponseMessage();
    }

    public void setRequestMethod(String a0) throws ProtocolException
    {
        delegate.setRequestMethod(a0);
    }

    public boolean usingProxy()
    {
        return delegate.usingProxy();
    }

}
