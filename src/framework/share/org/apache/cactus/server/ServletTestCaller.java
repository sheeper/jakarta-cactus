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
package org.apache.commons.cactus.server;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.cactus.*;

/**
 * Call the test method on the server side after assigning the servlet implicit
 * objects using reflection and retri
 *
 * @version @version@
 */
public class ServletTestCaller
{
    /**
     * Name of the attribute in the <code>application</code> scope that will
     * hold the results of the test.
     */
    private final static String TEST_RESULTS = "ServletTestRedirector_TestResults";

    /**
     * Call the method to test.
     *
     * @param theClassName the name of the test class to call
     * @param theMethod the name of the test method to call
     * @param theObjects the implicit objects that will be assigned by
     *                   reflection to the test class.
     */
    private void callTestMethod(String theClassName, String theMethod, ServletImplicitObjects theObjects) throws Throwable
    {
        // Get the class to call and build an instance of it.
        Class testClass = null;
        ServletTestCase testInstance = null;
        try {
            testClass = Class.forName(theClassName);
            Constructor constructor = testClass.getConstructor(new Class[] { String.class });
            testInstance = (ServletTestCase)constructor.newInstance(new Object[] { theMethod });
        } catch (Exception e) {
            throw new ServletException("Error instanciating class [" + theClassName + "]", e);
        }

        // Set the current method name field
        Field methodField = testClass.getField("currentTestMethod");
        methodField.set(testInstance, theMethod);

        // Set the request field of the test case class

        // Extract from the HTTP request the URL to simulate (if any)
        ServletURL url = ServletURL.loadFromRequest(theObjects.m_Request);

        Field requestField = testClass.getField("request");
        requestField.set(testInstance, new HttpServletRequestWrapper(theObjects.m_Request, url));

        // Set the response field of the test case class
        Field responseField = testClass.getField("response");
        responseField.set(testInstance, theObjects.m_Response);

        // Set the config field of the test case class
        Field configField = testClass.getField("config");
        configField.set(testInstance, new ServletConfigWrapper(theObjects.m_Config));

        // Set the session field of the test case class

        // Get a valid session object if the auto session flag is on

        // Get the autologin flag from the request
        String autoSession = theObjects.m_Request.getParameter(ServiceDefinition.AUTOSESSION_NAME_PARAM);
        boolean isAutomaticSession = new Boolean(autoSession).booleanValue();

        if (isAutomaticSession) {

            theObjects.m_Session = theObjects.m_Request.getSession(true);

            Field sessionField = testClass.getField("session");
            sessionField.set(testInstance, theObjects.m_Session);

        }

        // Call the setUp() method
        CallerHelper.callSetUpMethod(testInstance);

        // Get the method to call
        Method method;
        try {
            method = testClass.getMethod(theMethod, new Class[0]);
        } catch (Exception e) {
            throw new ServletException("Method " + theMethod + "() does not exist for class [" + theClassName + "].", e);
        }

        // Call the test method
        try {
            method.invoke(testInstance, null);
        } catch (IllegalAccessException e) {
            throw new ServletException("Illegal access", e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        // Call the tearDown() method
        CallerHelper.callTearDownMethod(testInstance);

    }

    /**
     * Calls a test method. The parameters needed to call this method are found
     * in the HTTP request. Save the results in the <code>application</code>
     * scope so that the Get Test Result service can find them.
     *
     * @param theObjects the implicit objects that will be assigned by
     *                   reflection to the test class.
     * @exception ServletException if an unexpected error occurred
     */
    public void doTest(ServletImplicitObjects theObjects) throws ServletException
    {
        ServletTestResult result = null;

        try {

            // Extract from the HTTP request the test class name and method to call.

            String testClassName = theObjects.m_Request.getParameter(ServiceDefinition.CLASS_NAME_PARAM);
            if (testClassName == null) {
                throw new ServletException("Missing parameter [" +
                    ServiceDefinition.CLASS_NAME_PARAM + "] in HTTP request.");
            }

            String methodName = theObjects.m_Request.getParameter(ServiceDefinition.METHOD_NAME_PARAM);
            if (methodName == null) {
                throw new ServletException("Missing parameter [" +
                    ServiceDefinition.METHOD_NAME_PARAM + "] in HTTP request.");
            }

            // Call the method to test
            callTestMethod(testClassName, methodName, theObjects);

            // Return an instance of <code>ServletTestResult</code> with a
            // positive result.
            result = new ServletTestResult();

        } catch (Throwable e) {

            // An error occurred, return an instance of
            // <code>ServletTestResult</code> with an exception.
            result = new ServletTestResult(e);

        }

        // Save the test result.
        theObjects.m_Config.getServletContext().setAttribute(TEST_RESULTS, result);

    }

    /**
     * Return the last test results as a serialized object in the HTTP response.
     *
     * @param theObjects the implicit objects that will be assigned by
     *                   reflection to the test class.
     * @exception ServletException if an unexpected error occurred
     */
    public void doGetResults(ServletImplicitObjects theObjects) throws ServletException
    {
        ServletTestResult result = (ServletTestResult)theObjects.m_Config.getServletContext().getAttribute(TEST_RESULTS);
        if (result == null) {
            throw new ServletException("No test results found in the application scope");
        }

        // Write back the results as a serialized object to the outgoing stream.
        try {

            OutputStream os = theObjects.m_Response.getOutputStream();

            // Write back the result object as a serialized object
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(result);
            oos.flush();
            oos.close();

        } catch (IOException e) {
            throw new ServletException("Error writing ServletTestResult instance to output stream", e);
        }
    }

}