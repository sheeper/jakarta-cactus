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

import java.net.HttpURLConnection;

import junit.framework.Test;

import org.apache.cactus.RequestDirectives;
import org.apache.cactus.WebRequest;
import org.apache.cactus.client.WebResponseObjectFactory;
import org.apache.cactus.client.connector.http.DefaultHttpClient;
import org.apache.cactus.configuration.Configuration;
import org.apache.cactus.configuration.WebConfiguration;
import org.apache.cactus.internal.WebRequestImpl;

/**
 * Delegator extension to support test cases using the HTTP protocol.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class WebClientTestCaseDelegate extends AbstractClientTestCaseDelegate
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
    private void runGenericTest(DefaultHttpClient theHttpClient)
        throws Throwable
    {
        WebRequest request = new WebRequestImpl(
            (WebConfiguration) getConfiguration());

        // Call the set up and begin methods to fill the request object
        callGlobalBeginMethod(request);
        callBeginMethod(request);

        // Run the web test
        HttpURLConnection connection = runWebTest(request, theHttpClient);

        // Call the end method
        Object response = callEndMethod(request, 
            new WebResponseObjectFactory(connection));

        // call the tear down method
        callGlobalEndMethod(request, new WebResponseObjectFactory(connection), 
            response);

        // Close the input stream (just in the case the user has not done it
        // in it's endXXX method (or if it has no endXXX method) ....
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
    private HttpURLConnection runWebTest(WebRequest theRequest,
        DefaultHttpClient theHttpClient) throws Throwable
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
