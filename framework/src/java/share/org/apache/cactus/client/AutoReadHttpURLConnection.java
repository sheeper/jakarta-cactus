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
package org.apache.cactus.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Permission;

import org.apache.cactus.util.log.Log;
import org.apache.cactus.util.log.LogService;

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
 * @author <a href="mailto:Bob.Davison@reuters.com">Bob Davison</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
final class AutoReadHttpURLConnection extends HttpURLConnection
{
    /**
     * The logger
     */
    private static final Log LOGGER =
        LogService.getInstance().
        getLog(AutoReadHttpURLConnection.class.getName());

    /**
     * Default size of array for copying data.
     */
    private static final int DEFAULT_CHUNK_SIZE = 16384;

    /**
     * The wrapped connection.
     */
    private HttpURLConnection delegate;

    /**
     * The read input stream.
     */
    private InputStream streamBuffer;

    /**
     * Constructs a an <code>AutoReadHttpURLConnection</code> object from an
     * <code>HttpURLConnection</code>.
     *
     * @param theConnection the original connection to wrap
     */
    AutoReadHttpURLConnection(HttpURLConnection theConnection)
    {
        super(null);
        this.delegate = theConnection;
    }

    /**
     * Returns an input stream containing the fully read contents of
     * the wrapped connection's input stream
     *
     * @return the input stream
     * @exception IOException if an error occurs when reading the input stream
     */
    public synchronized InputStream getInputStream() throws IOException
    {
        // Catch IOException to log the content of the error stream
        try {
            if (this.streamBuffer == null) {
                LOGGER.debug("Original connection = " + this.delegate);
                InputStream is = this.delegate.getInputStream();
                this.streamBuffer = getBufferedInputStream(is);
            }
        } catch (IOException e) {
            logErrorStream(this.delegate.getErrorStream());
            throw e;
        }

        return this.streamBuffer;
    }

    /**
     * Logs the HTTP error stream (used to get more information when we fail
     * to read from the HTTP URL connection).
     *
     * @param theErrorStream the error stream containing the error description
     * @exception IOException if an error occurs when reading the input stream
     */
    private void logErrorStream(InputStream theErrorStream) throws IOException
    {
        if (theErrorStream != null) {
            // Log content of error stream
            BufferedReader errorStream = new BufferedReader(
                new InputStreamReader(theErrorStream));
            String buffer;
            while ((buffer = errorStream.readLine()) != null) {
                LOGGER.debug("ErrorStream [" + buffer + "]");
            }
        }
    }

    /**
     * Fully read the HTTP Connection response stream until there is no
     * more bytes to read.
     *
     * @param theInputStream the input stream to fully read
     * @return the data read as a buffered input stream
     * @exception IOException if an error occurs when reading the input stream
     */
    private InputStream getBufferedInputStream(InputStream theInputStream)
        throws IOException
    {
        ByteArrayOutputStream os =
            new ByteArrayOutputStream(DEFAULT_CHUNK_SIZE);
        copy(theInputStream, os);
        ByteArrayInputStream bais =
            new ByteArrayInputStream(os.toByteArray());

        return bais;
    }

    /**
     * Copies the input stream passed as parameter to the output stream also
     * passed as parameter. The full stream is read until there is no more
     * bytes to read.
     *
     * @param theInputStream the input stream to read from
     * @param theOutputStream the output stream to write to
     * @exception IOException if an error occurs when reading the input stream
     */
    private void copy(InputStream theInputStream, OutputStream theOutputStream)
        throws IOException
    {
        // Only copy if there are data to copy ... The problem is that not
        // all servers return a content-length header. If there is no header
        // getContentLength() returns -1. It seems to work and it seems
        // that all servers that return no content-length header also do
        // not block on read() operations !

        LOGGER.debug("Content-Length : ["
            + this.delegate.getContentLength() + "]");

        if (this.delegate.getContentLength() != 0) {

            byte[] buf = new byte[DEFAULT_CHUNK_SIZE];
            int count;

            while (-1 != (count = theInputStream.read(buf))) {

                // log read data
                printReadLogs(count, buf);
                theOutputStream.write(buf, 0, count);
            }

        }
    }

