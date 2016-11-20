/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cactus.internal.client.connector.http;

import org.apache.cactus.WebRequest;
import org.apache.cactus.client.authentication.Authentication;
import org.apache.cactus.internal.configuration.Configuration;
import org.apache.cactus.internal.util.CookieUtil;
import org.apache.cactus.internal.util.UrlUtil;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Implementation of <code>ConnectionHelper</code> using Jakarta Commons
 * HttpClient.
 *
 * @version $Id: HttpClientConnectionHelper.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class HttpClientConnectionHelper implements ConnectionHelper
{
    /**
     * The <code>HttpMethod</code> used to connect to the HTTP server. It is
     * either a <code>GetMethod</code> or a <code>PostMethod</code>.
     */
    private HttpMethod method;

    /**
     * The URL that will be used for the HTTP connection.
     */
    private String url;

    /**
     * @param theURL the URL that will be used for the HTTP connection.
     */
    public HttpClientConnectionHelper(String theURL)
    {
        this.url = theURL;
    }

    /**
     * {@inheritDoc}
     * @see ConnectionHelper#connect(WebRequest, Configuration)
     */
    public HttpURLConnection connect(WebRequest theRequest, 
        Configuration theConfiguration) throws Throwable
    {
        URL url = new URL(this.url);

        HttpState state = new HttpState();

        // Choose the method that we will use to post data :
        // - If at least one parameter is to be sent in the request body, then
        //   we are doing a POST.
        // - If user data has been specified, then we are doing a POST
        if (theRequest.getParameterNamesPost().hasMoreElements()
            || (theRequest.getUserData() != null))
        {
            this.method = new PostMethod();
        }
        else
        {
            this.method = new GetMethod();
        }

        // Add Authentication headers, if necessary. This is the first
        // step to allow authentication to add extra headers, HTTP parameters,
        // etc.
        Authentication authentication = theRequest.getAuthentication();

        if (authentication != null)
        {
            authentication.configure(state, this.method, theRequest, 
                theConfiguration);
        }

        // Add the parameters that need to be passed as part of the URL
        url = HttpUtil.addHttpGetParameters(theRequest, url);

        this.method.setFollowRedirects(false);
        this.method.setPath(UrlUtil.getPath(url));
        this.method.setQueryString(UrlUtil.getQuery(url));

        // Sets the content type
        this.method.setRequestHeader("Content-type", 
            theRequest.getContentType());

        // Add the other header fields
        addHeaders(theRequest);

        // Add the POST parameters if no user data has been specified (user data
        // overried post parameters)
        if (theRequest.getUserData() != null)
        {
            addUserData(theRequest);
        }
        else
        {
            addHttpPostParameters(theRequest);
        }

        // Add the cookies to the state
        state.addCookies(CookieUtil.createHttpClientCookies(theRequest, 
            url));

        // Open the connection and get the result
        HttpClient client = new HttpClient();
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost(url.getHost(), url.getPort(),
            Protocol.getProtocol(url.getProtocol()));
        client.setState(state);
        client.executeMethod(hostConfiguration, this.method);

        // Wrap the HttpClient method in a java.net.HttpURLConnection object
        return new org.apache.commons.httpclient.util.HttpURLConnection(
            this.method, url);
    }
    
    /**
     * Add the HTTP parameters that need to be passed in the request body.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     */
    private void addHttpPostParameters(WebRequest theRequest)
    {
        // If no parameters, then exit
        if (!theRequest.getParameterNamesPost().hasMoreElements())
        {
            return;
        }

        Enumeration keys = theRequest.getParameterNamesPost();
        List parameters = new ArrayList();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            String[] values = theRequest.getParameterValuesPost(key);
            for (int i = 0; i < values.length; i++)
            {
                parameters.add(new NameValuePair(key, values[i]));
            }
        }
        ((PostMethod) this.method).setRequestBody(
            (NameValuePair[]) parameters.toArray(
                new NameValuePair[parameters.size()]));
    }

    /**
     * Add the Headers to the request.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     */
    private void addHeaders(WebRequest theRequest)
    {
        Enumeration keys = theRequest.getHeaderNames();

        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            String[] values = theRequest.getHeaderValues(key);

            StringBuffer fullHeaderValue = new StringBuffer(values[0]);

            for (int i = 1; i < values.length; i++)
            {
                fullHeaderValue.append("," + values[i]);
            }

            this.method.addRequestHeader(key, fullHeaderValue.toString());
        }
    }

    /**
     * Add user data in the request body.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @exception IOException if we fail to read the user data
     */
    private void addUserData(WebRequest theRequest) throws IOException
    {
        // If no user data, then exit
        if (theRequest.getUserData() == null)
        {
            return;
        }

        ((PostMethod) this.method).setRequestBody(theRequest.getUserData());
    }
}
