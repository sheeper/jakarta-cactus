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
package org.apache.cactus.server;

import java.io.UnsupportedEncodingException;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.cactus.ServletURL;

/**
 * Encapsulation class for the Servlet 2.3 API <code>HttpServletRequest</code>.
 * This is an implementation that delegates all the call to the
 * <code>HttpServletRequest</code> object passed in the constructor except for
 * some overiden methods which are use to simulate a URL. This is to be able to
 * simulate any URL that would have been used to call the test method : if this
 * was not done, the URL that would be returned (by calling the
 * <code>getRequestURI()</code> method or others alike) would be the URL of the
 * Cactus redirector servlet and not a URL that the test case want to simulate.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class HttpServletRequestWrapper 
    extends AbstractHttpServletRequestWrapper
{
    /**
     * Construct an <code>HttpServletRequest</code> instance that delegates
     * it's method calls to the request object passed as parameter and that
     * uses the URL passed as parameter to simulate a URL from which the request
     * would come from.
     *
     * @param theRequest the real HTTP request
     * @param theURL the URL to simulate or <code>null</code> if none
     */
    public HttpServletRequestWrapper(HttpServletRequest theRequest, 
        ServletURL theURL)
    {
        super(theRequest, theURL);
    }

    // Not modified methods --------------------------------------------------

    /**
     * @return the URL from the simulated URL or the real URL
     *         if a simulation URL has not been defined.
     * @see HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL()
    {
        StringBuffer result;

        if (this.url != null)
        {
            result = new StringBuffer(this.url.getProtocol() + "://"
                + getServerName() + ":" + getServerPort() + getContextPath() 
                + getServletPath() + getPathInfo());
        }
        else
        {
            result = this.request.getRequestURL();
        }

        return result;
    }

    /**
     * @see HttpServletRequest#setCharacterEncoding(String)
     */
    public void setCharacterEncoding(String theEnvironment)
        throws UnsupportedEncodingException
    {
        this.request.setCharacterEncoding(theEnvironment);
    }

    /**
     * @see HttpServletRequest#getParameterMap()
     */
    public Map getParameterMap()
    {
        return this.request.getParameterMap();
    }
}