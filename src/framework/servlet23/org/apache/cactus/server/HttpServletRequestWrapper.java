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
public class HttpServletRequestWrapper implements HttpServletRequest
{
    /**
     * The real HTTP request
     */
    private HttpServletRequest request;

    /**
     * The URL to simulate
     */
    private ServletURL url;

    /**
     * The logger
     */
    private static Log logger =
        LogService.getInstance().
        getLog(HttpServletRequestWrapper.class.getName());

    /**
     * Construct an <code>HttpServletRequest</code> instance that delegates
     * it's method calls to the request object passed as parameter and that
     * uses the URL passed as parameter to simulate a URL from which the request
     * would come from.
     *
     * @param theRequest the real HTTP request
     * @param theURL     the URL to simulate or <code>null</code> if none
     */
    public HttpServletRequestWrapper(HttpServletRequest theRequest,
        ServletURL theURL)
    {
        this.request = theRequest;
        this.url = theURL;
    }

    public HttpServletRequest getOriginalRequest()
    {
        return this.request;
    }

    public boolean isUserInRole(String theRole)
    {
        return this.request.isUserInRole(theRole);
    }

    public boolean isRequestedSessionIdValid()
    {
        return this.request.isRequestedSessionIdValid();
    }
    public boolean isRequestedSessionIdFromUrl()
    {
        return this.request.isRequestedSessionIdFromUrl();
    }