    /**
     * Format log data read from socket for pretty printing (replaces
     * asc char 10 by "\r", asc char 13 by "\n").
     *
     * @param theCount the number of bytes read in the buffer
     * @param theBuffer the buffer containing the data to print
     */
    private void printReadLogs(int theCount, byte[] theBuffer)
    {
        // Log portion of read data and replace asc 10 by \r and asc
        // 13 by /n
        StringBuffer prefix = new StringBuffer();
        for (int i = 0; i < theCount; i++) {
            if (theBuffer[i] == 10) {
                prefix.append("\\r");
            } else if (theBuffer[i] == 13) {
                prefix.append("\\n");
            } else {
                prefix.append((char) theBuffer[i]);
            }
        }

        LOGGER.debug("Read [" + theCount + "]: [" + prefix + "]");
    }

    // Delegated methods

    /**
     * @see java.net.HttpURLConnection#connect()
     */
    public void connect() throws IOException
    {
        this.delegate.connect();
    }

    /**
     * @see java.net.HttpURLConnection#getAllowUserInteraction()
     */
    public boolean getAllowUserInteraction()
    {
        return this.delegate.getAllowUserInteraction();
    }

    /**
     * @see java.net.HttpURLConnection#getContent()
     */
    public Object getContent() throws IOException
    {
        return this.delegate.getContent();
    }

    /**
     * @see java.net.HttpURLConnection#getContentEncoding()
     */
    public String getContentEncoding()
    {
        return this.delegate.getContentEncoding();
    }

    /**
     * @see java.net.HttpURLConnection#getContentLength()
     */
    public int getContentLength()
    {
        return this.delegate.getContentLength();
    }

    /**
     * @see java.net.HttpURLConnection#getContentType()
     */
    public String getContentType()
    {
        return this.delegate.getContentType();
    }

    /**
     * @see java.net.HttpURLConnection#getDate()
     */
    public long getDate()
    {
        return this.delegate.getDate();
    }

    /**
     * @see java.net.HttpURLConnection#getDefaultUseCaches()
     */
    public boolean getDefaultUseCaches()
    {
        return this.delegate.getDefaultUseCaches();
    }

    /**
     * @see java.net.HttpURLConnection#getDoInput()
     */
    public boolean getDoInput()
    {
        return this.delegate.getDoInput();
    }

    /**
     * @see java.net.HttpURLConnection#getDoOutput()
     */
    public boolean getDoOutput()
    {
        return this.delegate.getDoOutput();
    }

    /**
     * @see java.net.HttpURLConnection#getExpiration()
     */
    public long getExpiration()
    {
        return this.delegate.getExpiration();
    }

    /**
     * @see java.net.HttpURLConnection#getHeaderField(int)
     */
    public String getHeaderField(int thePosition)
    {
        return this.delegate.getHeaderField(thePosition);
    }

    /**
     * @see java.net.HttpURLConnection#getHeaderField(String)
     */
    public String getHeaderField(String theName)
    {
        return this.delegate.getHeaderField(theName);
    }

    /**
     * @see java.net.HttpURLConnection#getHeaderFieldDate(String, long)
     */
    public long getHeaderFieldDate(String theName, long theDefaultValue)
    {
        return this.delegate.getHeaderFieldDate(theName, theDefaultValue);
    }

    /**
     * @see java.net.HttpURLConnection#getHeaderFieldInt(String, int)
     */
    public int getHeaderFieldInt(String theName, int theDefaultValue)
    {
        return this.delegate.getHeaderFieldInt(theName, theDefaultValue);
    }

    /**
     * @see java.net.HttpURLConnection#getHeaderFieldKey(int)
     */
    public String getHeaderFieldKey(int thePosition)
    {
        return this.delegate.getHeaderFieldKey(thePosition);
    }

