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
import org.apache.commons.cactus.util.log.*;

/**
 * Encapsulation class for the Servlet 2.2 API <code>HttpServletRequest</code>.
 * This is an implementation that delegates all the call to the
 * <code>HttpServletRequest</code> object passed in the constructor except for
 * some overridden methods which are use to simulate a URL. This is to be able to
 * simulate any URL that would have been used to call the test method : if this
 * was not done, the URL that would be returned (by calling the
 * <code>getRequestURI()</code> method or others alike) would be the URL of the
 * server redirector servlet or JSP and not a URL that the test case want to
 * simulate.
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
     * The logger
     */
    private static Log m_Logger =
        LogService.getInstance().getLog(HttpServletRequestWrapper.class.getName());

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

    public boolean isRequestedSessionIdFromURL()
    {
        return m_Request.isRequestedSessionIdFromURL();
    }

    public Enumeration getLocales()
    {
        return m_Request.getLocales();
    }

    public String getHeader(String theName)
    {
        return m_Request.getHeader(theName);
    }

    /**
     * @return the context path from the simulated URL or the real context path
     *         if a simulation URL has not been defined.
     */
    public String getContextPath()
    {
        m_Logger.entry("getContextPath()");

        String result = m_Request.getContextPath();

        if (m_URL != null) {
            if (m_URL.getContextPath() != null) {
                result = m_URL.getContextPath();
                m_Logger.debug("Using simulated context : [" + result + "]");
            }
        }

        m_Logger.exit("getContextPath");
        return result;
    }

    public String getScheme()
    {
        return m_Request.getScheme();
    }

    /**
     * @return the path info from the simulated URL or the real path info
     *         if a simulation URL has not been defined.
     */
    public String getPathInfo()
    {
        m_Logger.entry("getPathInfo()");

        String result = m_Request.getPathInfo();

        if (m_URL != null) {
            result = m_URL.getPathInfo();
            m_Logger.debug("Using simulated PathInfo : [" + result + "]");
        }

        m_Logger.exit("getPathInfo");
        return result;
    }

    public String getAuthType()
    {
        return m_Request.getAuthType();
    }

    /**
     * @return the server name from the simulated URL or the real server name
     *         if a simulation URL has not been defined.
     */
    public String getServerName()
    {
        m_Logger.entry("getServerName()");

        String result = m_Request.getServerName();

        if (m_URL != null) {
            if (m_URL.getServerName() != null) {
                result = m_URL.getHost();
                m_Logger.debug("Using simulated server name : [" + result + "]");
            }
        }

        m_Logger.exit("getServerName");
        return result;
    }

    public String getRealPath(String thePath)
    {
        return m_Request.getRealPath(thePath);
    }

    public HttpSession getSession()
    {
        return m_Request.getSession();
    }

    public HttpSession getSession(boolean isCreate)
    {
        return m_Request.getSession(isCreate);
    }

    public String getRemoteHost()
    {
        return m_Request.getRemoteHost();
    }

    public Enumeration getHeaderNames()
    {
        return m_Request.getHeaderNames();
    }

    public boolean isUserInRole(String theRole)
    {
        return m_Request.isUserInRole(theRole);
    }

    /**
     * @return the server port number from the simulated URL or the real server
     *         port number if a simulation URL has not been defined. If not
     *         port is defined, then port 80 is returned.
     */
    public int getServerPort()
    {
        m_Logger.entry("getServerPort()");

        int result = m_Request.getServerPort();

        if (m_URL != null) {
            result = (m_URL.getPort() == -1) ? 80 : m_URL.getPort();
            m_Logger.debug("Using simulated server port : [" + result + "]");
        }

        m_Logger.exit("getServerPort");
        return result;
    }

    public BufferedReader getReader() throws IOException
    {
        return m_Request.getReader();
    }

    public int getContentLength()
    {
        return m_Request.getContentLength();
    }

    /**
     * @return the URI from the simulated URL or the real URI
     *         if a simulation URL has not been defined.
     */
    public String getRequestURI()
    {
        m_Logger.entry("getRequestURI()");

        String result = m_Request.getRequestURI();

        if (m_URL != null) {

            result = getContextPath() + 
                ((getServletPath() == null) ? "" : getServletPath()) + 
                ((getPathInfo() == null) ? "" : getPathInfo());

            m_Logger.debug("Using simulated request URI : [" + result + "]");
        }

        m_Logger.exit("getRequestURI");
        return result;
    }

    public String[] getParameterValues(String theName)
    {
        return m_Request.getParameterValues(theName);
    }

    public boolean isRequestedSessionIdFromUrl()
    {
        return m_Request.isRequestedSessionIdFromUrl();
    }

    public String getContentType()
    {
        return m_Request.getContentType();
    }

    public Locale getLocale()
    {
        return m_Request.getLocale();
    }

    public void removeAttribute(String theName)
    {
        m_Request.removeAttribute(theName);
    }

    public String getParameter(String theName)
    {
        return m_Request.getParameter(theName);
    }

    /**
     * @return the servlet path from the simulated URL or the real servlet path
     *         if a simulation URL has not been defined.
     */
    public String getServletPath()
    {
        m_Logger.entry("getServletPath()");

        String result = m_Request.getServletPath();

        if (m_URL != null) {
            result = m_URL.getServletPath();
            m_Logger.debug("Using simulated servlet path : [" + result + "]");
        }

        m_Logger.exit("getServletPath");
        return result;
    }

    public boolean isRequestedSessionIdFromCookie()
    {
        return m_Request.isRequestedSessionIdFromCookie();
    }

    public ServletInputStream getInputStream() throws IOException
    {
        return m_Request.getInputStream();
    }

    public Principal getUserPrincipal()
    {
        return m_Request.getUserPrincipal();
    }

    public boolean isSecure()
    {
        return m_Request.isSecure();
    }

    public String getPathTranslated()
    {
        return m_Request.getPathTranslated();
    }

    public String getRemoteAddr()
    {
        return m_Request.getRemoteAddr();
    }

    public String getCharacterEncoding()
    {
        return m_Request.getCharacterEncoding();
    }

    public Enumeration getParameterNames()
    {
        return m_Request.getParameterNames();
    }

    public String getMethod()
    {
        return m_Request.getMethod();
    }

    public void setAttribute(String theName, Object theAttribute)
    {
        m_Request.setAttribute(theName, theAttribute);
    }

    public Object getAttribute(String theName)
    {
        return m_Request.getAttribute(theName);
    }

    public int getIntHeader(String theName)
    {
        return m_Request.getIntHeader(theName);
    }

    public boolean isRequestedSessionIdValid()
    {
        return m_Request.isRequestedSessionIdValid();
    }

    /**
     * @return the query string from the simulated URL or the real query
     *         string if a simulation URL has not been defined.
     */
    public String getQueryString()
    {
        m_Logger.entry("getQueryString()");

        String result = m_Request.getQueryString();

        if (m_URL != null) {
            result = m_URL.getQueryString();
            m_Logger.debug("Using simulated query string : [" + result + "]");
        }

        m_Logger.exit("getQueryString");
        return result;
    }

    public long getDateHeader(String theName)
    {
        return m_Request.getDateHeader(theName);
    }

    public Enumeration getAttributeNames()
    {
        return m_Request.getAttributeNames();
    }

    public String getRemoteUser()
    {
        return m_Request.getRemoteUser();
    }

    public String getProtocol()
    {
        return m_Request.getProtocol();
    }

    public Enumeration getHeaders(String theName)
    {
        return m_Request.getHeaders(theName);
    }

    public String getRequestedSessionId()
    {
        return m_Request.getRequestedSessionId();
    }

    /**
     * @return a wrapped request dispatcher instead of the real one, so that
     *         forward() and include() calls will use the wrapped dispatcher
     *         passing it the *original* request [this is needed for some
     *         servlet engine like Tomcat 3.x which do not support the new
     *         mechanism introduced by Servlet 2.3 Filters].
     */
    public RequestDispatcher getRequestDispatcher(String thePath)
    {
        m_Logger.entry("getRequestDispatcher([" + thePath + "])");

        // I hate it, but we have to write some logic here ! Ideally we
        // shouldn't have to do this as it is supposed to be done by the servlet
        // engine. However as we are simulating the request URL, we have to
        // provide it ... This is where we can see the limitation of Cactus
        // (it has to mock some parts of the servlet engine) !

        if (thePath == null) {
            m_Logger.exit("getRequestDispatcher");
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
                m_Logger.exit("getRequestDispatcher");
                return null;
            }
        }
                
        m_Logger.debug("Computed full path : [" + fullPath + "]");

        dispatcher = new RequestDispatcherWrapper(
            m_Request.getRequestDispatcher(fullPath));

        m_Logger.exit("getRequestDispatcher");
        return dispatcher;
    }

    /**
     * Will concatenate 2 paths, dealing with ..
     * ( /a/b/c + d = /a/b/d, /a/b/c + ../d = /a/d ). Code borrowed from 
     * Tomcat 3.2.2 !
     *
     * @return null if error occurs
     */
    private String catPath(String lookupPath, String path)
    {
    	// Cut off the last slash and everything beyond
	    int index = lookupPath.lastIndexOf("/");
	    lookupPath = lookupPath.substring(0, index);
	
	    // Deal with .. by chopping dirs off the lookup path
	    while (path.startsWith("../")) { 
	        if (lookupPath.length() > 0) {
		        index = lookupPath.lastIndexOf("/");
		        lookupPath = lookupPath.substring(0, index);
	        } else {
    		// More ..'s than dirs, return null
	    	return null;
	        }
	    
    	    index = path.indexOf("../") + 3;
	        path = path.substring(index);
	    }
	
	    return lookupPath + "/" + path;
    }

    public Cookie[] getCookies()
    {
        return m_Request.getCookies();
    }

}
