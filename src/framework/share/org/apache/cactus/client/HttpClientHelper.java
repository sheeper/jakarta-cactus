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
package org.apache.cactus.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.commons.httpclient.Header;

import org.apache.cactus.ServletURL;
import org.apache.cactus.WebRequest;
import org.apache.cactus.client.authentication.AbstractAuthentication;
import org.apache.cactus.util.log.Log;
import org.apache.cactus.util.log.LogService;
import org.apache.cactus.util.ChainedRuntimeException;

/**
 * Helper class to open an HTTP connection to the server redirector and pass
 * to it HTTP parameters, Cookies and HTTP headers.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:Jason.Robertson@acs-inc.com">Jason Robertson</a>
 *
 * @version $Id$
 */
public class HttpClientHelper
{
    /**
     * The LOGGER
     */
    private static final Log LOGGER =
        LogService.getInstance().getLog(HttpClientHelper.class.getName());

    /**
     * The URL that will be used for the HTTP connection.
     */
    private String url;

    // Static initialisations
    static
    {

        // Do not follow redirects (because we are doing unit tests and
        // we need to be able to assert the returned headers, cookies, ...)
        HttpURLConnection.setFollowRedirects(false);

    }

    /**
     * @param theURL the URL that will be used for the HTTP connection.
     */
    public HttpClientHelper(String theURL)
    {
        this.url = theURL;
    }

    /**
     * Connects to the Cactus Redirector using HTTP.
     *
     * @param theRequest the request containing all data to pass to the
     *        server redirector.
     * @return the HTTP Connection used to connect to the redirector.
     * @exception Throwable if an unexpected error occured
     */
    public HttpURLConnection connect(WebRequest theRequest)
        throws Throwable
    {
        URL url = new URL(this.url);

        // Add the parameters that need to be passed as part of the URL
        url = addParametersGet(theRequest, url);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoInput(true);

        // Choose the method that we will use to post data :
        // - If at least one parameter is to be sent in the request body, then
        //   we are doing a POST.
        // - If user data has been specified, then we are doing a POST
        if (theRequest.getParameterNamesPost().hasMoreElements() ||
            (theRequest.getUserData() != null)) {

            connection.setDoOutput(true);
        } else {
            connection.setDoOutput(false);
        }

        connection.setUseCaches(false);

        // Sets the content type
        connection.setRequestProperty("Content-type",
            theRequest.getContentType());

        // Add Authentication headers, if necessary
        AbstractAuthentication authentication = theRequest.getAuthentication();
        if (authentication != null) {
            authentication.configure(connection);
        }

        // Add the other header fields
        addHeaders(theRequest, connection);

        // Add the cookies
        addCookies(theRequest, connection);

        // Add the POST parameters if no user data has been specified (user data
        // overried post parameters)
        if (theRequest.getUserData() != null) {
            addUserData(theRequest, connection);
        } else {
            addParametersPost(theRequest, connection);
        }

        // Log content length
        LOGGER.debug("ContentLength = [" + connection.getContentLength());

        // Open the connection and get the result
        connection.connect();

        return connection;
    }

    /**
     * Add user data in the request body.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theConnection the HTTP connection
     * @exception IOException if we fail to read the user data
     */
    private void addUserData(WebRequest theRequest,
        URLConnection theConnection) throws IOException
    {
        // If no user data, then exit
        if (theRequest.getUserData() == null) {
            return;
        }

        OutputStream out = getConnectionStream(theConnection);
        InputStream stream = theRequest.getUserData();

        byte[] buffer = new byte[2048];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }

