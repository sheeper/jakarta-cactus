/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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
package org.apache.cactus.internal.util;

import java.net.URL;
import java.util.Vector;

import org.apache.cactus.Cookie;
import org.apache.cactus.ServletURL;
import org.apache.cactus.WebRequest;
import org.apache.cactus.internal.client.ClientException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods to manipulate cookies and transform Cactus cookie objects 
 * to HttpClient cookie objects.
 *
 * @version $Id$
 * @since 1.5
 */
public class CookieUtil
{
    /**
     * The logger
     */
    private static final Log LOGGER = LogFactory.getLog(CookieUtil.class);

    /**
     * Returns the domain that will be used to send the cookies. If a host
     * was specified using <code>setURL()</code> then the domain will be
     * this host. Otherwise it will be the real redirector host.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theRealHost the real host to which we are connecting to. We will
     *        use it if no simulation host has been specified.
     * @return the cookie domain to use
     */
    public static String getCookieDomain(WebRequest theRequest, 
        String theRealHost)
    {
        String domain;
        ServletURL url = theRequest.getURL();

        if ((url != null) && (url.getHost() != null))
        {
            domain = url.getHost();
        }
        else
        {
            domain = theRealHost;
        }

        LOGGER.debug("Cookie validation domain = [" + domain + "]");

        return domain;
    }

    /**
     * Returns the port that will be used to send the cookies. If a port
     * was specified using <code>setURL()</code> then the port sent will be
     * this port. Otherwise it will be the real redirector port.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theRealPort the real port to which we are connecting to. We will
     *        use it if no simulation port has been specified.
     * @return the cookie domain to use
     */
    public static int getCookiePort(WebRequest theRequest, int theRealPort)
    {
        int port;
        ServletURL url = theRequest.getURL();

        if ((url != null) && (url.getHost() != null))
        {
            port = url.getPort();
        }
        else
        {
            port = theRealPort;
        }

        LOGGER.debug("Cookie validation port = [" + port + "]");

        return port;
    }

    /**
     * Returns the path that will be used to validate if a cookie will be
     * sent or not. The algorithm is as follows : if the cookie path is not
     * set (i.e. null) then the cookie is always sent (provided the domain
     * is right). If the cookie path is set, the cookie is sent only if
     * the request path starts with the same string as the cookie path. If
     * <code>setURL()</code> has been called, return the path it has been
     * set to (context + servletPath + pathInfo). Otherwise return the
     * real redirector path.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theRealPath the real path to which we are connecting to. We will
     *        use it if no simulation path has been specified.
     * @return the path to use to decide if a cookie will get sent
     */
    public static String getCookiePath(WebRequest theRequest, 
        String theRealPath)
    {
        String path;
        ServletURL url = theRequest.getURL();

        if ((url != null) && (url.getPath() != null))
        {
            path = url.getPath();
        }
        else
        {
            String file = theRealPath;

            if (file != null)
            {
                int q = file.lastIndexOf('?');

                if (q != -1)
                {
                    path = file.substring(0, q);
                }
                else
                {
                    path = file;
                }
            }
            else
            {
                path = null;
            }
        }

        LOGGER.debug("Cookie validation path = [" + path + "]");

        return path;
    }

    /**
     * Create a Commons-HttpClient cookie from a Cactus cookie, with information
     * from the web request and the URL.
     * 
     * @param theRequest The request
     * @param theUrl The URL
     * @param theCactusCookie The Cactus Cookie object
     * @return The HttpClient cookie
     */
    public static org.apache.commons.httpclient.Cookie createHttpClientCookie(
        WebRequest theRequest, URL theUrl, Cookie theCactusCookie)
    {
        // If no domain has been specified, use a default one
        String domain;
        if (theCactusCookie.getDomain() == null)
        {
            domain = CookieUtil.getCookieDomain(theRequest, theUrl.getHost());
        }
        else
        {
            domain = theCactusCookie.getDomain();
        }

        // If not path has been specified , use a default one
        String path;
        if (theCactusCookie.getPath() == null)
        {
            path = CookieUtil.getCookiePath(theRequest, theUrl.getFile());
        }
        else
        {
            path = theCactusCookie.getPath();
        }

        // Assemble the HttpClient cookie
        org.apache.commons.httpclient.Cookie httpclientCookie =
            new org.apache.commons.httpclient.Cookie(domain,
                theCactusCookie.getName(), theCactusCookie.getValue());
        httpclientCookie.setComment(theCactusCookie.getComment());
        httpclientCookie.setExpiryDate(
            theCactusCookie.getExpiryDate());
        httpclientCookie.setPath(path);
        httpclientCookie.setSecure(theCactusCookie.isSecure());
        
        return httpclientCookie;
    }

