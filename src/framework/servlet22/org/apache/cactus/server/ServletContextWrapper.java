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
 * @version @version@
 * @see RequestDispatcherWrapper
 */
public class ServletContextWrapper implements ServletContext
{
    /**
     * The original servlet context object
     */
    private ServletContext m_OriginalContext;

    /**
     * @param theOriginalContext the original servlet context object
     */
    public ServletContextWrapper(ServletContext theOriginalContext)
    {
        m_OriginalContext = theOriginalContext;
    }

    public String getMimeType(String theFilename)
    {
        return m_OriginalContext.getMimeType(theFilename);
    }

    public URL getResource(String thePath) throws MalformedURLException
    {
        return m_OriginalContext.getResource(thePath);
    }

    public InputStream getResourceAsStream(String thePath)
    {
        return m_OriginalContext.getResourceAsStream(thePath);
    }

    /**
     * @return our request dispatcher wrapper
     */
    public RequestDispatcher getRequestDispatcher(String thePath)
    {
        RequestDispatcher dispatcher = new RequestDispatcherWrapper(
            m_OriginalContext.getRequestDispatcher(thePath));
        return dispatcher;
    }

    public RequestDispatcher getNamedDispatcher(String theName)
    {
        RequestDispatcher dispatcher = new RequestDispatcherWrapper(
            m_OriginalContext.getNamedDispatcher(theName));
        return dispatcher;
    }

    public String getRealPath(String thePath)
    {
        return m_OriginalContext.getRealPath(thePath);
    }

    /**
     * @return our servlet context wrapper
     */
    public ServletContext getContext(String theUripath)
    {
        ServletContext context = new ServletContextWrapper(
            m_OriginalContext.getContext(theUripath));
        return context;
    }

    public String getServerInfo()
    {
        return m_OriginalContext.getServerInfo();
    }

    public String getInitParameter(String theName)
    {
        return m_OriginalContext.getInitParameter(theName);
    }

    public Enumeration getInitParameterNames()
    {
        return m_OriginalContext.getInitParameterNames();
    }

    public Object getAttribute(String theName)
    {
        return m_OriginalContext.getAttribute(theName);
    }

    public Enumeration getAttributeNames()
    {
        return m_OriginalContext.getAttributeNames();
    }

    public void setAttribute(String theName, Object theAttribute)
    {
        m_OriginalContext.setAttribute(theName, theAttribute);
    }

    public void removeAttribute(String theName)
    {
        m_OriginalContext.removeAttribute(theName);
    }

    public int getMajorVersion()
    {
        return m_OriginalContext.getMajorVersion();
    }

    public int getMinorVersion()
    {
        return m_OriginalContext.getMinorVersion();
    }

    public void log(String theMessage)
    {
        m_OriginalContext.log(theMessage);
    }

    public void log(String theMessage, Throwable theCause)
    {
        m_OriginalContext.log(theMessage, theCause);
    }

    // deprecated methods

    public Servlet getServlet(String theName) throws ServletException
    {
        return m_OriginalContext.getServlet(theName);
    }

    public Enumeration getServlets()
    {
        return m_OriginalContext.getServlets();
    }

    public Enumeration getServletNames()
    {
        return m_OriginalContext.getServletNames();
    }

    public void log(Exception theException, String theMessage)
    {
        m_OriginalContext.log(theException, theMessage);
    }

}
