/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */
package org.apache.commons.cactus.client;

import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.commons.cactus.*;
import org.apache.commons.cactus.util.log.*;

/**
 * Abstract class for performing the steps necessary to run a test. It involves
 * opening a first HTTP connection to a server redirector, reading the output
 * stream and then opening a second HTTP connection to retrieve the test result.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public abstract class AbstractHttpClient
{
    /**
     * The logger
     */
    private static Log logger =
        LogService.getInstance().getLog(AbstractHttpClient.class.getName());

    /**
     * Name of the Cactus configuration file
     */
    public final static String CONFIG_NAME = "cactus";

    /**
     * Properties file holding configuration data for Cactus.
     */
    public final static ResourceBundle CONFIG =
        PropertyResourceBundle.getBundle(CONFIG_NAME);

    /**
     * Check client configuration parameters (verify client classpath)
     */
    static {
        ClientConfigurationChecker.checkConfigProperties();
    }

    /**
     * @return the URL to call the redirector
     */
    protected abstract String getRedirectorURL();

    /**
     * Calls the test method indirectly by calling the Redirector servlet and
     * then open a second HTTP connection to retrieve the test results.
     *
     * @param theRequest the request containing all data to pass to the
     *                   redirector servlet.
     *
     * @return the <code>HttpURLConnection</code> that contains the HTTP
     *         response when the test was called.
     *
     * @exception Throwable if an error occured in the test method or in the
     *                      redirector servlet.
     */
    public HttpURLConnection doTest(ServletTestRequest theRequest)
        throws Throwable
    {
        this.logger.entry("doTest(" + theRequest + ")");

        // Open the first connection to the redirector to execute the test on
        // the server side
        HttpClientHelper helper1 =
                new HttpClientHelper(getRedirectorURL());

        // Specify the service to call on the redirector side
        theRequest.addParameter(ServiceDefinition.SERVICE_NAME_PARAM,
            ServiceEnumeration.CALL_TEST_SERVICE.toString());
        HttpURLConnection connection = helper1.connect(theRequest);

        // Wrap the connection to ensure that all servlet output is read
        // before we ask for results
        connection = new AutoReadHttpURLConnection(connection);

        // Trigger the transfer of data
        connection.getInputStream();

        // Open the second connection to get the test results
        HttpClientHelper helper2 =
                new HttpClientHelper(getRedirectorURL());

        ServletTestRequest resultsRequest = new ServletTestRequest();
        resultsRequest.addParameter(ServiceDefinition.SERVICE_NAME_PARAM,
            ServiceEnumeration.GET_RESULTS_SERVICE.toString());
        HttpURLConnection resultConnection = helper2.connect(resultsRequest);

        // Read the results as a serialized object
        ObjectInputStream ois =
            new ObjectInputStream(resultConnection.getInputStream());
        WebTestResult result = (WebTestResult)ois.readObject();

        ois.close();

        // Check if the returned result object returned contains an error or
        // not. If yes, we need to raise an exception so that the JUnit
        // framework can catch it

        if (result.hasException()) {

            // Wrap the exception message and stack trace into a fake
            // exception class with overloaded <code>printStackTrace()</code>
            // methods so that when JUnit calls this method it will print the
            // stack trace that was set on the server side.

            // If the error was an AssertionFailedError then we use an instance
            // of AssertionFailedErrorWrapper (so that JUnit recognize it is
            // an AssertionFailedError exception and print it differently in
            // it's runner console). Otherwise we use an instance of
            // ServletExceptionWrapper.

            if (result.getExceptionClassName().
                equals("junit.framework.AssertionFailedError")) {

                throw new AssertionFailedErrorWrapper(
                    result.getExceptionMessage(),
                    result.getExceptionClassName(),
                    result.getExceptionStackTrace());

            } else {

                throw new ServletExceptionWrapper(
                    result.getExceptionMessage(),
                    result.getExceptionClassName(),
                    result.getExceptionStackTrace());

            }

        }

        this.logger.exit("doTest");
        return connection;
    }

}
