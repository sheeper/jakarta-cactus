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
package org.apache.commons.cactus.util;

import java.net.*;
import java.util.*;
import java.io.*;

/**
 * Cactus utility classes to help assert returned results from server side
 * code.
 *
 * @version @version@
 */
public class AssertUtils
{
    /**
     * @param theConnection the connection object used to connect to the server
     *                      redirector.
     * @return the servlet output stream bytes as a string.
     */
    public static String getResponseAsString(HttpURLConnection theConnection) throws IOException
    {
        StringBuffer sb = new StringBuffer();
        BufferedReader input = new BufferedReader(new InputStreamReader(theConnection.getInputStream()));
        String str;
        while (null != ((str = input.readLine()))) {
            sb.append(str);
        }
        input.close ();

        return sb.toString();
    }

    /**
     * Extract the cookies from a HTTP connection.
     *
     * @param theConnection the HTTP connection from which to extract server
     *                      returned cookies.
     * @return a hashtable of <code>ClientCookie</code> objects.
     */
    public static Hashtable getCookies(HttpURLConnection theConnection)
    {
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
        String headerName = theConnection.getHeaderFieldKey(0);
        String headerValue = theConnection.getHeaderField(0);
        for (int i = 1; (headerName != null) || (headerValue != null); i++) {

            if ((headerName != null) && headerName.equals("Set-Cookie")) {

                // Parse the cookie definition
                Vector clientCookies = parseSetCookieHeader(headerValue);

                if (clientCookies.isEmpty()) {
                    continue;
                }

                // Check if the cookie name already exist in the hashtable.
                // If so, then add it to the vector of cookies for that name.

                String name = ((ClientCookie)clientCookies.elementAt(0)).getName();

                if (cookies.containsKey(name)) {
                    Vector cookieValues = (Vector)cookies.get(name);
                    cookieValues.addAll(clientCookies);
                } else {
                    Vector cookieValues = new Vector();
                    cookieValues.addAll(clientCookies);
                    cookies.put(name, cookieValues);
                }
            }

            headerName = theConnection.getHeaderFieldKey(i);
            headerValue = theConnection.getHeaderField(i);

        }

        return cookies;
    }

    /**
     * Parse a single "Set-Cookie" header.
     *
     * @return a vector og <code>ClientCookie</code> objects containing the
     *         parsed values from the "Set-Cookie" header.
     */
    protected static Vector parseSetCookieHeader(String theHeaderValue)
    {
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
                System.err.println("Bad 'Set-Cookie' syntax, missing '=' [" + param + "]");
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
                    System.err.println("Bad 'Set-Cookie' syntax, bad name [" + param + "]");
                    continue;
                }

            }

            // Create the client cookie
            ClientCookie cookie = new ClientCookie(name, value, comment,
                domain, maxAge, path, isSecure, version);

            cookies.add(cookie);
        }

        return cookies;
    }

}
