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

import org.apache.cactus.WebRequest;
import org.apache.cactus.Cookie;
import org.apache.commons.httpclient.Header;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Common helper methods for implementing <code>ConnectionHelper</code>. These
 * methods are common to any implementation.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public abstract class AbstractConnectionHelper implements ConnectionHelper
{
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
    protected URL addParametersGet(WebRequest theRequest, URL theURL)
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
     * @return the cookie string which will be added as a HTTP "Cookie" header
     *         or null if no cookie has been set
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theUrl the URL to connect to
     */
    public String getCookieString(WebRequest theRequest, URL theUrl)
    {
        // If no Cookies, then exit
        Vector cookies = theRequest.getCookies();
        if (!cookies.isEmpty()) {

            // transform the Cactus cookies into HttpClient cookies
            org.apache.commons.httpclient.Cookie[] httpclientCookies =
                new org.apache.commons.httpclient.Cookie[cookies.size()];
            for (int i = 0; i < cookies.size(); i++) {
                Cookie cactusCookie = (Cookie) cookies.elementAt(i);

                // If no domain has been specified, use a default one
                String domain;
                if (cactusCookie.getDomain() == null) {
                    domain = Cookie.getCookieDomain(theRequest,
                        theUrl.getHost());
                } else {
                    domain = cactusCookie.getDomain();
                }

                // If not path has been specified , use a default one
                String path;
                if (cactusCookie.getPath() == null) {
                    path = Cookie.getCookiePath(theRequest, theUrl.getFile());
                } else {
                    path = cactusCookie.getPath();
                }

                httpclientCookies[i] =
                    new org.apache.commons.httpclient.Cookie(
                        domain, cactusCookie.getName(),
                        cactusCookie.getValue());

                httpclientCookies[i].setComment(cactusCookie.getComment());
                httpclientCookies[i].setExpiryDate(
                        cactusCookie.getExpiryDate());
                httpclientCookies[i].setPath(path);
                httpclientCookies[i].setSecure(cactusCookie.isSecure());
            }

            // and create the cookie header to send
            Header cookieHeader =
                org.apache.commons.httpclient.Cookie.createCookieHeader(
                    Cookie.getCookieDomain(theRequest, theUrl.getHost()),
                    Cookie.getCookiePath(theRequest, theUrl.getFile()),
                    httpclientCookies);

            return cookieHeader.getValue();
        }

        return null;
    }

}