    /**
     * Transforms an array of Cactus cookies into an array of Commons-HttpClient
     * cookies, using information from the request and URL.
     * 
     * @param theRequest The request
     * @param theUrl The URL
     * @return The array of HttpClient cookies
     */
    public static org.apache.commons.httpclient.Cookie[] 
        createHttpClientCookies(WebRequest theRequest, URL theUrl)
    {
        Vector cactusCookies = theRequest.getCookies();
        
        // transform the Cactus cookies into HttpClient cookies
        org.apache.commons.httpclient.Cookie[] httpclientCookies = 
            new org.apache.commons.httpclient.Cookie[cactusCookies.size()];

        for (int i = 0; i < cactusCookies.size(); i++)
        {
            Cookie cactusCookie = (Cookie) cactusCookies.elementAt(i);
            httpclientCookies[i] = CookieUtil.createHttpClientCookie(
                theRequest, theUrl, cactusCookie);
        }

        return httpclientCookies;
    }

    /**
     * Create a HttpClient {@link Header} for cookies that matches
     * the domain and path.
     * 
     * @param theDomain the cookie domain to match
     * @param thePath the cookie path to match
     * @param theCookies the list of potential cookies
     * @return the HttpClient {@link Header} containing the matching 
     *         cookies
     * @throws ClientException if no cookie was matching the domain
     *         and path
     */
    public static Header createCookieHeader(String theDomain, String thePath,
        org.apache.commons.httpclient.Cookie[] theCookies)
        throws ClientException
    {
        Header cookieHeader = null;
        
        // separate domain into host and port
        int port = 80;
        String host = theDomain;
        int portIndex = theDomain.indexOf(":");
        if (portIndex != -1)
        {
            host = host.substring(0, portIndex);
            port = Integer.parseInt(theDomain.substring(portIndex + 1));
        }

        CookieSpec matcher = CookiePolicy.getDefaultSpec();
        org.apache.commons.httpclient.Cookie[] cookies =
            matcher.match(host, port, thePath, false, theCookies);
        if ((cookies != null) && (cookies.length > 0))
        {
            cookieHeader = matcher.formatCookieHeader(cookies);
        }
        
        if (cookieHeader == null)
        {
            throw new ClientException("Failed to create Cookie header for ["
                + "domain = [" + theDomain + ", path = [" + thePath
                + ", cookies = [" + theCookies + "]]. Turn on HttpClient "
                + "logging for more information about the error"); 
        }
        
        return cookieHeader;
    }

    /**
     * @return the cookie string which will be added as a HTTP "Cookie" header
     *         or null if no cookie has been set
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theUrl the URL to connect to
     * @throws ClientException if an error occurred when creating the cookie
     *         string
     */
    public static String getCookieString(WebRequest theRequest, URL theUrl)
        throws ClientException
    {
        // If no Cookies, then exit
        Vector cookies = theRequest.getCookies();

        if (!cookies.isEmpty())
        {
            // transform the Cactus cookies into HttpClient cookies
            org.apache.commons.httpclient.Cookie[] httpclientCookies = 
                CookieUtil.createHttpClientCookies(theRequest, theUrl);

            // and create the cookie header to send
            Header cookieHeader = createCookieHeader(
                CookieUtil.getCookieDomain(theRequest, theUrl.getHost()), 
                CookieUtil.getCookiePath(theRequest, theUrl.getFile()), 
                httpclientCookies);

            return cookieHeader.getValue();
        }

        return null;
    }
}
