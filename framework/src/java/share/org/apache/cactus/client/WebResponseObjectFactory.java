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
package org.apache.cactus.client;

import java.lang.reflect.Method;

import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.apache.cactus.Request;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;
import org.apache.cactus.internal.client.ClientException;

/**
 * Constructs Web response objects. Supports both Cactus
 * {@link org.apache.cactus.WebResponse} and HttpUnit 
 * <code>com.meterware.httpunit.WebResponse</code> response object creation.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
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
     * @exception ClientException if it failes to create a HttpClient
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
