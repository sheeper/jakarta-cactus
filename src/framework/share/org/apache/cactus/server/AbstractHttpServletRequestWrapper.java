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
package org.apache.cactus.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;

import org.apache.cactus.ServletURL;
import org.apache.cactus.util.log.Log;
import org.apache.cactus.util.log.LogService;

/**
 * Abstract wrapper around <code>HttpServletRequest</code>. This class provides
 * a common implementation of the wrapper for the different servlet API.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public abstract class AbstractHttpServletRequestWrapper
        implements HttpServletRequest
{
    /**
     * The real HTTP request
     */
    protected HttpServletRequest request;

    /**
     * The URL to simulate
     */
    protected ServletURL url;

    /**
     * The logger
     */
    private static final Log LOGGER =
        LogService.getInstance().
        getLog(AbstractHttpServletRequestWrapper.class.getName());

    /**
     * Construct an <code>HttpServletRequest</code> instance that delegates
     * it's method calls to the request object passed as parameter and that
     * uses the URL passed as parameter to simulate a URL from which the request
     * would come from.
     *
     * @param theRequest the real HTTP request
     * @param theURL     the URL to simulate or <code>null</code> if none
     */
    public AbstractHttpServletRequestWrapper(HttpServletRequest theRequest,
        ServletURL theURL)
    {
        this.request = theRequest;
        this.url = theURL;
    }

    /**
     * @return the original request object
     */
    public HttpServletRequest getOriginalRequest()
    {
        return this.request;
    }

    // Modified methods ------------------------------------------------------

    /**
     * @return the context path from the simulated URL or the real context path
     *         if a simulation URL has not been defined.
     */
    public String getContextPath()
    {
        String result = this.request.getContextPath();

        if (this.url != null) {
            if (this.url.getContextPath() != null) {
                result = this.url.getContextPath();
                LOGGER.debug("Using simulated context : [" + result + "]");
            }
        }

        return result;
    }

    /**
     * @return the path info from the simulated URL or the real path info
     *         if a simulation URL has not been defined.
     */
    public String getPathInfo()
    {
        String result = this.request.getPathInfo();

        if (this.url != null) {
            result = this.url.getPathInfo();
            LOGGER.debug("Using simulated PathInfo : [" + result + "]");
        }
        return result;
    }

    /**
     * @return the server name from the simulated URL or the real server name
     *         if a simulation URL has not been defined.
     */
    public String getServerName()
    {
        String result = this.request.getServerName();

        if (this.url != null) {
            if (this.url.getServerName() != null) {
                result = this.url.getHost();
                LOGGER.debug("Using simulated server name : [" + result +
                    "]");
            }
        }
        return result;
    }

    /**
     * @return the server port number from the simulated URL or the real server
     *         port number if a simulation URL has not been defined. If not
     *         port is defined, then port 80 is returned.
     */
    public int getServerPort()
    {
        int result = this.request.getServerPort();

        if (this.url != null) {
            result = (this.url.getPort() == -1) ? 80 : this.url.getPort();
            LOGGER.debug("Using simulated server port : [" + result + "]");
        }

        return result;
    }

    /**
     * @return the URI from the simulated URL or the real URI
     *         if a simulation URL has not been defined.
     */
    public String getRequestURI()
    {
        String result = this.request.getRequestURI();

        if (this.url != null) {

            result = getContextPath() +
                ((getServletPath() == null) ? "" : getServletPath()) +
                ((getPathInfo() == null) ? "" : getPathInfo());

            LOGGER.debug("Using simulated request URI : [" + result + "]");
        }

        return result;
    }

    /**
     * @return the servlet path from the simulated URL or the real servlet path
     *         if a simulation URL has not been defined.
     */
    public String getServletPath()
    {
        String result = this.request.getServletPath();

        if (this.url != null) {
            result = this.url.getServletPath();
            LOGGER.debug("Using simulated servlet path : [" + result +
                "]");
        }

        return result;
    }

    /**
     * @return any extra path information after the servlet name but
     *         before the query string, and translates it to a real path.
     *         Takes into account the simulated URL (if any).
     */
    public String getPathTranslated()
    {
        String pathTranslated;

        String pathInfo = this.url.getPathInfo();
        if (pathInfo != null) {

            // If getRealPath returns null then getPathTranslated should also
            // return null (see section SRV.4.5 of the Servlet 2.3 spec).
            if (this.request.getRealPath("/") == null) {
                pathTranslated = null;
            } else {

                // Compute the translated path using the root real path
                String newPathInfo = (pathInfo.startsWith("/") ?
                    pathInfo.substring(1) : pathInfo);
                if (this.request.getRealPath("/").endsWith("/")) {
                    pathTranslated = this.request.getRealPath("/") +
                        newPathInfo.replace('/', File.separatorChar);
                } else {
                    pathTranslated = this.request.getRealPath("/") +
                        File.separatorChar + newPathInfo.replace('/',
                            File.separatorChar);
                }
            }
        } else {
            pathTranslated = this.request.getPathTranslated();
        }

        return pathTranslated;
    }

    /**
     * @return the query string from the simulated URL or the real query
     *         string if a simulation URL has not been defined.
     */
    public String getQueryString()
    {
        String result = this.request.getQueryString();

        if (this.url != null) {
            result = this.url.getQueryString();
            LOGGER.debug("Using simulated query string : [" + result +
                "]");
        }
        return result;
    }

    /**
     * @param thePath the path to the resource
     * @return a wrapped request dispatcher instead of the real one, so that
     *         forward() and include() calls will use the wrapped dispatcher
     *         passing it the *original* request [this is needed for some
     *         servlet engine like Tomcat 3.x which do not support the new
     *         mechanism introduced by Servlet 2.3 Filters].
     * @see HttpServletRequest#getRequestDispatcher(String)
     */
    public RequestDispatcher getRequestDispatcher(String thePath)
    {
        // I hate it, but we have to write some logic here ! Ideally we
        // shouldn't have to do this as it is supposed to be done by the servlet
        // engine. However as we are simulating the request URL, we have to
        // provide it ... This is where we can see the limitation of Cactus
        // (it has to mock some parts of the servlet engine) !

        if (thePath == null) {
            return null;
        }

        RequestDispatcher dispatcher = null;
        String fullPath;

        // The spec says that the path can be relative, in which case it will
        // be relative to the request. So for relative paths, we need to take
        // into account the simulated URL (ServletURL).
        if (thePath.startsWith("/")) {

            fullPath = thePath;

        } else {

            String pI = getPathInfo();
            if (pI == null) {
                fullPath = catPath(getServletPath(), thePath);
            } else {
                fullPath = catPath(getServletPath() + pI, thePath);
            }

            if (fullPath == null) {
                return null;
            }
        }

        LOGGER.debug("Computed full path : [" + fullPath + "]");

        dispatcher = new RequestDispatcherWrapper(
            this.request.getRequestDispatcher(fullPath));

        return dispatcher;
    }

    /**
     * Will concatenate 2 paths, normalising it. For example :
     * ( /a/b/c + d = /a/b/d, /a/b/c + ../d = /a/d ). Code borrowed from
     * Tomcat 3.2.2 !
     *
     * @param theLookupPath the first part of the path
     * @param thePath the part to add to the lookup path
     * @return the concatenated thePath or null if an error occurs
     */
    private String catPath(String theLookupPath, String thePath)
    {
        // Cut off the last slash and everything beyond
        int index = theLookupPath.lastIndexOf("/");
        theLookupPath = theLookupPath.substring(0, index);

        // Deal with .. by chopping dirs off the lookup thePath
        while (thePath.startsWith("../")) {
            if (theLookupPath.length() > 0) {
                index = theLookupPath.lastIndexOf("/");
                theLookupPath = theLookupPath.substring(0, index);
            } else {
                // More ..'s than dirs, return null
                return null;
            }

            index = thePath.indexOf("../") + 3;
            thePath = thePath.substring(index);
        }

        return theLookupPath + "/" + thePath;
    }

    // Not modified methods --------------------------------------------------

    /**
     * @see HttpServletRequest#isRequestedSessionIdFromURL()
     */
    public boolean isRequestedSessionIdFromURL()
    {
        return this.request.isRequestedSessionIdFromURL();
    }

    /**
     * @see HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    public boolean isRequestedSessionIdFromUrl()
    {
        return this.request.isRequestedSessionIdFromURL();
    }

    /**
     * @see HttpServletRequest#isUserInRole(String)
     */
    public boolean isUserInRole(String theRole)
    {
        return this.request.isUserInRole(theRole);
    }

    /**
     * @see HttpServletRequest#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid()
    {
        return this.request.isRequestedSessionIdValid();
    }

    /**
     * @see HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    public boolean isRequestedSessionIdFromCookie()
    {
        return this.request.isRequestedSessionIdFromCookie();
    }

    /**
     * @see HttpServletRequest#getLocales()
     */
    public Enumeration getLocales()
    {
        return this.request.getLocales();
    }

    /**
     * @see HttpServletRequest#getHeader(String)
     */
    public String getHeader(String theName)
    {
        return this.request.getHeader(theName);
    }

    /**
     * @see HttpServletRequest#getHeaders(String)
     */
    public Enumeration getHeaders(String theName)
    {
        return this.request.getHeaders(theName);
    }

    /**
     * @see HttpServletRequest#getHeaderNames()
     */
    public Enumeration getHeaderNames()
    {
        return this.request.getHeaderNames();
    }

    /**
     * @see HttpServletRequest#getScheme()
     */
    public String getScheme()
    {
        return this.request.getScheme();
    }

    /**
     * @see HttpServletRequest#getAuthType()
     */
    public String getAuthType()
    {
        return this.request.getAuthType();
    }

    /**
     * @see HttpServletRequest#getRealPath(String)
     */
    public String getRealPath(String thePath)
    {
        return this.request.getRealPath(thePath);
    }

    /**
     * @see HttpServletRequest#getSession()
     */
    public HttpSession getSession()
    {
        return this.request.getSession();
    }

    /**
     * @see HttpServletRequest#getSession(boolean)
     */
    public HttpSession getSession(boolean isCreate)
    {
        return this.request.getSession(isCreate);
    }

    /**
     * @see HttpServletRequest#getRemoteHost()
     */
    public String getRemoteHost()
    {
        return this.request.getRemoteHost();
    }

    /**
     * @see HttpServletRequest#getReader()
     */
    public BufferedReader getReader() throws IOException
    {
        return this.request.getReader();
    }

    /**
     * @see HttpServletRequest#getContentLength()
     */
    public int getContentLength()
    {
        return this.request.getContentLength();
    }

    /**
     * @see HttpServletRequest#getParameterValues(String)
     */
    public String[] getParameterValues(String theName)
    {
        return this.request.getParameterValues(theName);
    }

    /**
     * @see HttpServletRequest#getContentType()
     */
    public String getContentType()
    {
        return this.request.getContentType();
    }

    /**
     * @see HttpServletRequest#getLocale()
     */
    public Locale getLocale()
    {
        return this.request.getLocale();
    }

    /**
     * @see HttpServletRequest#removeAttribute(String)
     */
    public void removeAttribute(String theName)
    {
        this.request.removeAttribute(theName);
    }

    /**
     * @see HttpServletRequest#getParameter(String)
     */
    public String getParameter(String theName)
    {
        return this.request.getParameter(theName);
    }

    /**
     * @see HttpServletRequest#getInputStream()
     */
    public ServletInputStream getInputStream() throws IOException
    {
        return this.request.getInputStream();
    }

    /**
     * @see HttpServletRequest#getUserPrincipal()
     */
    public Principal getUserPrincipal()
    {
        return this.request.getUserPrincipal();
    }

    /**
     * @see HttpServletRequest#isSecure()
     */
    public boolean isSecure()
    {
        return this.request.isSecure();
    }

    /**
     * @see HttpServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr()
    {
        return this.request.getRemoteAddr();
    }

    /**
     * @see HttpServletRequest#getCharacterEncoding()
     */
    public String getCharacterEncoding()
    {
        return this.request.getCharacterEncoding();
    }

    /**
     * @see HttpServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames()
    {
        return this.request.getParameterNames();
    }

    /**
     * @see HttpServletRequest#getMethod()
     */
    public String getMethod()
    {
        return this.request.getMethod();
    }

    /**
     * @see HttpServletRequest#setAttribute(String, Object)
     */
    public void setAttribute(String theName, Object theAttribute)
    {
        this.request.setAttribute(theName, theAttribute);
    }

    /**
     * @see HttpServletRequest#getAttribute(String)
     */
    public Object getAttribute(String theName)
    {
        return this.request.getAttribute(theName);
    }

    /**
     * @see HttpServletRequest#getIntHeader(String)
     */
    public int getIntHeader(String theName)
    {
        return this.request.getIntHeader(theName);
    }

    /**
     * @see HttpServletRequest#getDateHeader(String)
     */
    public long getDateHeader(String theName)
    {
        return this.request.getDateHeader(theName);
    }

    /**
     * @see HttpServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames()
    {
        return this.request.getAttributeNames();
    }

    /**
     * @see HttpServletRequest#getRemoteUser()
     */
    public String getRemoteUser()
    {
        return this.request.getRemoteUser();
    }

    /**
     * @see HttpServletRequest#getProtocol()
     */
    public String getProtocol()
    {
        return this.request.getProtocol();
    }

    /**
     * @see HttpServletRequest#getRequestedSessionId()
     */
    public String getRequestedSessionId()
    {
        return this.request.getRequestedSessionId();
    }

    /**
     * @see HttpServletRequest#getCookies()
     */
    public Cookie[] getCookies()
    {
        return this.request.getCookies();
    }
}