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

import junit.framework.TestCase;

import org.apache.cactus.client.ClientInitializer;
import org.apache.cactus.util.Configuration;
import org.apache.cactus.util.JUnitVersionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract class that is a thin layer on top of JUnit and that provides 
 * the ability to run common code before each test on the client side (note
 * that calling common tear down code is delegated to extending classes as the
 * method signature depends on the protocol used).
 *
 * In addition it provides ability to execute some one time initialisation
 * code (a pity this is not provided in JUnit). It can be useful to start
 * an embedded server for example.
 *
 * This class is independent of the protocol used to communicate from the
 * Cactus client side and the Cactus server side; it can be HTTP, JMS, etc.
 * Subclasses will define additional behaviour that depends on the protocol.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public abstract class AbstractClientTestCase extends TestCase
{
    /**
     * The prefix of a test method.
     */
    protected static final String TEST_METHOD_PREFIX = "test";

    /**
     * The prefix of a begin test method.
     */
    protected static final String BEGIN_METHOD_PREFIX = "begin";

    /**
     * The prefix of an end test method.
     */
    protected static final String END_METHOD_PREFIX = "end";

    /**
     * The name of the method that is called before each test on the client
     * side (if it exists).
     */
    protected static final String CLIENT_GLOBAL_BEGIN_METHOD = "begin";

    /**
     * The name of the method that is called after each test on the client
     * side (if it exists).
     */
    protected static final String CLIENT_GLOBAL_END_METHOD = "end";

    /**
     * Flag used to verify if client initialization has already been performed
     * for the current test suite or not.
     */
    private static boolean isClientInitialized;

    /**
     * The name of the current test method being executed. This name is valid
     * both on the client side and on the server side, meaning you can call it
     * from a <code>testXXX()</code>, <code>setUp()</code> or
     * <code>tearDown()</code> method, as well as from <code>beginXXX()</code>
     * and <code>endXXX()</code> methods.
     */
    private String currentTestMethod;

    /**
     * The logger (only used on the client side).
     */
    private Log logger;

    /**
     * The Cactus configuration.
     */
    private Configuration configuration;

    /**
     * Constructs a JUnit test case with the given name.
     *
     * @param theName the name of the test case
     */
    public AbstractClientTestCase(String theName)
    {
        super(theName);
        this.setCurrentTestMethod(JUnitVersionHelper.getTestCaseName(this));
    }

    /**
     * @return The logger used by the <code>TestCase</code> class and
     *         subclasses to perform logging.
     */
    protected final Log getLogger()
    {
        return this.logger;
    }

    /**
     * @param theLogger the logger to use 
     */
    protected void setLogger(Log theLogger)
    {
        this.logger = theLogger;
    }
    
    /**
     * @return the Cactus configuration
     */
    protected Configuration getConfiguration()
    {
        return this.configuration;
    }

    /**
     * Sets the Cactus configuration
     * 
     * @param theConfiguration the Cactus configuration
     */
    protected void setConfiguration(Configuration theConfiguration)
    {
        this.configuration = theConfiguration;
    }
   
    /**
     * @return the name of the test method to call without the
     *         TEST_METHOD_PREFIX prefix
     */
    private String getBaseMethodName()
    {
        // Sanity check
        if (!this.getCurrentTestMethod().startsWith(TEST_METHOD_PREFIX))
        {
            throw new RuntimeException("bad name ["
                + this.getCurrentTestMethod()
                + "]. It should start with ["
                + TEST_METHOD_PREFIX + "].");
        }

        return this.getCurrentTestMethod().substring(
            TEST_METHOD_PREFIX.length());
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
     * Runs the bare test.
     * This method is overridden from the JUnit <code>TestCase</code> class in
     * order to prevent the latter to call the <code>setUp()</code> and
     * <code>tearDown()</code> methods which, in our case, need to be ran in
     * the servlet engine by the servlet redirector class.
     *
     * @exception Throwable if any exception is thrown during the test. Any
     *            exception will be displayed by the JUnit Test Runner
     */
    public void runBare() throws Throwable
    {
        // We make sure we reinitialize The logger with the name of the
        // current extending class so that log statements will contain the
        // actual class name (that's why the logged instance is not static).
        this.logger = LogFactory.getLog(this.getClass());

        // Initialize client side configuration
        if (!isClientInitialized)
        {
            initializeClientSide();
        }

        // Mark beginning of test on client side
        getLogger().debug("------------- Test: " + this.getCurrentTestMethod());

        // Catch the exception just to have a chance to log it
        try
        {
            runTest();
        }
        catch (Throwable t)
        {
            logger.debug("Exception in test", t);
            throw t;
        }
    }

    /**
     * Perform client side initialization that need to be performed once
     * per test suite.
     */
    protected void initializeClientSide()
    {
        // Call abstract method that initialize Cactus configuration
        setConfiguration(createConfiguration());
        
        // Call client side initializer (if defined). It will be called only
        // once per test suite
        ClientInitializer.initialize(getConfiguration());
    }

    /**
     * Creates the Cactus configuration object
     * 
     * @return the Cactus configuration
     */
    protected abstract Configuration createConfiguration();
    
    /**
     * Call a begin method which takes Cactus WebRequest as parameter
     *
     * @param theRequest the request object which will contain data that will
     *        be used to connect to the Cactus server side redirectors.
     * @param theMethodName the name of the begin method to call
     * @exception Throwable any error that occurred when calling the method
     */
    private void callGenericBeginMethod(Request theRequest, 
        String theMethodName) throws Throwable
    {
        // First, verify if a begin method exist. If one is found, verify if
        // it has the correct signature. If not, send a warning.
        Method[] methods = getClass().getMethods();

        for (int i = 0; i < methods.length; i++)
        {
            if (methods[i].getName().equals(theMethodName))
            {
                // Check return type
                if (!methods[i].getReturnType().getName().equals("void"))
                {
                    fail("The method [" + methods[i].getName()
                        + "] should return void and not ["
                        + methods[i].getReturnType().getName() + "]");
                }

                // Check if method is public
                if (!Modifier.isPublic(methods[i].getModifiers()))
                {
                    fail("Method [" + methods[i].getName()
                        + "] should be declared public");
                }

                // Check parameters
                Class[] parameters = methods[i].getParameterTypes();

                if (parameters.length != 1)
                {
                    fail("The method [" + methods[i].getName()
                        + "] must accept a single parameter derived from "
                        + "class [" + WebRequest.class.getName() + "], "
                        + "but " + parameters.length
                        + " parameters were found");
                }
                else if (!theRequest.getClass().isAssignableFrom(parameters[0]))
                {
                    fail("The method [" + methods[i].getName()
                        + "] must accept a single parameter derived from "
                        + "class [" + theRequest.getClass().getName() + "], "
                        + "but found a [" + parameters[0].getName() + "] "
                        + "parameter instead");
                }

                try
                {
                    methods[i].invoke(this, new Object[] {theRequest});

                    break;
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
        }
    }

    /**
     * Call the global begin method. This is the method that is called before
     * each test if it exists. It is called on the client side only.
     *
     * @param theRequest the request object which will contain data that will
     *        be used to connect to the Cactus server side redirectors.
     * @exception Throwable any error that occurred when calling the method
     */
    protected void callClientGlobalBegin(Request theRequest) throws Throwable
    {
        callGenericBeginMethod(theRequest, CLIENT_GLOBAL_BEGIN_METHOD);
    }

    /**
     * Call the test case begin method.
     *
     * @param theRequest the request object to pass to the begin method.
     * @exception Throwable any error that occurred when calling the begin
     *            method for the current test case.
     */
    protected void callBeginMethod(Request theRequest) throws Throwable
    {
        callGenericBeginMethod(theRequest, getBeginMethodName());
    }

    /**
     * @return the name of the current test case being executed (it corresponds
     *         to the name of the test method with the "test" prefix removed.
     *         For example, for "testSomeTestOk" would return "someTestOk".
     */
    protected String getCurrentTestMethod()
    {
        return this.currentTestMethod;
    }

    /**
     * @param theCurrentTestMethod the name of the current test case.
     */
    private void setCurrentTestMethod(String theCurrentTestMethod)
    {
        this.currentTestMethod = theCurrentTestMethod;
    }
}