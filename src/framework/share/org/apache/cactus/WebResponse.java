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
package org.apache.commons.cactus;

import java.net.*;
import java.io.*;
import java.util.*;

import org.apache.commons.cactus.*;
import org.apache.commons.cactus.util.*;
import org.apache.commons.cactus.util.log.*;

/**
 * Default web response implementation that provides a minimal
 * API for asserting returned output stream from the server side. For more
 * complex assertions, use an <code>com.meterware.httpunit.WebResponse</code>
 * instead as parameter of your <code>endXXX()</code> methods.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class WebResponse
{
    /**
     * The logger
     */
    private static Log logger =
        LogService.getInstance().getLog(WebResponse.class.getName());

    /**
     * The connection object that was used to call the URL
     */
    private HttpURLConnection connection;

    /**
     * @param theConnection the original <code>HttpURLConnection</code> used
     *        to call the URL
     */
    public WebResponse(HttpURLConnection theConnection)
    {
        this.connection = theConnection;
    }

    /**
     * @return the original <code>HttpURLConnection</code> used to call the
     *         URL
     */
    public HttpURLConnection getConnection()
    {
        return this.connection;
    }

    /**
     * @return the text of the response (excluding headers) as a string.
     */
    public String getText()
    {
        StringBuffer sb = new StringBuffer();

        try {
            BufferedReader input = new BufferedReader(
                new InputStreamReader(this.connection.getInputStream()));
            char[] buffer = new char[2048];
            int nb;
            while (-1 != (nb = input.read(buffer, 0, 2048))) {
                sb.append(buffer, 0, nb);
            }
            input.close();
        } catch (IOException e) {
            throw new ChainedRuntimeException(e);
        }

        return sb.toString();
    }

    /**
     * @return the text of the response (excluding headers) as an array of
     *         strings (each string is a separate line from the output stream).
     */
    public String[] getTextAsArray()
    {
        Vector lines = new Vector();

        try {
            BufferedReader input = new BufferedReader(
                new InputStreamReader(this.connection.getInputStream()));
            String str;
            while (null != (str = input.readLine())) {
                lines.addElement(str);
            }
            input.close ();
        } catch (IOException e) {
            throw new ChainedRuntimeException(e);
        }

        // Fixme: I don't know why but if I don't use this dummy stuff I get a
        // ClassCastException !
        String[] dummy = new String[lines.size()];
        return (String[])(lines.toArray(dummy));
    }

    /**
     * @return a buffered input stream for reading the response data.
     **/
    public InputStream getInputStream()
    {
        try {
            return this.connection.getInputStream();
        } catch (IOException e) {
            throw new ChainedRuntimeException(e);
        }
    }

    /**
     * @return the returned cookies as a hashtable of <code>ClientCookie</code>
     *         objects indexed on the cookie name
     */
    public Hashtable getCookies()
    {
        this.logger.entry("getCookies()");

        // We conform to the RFC 2109 :
        //
        //   The syntax for the Set-Cookie response header is
        //
        //   set-cookie      =       "Set-Cookie:" cookies
        //   cookies         =       1#cookie
        //   cookie          =       NAME "=" VALUE *(";" cookie-av)
        //   NAME            =       attr
        //   VALUE           =       value
        //   cookie-av       =       "Comment" "=" value
        //                   |       "Domain" "=" value
        //                   |       "Max-Age" "=" value
        //                   |       "Path" "=" value
        //                   |       "Secure"
        //                   |       "Version" "=" 1*DIGIT

        Hashtable cookies = new Hashtable();

        // There can be several headers named "Set-Cookie", so loop through all
        // the headers, looking for cookies
        String headerName = this.connection.getHeaderFieldKey(0);
        String headerValue = this.connection.getHeaderField(0);
        for (int i = 1; (headerName != null) || (headerValue != null); i++) {

            this.logger.debug("Header name  = [" + headerName + "]");
            this.logger.debug("Header value = [" + headerValue + "]");

            if ((headerName != null) && headerName.equals("Set-Cookie")) {

                // Parse the cookie definition
                Vector clientCookies = parseSetCookieHeader(headerValue);

                if (clientCookies.isEmpty()) {
                    continue;
                }

                // Check if the cookie name already exist in the hashtable.
                // If so, then add it to the vector of cookies for that name.

                String name =
                    ((ClientCookie)clientCookies.elementAt(0)).getName();

                if (cookies.containsKey(name)) {
                    Vector cookieValues = (Vector)cookies.get(name);
                    cookieValues.addAll(clientCookies);
                } else {
                    Vector cookieValues = new Vector();
                    cookieValues.addAll(clientCookies);
                    cookies.put(name, cookieValues);
                }
            }

            headerName = this.connection.getHeaderFieldKey(i);
            headerValue = this.connection.getHeaderField(i);

        }

        this.logger.exit("getCookies");
        return cookies;
    }

    /**
     * Parse a single "Set-Cookie" header.
     *
     * @return a vector og <code>ClientCookie</code> objects containing the
     *         parsed values from the "Set-Cookie" header.
     */
    protected Vector parseSetCookieHeader(String theHeaderValue)
    {
        this.logger.entry("parseSetCookieHeader([" + theHeaderValue + "])");

        String name;
        String value;
        String comment = null;
        String path = null;
        String domain = null;
        long maxAge = 0;
        boolean isSecure = false;
        float version = 1;

        Vector cookies = new Vector();

        // Find all cookies, they are comma-separated
        StringTokenizer stCookies = new StringTokenizer(theHeaderValue, ",");
        while (stCookies.hasMoreTokens()) {
            String singleCookie = stCookies.nextToken();
            singleCookie = singleCookie.trim();

            // Parse a single cookie

            // Extract cookie values, they are semi-colon separated
            StringTokenizer stParams = new StringTokenizer(singleCookie, ";");

            // The first parameter is always NAME = VALUE
            String param = stParams.nextToken();
            param = param.trim();

            int pos = param.indexOf("=");
            if (pos < 0) {
                this.logger.warn("Bad 'Set-Cookie' syntax, missing '=' [" +
                    param + "], ignoring it !");
                continue;
            }

            name = param.substring(0, pos).trim();
            value = param.substring(pos + 1).trim();

            while (stParams.hasMoreTokens()) {
                param = stParams.nextToken();
                param = param.trim();

                String left;
                String right;

                // Tokenize on "="
                pos = param.indexOf("=");
                if (pos < 0) {
                    left = param;
                    right = "";
                } else {
                    left = param.substring(0, pos).trim();
                    right = param.substring(pos + 1).trim();
                }

                // Is it a comment ?
                if (left.equalsIgnoreCase("comment")) {
                    comment = right;
                } else if (left.equalsIgnoreCase("domain")) {
                    domain = right;
                } else if (left.equalsIgnoreCase("max-age")) {
                    maxAge = Long.parseLong(right);
                } else if (left.equalsIgnoreCase("path")) {
                    path = right;
                } else if (left.equalsIgnoreCase("secure")) {
                    isSecure = true;
                } else if (left.equalsIgnoreCase("version")) {
                    version = Float.parseFloat(right);
                } else {
                    this.logger.warn("Bad 'Set-Cookie' syntax, bad name [" +
                        param + "], ignoring it !");
                    continue;
                }

            }

            // Create the client cookie
            ClientCookie cookie = new ClientCookie(name, value, comment,
                domain, maxAge, path, isSecure, version);

            cookies.add(cookie);
        }

        this.logger.exit("parseSetCookieHeader");
        return cookies;
    }

}
