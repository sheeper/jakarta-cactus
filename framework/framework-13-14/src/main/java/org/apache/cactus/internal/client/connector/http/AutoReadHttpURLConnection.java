/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cactus.internal.client.connector.http;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * @version $Id: AutoReadHttpURLConnection.java 239010 2004-06-19 15:10:53Z vmassol $
 */
final class AutoReadHttpURLConnection extends HttpURLConnection
{
    /**
     * The logger.
     */
    private static final Log LOGGER = 
        LogFactory.getLog(AutoReadHttpURLConnection.class);

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
     * the wrapped connection's input stream.
     *
     * @return the input stream
     * @exception IOException if an error occurs when reading the input stream
     */
    public synchronized InputStream getInputStream() throws IOException
    {
        // Catch IOException to log the content of the error stream
        try
        {
            if (this.streamBuffer == null)
            {
                LOGGER.debug("Original connection = " + this.delegate);

                InputStream is = this.delegate.getInputStream();

                this.streamBuffer = getBufferedInputStream(is);
            }
        }
        catch (IOException e)
        {
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
        if (theErrorStream != null)
        {
            // Log content of error stream
            BufferedReader errorStream = 
                new BufferedReader(new InputStreamReader(theErrorStream));
            String buffer;

            while ((buffer = errorStream.readLine()) != null)
            {
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

        ByteArrayInputStream bais = new ByteArrayInputStream(os.toByteArray());

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
        LOGGER.debug("Content-Length : [" + this.delegate.getContentLength()
            + "]");

        if (theInputStream != null && this.delegate.getContentLength() != 0)
        {
            byte[] buf = new byte[DEFAULT_CHUNK_SIZE];
            int count;

            while (-1 != (count = theInputStream.read(buf)))
            {
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

        for (int i = 0; i < theCount; i++)
        {
            if (theBuffer[i] == 10)
            {
                prefix.append("\\r");
            }
            else if (theBuffer[i] == 13)
            {
                prefix.append("\\n");
            }
            else
            {
                prefix.append((char) theBuffer[i]);
            }
        }

        LOGGER.debug("Read [" + theCount + "]: [" + prefix + "]");
    }

    // Delegated methods

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#connect()
     */
    public void connect() throws IOException
    {
        this.delegate.connect();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getAllowUserInteraction()
     */
    public boolean getAllowUserInteraction()
    {
        return this.delegate.getAllowUserInteraction();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getContent()
     */
    public Object getContent() throws IOException
    {
        return this.delegate.getContent();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getContentEncoding()
     */
    public String getContentEncoding()
    {
        return this.delegate.getContentEncoding();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getContentLength()
     */
    public int getContentLength()
    {
        return this.delegate.getContentLength();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getContentType()
     */
    public String getContentType()
    {
        return this.delegate.getContentType();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getDate()
     */
    public long getDate()
    {
        return this.delegate.getDate();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getDefaultUseCaches()
     */
    public boolean getDefaultUseCaches()
    {
        return this.delegate.getDefaultUseCaches();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getDoInput()
     */
    public boolean getDoInput()
    {
        return this.delegate.getDoInput();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getDoOutput()
     */
    public boolean getDoOutput()
    {
        return this.delegate.getDoOutput();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getExpiration()
     */
    public long getExpiration()
    {
        return this.delegate.getExpiration();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getHeaderField(int)
     */
    public String getHeaderField(int thePosition)
    {
        return this.delegate.getHeaderField(thePosition);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getHeaderField(String)
     */
    public String getHeaderField(String theName)
    {
        return this.delegate.getHeaderField(theName);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getHeaderFieldDate(String, long)
     */
    public long getHeaderFieldDate(String theName, long theDefaultValue)
    {
        return this.delegate.getHeaderFieldDate(theName, theDefaultValue);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getHeaderFieldInt(String, int)
     */
    public int getHeaderFieldInt(String theName, int theDefaultValue)
    {
        return this.delegate.getHeaderFieldInt(theName, theDefaultValue);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getHeaderFieldKey(int)
     */
    public String getHeaderFieldKey(int thePosition)
    {
        return this.delegate.getHeaderFieldKey(thePosition);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getIfModifiedSince()
     */
    public long getIfModifiedSince()
    {
        return this.delegate.getIfModifiedSince();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getLastModified()
     */
    public long getLastModified()
    {
        return this.delegate.getLastModified();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException
    {
        return this.delegate.getOutputStream();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getPermission()
     */
    public Permission getPermission() throws IOException
    {
        return this.delegate.getPermission();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getRequestProperty(String)
     */
    public String getRequestProperty(String theKey)
    {
        return this.delegate.getRequestProperty(theKey);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getURL()
     */
    public URL getURL()
    {
        return this.delegate.getURL();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getUseCaches()
     */
    public boolean getUseCaches()
    {
        return this.delegate.getUseCaches();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#setAllowUserInteraction(boolean)
     */
    public void setAllowUserInteraction(boolean hasInteraction)
    {
        this.delegate.setAllowUserInteraction(hasInteraction);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#setDefaultUseCaches(boolean)
     */
    public void setDefaultUseCaches(boolean isUsingDefaultCache)
    {
        this.delegate.setDefaultUseCaches(isUsingDefaultCache);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#setDoInput(boolean)
     */
    public void setDoInput(boolean isInput)
    {
        this.delegate.setDoInput(isInput);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#setDoOutput(boolean)
     */
    public void setDoOutput(boolean isOutput)
    {
        this.delegate.setDoOutput(isOutput);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#setIfModifiedSince(long)
     */
    public void setIfModifiedSince(long isModifiedSince)
    {
        this.delegate.setIfModifiedSince(isModifiedSince);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#setRequestProperty(String, String)
     */
    public void setRequestProperty(String theKey, String theValue)
    {
        this.delegate.setRequestProperty(theKey, theValue);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#setUseCaches(boolean)
     */
    public void setUseCaches(boolean isUsingCaches)
    {
        this.delegate.setUseCaches(isUsingCaches);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#toString()
     */
    public String toString()
    {
        return this.delegate.toString();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#disconnect()
     */
    public void disconnect()
    {
        this.delegate.disconnect();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getErrorStream()
     */
    public InputStream getErrorStream()
    {
        return this.delegate.getErrorStream();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getRequestMethod()
     */
    public String getRequestMethod()
    {
        return this.delegate.getRequestMethod();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getResponseCode()
     */
    public int getResponseCode() throws IOException
    {
        return this.delegate.getResponseCode();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#getResponseMessage()
     */
    public String getResponseMessage() throws IOException
    {
        return this.delegate.getResponseMessage();
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#setRequestMethod(String)
     */
    public void setRequestMethod(String theMethod) throws ProtocolException
    {
        this.delegate.setRequestMethod(theMethod);
    }

    /**
     * {@inheritDoc}
     * @see java.net.HttpURLConnection#usingProxy()
     */
    public boolean usingProxy()
    {
        return this.delegate.usingProxy();
    }
}
