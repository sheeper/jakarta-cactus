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
package org.apache.commons.cactus.server;

import java.util.*;
import java.io.*;
import java.security.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Wrapper around <code>ServletContext</code> which overrides the
 * <code>getRequestDispatcher()</code> method to return our own wrapper around
 * <code>RequestDispatcher</code>.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 * @see RequestDispatcherWrapper
 */
public class ServletContextWrapper implements ServletContext
{
    /**
     * The original servlet context object
     */
    private ServletContext originalContext;

    /**
     * @param theOriginalContext the original servlet context object
     */
    public ServletContextWrapper(ServletContext theOriginalContext)
    {
        this.originalContext = theOriginalContext;
    }

    public String getMimeType(String theFilename)
    {
        return this.originalContext.getMimeType(theFilename);
    }

    public URL getResource(String thePath) throws MalformedURLException
    {
        return this.originalContext.getResource(thePath);
    }

    public InputStream getResourceAsStream(String thePath)
    {
        return this.originalContext.getResourceAsStream(thePath);
    }

    /**
     * @return our request dispatcher wrapper
     */
    public RequestDispatcher getRequestDispatcher(String thePath)
    {
        RequestDispatcher dispatcher = new RequestDispatcherWrapper(
            this.originalContext.getRequestDispatcher(thePath));
        return dispatcher;
    }

    public RequestDispatcher getNamedDispatcher(String theName)
    {
        RequestDispatcher dispatcher = new RequestDispatcherWrapper(
            this.originalContext.getNamedDispatcher(theName));
        return dispatcher;
    }

    public String getRealPath(String thePath)
    {
        return this.originalContext.getRealPath(thePath);
    }

    /**
     * @return our servlet context wrapper
     */
    public ServletContext getContext(String theUripath)
    {
        ServletContext context = new ServletContextWrapper(
            this.originalContext.getContext(theUripath));
        return context;
    }

    public String getServerInfo()
    {
        return this.originalContext.getServerInfo();
    }

    public String getInitParameter(String theName)
    {
        return this.originalContext.getInitParameter(theName);
    }

    public Enumeration getInitParameterNames()
    {
        return this.originalContext.getInitParameterNames();
    }

    public Object getAttribute(String theName)
    {
        return this.originalContext.getAttribute(theName);
    }

    public Enumeration getAttributeNames()
    {
        return this.originalContext.getAttributeNames();
    }

    public void setAttribute(String theName, Object theAttribute)
    {
        this.originalContext.setAttribute(theName, theAttribute);
    }

    public void removeAttribute(String theName)
    {
        this.originalContext.removeAttribute(theName);
    }

    public int getMajorVersion()
    {
        return this.originalContext.getMajorVersion();
    }

    public int getMinorVersion()
    {
        return this.originalContext.getMinorVersion();
    }

    public void log(String theMessage)
    {
        this.originalContext.log(theMessage);
    }

    public void log(String theMessage, Throwable theCause)
    {
        this.originalContext.log(theMessage, theCause);
    }

    // deprecated methods

    public Servlet getServlet(String theName) throws ServletException
    {
        return this.originalContext.getServlet(theName);
    }

    public Enumeration getServlets()
    {
        return this.originalContext.getServlets();
    }

    public Enumeration getServletNames()
    {
        return this.originalContext.getServletNames();
    }

    public void log(Exception theException, String theMessage)
    {
        this.originalContext.log(theException, theMessage);
    }

}
