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
package org.apache.commons.cactus.client;

import java.util.*;
import java.net.*;
import java.io.*;

import junit.framework.*;

import org.apache.commons.cactus.*;
import org.apache.commons.cactus.util.log.*;

/**
 * Helper class to open an HTTP connection to the server redirector and pass
 * to it HTTP parameters, Cookies and HTTP headers.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
class HttpClientHelper
{
    /**
     * The logger
     */
    private static Log logger =
        LogService.getInstance().getLog(HttpClientHelper.class.getName());

    /**
     * The URL that will be used for the HTTP connection.
     */
    private String url;

    /**
     * @param theURL the URL that will be used for the HTTP connection.
     */
    public HttpClientHelper(String theURL)
    {
        this.logger.entry("HttpClientHelper([" + theURL + "])");

        this.url = theURL;

        this.logger.exit("HttpClientHelper");
    }

    /**
     * Add the parameters to the request using a GET method.
     *
     * @param theRequest the request containing all data to pass to the server
     *                   redirector.
     * @param theURL the URL used to connect to the server redirector.
     * @return the new URL
     */
    private URL addParametersUsingGet(ServletTestRequest theRequest, URL theURL)
        throws Throwable
    {
        // If no parameters, then exit
        if (!theRequest.getParameterNames().hasMoreElements()) {
            return theURL;
        }

        StringBuffer queryString = new StringBuffer();

        Enumeration keys = theRequest.getParameterNames();

        if (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String[] values = theRequest.getParameterValues(key);
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
            String key = (String)keys.nextElement();
            String[] values = theRequest.getParameterValues(key);
            for (int i = 0; i < values.length; i++) {
                queryString.append('&');
                queryString.append(key);
                queryString.append('=');
                queryString.append(URLEncoder.encode(values[i]));
            }
        }

        String file;
        if (theURL.toString().indexOf("?") > 0) {
            file = theURL.getFile() + "&" + queryString.toString();
        } else {
            file = theURL.getFile() + "?" + queryString.toString();
        }

        return new URL(theURL.getProtocol(), theURL.getHost(),
            theURL.getPort(), file);
    }

    /**
     * Add the parameters to the request using a POST method.
     *
     * @param theRequest the request containing all data to pass to the server
     *                   redirector.
     * @param theConnection the HTTP connection
     */
    private void addParametersUsingPost(ServletTestRequest theRequest,
        URLConnection theConnection) throws Throwable
    {
        // If no parameters, then exit
        if (!theRequest.getParameterNames().hasMoreElements()) {
            return;
        }

        PrintWriter out;
        try {
            out = new PrintWriter(theConnection.getOutputStream());
        } catch (ConnectException e) {

            // Cannot connect to server, try to explain why ...
            String reason = "Cannot connect to URL [" + theConnection.getURL() +
                "]. Reason : [" + e.getMessage() + "]\r\n";
            reason += "Possible reasons :\r\n";
            reason += "\t- The server is not running,\r\n";
            reason += "\t- The server redirector is not correctly mapped in " +
                "web.xml,\r\n";
            reason += "\t- Something else ... !";

            throw new Exception(reason);
        }

        StringBuffer queryString = new StringBuffer();

        Enumeration keys = theRequest.getParameterNames();

        if (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String[] values = theRequest.getParameterValues(key);
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
            String key = (String)keys.nextElement();
            String[] values = theRequest.getParameterValues(key);
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
     * Add the Cookies to the request.
     *
     * @param theRequest the request containing all data to pass to the server
     *                   redirector.
     * @param theConnection the HTTP connection
     */
    private void addCookies(ServletTestRequest theRequest,
        URLConnection theConnection)
    {
        this.logger.entry("addCookies(...)");

        // If no Cookies, then exit
        if (!theRequest.getCookieNames().hasMoreElements()) {
            this.logger.exit("addCookies");
            return;
        }

        Enumeration keys = theRequest.getCookieNames();

        StringBuffer cookieString = new StringBuffer();

        // Format of a Cookie string is (according to RFC 2109) :
        //   cookie          =       "Cookie:" cookie-version
        //                           1*((";" | ",") cookie-value)
        //   cookie-value    =       NAME "=" VALUE [";" path] [";" domain]
        //   cookie-version  =       "$Version" "=" value
        //   NAME            =       attr
        //   VALUE           =       value
        //   path            =       "$Path" "=" value
        //   domain          =       "$Domain" "=" value

        // Write the cookie version first
        cookieString.append("$Version=1");

        // Possible improvement here: to add support for :
        // - path
        // - domain

        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String value = (String)theRequest.getCookieValue(key);
            cookieString.append(';');
            cookieString.append(key);
            cookieString.append('=');
            cookieString.append(value);
        }

        this.logger.debug("Cookie string = [" + cookieString + "]");

        theConnection.setRequestProperty("Cookie", cookieString.toString());

        this.logger.exit("addCookies");
    }

    /**
     * Add the Headers to the request.
     *
     * @param theRequest the request containing all data to pass to the server
     *                   redirector.
     * @param theConnection the HTTP connection
     */
    private void addHeaders(ServletTestRequest theRequest,
        URLConnection theConnection)
    {
        Enumeration keys = theRequest.getHeaderNames();

        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
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

    /**
     * Calls the Servlet Redirector.
     *
     * @param theRequest the request containing all data to pass to the
     *                   server redirector.
     *
     * @exception Throwable if an unexpected error occured
     */
    public HttpURLConnection connect(ServletTestRequest theRequest)
        throws Throwable
    {
        this.logger.entry("connect(" + theRequest + ")");

        URL url = new URL(this.url);

        // If the method is GET, add the parameters to the URL
        if (theRequest.getMethod().equals(theRequest.GET_METHOD)) {
            url = addParametersUsingGet(theRequest, url);
        }

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        connection.setDoInput(true);

        // Choose the method that we will use to post data
        if (theRequest.getMethod().equals(theRequest.POST_METHOD)) {
            connection.setDoOutput(true);
        } else {
            connection.setDoOutput(false);
        }

        connection.setUseCaches(false);

        // Add the other header fields
        addHeaders(theRequest, connection);

        // Add the cookies
        addCookies(theRequest, connection);

        // Add the POST parameters
        if (theRequest.getMethod().equals(theRequest.POST_METHOD)) {
            addParametersUsingPost(theRequest, connection);
        }

        // Open the connection and get the result
        connection.connect();

        this.logger.exit("connect");
        return connection;
    }

}
