/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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
package org.apache.cactus.client.connector.http;

import java.io.IOException;
import java.net.HttpURLConnection;

import junit.framework.Test;

import org.apache.cactus.Request;
import org.apache.cactus.RequestDirectives;
import org.apache.cactus.WebRequest;
import org.apache.cactus.client.ResponseObjectFactory;
import org.apache.cactus.client.WebResponseObjectFactory;
import org.apache.cactus.client.connector.ProtocolHandler;
import org.apache.cactus.client.connector.ProtocolState;
import org.apache.cactus.configuration.WebConfiguration;
import org.apache.cactus.internal.WebRequestImpl;
import org.apache.cactus.util.JUnitVersionHelper;

/**
 * Implementation for the HTTP protocol. It connects to the redirector proxy
 * using HTTP and passing Cactus information (test case to run, etc) as HTTP
 * GET parameters. 
 * 
 * @version $Id$
 */
public class HttpProtocolHandler implements ProtocolHandler
{
    /**
     * Cactus configuration data to use. In particular contains useful 
     * configuration data for the HTTP connector (e.g. redirector URL).
     */
    private WebConfiguration configuration;

    /**
     * @param theConfiguration configuration data
     */
    public HttpProtocolHandler(WebConfiguration theConfiguration)
    {
        this.configuration = theConfiguration;
    }

    // Interface methods ----------------------------------------------------
    
    /**
     * @see ProtocolHandler#createRequest()
     */
    public Request createRequest()
    {
        return new WebRequestImpl(getConfiguration());
    }
    
    /**
     * @see ProtocolHandler#runTest(Test, Test, Request)
     */
    public ProtocolState runTest(Test theDelegatedTest, Test theWrappedTest,
        Request theRequest) throws Throwable
    {
        WebRequest request = (WebRequest) theRequest;

        // Run the web test
        HttpURLConnection connection = runWebTest(theDelegatedTest, 
            theWrappedTest, request); 

        HttpProtocolState state = new HttpProtocolState();
        state.setConnection(connection);
        return state;
    }

    /**
     * @see ProtocolHandler#createResponseObjectFactory(ProtocolState)
     */
    public ResponseObjectFactory createResponseObjectFactory(
        ProtocolState theState)
    {
        HttpProtocolState state = (HttpProtocolState) theState;
        return new WebResponseObjectFactory(state.getConnection());
    }
    
    /**
     * @see ProtocolHandler#afterTest(ProtocolState)
     */
    public void afterTest(ProtocolState theState) throws IOException
    {
        HttpProtocolState state = (HttpProtocolState) theState;
        
        // Close the input stream (just in the case the user has not done it
        // in it's endXXX method (or if it has no endXXX method) ....
        state.getConnection().getInputStream().close();       
    }

    // Private methods ----------------------------------------------------
    
    /**
     * @return configuration data
     */
    private WebConfiguration getConfiguration()
    {
        return this.configuration;
    }
    
    /**
     * Run the web test by connecting to the server redirector proxy and
     * execute the tests on the server side.
     *
     * @param theDelegatedTest the Cactus test to execute
     * @param theWrappedTest optionally specify a pure JUnit test case that is
     *        being wrapped and will be executed on the server side
     * @param theRequest the request containing data to connect to the 
     *        redirector proxy
     * @return the HTTP connection object that was used to call the server side
     * @exception Throwable any error that occurred when calling the test method
     *            for the current test case.
     */
    private HttpURLConnection runWebTest(Test theDelegatedTest, 
        Test theWrappedTest, WebRequest theRequest) throws Throwable
    {
        // Add the class name, the method name, to the request to simulate and
        // automatic session creation flag to the request
        RequestDirectives directives = new RequestDirectives(theRequest);
        directives.setClassName(theDelegatedTest.getClass().getName());
        directives.setMethodName(getCurrentTestName(theDelegatedTest));
        directives.setAutoSession(
            theRequest.getAutomaticSession() ? "true" : "false");

        // Add the wrapped test if it is not equal to our current instance
        if (theWrappedTest != null)
        {
            directives.setWrappedTestName(theWrappedTest.getClass().getName());
        }

        // Add the simulated URL (if one has been defined)
        if (theRequest.getURL() != null)
        {
            theRequest.getURL().saveToRequest(theRequest);
        }

        // Open the HTTP connection to the servlet redirector and manage errors
        // that could be returned in the HTTP response.
        DefaultHttpClient client = new DefaultHttpClient(getConfiguration());
        HttpURLConnection connection = client.doTest(theRequest);

        return connection;
    }

    /**
     * @param theDelegatedTest the Cactus test to execute
     * @return the name of the current test case being executed (it corresponds
     *         to the name of the test method with the "test" prefix removed.
     *         For example, for "testSomeTestOk" would return "someTestOk".
     */
    private String getCurrentTestName(Test theDelegatedTest)
    {
        return JUnitVersionHelper.getTestCaseName(theDelegatedTest);        
    }
}
