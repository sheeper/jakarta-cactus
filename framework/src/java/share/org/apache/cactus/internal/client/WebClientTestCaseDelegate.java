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
package org.apache.cactus.internal.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.net.HttpURLConnection;

import junit.framework.Test;

import org.apache.cactus.RequestDirectives;
import org.apache.cactus.WebRequest;
import org.apache.cactus.client.ClientException;
import org.apache.cactus.client.WebResponseObjectFactory;
import org.apache.cactus.client.connector.http.DefaultHttpClient;
import org.apache.cactus.configuration.Configuration;
import org.apache.cactus.configuration.WebConfiguration;

/**
 * Delegator extension to support test cases using the HTTP protocol. It adds 
 * support for end methods (as they are dependent on the protocol used, which 
 * is HTTP here).
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class WebClientTestCaseDelegate extends ClientTestCaseDelegate
{
    /**
     * @param theDelegatedTest the test we are delegating for
     * @param theWrappedTest the test being wrapped by this delegator (or null 
     *        if none)
     * @param theConfiguration the configuration to use 
     */
    public WebClientTestCaseDelegate(Test theDelegatedTest, 
        Test theWrappedTest, Configuration theConfiguration)
    {
        super(theDelegatedTest, theWrappedTest, theConfiguration);
    }

    /**
     * Call the global end method. This is the method that is called after
     * each test if it exists. It is called on the client side only.
     *
     * @param theRequest the request data that were used to open the
     *        connection.
     * @param theConnection the <code>HttpURLConnection</code> that was used
     *        to open the connection to the redirection servlet. The response
     *        codes, headers, cookies can be checked using the get methods of
     *        this object.
     * @param theMethodName the name of the end method to call
     * @param theResponse the Response object if it exists. Can be null in
     *        which case it is created from the HttpURLConnection
     * @return the created WebReponse object (either Cactus or HttpClient)
     * @exception Throwable any error that occurred when calling the end method
     *            for the current test case.
     */
    private Object callGenericEndMethod(WebRequest theRequest, 
        HttpURLConnection theConnection, String theMethodName, 
        Object theResponse) throws Throwable
    {
        Method methodToCall = null;
        Object paramObject = null;

        Method[] methods = getWrappedTest().getClass().getMethods();

        for (int i = 0; i < methods.length; i++)
        {
            if (methods[i].getName().equals(theMethodName))
            {
                // Check return type
                if (!methods[i].getReturnType().getName().equals("void"))
                {
                    fail("The method [" + methods[i].getName()
                        + "] should return void and not ["
                        + methods[i].getReturnType().getName() + "]");
                }

                // Check if method is public
                if (!Modifier.isPublic(methods[i].getModifiers()))
                {
                    fail("Method [" + methods[i].getName()
                        + "] should be declared public");
                }

                // Check parameters
                Class[] parameters = methods[i].getParameterTypes();

                // Verify only one parameter is defined
                if (parameters.length != 1)
                {
                    fail("The method [" + methods[i].getName()
                        + "] must only have a single parameter");
                }

                paramObject = theResponse;

                if (paramObject == null)
                {
                    try
                    {
                        paramObject = new WebResponseObjectFactory()
                            .getResponseObject(parameters[0].getName(), 
                            theRequest, theConnection);
                    }
                    catch (ClientException e)
                    {
                        throw new ClientException("The method ["
                            + methods[i].getName() 
                            + "] has a bad parameter of type ["
                            + parameters[0].getName() + "]", e);
                    }
                }

                // Has a method to call already been found ?
                if (methodToCall != null)
                {
                    fail("There can only be one method ["
                        + methods[i].getName() + "] per test case. "
                        + "Test case [" + this.getCurrentTestName()
                        + "] has two at least !");
                }

                methodToCall = methods[i];
            }
        }

        if (methodToCall != null)
        {
            try
            {
                methodToCall.invoke(getWrappedTest(), 
                    new Object[] {paramObject});
            }
            catch (InvocationTargetException e)
            {
                e.fillInStackTrace();
                throw e.getTargetException();
            }
            catch (IllegalAccessException e)
            {
                e.fillInStackTrace();
                throw e;
            }
        }

        return paramObject;
    }

    /**
     * Call the client tear down up method if it exists.
     *
     * @param theRequest the request data that were used to open the
     *                   connection.
     * @param theConnection the <code>HttpURLConnection</code> that was used
     *        to open the connection to the redirection servlet. The response
     *        codes, headers, cookies can be checked using the get methods of
     *        this object.
     * @param theResponse the Response object if it exists. Can be null in
     *        which case it is created from the HttpURLConnection
     * @exception Throwable any error that occurred when calling the method
     */
    protected void callClientGlobalEnd(WebRequest theRequest, 
        HttpURLConnection theConnection, Object theResponse) throws Throwable
    {
        callGenericEndMethod(theRequest, theConnection, 
            CLIENT_GLOBAL_END_METHOD, theResponse);
    }

    /**
     * Call the test case end method
     *
     * @param theRequest the request data that were used to open the
     *                   connection.
     * @param theConnection the <code>HttpURLConnection</code> that was used
     *        to open the connection to the redirection servlet. The response
     *        codes, headers, cookies can be checked using the get methods of
     *        this object.
     * @return the created WebReponse object (either Cactus or HttpClient)
     * @exception Throwable any error that occurred when calling the end method
     *         for the current test case.
     */
    public Object callEndMethod(WebRequest theRequest, 
        HttpURLConnection theConnection) throws Throwable
    {
        return callGenericEndMethod(theRequest, theConnection, 
            getEndMethodName(), null);
    }

    /**
     * Runs a test case. This method is overriden from the JUnit
     * <code>TestCase</code> class in order to seamlessly call the
     * Cactus redirection servlet.
     *
     * @exception Throwable if any error happens during the execution of
     *            the test
     */
    public void runTest() throws Throwable
    {
        runGenericTest(new DefaultHttpClient(
            (WebConfiguration) getConfiguration()));        
    }

    /**
     * Execute the test case begin method, then connect to the server proxy
     * redirector (where the test case test method is executed) and then
     * executes the test case end method.
     *
     * @param theHttpClient the HTTP client class to use to connect to the
     *        proxy redirector.
     * @exception Throwable any error that occurred when calling the test method
     *            for the current test case.
     */
    protected void runGenericTest(DefaultHttpClient theHttpClient)
        throws Throwable
    {
        WebRequest request = new WebRequest(
            (WebConfiguration) getConfiguration());

        // Call the set up and begin methods to fill the request object
        callClientGlobalBegin(request);
        callBeginMethod(request);

        // Run the web test
        HttpURLConnection connection = runWebTest(request, theHttpClient);

        // Call the end method
        Object response = callEndMethod(request, connection);

        // call the tear down method
        callClientGlobalEnd(request, connection, response);

        // Close the input stream (just in the case the user has not done it
        // in it's endXXX method (or if he has no endXXX method) ....
        connection.getInputStream().close();
    }

    /**
     * Run the web test by connecting to the server proxy
     * redirector (where the test case test method is executed).
     *
     * @param theRequest the request object which will contain data that will
     *        be used to connect to the Cactus server side redirectors.
     * @param theHttpClient the HTTP client class to use to connect to the
     *        proxy redirector.
     * @return the HTTP connection object that was used to call the server side
     * @exception Throwable any error that occurred when calling the test method
     *            for the current test case.
     */
    private HttpURLConnection runWebTest(
        WebRequest theRequest,
        DefaultHttpClient theHttpClient)
        throws Throwable
    {
        // Add the class name, the method name, to the request to simulate and
        // automatic session creation flag to the request
        RequestDirectives directives = new RequestDirectives(theRequest);
        directives.setClassName(getDelegatedTest().getClass().getName());
        directives.setMethodName(getCurrentTestName());
        directives.setAutoSession(
            theRequest.getAutomaticSession() ? "true" : "false");

        // Add the wrapped test if it is not equal to our current instance
        if (isWrappingATest())
        {
              directives.setWrappedTestName(getWrappedTestName());
        }
        // Add the simulated URL (if one has been defined)
        if (theRequest.getURL() != null)
        {
            theRequest.getURL().saveToRequest(theRequest);
        }

        // Open the HTTP connection to the servlet redirector
        // and manage errors that could be returned in the
        // HTTP response.
        HttpURLConnection connection = theHttpClient.doTest(theRequest);

        return connection;
    }
}
