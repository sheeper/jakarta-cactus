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
package org.apache.cactus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;

import org.apache.cactus.client.DefaultHttpClient;
import org.apache.cactus.util.WebConfiguration;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract Test Case for Web Test Cases that extends 
 * {@link AbstractWebClientTestCase} to add support for running tests
 * on the server side.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public abstract class AbstractWebServerTestCase extends AbstractWebClientTestCase
{
    /**
     * Constructs a JUnit test case with the given name.
     *
     * @param theName the name of the test case
     */
    public AbstractWebServerTestCase(String theName)
    {
        super(theName);
    }

    /**
     * Run the test that was specified in the constructor on the server side,
     * calling <code>setUp()</code> and <code>tearDown()</code>.
     *
     * @exception Throwable any error that occurred when calling the test method
     *         for the current test case, on the server side.
     */
    public void runBareServerTest() throws Throwable
    {
        // Initialize the logging system. As this class is instanciated both
        // on the server side and on the client side, we need to differentiate
        // the logging initialisation. This method is only called on the server
        // side, so we instanciate the log for server side here.
        if (getLogger() == null)
        {
            setLogger(LogFactory.getLog(this.getClass()));
        }

        setUp();

        try
        {
            runServerTest();
        }
        finally
        {
            tearDown();
        }
    }

    /**
     * Run the test that was specified in the constructor on the server side.
     *
     * @exception Throwable any error that occurred when calling the test method
     *         for the current test case, on the server side.
     */
    protected void runServerTest() throws Throwable
    {
        Method runMethod = null;

        try
        {
            // use getMethod to get all public inherited
            // methods. getDeclaredMethods returns all
            // methods of this class but excludes the
            // inherited ones.
            runMethod = getClass().getMethod(this.getCurrentTestMethod(), 
                new Class[0]);
        }
        catch (NoSuchMethodException e)
        {
            fail("Method [" + this.getCurrentTestMethod()
                + "()] does not exist for class [" + this.getClass().getName()
                + "].");
        }

        if ((runMethod != null) && !Modifier.isPublic(runMethod.getModifiers()))
        {
            fail("Method [" + this.getCurrentTestMethod()
                + "()] should be public");
        }

        try
        {
            runMethod.invoke(this, new Class[0]);
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
    private HttpURLConnection runWebTest(WebRequest theRequest, 
        DefaultHttpClient theHttpClient) throws Throwable
    {
        // Add the class name, the method name, the URL to simulate and
        // automatic session creation flag to the request
        // Note: All these pareameters are passed in the URL. This is to allow
        // the user to send whatever he wants in the request body. For example
        // a file, ...
        theRequest.addParameter(HttpServiceDefinition.CLASS_NAME_PARAM, 
            this.getClass().getName(), WebRequest.GET_METHOD);
        theRequest.addParameter(HttpServiceDefinition.METHOD_NAME_PARAM, 
            this.getCurrentTestMethod(), WebRequest.GET_METHOD);
        theRequest.addParameter(HttpServiceDefinition.AUTOSESSION_NAME_PARAM, 
            theRequest.getAutomaticSession() ? "true" : "false", 
            WebRequest.GET_METHOD);

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

    /**
     * Runs a test case. This method is overriden from the JUnit
     * <code>TestCase</code> class in order to seamlessly call the
     * Cactus redirection servlet.
     *
     * @exception Throwable if any error happens during the execution of
     *            the test
     */
    protected void runTest() throws Throwable
    {
        runGenericTest(new DefaultHttpClient(
            (WebConfiguration) getConfiguration()));        
    }

}