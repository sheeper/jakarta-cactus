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
package org.apache.cactus.mock;

import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Mock implementation of <code>HttpURLConnection</code>.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class MockHttpURLConnection extends HttpURLConnection
{
    /**
     * Store the header fields that the <code>getHeaderField()</code> will
     * return.
     */
    private String getHeaderFieldValue;

    /**
     * Store the input streams that the <code>getInputStream()</code> will
     * return.
     */
    private InputStream getInputStreamValue;

    // -----------------------------------------------------------------------
    // Methods overriding those from HttpURLConnection
    // -----------------------------------------------------------------------

    /**
     * @param theURL the underlying URL
     */
    public MockHttpURLConnection(URL theURL)
    {
        super(theURL);
    }

    // -----------------------------------------------------------------------
    // Methods added on top of those found in HttpURLConnection
    // -----------------------------------------------------------------------

    /**
     * Sets the header field value that will be returned by
     * <code>getHeaderField()</code>.
     *
     * @param theValue the header field value
     */
    public void setExpectedGetHeaderField(String theValue)
    {
        this.getHeaderFieldValue = theValue;
    }

    /**
     * Sets the input stream value that will be returned by
     * <code>getInputStream()</code>.
     *
     * @param theValue the input stream value
     */
    public void setExpectedGetInputStream(InputStream theValue)
    {
        this.getInputStreamValue = theValue;
    }

    /**
     * @see HttpURLConnection#getHeaderField(int)
     */
    public String getHeaderField(int theFieldNumber)
    {
        if (this.getHeaderFieldValue == null)
        {
            throw new RuntimeException(
                "Must call setExpectedGetHeaderField() first !");
        }

        return this.getHeaderFieldValue;
    }

    /**
     * @see HttpURLConnection#getInputStream()
     */
    public InputStream getInputStream()
    {
        if (this.getInputStreamValue == null)
        {
            throw new RuntimeException(
                "Must call setExpectedGetInputStream() first !");
        }

        return this.getInputStreamValue;
    }

    // -----------------------------------------------------------------------
    // Methods needed because HttpURLConnection is an abstract class
    // -----------------------------------------------------------------------

    /**
     * @see HttpURLConnection#usingProxy()
     */
    public boolean usingProxy()
    {
        return false;
    }

    /**
     * @see HttpURLConnection#disconnect()
     */
    public void disconnect()
    {
    }

    /**
     * @see HttpURLConnection#connect()
     */
    public void connect()
    {
    }
}