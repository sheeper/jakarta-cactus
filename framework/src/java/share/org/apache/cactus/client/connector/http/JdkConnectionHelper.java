/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
package org.apache.cactus.client.connector.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import java.util.Enumeration;

import org.apache.cactus.WebRequest;
import org.apache.cactus.client.authentication.Authentication;
import org.apache.cactus.configuration.Configuration;
import org.apache.cactus.util.ChainedRuntimeException;
import org.apache.cactus.util.CookieUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of <code>ConnectionHelper</code> using the JDK
 * <code>HttpURLConnection</code> class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:Jason.Robertson@acs-inc.com">Jason Robertson</a>
 *
 * @version $Id$
 */
public class JdkConnectionHelper implements ConnectionHelper
{
    /**
     * The logger
     */
    private static final Log LOGGER = 
        LogFactory.getLog(JdkConnectionHelper.class);

    // Static initialisations
    static
    {
        // Do not follow redirects (because we are doing unit tests and
        // we need to be able to assert the returned headers, cookies, ...)
        HttpURLConnection.setFollowRedirects(false);
    }

    /**
     * The URL that will be used for the HTTP connection.
     */
    private String url;

    /**
     * @param theURL the URL that will be used for the HTTP connection.
     */
    public JdkConnectionHelper(String theURL)
    {
        this.url = theURL;
    }

    /**
     * @see ConnectionHelper#connect(WebRequest, Configuration)
     */
    public HttpURLConnection connect(WebRequest theRequest,
        Configuration theConfiguration) throws Throwable
    {
        URL url = new URL(this.url);

        // Add Authentication headers, if necessary. This is the first
        // step to allow authentication to add extra headers, HTTP parameters,
        // etc.
        Authentication authentication = theRequest.getAuthentication();

        if (authentication != null)
        {
            authentication.configure(theRequest, theConfiguration);
        }

        // Add the parameters that need to be passed as part of the URL
        url = HttpUtil.addHttpGetParameters(theRequest, url);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoInput(true);

        // Choose the method that we will use to post data :
        // - If at least one parameter is to be sent in the request body, then
        //   we are doing a POST.
        // - If user data has been specified, then we are doing a POST
        if (theRequest.getParameterNamesPost().hasMoreElements()
            || (theRequest.getUserData() != null))
        {
            connection.setDoOutput(true);
        }
        else
        {
            connection.setDoOutput(false);
        }

        connection.setUseCaches(false);

        // Sets the content type
        connection.setRequestProperty("Content-type", 
            theRequest.getContentType());

        // Add the other header fields
        addHeaders(theRequest, connection);

        // Add the cookies
        String cookieString = CookieUtil.getCookieString(theRequest, url);

        if (cookieString != null)
        {
            connection.setRequestProperty("Cookie", cookieString);
        }

        // Add the POST parameters if no user data has been specified (user data
        // overried post parameters)
        if (theRequest.getUserData() != null)
        {
            addUserData(theRequest, connection);
        }
        else
        {
            addHttpPostParameters(theRequest, connection);
        }


        // Log content length
        LOGGER.debug("ContentLength = [" + connection.getContentLength() + "]");


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
    private void addUserData(WebRequest theRequest, URLConnection theConnection)
                      throws IOException
    {
        // If no user data, then exit
        if (theRequest.getUserData() == null)
        {
            return;
        }

        OutputStream out = getConnectionStream(theConnection);
        InputStream stream = theRequest.getUserData();

        byte[] buffer = new byte[2048];
        int length;

        while ((length = stream.read(buffer)) != -1)
        {
            out.write(buffer, 0, length);
        }

        out.close();
    }

    /**
     * Add the HTTP parameters that need to be passed in the request body.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theConnection the HTTP connection
     */
    private void addHttpPostParameters(WebRequest theRequest, 
                                   URLConnection theConnection)
    {
        // If no parameters, then exit
        if (!theRequest.getParameterNamesPost().hasMoreElements())
        {
            return;
        }

        PrintWriter out = new PrintWriter(getConnectionStream(theConnection));
        StringBuffer queryString = new StringBuffer();
        Enumeration keys = theRequest.getParameterNamesPost();

        if (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            String[] values = theRequest.getParameterValuesPost(key);

            queryString.append(key);
            queryString.append('=');
            queryString.append(URLEncoder.encode(values[0]));

            for (int i = 1; i < values.length; i++)
            {
                queryString.append('&');
                queryString.append(key);
                queryString.append('=');
                queryString.append(URLEncoder.encode(values[i]));
            }
        }

        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            String[] values = theRequest.getParameterValuesPost(key);

            for (int i = 0; i < values.length; i++)
            {
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

        try
        {
            out = theConnection.getOutputStream();
        }
        catch (IOException e)
        {
            // Cannot connect to server, try to explain why ...
            String reason = "Cannot connect to URL [" + theConnection.getURL()
                + "]. Reason : [" + e.getMessage() + "]\r\n";
            reason += "Possible reasons :\r\n";
            reason += "\t- The server is not running,\r\n";
            reason += ("\t- The server redirector is not correctly mapped in " 
                + "web.xml,\r\n");
            reason += "\t- Something else ... !";

            throw new ChainedRuntimeException(reason);
        }

        return out;
    }

    /**
     * Add the Headers to the request.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theConnection the HTTP connection
     */
    private void addHeaders(WebRequest theRequest, URLConnection theConnection)
    {
        Enumeration keys = theRequest.getHeaderNames();

        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            String[] values = theRequest.getHeaderValues(key);

            // As the URLConnection.setRequestProperty will overwrite any
            // property already set we have to regroup the multi valued
            // headers into a single header name entry.
            // Question: Is this an implementation bug ? It seems because
            // on the server side, I cannot use the request.getHeaders() (it
            // only returns a single header).
            StringBuffer fullHeaderValue = new StringBuffer(values[0]);

            for (int i = 1; i < values.length; i++)
            {
                fullHeaderValue.append("," + values[i]);
            }

            theConnection.setRequestProperty(key, fullHeaderValue.toString());
        }
    }
}
