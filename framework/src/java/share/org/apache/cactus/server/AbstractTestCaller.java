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
package org.apache.cactus.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import javax.servlet.ServletException;

import org.apache.cactus.AbstractTestCase;
import org.apache.cactus.ServiceDefinition;
import org.apache.cactus.WebTestResult;
import org.apache.cactus.util.log.Log;
import org.apache.cactus.util.log.LogService;

/**
 * Responsible for instanciating the <code>TestCase</code> class on the server
 * side, set up the implicit objects and call the test method. This class
 * provides a common abstraction for all test web requests.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:ndlesiecki@apache.org">Nicholas Lesiecki</a>
 *
 * @version $Id$
 */
public abstract class AbstractTestCaller
{
    /**
     * Name of the attribute in the <code>application</code> scope that will
     * hold the results of the test.
     */
    protected static final String TEST_RESULTS =
        "ServletTestRedirector_TestResults";

    /**
     * The logger.
     */
    private static final Log LOGGER =
        LogService.getInstance().getLog(AbstractTestCaller.class.getName());

    /**
     * The implicit objects (which will be used to set the test case fields
     * in the <code>setTesCaseFields</code> method.
     */
    protected WebImplicitObjects webImplicitObjects;

    /**
     * @param theObjects the implicit objects coming from the redirector
     */
    public AbstractTestCaller(WebImplicitObjects theObjects)
    {
        this.webImplicitObjects = theObjects;
    }

    /**
     * Sets the implicit object in the test case class
     *
     * @param theTestCase the instance of the test case class on which the
     *        class variable (implicit objects) should be set
     * @exception Exception if an errors occurs when setting the implicit
     *            objects
     */
    protected abstract void setTestCaseFields(AbstractTestCase theTestCase)
        throws Exception;

