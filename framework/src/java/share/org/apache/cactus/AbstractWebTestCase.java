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
package org.apache.cactus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.apache.cactus.client.AbstractHttpClient;
import org.apache.cactus.util.ChainedRuntimeException;

/**
 * Abstract class for Web Test Cases (i.e. HTTP connection to the server) that
 * (<code>ServletTestCase</code>, <code>FilterTestCase</code>, ...) must
 * extend.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public abstract class AbstractWebTestCase extends AbstractTestCase
{
    /**
     * Constructs a JUnit test case with the given name.
     *
     * @param theName the name of the test case
     */
    public AbstractWebTestCase(String theName)
    {
        super(theName);
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
     * @exception Throwable any error that occurred when calling the end method
     *         for the current test case.
     */
    protected void callEndMethod(WebRequest theRequest,
        HttpURLConnection theConnection) throws Throwable
    {
        // First, verify if an end method exist. If one is found, verify if
        // it has the correct signature. If not, send a warning. Also
        // verify that only one end method is defined for a given test.
        Method methodToCall = null;
        Object paramObject = null;

        Method[] methods = getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(getEndMethodName())) {

                // Check return type
                if (!methods[i].getReturnType().getName().equals("void")) {
                    fail("The end method [" + methods[i].getName()
                        + "] should return void and not ["
                        + methods[i].getReturnType().getName() + "]");
                }

                // Check if method is public
                if (!Modifier.isPublic(methods[i].getModifiers())) {
                    fail("Method [" + methods[i].getName()
                        + "] should be declared public");
                }

                // Check parameters
                Class[] parameters = methods[i].getParameterTypes();

                // Verify only one parameter is defined
                if (parameters.length != 1) {
                    fail("The end method [" + methods[i].getName()
                        + "] must only have a single parameter");
                }

                // Is it a Http Unit WebResponse ?
                if (parameters[0].getName().
                    equals("com.meterware.httpunit.WebResponse")) {

                    paramObject = createHttpUnitWebResponse(theConnection);

                    // Is it a Cactus WebResponse ?
                } else if (parameters[0].getName().
                    equals("org.apache.cactus.WebResponse")) {

                    paramObject = new WebResponse(theRequest, theConnection);

                    // Is it an old HttpURLConnection (deprecated) ?
                } else if (parameters[0].getName().
                    equals("java.net.HttpURLConnection")) {

                    paramObject = theConnection;

                    // Else it is an error ...
                } else {
                    fail("The end method [" + methods[i].getName()
                        + "] has a bad parameter of type ["
                        + parameters[0].getName() + "]");
                }

                // Has a method to call already been found ?
                if (methodToCall != null) {
                    fail("There can only be one end method per test case. "
                        + "Test case [" + this.getCurrentTestMethod()
                        + "] has two at least !");
                }

                methodToCall = methods[i];

            }
        }

        if (methodToCall != null) {

            try {

                methodToCall.invoke(this, new Object[]{paramObject});

            } catch (InvocationTargetException e) {
                e.fillInStackTrace();
                throw e.getTargetException();
            } catch (IllegalAccessException e) {
                e.fillInStackTrace();
                throw e;
            }
        }

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
     */
    private Object createHttpUnitWebResponse(HttpURLConnection theConnection)
    {
        Object webResponse;

        try {
            Class responseClass =
                Class.forName("com.meterware.httpunit.WebResponse");
            Method method = responseClass.getMethod("newResponse",
                new Class[]{URLConnection.class});
            webResponse = method.invoke(null, new Object[]{theConnection});
        } catch (Exception e) {
            throw new ChainedRuntimeException("Error calling "
                + "[public static com.meterware.httpunit.WebResponse "
                + "com.meterware.httpunit.WebResponse.newResponse("
                + "java.net.URLConnection) throws java.io.IOException]", e);
        }

        return webResponse;
    }

    /**
     * Execute the test case begin method, then connect to the server proxy
     * redirector (where the test case test method is executed) and then
     * executes the test case end method.
     *
     * @param theHttpClient the HTTP client class to use to connect to the
     *                      proxy redirector.
     * @exception Throwable any error that occurred when calling the test method
     *         for the current test case.
     */
    protected void runGenericTest(AbstractHttpClient theHttpClient)
        throws Throwable
    {
        // Call the begin method to fill the request object
        WebRequest request = new WebRequest();
        callBeginMethod(request);

        // Add the class name, the method name, the URL to simulate and
        // automatic session creation flag to the request

        // Note: All these pareameters are passed in the URL. This is to allow
        // the user to send whatever he wants in the request body. For example
        // a file, ...
        request.addParameter(HttpServiceDefinition.CLASS_NAME_PARAM,
            this.getClass().getName(), WebRequest.GET_METHOD);
        request.addParameter(HttpServiceDefinition.METHOD_NAME_PARAM,
            this.getCurrentTestMethod(), WebRequest.GET_METHOD);
        request.addParameter(HttpServiceDefinition.AUTOSESSION_NAME_PARAM,
            request.getAutomaticSession() ? "true" : "false",
            WebRequest.GET_METHOD);

        // Add the simulated URL (if one has been defined)
        if (request.getURL() != null) {
            request.getURL().saveToRequest(request);
        }

        // Open the HTTP connection to the servlet redirector
        // and manage errors that could be returned in the
        // HTTP response.
        HttpURLConnection connection = theHttpClient.doTest(request);

        // Call the end method
        callEndMethod(request, connection);

        // Close the input stream (just in the case the user has not done it
        // in it's endXXX method (or if he has no endXXX method) ....
        connection.getInputStream().close();
    }

}
