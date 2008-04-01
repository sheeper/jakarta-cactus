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

import java.net.HttpURLConnection;

import org.apache.cactus.WebRequest;
import org.apache.cactus.internal.HttpServiceDefinition;
import org.apache.cactus.internal.RequestDirectives;
import org.apache.cactus.internal.ServiceEnumeration;
import org.apache.cactus.internal.WebRequestImpl;
import org.apache.cactus.internal.WebTestResult;
import org.apache.cactus.internal.client.AssertionFailedErrorWrapper;
import org.apache.cactus.internal.client.ParsingException;
import org.apache.cactus.internal.client.ServletExceptionWrapper;
import org.apache.cactus.internal.client.WebTestResultParser;
import org.apache.cactus.internal.configuration.WebConfiguration;
import org.apache.cactus.internal.util.IoUtil;
import org.apache.cactus.util.ChainedRuntimeException;

/**
 * Performs the steps necessary to run a test. It involves
 * opening a first HTTP connection to a server redirector, reading the output
 * stream and then opening a second HTTP connection to retrieve the test 
 * result.
 *
 * @version $Id: DefaultHttpClient.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class DefaultHttpClient
{
    /**
     * Cactus configuration.
     */
    protected WebConfiguration configuration;
    
    /**
     * Initialize the Http client.
     * 
     * @param theConfiguration the Cactus configuration
     */
    public DefaultHttpClient(WebConfiguration theConfiguration)
    {
        this.configuration = theConfiguration;
    }

    /**
     * Calls the test method indirectly by calling the Redirector servlet and
     * then open a second HTTP connection to retrieve the test results.
     *
     * @param theRequest the request containing all data to pass to the
     *        redirector servlet.
     *
     * @return the <code>HttpURLConnection</code> that contains the HTTP
     *         response when the test was called.
     *
     * @exception Throwable if an error occured in the test method or in the
     *            redirector servlet.
     */
    public HttpURLConnection doTest(WebRequest theRequest) throws Throwable
    {
        // Open the first connection to the redirector to execute the test on
        // the server side
        HttpURLConnection connection = callRunTest(theRequest);

        // Open the second connection to get the test results
        WebTestResult result = null;

        try
        {
            result = callGetResult(theRequest);
        }
        catch (ParsingException e)
        {
            String url = this.configuration.getRedirectorURL(theRequest);
            throw new ChainedRuntimeException("Failed to get the test "
                + "results at [" + url + "]", e);
        }

        // Check if the returned result object returned contains an error or
        // not. If yes, we need to raise an exception so that the JUnit
        // framework can catch it
        if (result.hasException())
        {
            // Wrap the exception message and stack trace into a fake
            // exception class with overloaded <code>printStackTrace()</code>
            // methods so that when JUnit calls this method it will print the
            // stack trace that was set on the server side.
            // If the error was an AssertionFailedError or ComparisonFailure
            // then we use an instance of AssertionFailedErrorWrapper (so that 
            // JUnit recognize it is an AssertionFailedError exception and 
            // print it differently in it's runner console). Otherwise we use 
            // an instance of ServletExceptionWrapper.

            // Note: We have to test the exceptions by string name as the JUnit
            // AssertionFailedError class is unfortunately not serializable...

            if ((result.getExceptionClassName().equals(
                "junit.framework.AssertionFailedError"))
                || (result.getExceptionClassName().equals(
                "junit.framework.ComparisonFailure")))
            {
                throw new AssertionFailedErrorWrapper(
                    result.getExceptionMessage(), 
                    result.getExceptionClassName(), 
                    result.getExceptionStackTrace());
            }
            else
            {
                throw new ServletExceptionWrapper(
                    result.getExceptionMessage(), 
                    result.getExceptionClassName(), 
                    result.getExceptionStackTrace());
            }
        }

        return connection;
    }

    /**
     * Execute the test by calling the redirector.
     *
     * @param theRequest the request containing all data to pass to the
     *        redirector servlet.
     * @return the <code>HttpURLConnection</code> that contains the HTTP
     *         response when the test was called.
     *
     * @exception Throwable if an error occured in the test method or in the
     *            redirector servlet.
     */
    private HttpURLConnection callRunTest(WebRequest theRequest) 
        throws Throwable
    {
        // Specify the service to call on the redirector side
        theRequest.addParameter(HttpServiceDefinition.SERVICE_NAME_PARAM, 
            ServiceEnumeration.CALL_TEST_SERVICE.toString(), 
            WebRequest.GET_METHOD);

        // Open the first connection to the redirector to execute the test on
        // the server side
        HttpClientConnectionHelper helper = 
            new HttpClientConnectionHelper(
                this.configuration.getRedirectorURL(theRequest));
        
        HttpURLConnection connection = 
            helper.connect(theRequest, this.configuration); 

        // Wrap the connection to ensure that all servlet output is read
        // before we ask for results
        connection = new AutoReadHttpURLConnection(connection);

        // Trigger the transfer of data
        connection.getInputStream();

        return connection;
    }

    /**
     * Get the test result from the redirector.
     *
     * @param theOriginalRequest the request that was used to run the test
     * @return the result that was returned by the redirector.
     *
     * @exception Throwable if an error occured in the test method or in the
     *            redirector servlet.
     */
    private WebTestResult callGetResult(WebRequest theOriginalRequest) 
        throws Throwable
    {
        WebRequest resultsRequest = new WebRequestImpl(this.configuration);
        RequestDirectives directives = new RequestDirectives(resultsRequest);
        directives.setService(ServiceEnumeration.GET_RESULTS_SERVICE);

        // Use the same redirector as was used by the original request
        resultsRequest.setRedirectorName(
            theOriginalRequest.getRedirectorName());
        
        // Add authentication details
        if (theOriginalRequest.getAuthentication() != null)
        {
            resultsRequest.setAuthentication(
                theOriginalRequest.getAuthentication());
        }

        // Open the second connection to get the test results
        HttpClientConnectionHelper helper = 
            new HttpClientConnectionHelper(
                this.configuration.getRedirectorURL(resultsRequest));

        HttpURLConnection resultConnection = 
            helper.connect(resultsRequest, this.configuration);

        if (resultConnection.getResponseCode() != 200)
        {
            throw new ParsingException("Not a valid response ["
                + resultConnection.getResponseCode() + " "
                + resultConnection.getResponseMessage() + "]");
        }

        // Read the test result
        WebTestResultParser parser = new WebTestResultParser();
        return parser.parse(
            IoUtil.getText(resultConnection.getInputStream(), "UTF-8"));
    }
}
