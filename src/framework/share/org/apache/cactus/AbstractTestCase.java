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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
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
package org.apache.cactus;

import java.lang.reflect.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;

import junit.framework.*;

import org.apache.cactus.client.*;
import org.apache.cactus.server.*;
import org.apache.cactus.util.log.*;
import org.apache.cactus.util.*;

/**
 * Abstract class that specific test cases (<code>ServletTestCase</code>,
 * <code>FilterTestCase</code>, ...) must extend. Provides generally useful
 * methods fro writing a specific test case.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public abstract class AbstractTestCase extends TestCase
{
    /**
     * The prefix of a test method.
     */
    protected final static String TEST_METHOD_PREFIX = "test";

    /**
     * The prefix of a begin test method.
     */
    protected final static String BEGIN_METHOD_PREFIX = "begin";

    /**
     * The prefix of an end test method.
     */
    protected final static String END_METHOD_PREFIX = "end";

    /**
     * Name of properties file to initialize logging subsystem
     */
    public final static String LOG_CLIENT_CONFIG = "log_client.properties";

    /**
     * The name of the current test method being executed. This name is valid
     * both on the client side and on the server side, meaning you can call it
     * from a <code>testXXX()</code>, <code>setUp()</code> or
     * <code>tearDown()</code> method, as well as from <code>beginXXX()</code>
     * and <code>endXXX()</code> methods.
     */
    public String currentTestMethod;

    /**
     * The logger (only used on the client side).
     */
    protected Log logger;

    /**
     * Constructs a JUnit test case with the given name.
     *
     * @param theName the name of the test case
     */
    public AbstractTestCase(String theName)
    {
        super(theName);
        this.currentTestMethod = getName();
    }

    /**
     * @return the name of the test method to call without the
     *         TEST_METHOD_PREFIX prefix
     */
    private String getBaseMethodName()
    {
        // Sanity check
        if (!getName().startsWith(TEST_METHOD_PREFIX)) {
            throw new RuntimeException("bad name [" + getName() +
                "]. It should start with [" + TEST_METHOD_PREFIX + "].");
        }

        return getName().substring(TEST_METHOD_PREFIX.length());
    }

    /**
     * @return the name of the test begin method to call that initialize the
     *         test by initializing the <code>WebRequest</code> object
     *         for the test case.
     */
    protected String getBeginMethodName()
    {
        return BEGIN_METHOD_PREFIX + getBaseMethodName();
    }

    /**
     * @return the name of the test end method to call when the test has been
     *         run on the server. It can be used to verify returned headers,
     *         cookies, ...
     */
    protected String getEndMethodName()
    {
        return END_METHOD_PREFIX + getBaseMethodName();
    }

    /**
     * Call the test case begin method
     *
     * @param theRequest the <code>WebRequest</code> object to
     *                   pass to the begin method.
     */
    protected void callBeginMethod(WebRequest theRequest)
        throws Throwable
    {
        // First, verify if a begin method exist. If one is found, verify if
        // it has the correct signature. If not, send a warning.
        Method[] methods = getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(getBeginMethodName())) {

                // Check return type
                if (!methods[i].getReturnType().getName().equals("void")) {
                    fail("The begin method [" + methods[i].getName() +
                        "] should return void and not [" +
                        methods[i].getReturnType().getName() + "]");
                }

                // Check if method is public
                if (!Modifier.isPublic(methods[i].getModifiers())) {
                   fail("Method [" + methods[i].getName() +
                        "] should be declared public");
                }

                // Check parameters
                Class[] parameters = methods[i].getParameterTypes();
                if (parameters.length != 1) {

                    fail("The begin method [" + methods[i].getName() +
                        "] must accept a single parameter derived from " +
                        "class [" + WebRequest.class.getName() + "], " +
                        "but " + parameters.length + " parameters were found");

                } else if (
                    !WebRequest.class.isAssignableFrom(parameters[0])) {

                    fail("The begin method [" + methods[i].getName() +
                        "] must accept a single parameter derived from " +
                        "class [" + WebRequest.class.getName() + "], " +
                        "but found a [" + parameters[0].getName() + "] " +
                        "parameter instead");
                }

                try {

                    methods[i].invoke(this, new Object[] { theRequest });

                } catch (InvocationTargetException e) {
                    e.fillInStackTrace();
                    throw e.getTargetException();
                }
                catch (IllegalAccessException e) {
                    e.fillInStackTrace();
                    throw e;
                }

            }
        }
    }

    /**
     * Call the test case end method
     *
     * @param theRequest the request data that were used to open the
     *                   connection.
     * @param theConnection the <code>HttpURLConnection</code> that was used
     *        to open the connection to the redirection servlet. The response
     *        codes, headers, cookies can be checked using the get methods of
     *        this object.
     */
    protected void callEndMethod(WebRequest theRequest,
        HttpURLConnection theConnection) throws Throwable
    {
        // First, verify if an end method exist. If one is found, verify if
        // it has the correct signature. If not, send a warning. Also
        // verify that only one end method is defined for a given test.
        Method methodToCall = null;
        Object paramObject = null;

        Method[] methods = getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(getEndMethodName())) {

                // Check return type
                if (!methods[i].getReturnType().getName().equals("void")) {
                    fail("The end method [" + methods[i].getName() +
                        "] should return void and not [" +
                        methods[i].getReturnType().getName() + "]");
                }

                // Check if method is public
                if (!Modifier.isPublic(methods[i].getModifiers())) {
                    fail("Method [" + methods[i].getName() +
                        "] should be declared public");
                }

                // Check parameters
                Class[] parameters = methods[i].getParameterTypes();

                // Verify only one parameter is defined
                if (parameters.length != 1) {
                    fail("The end method [" + methods[i].getName() +
                        "] must only have a single parameter");
                }

                // Is it a Http Unit WebResponse ?
                if (parameters[0].getName().
                    equals("com.meterware.httpunit.WebResponse")) {

                    paramObject = createHttpUnitWebResponse(theConnection);

                // Is it a Cactus WebResponse ?
                } else if (parameters[0].getName().
                    equals("org.apache.cactus.WebResponse")) {

                    paramObject = new WebResponse(theRequest, theConnection);

                // Is it an old HttpURLConnection (deprecated) ?
                } else if (parameters[0].getName().
                    equals("java.net.HttpURLConnection")) {

                    paramObject = theConnection;

                // Else it is an error ...
                } else {
                    fail("The end method [" + methods[i].getName() +
                        "] has a bad parameter of type [" +
                        parameters[0].getName() + "]");
                }

                // Has a method to call already been found ?
                if (methodToCall != null) {
                    fail("There can only be one end method per test case. " +
                        "Test case [" + this.currentTestMethod +
                         "] has two at least !");
                }

                methodToCall = methods[i];

            }
        }

        if (methodToCall != null) {

            try {

                methodToCall.invoke(this, new Object[] { paramObject });

            } catch (InvocationTargetException e) {
                e.fillInStackTrace();
                throw e.getTargetException();
            }
            catch (IllegalAccessException e) {
                e.fillInStackTrace();
                throw e;
            }
        }

    }

    /**
     * Create a HttpUnit <code>WebResponse</code> object by reflection (so
     * that we don't need the HttpUnit jar for users who are not using
     * the HttpUnit endXXX() signature).
     *
     * @return a HttpUnit <code>WebResponse</code> object
     */
    private Object createHttpUnitWebResponse(HttpURLConnection theConnection)
    {
        Object webResponse;

        try {
            Class responseClass =
                Class.forName("com.meterware.httpunit.WebResponse");
            Method method = responseClass.getMethod("newResponse",
                new Class[] { URLConnection.class });
            webResponse = method.invoke(null, new Object[] { theConnection });
        } catch (Exception e) {
            throw new ChainedRuntimeException("Error calling " +
                "[public static com.meterware.httpunit.WebResponse " +
                "com.meterware.httpunit.WebResponse.newResponse(" +
                "java.net.URLConnection) throws java.io.IOException]", e);
        }

        return webResponse;
    }

    /**
     * Runs the bare test sequence. This method is overridden from the
     * JUnit <code>TestCase</code> class in order to prevent the latter
     * to call the <code>setUp()</code> and <code>tearDown()</code> methods
     * which, in our case, need to be ran in the servlet engine by the
     * servlet redirector class.
     *
     * @exception Throwable if any exception is thrown
     */
    public void runBare() throws Throwable
    {
        // Run some configuration checks
        ClientConfigurationChecker.getInstance().checkCactusProperties();
        ClientConfigurationChecker.getInstance().checkHttpClient();
        ClientConfigurationChecker.getInstance().checkLog4j();

        // Initialize the logging system. As this class is instanciated both
        // on the server side and on the client side, we need to differentiate
        // the logging initialisation. This method is only called on the client
        // side, so we instanciate the log for client side here.
        if (!LogService.getInstance().isInitialized()) {
            LogService.getInstance().init("/" + AbstractTestCase.LOG_CLIENT_CONFIG);
        }

        // We make sure we reinitialize the logger with the name of the
        // current class (that's why the logged instance is not static).
        this.logger =
            LogService.getInstance().getLog(this.getClass().getName());

        // Catch the exception just to have a chance to log it
        try {
            runTest();
        } catch (Throwable t) {
            logger.debug("Exception in test", t);
            throw t;
        }

    }

    /**
     * Runs a test case. This method is overriden from the JUnit
     * <code>TestCase</code> class in order to seamlessly call the
     * Cactus redirection servlet.
     */
    protected abstract void runTest() throws Throwable;

    /**
     * Execute the test case begin method, then connect to the server proxy
     * redirector (where the test case test method is executed) and then
     * executes the test case end method.
     *
     * @param theHttpClient the HTTP client class to use to connect to the
     *                      proxy redirector.
     */
    protected void runGenericTest(AbstractHttpClient theHttpClient)
        throws Throwable
    {
        logger.entry("runGenericTest(...)");

        // Log the test name
        logger.debug("Test case = " + currentTestMethod);

        // Call the begin method to fill the request object
        WebRequest request = new WebRequest();
        callBeginMethod(request);

        // Add the class name, the method name, the URL to simulate and
        // automatic session creation flag to the request

        // Note: All these pareameters are passed in the URL. This is to allow
        // the user to send whatever he wants in the request body. For example
        // a file, ...
        request.addParameter(ServiceDefinition.CLASS_NAME_PARAM,
            this.getClass().getName(), WebRequest.GET_METHOD);
        request.addParameter(ServiceDefinition.METHOD_NAME_PARAM, getName(),
            WebRequest.GET_METHOD);
        request.addParameter(ServiceDefinition.AUTOSESSION_NAME_PARAM,
            new Boolean(request.getAutomaticSession()).toString(),
            WebRequest.GET_METHOD);

        // Add the simulated URL (if one has been defined)
        if (request.getURL() != null) {
            request.getURL().saveToRequest(request);
        }

        // Open the HTTP connection to the servlet redirector
        // and manage errors that could be returned in the
        // HTTP response.
        HttpURLConnection connection = theHttpClient.doTest(request);

        // Call the end method
        callEndMethod(request, connection);

        // Close the intput stream (just in the case the user has not done it
        // in it's endXXX method (or if he has no endXXX method) ....
        connection.getInputStream().close();

        logger.exit("runGenericTest");
     }

    // Methods below are only called by the Cactus redirector on the server
    // side

	/**
	 * Run the test that was specified in the constructor on the server side,
     * calling <code>setUp()</code> and <code>tearDown()</code>.
	 */
    public void runBareServerTest() throws Throwable
    {
        // Initialize the logging system. As this class is instanciated both
        // on the server side and on the client side, we need to differentiate
        // the logging initialisation. This method is only called on the server
        // side, so we instanciate the log for server side here.
        if (this.logger == null) {
            this.logger =
                LogService.getInstance().getLog(this.getClass().getName());
        }

        logger.entry("runBareServerTest()");

        setUp();
        try {
            runServerTest();
        }
        finally {
            tearDown();
        }

        logger.exit("runBareServerTest");
	}

	/**
	 * Run the test that was specified in the constructor on the server side,
	 */
	protected void runServerTest() throws Throwable
    {
        logger.entry("runServerTest()");

		Method runMethod= null;
		try {
			// use getMethod to get all public inherited
			// methods. getDeclaredMethods returns all
			// methods of this class but excludes the
			// inherited ones.
			runMethod = getClass().getMethod(this.currentTestMethod,
                new Class[0]);

		} catch (NoSuchMethodException e) {
            fail("Method [" + this.currentTestMethod +
                "()] does not exist for class [" +
                this.getClass().getName() + "].");
		}
		if (runMethod != null && !Modifier.isPublic(runMethod.getModifiers())) {
			fail("Method [" + this.currentTestMethod + "()] should be public");
		}

		try {
			runMethod.invoke(this, new Class[0]);
		}
		catch (InvocationTargetException e) {
			e.fillInStackTrace();
			throw e.getTargetException();
		}
		catch (IllegalAccessException e) {
			e.fillInStackTrace();
			throw e;
		}

        logger.exit("runServerTest");
	}

}
