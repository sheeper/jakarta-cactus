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
package org.apache.cactus.client.connector;

import java.net.HttpURLConnection;

import org.apache.cactus.HttpServiceDefinition;
import org.apache.cactus.ServiceEnumeration;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;
import org.apache.cactus.WebTestResult;
import org.apache.cactus.client.AssertionFailedErrorWrapper;
import org.apache.cactus.client.ParsingException;
import org.apache.cactus.client.ServletExceptionWrapper;
import org.apache.cactus.client.WebTestResultParser;
import org.apache.cactus.configuration.WebConfiguration;
import org.apache.cactus.util.ChainedRuntimeException;
import org.apache.cactus.util.IoUtil;

/**
 * Performs the steps necessary to run a test. It involves
 * opening a first HTTP connection to a server redirector, reading the output
 * stream and then opening a second HTTP connection to retrieve the test 
 * result.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:Jason.Robertson@acs-inc.com">Jason Robertson</a>
 *
 * @version $Id$
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
            throw new ChainedRuntimeException("Failed to get the test "
                + "results. This is probably due to an error that happened on "
                + "the server side when trying to execute the tests. Here is "
                + "what was returned by the server : ["
                + new WebResponse(theRequest, connection).getText() + "]", e);
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
            // If the error was an AssertionFailedError then we use an instance
            // of AssertionFailedErrorWrapper (so that JUnit recognize it is
            // an AssertionFailedError exception and print it differently in
            // it's runner console). Otherwise we use an instance of
            // ServletExceptionWrapper.
            if (result.getExceptionClassName().equals(
                "junit.framework.AssertionFailedError"))
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
        ConnectionHelper helper = ConnectionHelperFactory.getConnectionHelper(
            this.configuration.getRedirectorURL(theRequest), 
            this.configuration);

        HttpURLConnection connection = helper.connect(theRequest);

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
        WebRequest resultsRequest = new WebRequest(this.configuration);

        resultsRequest.addParameter(HttpServiceDefinition.SERVICE_NAME_PARAM, 
            ServiceEnumeration.GET_RESULTS_SERVICE.toString(), 
            WebRequest.GET_METHOD);

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
        ConnectionHelper helper = ConnectionHelperFactory.getConnectionHelper(
            this.configuration.getRedirectorURL(resultsRequest),
            this.configuration);

        HttpURLConnection resultConnection = helper.connect(resultsRequest);

        // Read the test result
        WebTestResultParser parser = new WebTestResultParser();
        WebTestResult result = parser.parse(
            IoUtil.getText(resultConnection.getInputStream(), "UTF-8"));

        return result;
    }
}