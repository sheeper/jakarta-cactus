/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.cactus.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import java.lang.reflect.Constructor;

import java.security.Principal;

import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.cactus.ServletURL;
import org.apache.cactus.util.ChainedRuntimeException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Abstract wrapper around {@link HttpServletRequest}. This class provides
 * a common implementation of the wrapper for the different Servlet APIs.
 * This is an implementation that delegates all the call to the
 * {@link HttpServletRequest} object passed in the constructor except for
 * some overidden methods which are use to simulate a URL. This is to be able 
 * to simulate any URL that would have been used to call the test method : if 
 * this was not done, the URL that would be returned (by calling the
 * {@link HttpServletRequest#getRequestURI()} method or others alike) would be 
 * the URL of the Cactus redirector servlet and not a URL that the test case 
 * want to simulate.
 *
 * @version $Id: AbstractHttpServletRequestWrapper.java 292559 2005-09-29 21:36:43Z kenney $
 */
public abstract class AbstractHttpServletRequestWrapper
    implements HttpServletRequest
{
    /**
     * The logger.
     */
    private static final Log LOGGER = 
        LogFactory.getLog(AbstractHttpServletRequestWrapper.class);

    /**
     * The real HTTP request.
     */
    protected HttpServletRequest request;

    /**
     * The URL to simulate.
     */
    protected ServletURL url;

    /**
     * Remote IP address to simulate (if any).
     * @see #setRemoteIPAddress(String)
     */
    protected String remoteIPAddress;

    /**
     * Remote Host name to simulate (if any).
     * @see #setRemoteHostName(String)
     */
    protected String remoteHostName;

    /**
     * Remote user to simulate (if any).
     * @see #setRemoteUser(String)
     */
    protected String remoteUser;

    // New methods not in the interface --------------------------------------

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
     * {@inheritDoc}
     * @see HttpServletRequest#newInstance()
     */
    public static AbstractHttpServletRequestWrapper newInstance(
        HttpServletRequest theOriginalRequest, ServletURL theURL)
    {
        try
        {
            Class clazz = Class.forName(
                "org.apache.cactus.server.HttpServletRequestWrapper");
            Object[] args = new Object[] {theOriginalRequest, theURL};

            Constructor constructor = clazz.getConstructor(new Class[] {
                HttpServletRequest.class, ServletURL.class });

            return (AbstractHttpServletRequestWrapper) constructor.
               newInstance(args);
        }
        catch (Throwable t)
        {
            throw new ChainedRuntimeException(
                "Failed to create HttpServletRequestWrapper", t);
        }
    }


    /**
     * @return the original request object
     */
    public HttpServletRequest getOriginalRequest()
    {
        return this.request;
    }

    /**
     * Simulates the remote IP address (ie the client IP address).
     *
     * @param theRemoteIPAddress the simulated IP address in string format.
     *        Exemple : "127.0.0.1"
     */
    public void setRemoteIPAddress(String theRemoteIPAddress)
    {
        this.remoteIPAddress = theRemoteIPAddress;
    }

    /**
     * Simulates the remote host name(ie the client host name).
     *
     * @param theRemoteHostName the simulated host name in string format.
     *        Exemple : "atlantis"
     */
    public void setRemoteHostName(String theRemoteHostName)
    {
        this.remoteHostName = theRemoteHostName;
    }

    /**
     * Sets the remote user name to simulate.
     *
     * @param theRemoteUser the simulated remote user name
     */
    public void setRemoteUser(String theRemoteUser)
    {
        this.remoteUser = theRemoteUser;
    }

    // Modified methods ------------------------------------------------------

    /**
     * @return the context path from the simulated URL or the real context path
     *         if a simulation URL has not been defined. The real context path
     *         will be returned if the context path defined in the simulated 
     *         URL has a null value.
     */
    public String getContextPath()
    {
        String result = this.request.getContextPath();

        if ((this.url != null) && (this.url.getContextPath() != null))
        {
            result = this.url.getContextPath();
            LOGGER.debug("Using simulated context : [" + result + "]");
        }

        return result;
    }

    /**
     * @return the path info from the simulated URL or the real path info
     *         if a simulation URL has not been defined.
     */
    public String getPathInfo()
    {
        String result;

        if (this.url != null)
        {
            result = this.url.getPathInfo();
            LOGGER.debug("Using simulated PathInfo : [" + result + "]");
        }
        else
        {
            result = this.request.getPathInfo();
        }

        return result;
    }

    /**
     * @return the server name from the simulated URL or the real server name
     *         if a simulation URL has not been defined. If the server name
     *         defined in the simulation URL is null, return the real server
     *         name.
     */
    public String getServerName()
    {
        String result = this.request.getServerName();

        if ((this.url != null) && (this.url.getHost() != null))
        {
            result = this.url.getHost();
            LOGGER.debug("Using simulated server name : [" + result + "]");
        }

        return result;
    }

    /**
     * @return the server port number from the simulated URL or the real server
     *         port number if a simulation URL has not been defined. If no
     *         port is defined in the simulation URL, then port 80 is returned.
     *         If the server name has been defined with a null value in
     *         in the simulation URL, return the real server port. 
     */
    public int getServerPort()
    {
        int result = this.request.getServerPort();

        if ((this.url != null) && (this.url.getServerName() != null))
        {
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
        String result;

        if (this.url != null)
        {
            result = getContextPath()
                + ((getServletPath() == null) ? "" : getServletPath())
                + ((getPathInfo() == null) ? "" : getPathInfo());

            LOGGER.debug("Using simulated request URI : [" + result + "]");
        }
        else
        {
            result = this.request.getRequestURI();
        }

        return result;
    }

    /**
     * @return the servlet path from the simulated URL or the real servlet path
     *         if a simulation URL has not been defined. The real servlet path
     *         will be returned if the servlet path defined in the simulated 
     *         URL has a null value.
     */
    public String getServletPath()
    {
        String result = this.request.getServletPath();

        if ((this.url != null) && (this.url.getServletPath() != null))
        {
            result = this.url.getServletPath();
            LOGGER.debug("Using simulated servlet path : [" + result + "]");
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

        if ((this.url != null) && (this.url.getPathInfo() != null))
        {
            String pathInfo = this.url.getPathInfo();
            
            // If getRealPath returns null then getPathTranslated should also
            // return null (see section SRV.4.5 of the Servlet 2.3 spec).
            if (this.request.getRealPath("/") == null)
            {
                pathTranslated = null;
            }
            else
            {
                // Compute the translated path using the root real path
                String newPathInfo = (pathInfo.startsWith("/")
                    ? pathInfo.substring(1) : pathInfo);

                if (this.request.getRealPath("/").endsWith("/"))
                {
                    pathTranslated = this.request.getRealPath("/")
                        + newPathInfo.replace('/', File.separatorChar);
                }
                else
                {
                    pathTranslated = this.request.getRealPath("/")
                        + File.separatorChar + newPathInfo.replace('/', 
                        File.separatorChar);
                }
            }
        }
        else
        {
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
        String result;

        if (this.url != null)
        {
            result = this.url.getQueryString();
            LOGGER.debug("Using simulated query string : [" + result + "]");
        }
        else
        {
            result = this.request.getQueryString();
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
        if (thePath == null)
        {
            return null;
        }

        RequestDispatcher dispatcher = null;
        String fullPath;

        // The spec says that the path can be relative, in which case it will
        // be relative to the request. So for relative paths, we need to take
        // into account the simulated URL (ServletURL).
        if (thePath.startsWith("/"))
        {
            fullPath = thePath;
        }
        else
        {
            String pI = getPathInfo();

            if (pI == null)
            {
                fullPath = catPath(getServletPath(), thePath);
            }
            else
            {
                fullPath = catPath(getServletPath() + pI, thePath);
            }

            if (fullPath == null)
            {
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
        while (thePath.startsWith("../"))
        {
            if (theLookupPath.length() > 0)
            {
                index = theLookupPath.lastIndexOf("/");
                theLookupPath = theLookupPath.substring(0, index);
            }
            else
            {
                // More ..'s than dirs, return null
                return null;
            }

            index = thePath.indexOf("../") + 3;
            thePath = thePath.substring(index);
        }

        return theLookupPath + "/" + thePath;
    }

    /**
     * @return the simulated remote IP address if any or the real one.
     *
     * @see HttpServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr()
    {
        String remoteIPAddress;

        if (this.remoteIPAddress != null)
        {
            remoteIPAddress = this.remoteIPAddress;
        }
        else
        {
            remoteIPAddress = this.request.getRemoteAddr();
        }

        return remoteIPAddress;
    }

    /**
     * @return the simulated remote host name if any or the real one.
     *
     * @see HttpServletRequest#getRemoteHost()
     */
    public String getRemoteHost()
    {
        String remoteHostName;

        if (this.remoteHostName != null)
        {
            remoteHostName = this.remoteHostName;
        }
        else
        {
            remoteHostName = this.request.getRemoteHost();
        }

        return remoteHostName;
    }

    /**
     * @return the simulated remote user name if any or the real one.
     *
     * @see HttpServletRequest#getRemoteUser()
     */
    public String getRemoteUser()
    {
        String remoteUser;

        if (this.remoteUser != null)
        {
            remoteUser = this.remoteUser;
        }
        else
        {
            remoteUser = this.request.getRemoteUser();
        }

        return remoteUser;
    }

    // Not modified methods --------------------------------------------------

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#isRequestedSessionIdFromURL()
     */
    public boolean isRequestedSessionIdFromURL()
    {
        return this.request.isRequestedSessionIdFromURL();
    }

    /**
     * {@inheritDoc} 
     * @see HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    public boolean isRequestedSessionIdFromUrl()
    {
        return this.request.isRequestedSessionIdFromURL();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#isUserInRole(String)
     */
    public boolean isUserInRole(String theRole)
    {
        return this.request.isUserInRole(theRole);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid()
    {
        return this.request.isRequestedSessionIdValid();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    public boolean isRequestedSessionIdFromCookie()
    {
        return this.request.isRequestedSessionIdFromCookie();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getLocales()
     */
    public Enumeration getLocales()
    {
        return this.request.getLocales();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getHeader(String)
     */
    public String getHeader(String theName)
    {
        return this.request.getHeader(theName);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getHeaders(String)
     */
    public Enumeration getHeaders(String theName)
    {
        return this.request.getHeaders(theName);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getHeaderNames()
     */
    public Enumeration getHeaderNames()
    {
        return this.request.getHeaderNames();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getScheme()
     */
    public String getScheme()
    {
        return this.request.getScheme();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getAuthType()
     */
    public String getAuthType()
    {
        return this.request.getAuthType();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getRealPath(String)
     */
    public String getRealPath(String thePath)
    {
        return this.request.getRealPath(thePath);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getSession()
     */
    public HttpSession getSession()
    {
        return this.request.getSession();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getSession(boolean)
     */
    public HttpSession getSession(boolean isCreate)
    {
        return this.request.getSession(isCreate);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getReader()
     */
    public BufferedReader getReader() throws IOException
    {
        return this.request.getReader();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getContentLength()
     */
    public int getContentLength()
    {
        return this.request.getContentLength();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getParameterValues(String)
     */
    public String[] getParameterValues(String theName)
    {
        return this.request.getParameterValues(theName);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getContentType()
     */
    public String getContentType()
    {
        return this.request.getContentType();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getLocale()
     */
    public Locale getLocale()
    {
        return this.request.getLocale();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#removeAttribute(String)
     */
    public void removeAttribute(String theName)
    {
        this.request.removeAttribute(theName);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getParameter(String)
     */
    public String getParameter(String theName)
    {
        return this.request.getParameter(theName);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getInputStream()
     */
    public ServletInputStream getInputStream() throws IOException
    {
        return this.request.getInputStream();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getUserPrincipal()
     */
    public Principal getUserPrincipal()
    {
        return this.request.getUserPrincipal();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#isSecure()
     */
    public boolean isSecure()
    {
        return this.request.isSecure();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getCharacterEncoding()
     */
    public String getCharacterEncoding()
    {
        return this.request.getCharacterEncoding();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames()
    {
        return this.request.getParameterNames();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getMethod()
     */
    public String getMethod()
    {
        return this.request.getMethod();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#setAttribute(String, Object)
     */
    public void setAttribute(String theName, Object theAttribute)
    {
        this.request.setAttribute(theName, theAttribute);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getAttribute(String)
     */
    public Object getAttribute(String theName)
    {
        return this.request.getAttribute(theName);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getIntHeader(String)
     */
    public int getIntHeader(String theName)
    {
        return this.request.getIntHeader(theName);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getDateHeader(String)
     */
    public long getDateHeader(String theName)
    {
        return this.request.getDateHeader(theName);
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames()
    {
        return this.request.getAttributeNames();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getRequestedSessionId()
     */
    public String getRequestedSessionId()
    {
        return this.request.getRequestedSessionId();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getCookies()
     */
    public Cookie[] getCookies()
    {
        return this.request.getCookies();
    }

    /**
     * {@inheritDoc}
     * @see HttpServletRequest#getProtocol()
     */
    public String getProtocol()
    {
        return this.request.getProtocol();
    }
}