    /**
     * Calls a test method. The parameters needed to call this method are found
     * in the HTTP request. Save the results in the <code>application</code>
     * scope so that the Get Test Result service can find them.
     *
     * @exception ServletException if an unexpected error occurred
     */
    public void doTest() throws ServletException
    {
        WebTestResult result = null;

        try {

            // Create an instance of the test class
            AbstractTestCase testInstance = getTestClassInstance(
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

        LOGGER.debug("Test result : [" + result + "]");

        // Set the test result.
        this.webImplicitObjects.getServletContext().setAttribute(TEST_RESULTS,
            result);

        LOGGER.debug("Result saved in context scope");
    }

    /**
     * Return the last test results in the HTTP response.
     *
     * @exception ServletException if an unexpected error occurred
     */
    public void doGetResults() throws ServletException
    {
        // One could think there is a potential risk that the client side of
        // Cactus will request the result before it has been written to the
        // context scope as the HTTP request will not block in some containers.
        // However this will not happend because on the client side, once the
        // first request is done to execute the test, all the result is read
        // by the AutoReadHttpURLConnection class, thus ensuring that the
        // request is fully finished and the resukt has been committed ...

        WebTestResult result =
            (WebTestResult) (this.webImplicitObjects.getServletContext().
            getAttribute(TEST_RESULTS));

        LOGGER.debug("Test Result = [" + result + "]");

        // Write back the results to the outgoing stream as an XML string.
        try {

            PrintWriter pw =
                this.webImplicitObjects.getHttpServletResponse().getWriter();
            pw.print(result.toXml());
            pw.close();

        } catch (IOException e) {
            String message = "Error writing WebTestResult instance to output " +
                "stream";
            LOGGER.error(message, e);
            throw new ServletException(message, e);
        }
    }

    /**
     * Run the connection test between client and server. This is just to
     * ensure that configuration is set up correctly.
     *
     * @exception ServletException if an unexpected error occurred
     */
    public void doRunTest() throws ServletException
    {
        // Do not return any http response (not needed). It is enough to
        // know this point has been reached ... it means the connection has
        // been established !
    }

    /**
     * @return the class to test class name, extracted from the HTTP request
     * @exception ServletException if the class name of the test case is missing
     *            from the HTTP request
     */
    protected String getTestClassName() throws ServletException
    {
        String queryString =
            this.webImplicitObjects.getHttpServletRequest().getQueryString();
        String className = ServletUtil.getQueryStringParameter(queryString,
            ServiceDefinition.CLASS_NAME_PARAM);

        if (className == null) {
            String message = "Missing class name parameter [" +
                ServiceDefinition.CLASS_NAME_PARAM + "] in HTTP request.";
            LOGGER.error(message);
            throw new ServletException(message);
        }

        LOGGER.debug("Class to call = " + className);

        return className;
    }

    /**
     * @return the class method to call for the current test case, extracted
     *         from the HTTP request
     * @exception ServletException if the method name of the test case is
     *            missing from the HTTP request
     */
    protected String getTestMethodName() throws ServletException
    {
        String queryString =
            this.webImplicitObjects.getHttpServletRequest().getQueryString();
        String methodName = ServletUtil.getQueryStringParameter(queryString,
            ServiceDefinition.METHOD_NAME_PARAM);

        if (methodName == null) {
            String message = "Missing method name parameter [" +
                ServiceDefinition.METHOD_NAME_PARAM + "] in HTTP request.";
            LOGGER.error(message);
            throw new ServletException(message);
        }

        LOGGER.debug("Method to call = " + methodName);

        return methodName;
    }

    /**
     * @return true if the auto session flag for the Session can be found in
     *         the HTTP request
     */
    protected boolean isAutoSession()
    {
        String queryString =
            this.webImplicitObjects.getHttpServletRequest().getQueryString();
        String autoSession = ServletUtil.getQueryStringParameter(queryString,
            ServiceDefinition.AUTOSESSION_NAME_PARAM);

        boolean isAutomaticSession = new Boolean(autoSession).booleanValue();

        LOGGER.debug("Auto session is " + isAutomaticSession);

        return isAutomaticSession;
    }

    /**
     * @param theClassName the name of the test class
     * @param theTestCaseName the name of the current test case
     * @return an instance of the test class to call
     * @exception ServletException if the test case instance for the current
     *            test fails to be instanciated (for example if some
     *            information is missing from the HTTP request)
     */
    protected AbstractTestCase getTestClassInstance(String theClassName,
        String theTestCaseName) throws ServletException
    {
        // Get the class to call and build an instance of it.
        Class testClass = getTestClassClass(theClassName);
        AbstractTestCase testInstance = null;
        try {
            Constructor constructor = testClass.getConstructor(
                new Class[]{String.class});
            testInstance = (AbstractTestCase) constructor.newInstance(
                new Object[]{theTestCaseName});
        } catch (Exception e) {
            String message = "Error instantiating class [" + theClassName +
                "(" + theTestCaseName + ")]";
            LOGGER.error(message, e);
            throw new ServletException(message, e);
        }

        return testInstance;
    }

    /**
     * @param theClassName the name of the test class
     * @return the class object the test class to call
     * @exception ServletException if the class of the current test case
     *            cannot be loaded in memory (i.e. it is not in the
     *            classpath)
     */
    protected Class getTestClassClass(String theClassName)
        throws ServletException
    {
        // Get the class to call and build an instance of it.
        Class testClass = null;
        try {

            try {
                // Try first from Context class loader so that we can put the
                // Cactus jar as an external library.
                testClass = getTestClassFromContextClassLoader(theClassName);
            } catch (Exception internalException) {
                // It failed... Try from the webapp classloader.
                testClass = getTestClassFromWebappClassLoader(theClassName);
            }

        } catch (Exception e) {
            String message = "Error finding class [" + theClassName +
                "] using both the Context classloader and the webapp " +
                "classloader. Possible causes include:\r\n";
            message += "\t- Your webapp does not include your test " +
                "classes,\r\n";
            message += "\t- The cactus.jar is not located in your " +
                "WEB-INF/lib directory and your Container has not set the " +
                "Context classloader to point to the webapp one";

            LOGGER.error(message, e);
            throw new ServletException(message, e);
        }

        return testClass;
    }

    /**
     * Try loading test class using the Context class loader.
     *
     * @param theClassName the test class to load
     * @return the <code>Class</code> object for the class to load
     * @exception ClassNotFoundException if the class cannot be loaded through
     *            this class loader
     */
    private Class getTestClassFromContextClassLoader(String theClassName)
        throws ClassNotFoundException
    {
        return Class.forName(theClassName, true,
            Thread.currentThread().getContextClassLoader());
    }

    /**
     * Try loading test class using the Webapp class loader.
     *
     * @param theClassName the test class to load
     * @return the <code>Class</code> object for the class to load
     * @exception ClassNotFoundException if the class cannot be loaded through
     *            this class loader
     */
    private Class getTestClassFromWebappClassLoader(String theClassName)
        throws ClassNotFoundException
    {
        return Class.forName(theClassName, true,
            this.getClass().getClassLoader());
    }

}