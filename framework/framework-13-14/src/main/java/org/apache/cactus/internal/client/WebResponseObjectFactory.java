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
package org.apache.cactus.internal.client;

import java.lang.reflect.Method;

import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.apache.cactus.Request;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;
import org.apache.cactus.spi.client.ResponseObjectFactory;

/**
 * Constructs Web response objects. Supports both Cactus
 * {@link org.apache.cactus.WebResponse} and HttpUnit 
 * <code>com.meterware.httpunit.WebResponse</code> response object creation.
 *
 * @version $Id: WebResponseObjectFactory.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class WebResponseObjectFactory implements ResponseObjectFactory
{
    /**
     * Connection object used to connect to the Cactus server side. Required
     * to create the response object.  
     */
    private HttpURLConnection connection;
    
    /**
     * @param theConnection the connection object used to connect to the 
     *        Cactus server side
     */
    public WebResponseObjectFactory(HttpURLConnection theConnection)
    {
        this.connection = theConnection;
    }
       
    /**
     * {@inheritDoc}
     * @see ResponseObjectFactory#getResponseObject
     */
    public Object getResponseObject(String theClassName, Request theRequest) 
        throws ClientException
    {
        Object responseObject;

        // Is it a Http Unit WebResponse ?
        if (theClassName.equals("com.meterware.httpunit.WebResponse"))
        {
            responseObject = createHttpUnitWebResponse(this.connection);

            // Is it a Cactus WebResponse ?
        }
        else if (theClassName.equals("org.apache.cactus.WebResponse"))
        {
            responseObject = new WebResponse((WebRequest) theRequest, 
                this.connection);

            // Is it an old HttpURLConnection (deprecated) ?
        }
        else if (theClassName.equals("java.net.HttpURLConnection"))
        {
            responseObject = this.connection;
        }
        else
        {
            // Else it is an error ...
            throw new ClientException("Invalid parameter type [" + theClassName
                + "]");
        }

        return responseObject;
    }

    /**
     * Create a HttpUnit <code>WebResponse</code> object by reflection (so
     * that we don't need the HttpUnit jar for users who are not using
     * the HttpUnit endXXX() signature).
     *
     * @param theConnection the HTTP connection that was used when connecting
     *        to the server side and which now contains the returned HTTP
     *        response that we will pass to HttpUnit so that it can construt
     *        a <code>com.meterware.httpunit.WebResponse</code> object.
     * @return a HttpUnit <code>WebResponse</code> object
     * @throws ClientException if it failes to create a HttpClient
     *            WebResponse object for any reason
     */
    private Object createHttpUnitWebResponse(HttpURLConnection theConnection)
        throws ClientException
    {
        Object webResponse;

        try
        {
            Class responseClass = 
                Class.forName("com.meterware.httpunit.WebResponse");
            Method method = responseClass.getMethod("newResponse", 
                new Class[] {URLConnection.class});

            webResponse = method.invoke(null, new Object[] {theConnection});
        }
        catch (Exception e)
        {
            throw new ClientException("Error calling "
                + "[public static com.meterware.httpunit.WebResponse "
                + "com.meterware.httpunit.WebResponse.newResponse("
                + "java.net.URLConnection) throws java.io.IOException]", e);
        }

        return webResponse;
    }
}