        out.close();
    }

    /**
     * Add the HTTP parameters that need to be passed in the query string of
     * the URL.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theURL the URL used to connect to the server redirector.
     * @return the new URL
     * @exception MalformedURLException if the URL is malformed
     */
    private URL addParametersGet(WebRequest theRequest, URL theURL)
        throws MalformedURLException
    {
        // If no parameters, then exit
        if (!theRequest.getParameterNamesGet().hasMoreElements()) {
            return theURL;
        }

        StringBuffer queryString = new StringBuffer();

        Enumeration keys = theRequest.getParameterNamesGet();

        if (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String[] values = theRequest.getParameterValuesGet(key);
            queryString.append(key);
            queryString.append('=');
            queryString.append(URLEncoder.encode(values[0]));
            for (int i = 1; i < values.length; i++) {
                queryString.append('&');
                queryString.append(key);
                queryString.append('=');
                queryString.append(URLEncoder.encode(values[i]));
            }
        }

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String[] values = theRequest.getParameterValuesGet(key);
            for (int i = 0; i < values.length; i++) {
                queryString.append('&');
                queryString.append(key);
                queryString.append('=');
                queryString.append(URLEncoder.encode(values[i]));
            }
        }

        String file = theURL.getFile();

        // Remove the trailing "/" if there is one
        if (file.endsWith("/")) {
            file = file.substring(0, file.length() - 1);
        }

        if (theURL.toString().indexOf("?") > 0) {
            file = file + "&" + queryString.toString();
        } else {
            file = file + "?" + queryString.toString();
        }

        return new URL(theURL.getProtocol(), theURL.getHost(),
            theURL.getPort(), file);
    }

    /**
     * Add the HTTP parameters that need to be passed in the request body.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theConnection the HTTP connection
     */
    private void addParametersPost(WebRequest theRequest,
        URLConnection theConnection)
    {
        // If no parameters, then exit
        if (!theRequest.getParameterNamesPost().hasMoreElements()) {
            return;
        }

        PrintWriter out = new PrintWriter(getConnectionStream(theConnection));
        StringBuffer queryString = new StringBuffer();
        Enumeration keys = theRequest.getParameterNamesPost();

        if (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String[] values = theRequest.getParameterValuesPost(key);
            queryString.append(key);
            queryString.append('=');
            queryString.append(URLEncoder.encode(values[0]));
            for (int i = 1; i < values.length; i++) {
                queryString.append('&');
                queryString.append(key);
                queryString.append('=');
                queryString.append(URLEncoder.encode(values[i]));
            }
        }

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String[] values = theRequest.getParameterValuesPost(key);
            for (int i = 0; i < values.length; i++) {
                queryString.append('&');
                queryString.append(key);
                queryString.append('=');
                queryString.append(URLEncoder.encode(values[i]));
            }
        }

        out.print(queryString.toString());
        out.close();
    }

    /**
     * @param theConnection the HTTP connection
     * @return an output stream to write in the request body
     */
    private OutputStream getConnectionStream(URLConnection theConnection)
    {
        OutputStream out;
        try {
            out = theConnection.getOutputStream();
        } catch (IOException e) {
            // Cannot connect to server, try to explain why ...
            String reason = "Cannot connect to URL [" + theConnection.getURL() +
                "]. Reason : [" + e.getMessage() + "]\r\n";
            reason += "Possible reasons :\r\n";
            reason += "\t- The server is not running,\r\n";
            reason += "\t- The server redirector is not correctly mapped in " +
                "web.xml,\r\n";
            reason += "\t- Something else ... !";

            throw new ChainedRuntimeException(reason);
        }

        return out;
    }

    /**
     * Add the Cookies to the request.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theConnection the HTTP connection
     */
    private void addCookies(WebRequest theRequest,
        URLConnection theConnection)
    {
        // If no Cookies, then exit
        Vector cookies = theRequest.getCookies();
        if (!cookies.isEmpty()) {

            // transform the Cactus cookies into HttpClient cookies
            org.apache.commons.httpclient.Cookie[] httpclientCookies =
                new org.apache.commons.httpclient.Cookie[cookies.size()];
            for (int i = 0; i < cookies.size(); i++) {
                org.apache.cactus.Cookie cactusCookie =
                    (org.apache.cactus.Cookie) cookies.elementAt(i);
                httpclientCookies[i] =
                    new org.apache.commons.httpclient.Cookie(
                        cactusCookie.getDomain(), cactusCookie.getName(),
                        cactusCookie.getValue());
                httpclientCookies[i].setComment(cactusCookie.getComment());
                httpclientCookies[i].setExpiryDate(
                        cactusCookie.getExpiryDate());
                httpclientCookies[i].setPath(cactusCookie.getPath());
                httpclientCookies[i].setSecure(cactusCookie.isSecure());
            }

            // and create the cookie header to send
            Header cookieHeader =
                org.apache.commons.httpclient.Cookie.createCookieHeader(
                    HttpClientHelper.getDomain(theRequest, theConnection),
                    HttpClientHelper.getPath(theRequest, theConnection),
                    httpclientCookies);

            LOGGER.debug("Cookie string = [" + cookieHeader.getValue() +
                "]");

            theConnection.setRequestProperty("Cookie",
                cookieHeader.getValue());
        }
    }

    /**
     * Returns the domain that will be used to send the cookies. If a host
     * was specified using <code>setURL()</code> then the domain will be
     * this host. Otherwise it will be the redirector host.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theConnection the HTTP connection
     * @return the cookie domain to use
     */
    public static String getDomain(WebRequest theRequest,
        URLConnection theConnection)
    {
        String domain;
        ServletURL url = theRequest.getURL();

        if ((url != null) && (url.getHost() != null)) {
            domain = url.getHost();
        } else {
            domain = theConnection.getURL().getHost();
        }

        LOGGER.debug("Cookie validation domain = [" + domain + "]");

        return domain;
    }

    /**
     * Returns the domain that will be used to send the cookies. If a host
     * was specified using <code>setURL()</code> then the domain will be
     * this host. Otherwise it will be the redirector host.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theConnection the HTTP connection
     * @return the cookie domain to use
     */
    public static int getPort(WebRequest theRequest,
        URLConnection theConnection)
    {
        int port;
        ServletURL url = theRequest.getURL();

        if ((url != null) && (url.getHost() != null)) {
            port = url.getPort();
        } else {
            port = theConnection.getURL().getPort();
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
     * redirector path.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theConnection the HTTP connection
     * @return the path to use to decide if a cookie will get sent
     */
    public static String getPath(WebRequest theRequest,
            URLConnection theConnection)
    {
        String path;
        ServletURL url = theRequest.getURL();

        if ((url != null) && (url.getPath() != null)) {
            path = url.getPath();
        } else {

            // We do not use the URL.getPath() API as it was only introduced
            // in JDK 1.3 and we want to retain compatibility with JDK 1.2.
            // Using JDK 1.3, we would have written :
            //      path = theConnection.getURL().getPath();

            String file = theConnection.getURL().getFile();
            if (file != null) {
                int q = file.lastIndexOf('?');
                if (q != -1) {
                    path = file.substring(0, q);
                } else {
                    path = file;
                }
            } else {
                path = null;
            }

        }

        LOGGER.debug("Cookie validation pah = [" + path + "]");

        return path;
    }

    /**
     * Add the Headers to the request.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theConnection the HTTP connection
     */
    private void addHeaders(WebRequest theRequest,
        URLConnection theConnection)
    {
        Enumeration keys = theRequest.getHeaderNames();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String[] values = theRequest.getHeaderValues(key);

            // As the URLConnection.setRequestProperty will overwrite any
            // property already set we have to regroup the multi valued
            // headers into a single header name entry.
            // Question: Is this an implementation bug ? It seems because
            // on the server side, I cannot use the request.getHeaders() (it
            // only returns a single header).

            StringBuffer fullHeaderValue = new StringBuffer(values[0]);
            for (int i = 1; i < values.length; i++) {
                fullHeaderValue.append("," + values[i]);
            }
            theConnection.setRequestProperty(key, fullHeaderValue.toString());

        }
    }

}
