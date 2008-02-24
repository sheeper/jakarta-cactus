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
package org.apache.cactus.internal.client.connector.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;

import org.apache.cactus.WebRequest;

/**
 * Utility methods to manipulate HTTP requests.
 *
 * @version $Id: HttpUtil.java 238991 2004-05-22 11:34:50Z vmassol $
 * @since 1.5
 */
public class HttpUtil
{
    /**
     * Add HTTP GET parameters to the URL passed as parameter.
     *
     * @param theRequest the request containing the HTTP GET parameters to add
     * @param theURL the URL that will be enriched with the HTTP GET parameters
     * @return the enriched URL
     * @exception MalformedURLException if the URL is malformed
     */
    public static URL addHttpGetParameters(WebRequest theRequest, URL theURL)
        throws MalformedURLException
    {
        // If no parameters, then exit
        if (!theRequest.getParameterNamesGet().hasMoreElements())
        {
            return theURL;
        }

        StringBuffer queryString = new StringBuffer();

        Enumeration keys = theRequest.getParameterNamesGet();

        if (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            String[] values = theRequest.getParameterValuesGet(key);

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
            String[] values = theRequest.getParameterValuesGet(key);

            for (int i = 0; i < values.length; i++)
            {
                queryString.append('&');
                queryString.append(key);
                queryString.append('=');
                queryString.append(URLEncoder.encode(values[i]));
            }
        }

        String file = theURL.getFile();

        // Remove the trailing "/" if there is one
        if (file.endsWith("/"))
        {
            file = file.substring(0, file.length() - 1);
        }

        if (theURL.toString().indexOf("?") > 0)
        {
            file = file + "&" + queryString.toString();
        }
        else
        {
            file = file + "?" + queryString.toString();
        }

        return new URL(theURL.getProtocol(), theURL.getHost(), 
            theURL.getPort(), file);
    }
}
