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

import junit.framework.*;

import org.apache.commons.cactus.*;

/**
 * Manage the logic for calling a test method (which need access to JSP
 * objects) located on the server side. First opens an HTTP connection to
 * the redirector JSP (which in trun calls the test) and get the test results
 * by opening a second HTTP connection but to the Servlet redirector (the tests
 * were saved in the application context scope).
 *
 * @version @version@
 */
public class JspHttpClient extends AbstractHttpClient
{
    /**
     * Default URL to call the <code>jspRedirector</code> JSP.
     */
    protected final static String m_JspRedirectorURL = 
        PropertyResourceBundle.getBundle("cactus").getString("cactus.jspRedirectorURL");

    /**
     * Calls the test method indirectly by calling the Redirector JSP and
     * then open a second HTTP connection to the Servlet Redirector to retrieve
     * the test results.
     *
     * @param theRequest the request containing all data to pass to the
     *                   redirector JSP.
     *
     * @return the <code>HttpURLConnection</code> object that contains the HTTP
     *         response when the test was called.
     *
     * @exception Throwable if an error occured in the test method or in the
     *                      redirector servlet.
     */
    public HttpURLConnection doTest(ServletTestRequest theRequest) throws Throwable
    {
        ServletTestResult result = null;
        HttpURLConnection connection = null;

        // Open the first connection to the redirector JSP
        HttpClientHelper helper1 = new HttpClientHelper(m_JspRedirectorURL);

        // Specify the service to call on the redirector side
        theRequest.addParameter(ServiceDefinition.SERVICE_NAME_PARAM,
            ServiceEnumeration.CALL_TEST_SERVICE.toString());
        connection = helper1.connect(theRequest);

        // Note: We need to get the input stream here to trigger the actual
        // call to the servlet ... Don't know why exactly ... :(
        connection.getInputStream();

        // Open the second connection (to the Servlet redirector) to get the
        // test results
        HttpClientHelper helper2 = new HttpClientHelper(m_ServletRedirectorURL);
        ServletTestRequest resultsRequest = new ServletTestRequest();
        resultsRequest.addParameter(ServiceDefinition.SERVICE_NAME_PARAM,
            ServiceEnumeration.GET_RESULTS_SERVICE.toString());
        HttpURLConnection resultConnection = helper2.connect(resultsRequest);

        // Read the results as a serialized object
        ObjectInputStream ois = new ObjectInputStream(resultConnection.getInputStream());
        result = (ServletTestResult)ois.readObject();

        ois.close();

        // Check if the result object returned from the redirection servlet
        // contains an error or not. If yes, we need to raise an exception
        // for the JUnit framework to catch it.

        if (result.hasException()) {

            // Wrap the exception message and stack trace into a fake
            // class that extends Throwable (i.e.
            // <code>ServletExceptionWrapper</code>) with an overloaded
            // <code>printStackTrace()</code> methods so that when JUnit calls
            // this method it will print the stack trace that was set on the
            // <code>Throwable</code> is transient and thus cannot be
            // serialized !
            throw new ServletExceptionWrapper(
                result.getExceptionMessage(), result.getExceptionClassName(),
                result.getExceptionStackTrace());
        }

        return connection;
    }

}