    public boolean isRequestedSessionIdFromURL()
    {
        return this.request.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromCookie()
    {
        return this.request.isRequestedSessionIdFromCookie();
    }

    public Principal getUserPrincipal()
    {
        return this.request.getUserPrincipal();
    }

    public HttpSession getSession(boolean isCreate)
    {
        return this.request.getSession(isCreate);
    }

    public HttpSession getSession()
    {
        return this.request.getSession();
    }

    /**
     * @return the servlet path from the simulated URL or the real servlet path
     *         if a simulation URL has not been defined.
     */
    public String getServletPath()
    {
        this.logger.entry("getServletPath()");

        String result = this.request.getServletPath();

        if (this.url != null) {
            result = this.url.getServletPath();
            this.logger.debug("Using simulated servlet path : [" + result +
                "]");
        }

        this.logger.exit("getServletPath");
        return result;
    }

    public String getRequestedSessionId()
    {
        return this.request.getRequestedSessionId();
    }

    public StringBuffer getRequestURL()
    {
        return this.request.getRequestURL();
    }

    /**
     * @return the URI from the simulated URL or the real URI
     *         if a simulation URL has not been defined.
     */
    public String getRequestURI()
    {
        this.logger.entry("getRequestURI()");

        String result = this.request.getRequestURI();

        if (this.url != null) {

            result = getContextPath() + 
                ((getServletPath() == null) ? "" : getServletPath()) + 
                ((getPathInfo() == null) ? "" : getPathInfo());

            this.logger.debug("Using simulated request URI : [" + result + "]");
        }

        this.logger.exit("getRequestURI");
        return result;
    }

    public String getRemoteUser()
    {
        return this.request.getRemoteUser();
    }

    /**
     * @return the query string from the simulated URL or the real query
     *         string if a simulation URL has not been defined.
     */
    public String getQueryString()
    {
        this.logger.entry("getQueryString()");

        String result = this.request.getQueryString();

        if (this.url != null) {
            result = this.url.getQueryString();
            this.logger.debug("Using simulated query string : [" + result +
                "]");
        }

        this.logger.exit("getQueryString");
        return result;
    }

    public String getPathTranslated()
    {
        return this.request.getPathTranslated();
    }

    /**
     * @return the path info from the simulated URL or the real path info
     *         if a simulation URL has not been defined.
     */
    public String getPathInfo()
    {
        this.logger.entry("getPathInfo()");

        String result = this.request.getPathInfo();

        if (this.url != null) {
            result = this.url.getPathInfo();
            this.logger.debug("Using simulated PathInfo : [" + result + "]");
        }

        this.logger.exit("getPathInfo");
        return result;
    }

    public String getMethod()
    {
        return this.request.getMethod();
    }

    public int getIntHeader(String theName)
    {
        return this.request.getIntHeader(theName);
    }

    public Enumeration getHeaders(String theName)
    {
        return this.request.getHeaders(theName);
    }

    public Enumeration getHeaderNames()
    {
        return this.request.getHeaderNames();
    }

    public String getHeader(String theName)
    {
        return this.request.getHeader(theName);
    }

    public long getDateHeader(String theName)
    {
        return this.request.getDateHeader(theName);
    }

    public Cookie[] getCookies()
    {
        return this.request.getCookies();
    }

    /**
     * @return the context path from the simulated URL or the real context path
     *         if a simulation URL has not been defined.
     */
    public String getContextPath()
    {
        this.logger.entry("getContextPath()");

        String result = this.request.getContextPath();

        if (this.url != null) {
            if (this.url.getContextPath() != null) {
                result = this.url.getContextPath();
                this.logger.debug("Using simulated context : [" + result + "]");
            }
        }

        this.logger.exit("getContextPath");
        return result;
    }

    public String getAuthType()
    {
        return this.request.getAuthType();
    }

    public void setCharacterEncoding(String env)
        throws UnsupportedEncodingException
    {
        this.request.setCharacterEncoding(env);
    }

    public void setAttribute(String theName, Object theAttribute)
    {
        this.request.setAttribute(theName, theAttribute);
    }

    public void removeAttribute(String theName)
    {
        this.request.removeAttribute(theName);
    }

    public boolean isSecure()
    {
        return this.request.isSecure();
    }

    /**
     * @return the server port number from the simulated URL or the real server
     *         port number if a simulation URL has not been defined. If not
     *         port is defined, then port 80 is returned.
     */
    public int getServerPort()
    {
        this.logger.entry("getServerPort()");

        int result = this.request.getServerPort();

        if (this.url != null) {
            result = (this.url.getPort() == -1) ? 80 : this.url.getPort();
            this.logger.debug("Using simulated server port : [" + result + "]");
        }

        this.logger.exit("getServerPort");
        return result;
    }

    /**
     * @return the server name from the simulated URL or the real server name
     *         if a simulation URL has not been defined.
     */
    public String getServerName()
    {
        this.logger.entry("getServerName()");

        String result = this.request.getServerName();

        if (this.url != null) {
            if (this.url.getServerName() != null) {
                result = this.url.getHost();
                this.logger.debug("Using simulated server name : [" + result +
                    "]");
            }
        }

        this.logger.exit("getServerName");
        return result;
    }

    public String getScheme()
    {
        return this.request.getScheme();
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
        this.logger.entry("getRequestDispatcher([" + thePath + "])");

        // I hate it, but we have to write some logic here ! Ideally we
        // shouldn't have to do this as it is supposed to be done by the servlet
        // engine. However as we are simulating the request URL, we have to
        // provide it ... This is where we can see the limitation of Cactus
        // (it has to mock some parts of the servlet engine) !

        if (thePath == null) {
            this.logger.exit("getRequestDispatcher");
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
                this.logger.exit("getRequestDispatcher");
                return null;
            }

        }
                
        this.logger.debug("Computed full path : [" + fullPath + "]");

        dispatcher = new RequestDispatcherWrapper(
            this.request.getRequestDispatcher(fullPath));

        this.logger.exit("getRequestDispatcher");
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

    public String getRemoteHost()
    {
        return this.request.getRemoteHost();
    }

    public String getRemoteAddr()
    {
        return this.request.getRemoteAddr();
    }

    public String getRealPath(String thePath)
    {
        return this.request.getRealPath(thePath);
    }

    public BufferedReader getReader() throws IOException
    {
        return this.request.getReader();
    }

    public String getProtocol()
    {
        return this.request.getProtocol();
    }

    public String[] getParameterValues(String theName)
    {
        return this.request.getParameterValues(theName);
    }

    public Enumeration getParameterNames()
    {
        return this.request.getParameterNames();
    }

    public Map getParameterMap()
    {
        return this.request.getParameterMap();
    }

    public String getParameter(String theName)
    {
        return this.request.getParameter(theName);
    }

    public Enumeration getLocales()
    {
        return this.request.getLocales();
    }

    public Locale getLocale()
    {
        return this.request.getLocale();
    }

    public ServletInputStream getInputStream() throws IOException
    {
        return this.request.getInputStream();
    }

    public String getContentType()
    {
        return this.request.getContentType();
    }

    public int getContentLength()
    {
        return this.request.getContentLength();
    }

    public String getCharacterEncoding()
    {
        return this.request.getCharacterEncoding();
    }

    public Enumeration getAttributeNames()
    {
        return this.request.getAttributeNames();
    }

    public Object getAttribute(String theName)
    {
        return this.request.getAttribute(theName);
    }

}
