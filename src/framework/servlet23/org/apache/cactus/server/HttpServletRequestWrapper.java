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

import org.apache.commons.cactus.*;

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
 * @version @version@
 */
public class HttpServletRequestWrapper implements HttpServletRequest
{
    /**
     * The real HTTP request
     */
    private HttpServletRequest m_Request;

    /**
     * The URL to simulate
     */
    private ServletURL m_URL;

    /**
     * Construct an <code>HttpServletRequest</code> instance that delegates
     * it's method calls to the request object passed as parameter and that
     * uses the URL passed as parameter to simulate a URL from which the request
     * would come from.
     *
     * @param theRequest the real HTTP request
     * @param theURL     the URL to simulate or <code>null</code> if none
     */
    public HttpServletRequestWrapper(HttpServletRequest theRequest, ServletURL theURL)
    {
        m_Request = theRequest;
        m_URL = theURL;
    }

    public HttpServletRequest getOriginalRequest()
    {
        return m_Request;
    }

    public boolean isUserInRole(String theRole)
    {
        return m_Request.isUserInRole(theRole);
    }

    public boolean isRequestedSessionIdValid()
    {
        return m_Request.isRequestedSessionIdValid();
    }
    public boolean isRequestedSessionIdFromUrl()
    {
        return m_Request.isRequestedSessionIdFromUrl();
    }

    public boolean isRequestedSessionIdFromURL()
    {
        return m_Request.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromCookie()
    {
        return m_Request.isRequestedSessionIdFromCookie();
    }

    public Principal getUserPrincipal()
    {
        return m_Request.getUserPrincipal();
    }

    public HttpSession getSession(boolean isCreate)
    {
        return m_Request.getSession(isCreate);
    }

    public HttpSession getSession()
    {
        return m_Request.getSession();
    }

    /**
     * @return the servlet path from the simulated URL or the real servlet path
     *         if a simulation URL has not been defined.
     */
    public String getServletPath()
    {
        if (m_URL != null) {
            return m_URL.getServletPath();
        }
        return m_Request.getServletPath();
    }

    public String getRequestedSessionId()
    {
        return m_Request.getRequestedSessionId();
    }

    public StringBuffer getRequestURL()
    {
        return m_Request.getRequestURL();
    }

    /**
     * @return the URI from the simulated URL or the real URI
     *         if a simulation URL has not been defined.
     */
    public String getRequestURI()
    {
        if (m_URL != null) {
            return m_URL.getURL().getFile();
        }
        return m_Request.getRequestURI();
    }

    public String getRemoteUser()
    {
        return m_Request.getRemoteUser();
    }

    /**
     * @return the query string from the simulated URL or the real query
     *         string if a simulation URL has not been defined.
     */
    public String getQueryString()
    {
        if (m_URL != null) {
            return m_URL.getQueryString();
        }
        return m_Request.getQueryString();
    }

    public String getPathTranslated()
    {
        return m_Request.getPathTranslated();
    }

    /**
     * @return the path info from the simulated URL or the real path info
     *         if a simulation URL has not been defined.
     */
    public String getPathInfo()
    {
        if (m_URL != null) {
            return m_URL.getPathInfo();
        }
        return m_Request.getPathInfo();
    }

    public String getMethod()
    {
        return m_Request.getMethod();
    }

    public int getIntHeader(String theName)
    {
        return m_Request.getIntHeader(theName);
    }

    public Enumeration getHeaders(String theName)
    {
        return m_Request.getHeaders(theName);
    }

    public Enumeration getHeaderNames()
    {
        return m_Request.getHeaderNames();
    }

    public String getHeader(String theName)
    {
        return m_Request.getHeader(theName);
    }

    public long getDateHeader(String theName)
    {
        return m_Request.getDateHeader(theName);
    }

    public Cookie[] getCookies()
    {
        return m_Request.getCookies();
    }

    /**
     * @return the context path from the simulated URL or the real context path
     *         if a simulation URL has not been defined.
     */
    public String getContextPath()
    {
        if (m_URL != null) {
            return m_URL.getContextPath();
        }
        return m_Request.getContextPath();
    }

    public String getAuthType()
    {
        return m_Request.getAuthType();
    }

    public void setCharacterEncoding(String env) throws UnsupportedEncodingException
    {
        m_Request.setCharacterEncoding(env);
    }

    public void setAttribute(String theName, Object theAttribute)
    {
        m_Request.setAttribute(theName, theAttribute);
    }

    public void removeAttribute(String theName)
    {
        m_Request.removeAttribute(theName);
    }

    public boolean isSecure()
    {
        return m_Request.isSecure();
    }

    /**
     * @return the server port number from the simulated URL or the real server
     *         port number if a simulation URL has not been defined. If not
     *         port is defined, then port 80 is returned.
     */
    public int getServerPort()
    {
        if (m_URL != null) {
            if (m_URL.getURL().getPort() == -1) {
                return 80;
            }
            return m_URL.getURL().getPort();
        }
        return m_Request.getServerPort();
    }

    /**
     * @return the server name from the simulated URL or the real server name
     *         if a simulation URL has not been defined.
     */
    public String getServerName()
    {
        if (m_URL != null) {
            return m_URL.getURL().getHost();
        }
        return m_Request.getServerName();
    }

    public String getScheme()
    {
        return m_Request.getScheme();
    }

    public RequestDispatcher getRequestDispatcher(String thePath)
    {
        return m_Request.getRequestDispatcher(thePath);
    }

    public String getRemoteHost()
    {
        return m_Request.getRemoteHost();
    }

    public String getRemoteAddr()
    {
        return m_Request.getRemoteAddr();
    }

    public String getRealPath(String thePath)
    {
        return m_Request.getRealPath(thePath);
    }

    public BufferedReader getReader() throws IOException
    {
        return m_Request.getReader();
    }

    public String getProtocol()
    {
        return m_Request.getProtocol();
    }

    public String[] getParameterValues(String theName)
    {
        return m_Request.getParameterValues(theName);
    }

    public Enumeration getParameterNames()
    {
        return m_Request.getParameterNames();
    }

    public Map getParameterMap()
    {
        return m_Request.getParameterMap();
    }

    public String getParameter(String theName)
    {
        return m_Request.getParameter(theName);
    }

    public Enumeration getLocales()
    {
        return m_Request.getLocales();
    }

    public Locale getLocale()
    {
        return m_Request.getLocale();
    }

    public ServletInputStream getInputStream() throws IOException
    {
        return m_Request.getInputStream();
    }

    public String getContentType()
    {
        return m_Request.getContentType();
    }

    public int getContentLength()
    {
        return m_Request.getContentLength();
    }

    public String getCharacterEncoding()
    {
        return m_Request.getCharacterEncoding();
    }

    public Enumeration getAttributeNames()
    {
        return m_Request.getAttributeNames();
    }

    public Object getAttribute(String theName)
    {
        return m_Request.getAttribute(theName);
    }

}
