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
import org.apache.commons.cactus.util.log.*;

/**
 * Responsible for instanciating the <code>TestCase</code> class on the server
 * side, set up the implicit objects and call the test method.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ServletTestCaller
{
    /**
     * Name of the attribute in the <code>application</code> scope that will
     * hold the results of the test.
     */
    private final static String TEST_RESULTS =
        "ServletTestRedirector_TestResults";

    /**
     * The logger
     */
    protected static Log logger =
        LogService.getInstance().getLog(ServletTestCaller.class.getName());

    /**
     * The implicit objects (which will be used to set the test case fields
     * in the <code>setTesCaseFields</code> method.
     */
    protected ServletImplicitObjects servletImplicitObjects;

    /**
     * @param theObjects the implicit objects coming from the redirector
     */
    public ServletTestCaller(ServletImplicitObjects theObjects)
    {
        this.servletImplicitObjects = theObjects;
    }

    /**
     * Calls a test method. The parameters needed to call this method are found
     * in the HTTP request. Save the results in the <code>application</code>
     * scope so that the Get Test Result service can find them.
     *
     * @exception ServletException if an unexpected error occurred
     */
    public void doTest() throws ServletException
    {
        this.logger.entry("doTest()");

        WebTestResult result = null;

        // Reset TEST_RESULTS to a new results holder to prevent premature
        // requests for results from seeing either no results or old results
        ResultHolder holder = new ResultHolder();
        this.servletImplicitObjects.getServletConfig().getServletContext().
            setAttribute(TEST_RESULTS, holder);

        this.logger.debug("Result holder semaphore is in place");

        // From this point forward, any thread trying to access the result
        // stored in the holder, itself stored in the application scope, will
        // block and wait until a result is set.

        try {

            // Create an instance of the test class
            ServletTestCase testInstance = getTestClassInstance(
                getTestClassName(), getTestMethodName());

            // Set its fields (implicit objects)
            setTestCaseFields(testInstance);

            // Call it's method corresponding to the current test case
            testInstance.runBareServerTest();

            // Return an instance of <code>WebTestResult</code> with a
            // positive result.
            result = new WebTestResult();

        } catch (Throwable e) {

            // An error occurred, return an instance of
            // <code>WebTestResult</code> with an exception.
            result = new WebTestResult(e);

        }

        // Set the test result. This will deactivate the semaphore.
        holder.setResult(result);

        this.logger.debug("Result holder semaphore inactive (result set in " +
            "holder)");

        this.logger.exit("doTest");
    }

    /**
     * Return the last test results as a serialized object in the HTTP response.
     *
     * @exception ServletException if an unexpected error occurred
     */
    public void doGetResults() throws ServletException
    {
        this.logger.entry("doGetResults()");

        this.logger.debug("Try to read results from Holder ...");

        ResultHolder holder = (ResultHolder)(
            this.servletImplicitObjects.getServletConfig().
            getServletContext().getAttribute(TEST_RESULTS));

        WebTestResult result = holder.getResult();

        this.logger.debug("... results has been read");

        // Write back the results as a serialized object to the outgoing stream.
        try {

            OutputStream os =
                this.servletImplicitObjects.getHttpServletResponse().
                getOutputStream();

            // Write back the result object as a serialized object
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(result);
            oos.flush();
            oos.close();

        } catch (IOException e) {
            String message = "Error writing WebTestResult instance to output " +
                "stream";
            this.logger.error(message, e);
            throw new ServletException(message, e);
        }

        this.logger.exit("doGetResults");
    }

    /**
     * @return the class to test class name, extracted from the HTTP request
     */
    protected String getTestClassName() throws ServletException
    {
        this.logger.entry("getTestClassName()");

        String className = this.servletImplicitObjects.
            getHttpServletRequest().
            getParameter(ServiceDefinition.CLASS_NAME_PARAM);

        if (className == null) {
            String message = "Missing class name parameter [" +
                ServiceDefinition.CLASS_NAME_PARAM + "] in HTTP request.";
            this.logger.error(message);
            throw new ServletException(message);
        }

        this.logger.debug("Class to call = " + className);

        this.logger.entry("getTestClassName");
        return className;
    }

    /**
     * @return the class method to call for the current test case, extracted
     *         from the HTTP request
     */
    protected String getTestMethodName() throws ServletException
    {
        this.logger.entry("getTestMethodName()");

        String methodName = this.servletImplicitObjects.getHttpServletRequest().
            getParameter(ServiceDefinition.METHOD_NAME_PARAM);

        if (methodName == null) {
            String message = "Missing method name parameter [" +
                ServiceDefinition.METHOD_NAME_PARAM + "] in HTTP request.";
            this.logger.error(message);
            throw new ServletException(message);
        }

        this.logger.debug("Method to call = " + methodName);

        this.logger.exit("getTestMethodName");
        return methodName;
    }

    /**
     * @return true if the auto session flag for the Session can be found in
     *         the HTTP request
     */
    protected boolean isAutoSession()
    {
        this.logger.entry("isAutoSession()");

        String autoSession = this.servletImplicitObjects.
            getHttpServletRequest().
            getParameter(ServiceDefinition.AUTOSESSION_NAME_PARAM);

        boolean isAutomaticSession = new Boolean(autoSession).booleanValue();

        this.logger.debug("Auto session is " + isAutomaticSession);

        this.logger.exit("isAutoSession");
        return isAutomaticSession;
    }

    /**
     * @param theClassName the name of the test class
     * @param theTestCaseName the name of the current test case
     * @return an instance of the test class to call
     */
    protected ServletTestCase getTestClassInstance(String theClassName,
        String theTestCaseName) throws ServletException
    {
        this.logger.entry("getTestClassInstance(" + theClassName + ", " +
            theTestCaseName + ")");

        // Get the class to call and build an instance of it.
        Class testClass = null;
        ServletTestCase testInstance = null;
        try {
            testClass = getTestClassClass(theClassName);
            Constructor constructor = testClass.getConstructor(
                new Class[] { String.class });
            testInstance = (ServletTestCase)constructor.newInstance(
                new Object[] { theTestCaseName });
        } catch (Exception e) {
            String message = "Error instanciating class [" + theClassName +
                "(" + theTestCaseName + ")]";
            this.logger.error(message, e);
            throw new ServletException(message, e);
        }

        this.logger.exit("getTestClassInstance");
        return testInstance;
    }

    /**
     * @param theClassName the name of the test class
     * @param theTestCaseName the name of the current test case
     * @return the class object the test class to call
     */
    protected Class getTestClassClass(String theClassName)
        throws ServletException
    {
        this.logger.entry("getTestClassClass(" + theClassName + ")");

        // Get the class to call and build an instance of it.
        Class testClass = null;
        try {
            testClass = Class.forName(theClassName);
        } catch (Exception e) {
            String message = "Error finding class [" + theClassName +
                "] in classpath";
            this.logger.error(message, e);
            throw new ServletException(message, e);
        }

        this.logger.exit("getTestClassClass");
        return testClass;
    }

    /**
     * Sets the test case fields using the implicit objects (using reflection).
     * @param theTestInstance the test class instance
     */
    protected void setTestCaseFields(ServletTestCase theTestInstance)
        throws Exception
    {
        this.logger.entry("setTestCaseFields(" + theTestInstance + ")");

        // Sets the request field of the test case class
        // ---------------------------------------------

        // Extract from the HTTP request the URL to simulate (if any)
        HttpServletRequest request =
            this.servletImplicitObjects.getHttpServletRequest();

        ServletURL url = ServletURL.loadFromRequest(request);

        Field requestField = theTestInstance.getClass().getField("request");
        requestField.set(theTestInstance,
            new HttpServletRequestWrapper(request, url));

        // Set the response field of the test case class
        // ---------------------------------------------

        Field responseField = theTestInstance.getClass().getField("response");
        responseField.set(theTestInstance,
            this.servletImplicitObjects.getHttpServletResponse());

        // Set the config field of the test case class
        // -------------------------------------------

        Field configField = theTestInstance.getClass().getField("config");
        configField.set(theTestInstance,
            new ServletConfigWrapper(
                this.servletImplicitObjects.getServletConfig()));

        // Set the session field of the test case class
        // --------------------------------------------

        // Create a Session object if the auto session flag is on
        if (isAutoSession()) {

            HttpSession session =
                this.servletImplicitObjects.getHttpServletRequest().
                getSession(true);

            Field sessionField = theTestInstance.getClass().getField("session");
            sessionField.set(theTestInstance, session);

        }

        this.logger.exit("setTestCaseFields");
    }

}