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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
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
package org.apache.cactus.mock;

import java.net.*;
import java.io.*;
import java.util.*;

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
     * return (for each call, the last entry of the vector will be returned
     * and removed from the vector).
     */
    private Vector getHeaderFieldValues = new Vector();

    /**
     * Store the input streams that the <code>getHeaderField()</code> will
     * return (for each call, the last entry of the vector will be returned
     * and removed from the vector).
     */
    private Vector getInputStreamValues = new Vector();

    // -----------------------------------------------------------------------
    // Methods added on top of those found in HttpURLConnection
    // -----------------------------------------------------------------------

    /**
     * Add a new header field value to the vector of values that will be
     * returned by <code>getHeaderField()</code>.
     *
     * @param theValue the header file value to add
     */
    public void addGetHeaderFieldValue(String theValue)
    {
        this.getHeaderFieldValues.addElement(theValue);
    }

    /**
     * Add a new input stream to the vector of values that will be
     * returned by <code>getInputStream()</code>.
     *
     * @param theValue the input stream to add
     */
    public void addGetInputStream(InputStream theValue)
    {
        this.getInputStreamValues.addElement(theValue);
    }

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

    /**
     * See <code>java.net.URLConnection.getHeaderField</code>.
     */
    public String getHeaderField(int fieldNumber)
    {
        if (this.getHeaderFieldValues.isEmpty()) {
            throw new RuntimeException("Must call addGetHeaderFieldValue() " +
                "first !");
        }
        String result = (String)this.getHeaderFieldValues.elementAt(
            this.getHeaderFieldValues.size() - 1);
        this.getHeaderFieldValues.removeElementAt(
            this.getHeaderFieldValues.size() - 1);
        return result;
    }

    /**
     * See <code>java.net.URLConnection.getInputStream</code>.
     */
    public InputStream getInputStream()
    {
        if (this.getInputStreamValues.isEmpty()) {
            throw new RuntimeException("Must call addGetInputStream() " +
                "first !");
        }
        InputStream result = (InputStream)this.getInputStreamValues.elementAt(
            this.getInputStreamValues.size() - 1);
        this.getInputStreamValues.removeElementAt(
            this.getInputStreamValues.size() - 1);
        return result;
    }

    // -----------------------------------------------------------------------
    // Methods needed because HttpURLConnection is an abstract class
    // -----------------------------------------------------------------------

    public boolean usingProxy()
    {
        return false;
    }

    public void disconnect()
    {
    }

    public void connect()
    {
    }

}