    /**
     * @see java.net.HttpURLConnection#getIfModifiedSince()
     */
    public long getIfModifiedSince()
    {
        return this.delegate.getIfModifiedSince();
    }

    /**
     * @see java.net.HttpURLConnection#getLastModified()
     */
    public long getLastModified()
    {
        return this.delegate.getLastModified();
    }

    /**
     * @see java.net.HttpURLConnection#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException
    {
        return this.delegate.getOutputStream();
    }

    /**
     * @see java.net.HttpURLConnection#getPermission()
     */
    public Permission getPermission() throws IOException
    {
        return this.delegate.getPermission();
    }

    /**
     * @see java.net.HttpURLConnection#getRequestProperty(String)
     */
    public String getRequestProperty(String theKey)
    {
        return this.delegate.getRequestProperty(theKey);
    }

    /**
     * @see java.net.HttpURLConnection#getURL()
     */
    public URL getURL()
    {
        return this.delegate.getURL();
    }

    /**
     * @see java.net.HttpURLConnection#getUseCaches()
     */
    public boolean getUseCaches()
    {
        return this.delegate.getUseCaches();
    }

    /**
     * @see java.net.HttpURLConnection#setAllowUserInteraction(boolean)
     */
    public void setAllowUserInteraction(boolean hasInteraction)
    {
        this.delegate.setAllowUserInteraction(hasInteraction);
    }

    /**
     * @see java.net.HttpURLConnection#setDefaultUseCaches(boolean)
     */
    public void setDefaultUseCaches(boolean isUsingDefaultCache)
    {
        this.delegate.setDefaultUseCaches(isUsingDefaultCache);
    }

    /**
     * @see java.net.HttpURLConnection#setDoInput(boolean)
     */
    public void setDoInput(boolean isInput)
    {
        this.delegate.setDoInput(isInput);
    }

    /**
     * @see java.net.HttpURLConnection#setDoOutput(boolean)
     */
    public void setDoOutput(boolean isOutput)
    {
        this.delegate.setDoOutput(isOutput);
    }

    /**
     * @see java.net.HttpURLConnection#setIfModifiedSince(long)
     */
    public void setIfModifiedSince(long isModifiedSince)
    {
        this.delegate.setIfModifiedSince(isModifiedSince);
    }

    /**
     * @see java.net.HttpURLConnection#setRequestProperty(String, String)
     */
    public void setRequestProperty(String theKey, String theValue)
    {
        this.delegate.setRequestProperty(theKey, theValue);
    }

    /**
     * @see java.net.HttpURLConnection#setUseCaches(boolean)
     */
    public void setUseCaches(boolean isUsingCaches)
    {
        this.delegate.setUseCaches(isUsingCaches);
    }

    /**
     * @see java.net.HttpURLConnection#toString()
     */
    public String toString()
    {
        return this.delegate.toString();
    }

    /**
     * @see java.net.HttpURLConnection#disconnect()
     */
    public void disconnect()
    {
        this.delegate.disconnect();
    }

    /**
     * @see java.net.HttpURLConnection#getErrorStream()
     */
    public InputStream getErrorStream()
    {
        return this.delegate.getErrorStream();
    }

    /**
     * @see java.net.HttpURLConnection#getRequestMethod()
     */
    public String getRequestMethod()
    {
        return this.delegate.getRequestMethod();
    }

    /**
     * @see java.net.HttpURLConnection#getResponseCode()
     */
    public int getResponseCode() throws IOException
    {
        return this.delegate.getResponseCode();
    }

    /**
     * @see java.net.HttpURLConnection#getResponseMessage()
     */
    public String getResponseMessage() throws IOException
    {
        return this.delegate.getResponseMessage();
    }

    /**
     * @see java.net.HttpURLConnection#setRequestMethod(String)
     */
    public void setRequestMethod(String theMethod) throws ProtocolException
    {
        this.delegate.setRequestMethod(theMethod);
    }

    /**
     * @see java.net.HttpURLConnection#usingProxy()
     */
    public boolean usingProxy()
    {
        return this.delegate.usingProxy();
    }

}
