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
package org.apache.cactus.internal.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.Assert;
import junit.framework.Test;

import org.apache.cactus.Request;
import org.apache.cactus.client.ResponseObjectFactory;
import org.apache.cactus.client.connector.ProtocolHandler;
import org.apache.cactus.client.connector.ProtocolState;
import org.apache.cactus.configuration.Configuration;
import org.apache.cactus.util.JUnitVersionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides the ability to run common code before and after each test on the 
 * client side. All the methods provided are independent of any communication 
 * protocol between client side and server side (HTTP, JMS, etc). Any protocol 
 * dependent methods must be provided and implemented in the 
 * {@link ProtocolHandler} implementation class.
 *  
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ClientTestCaseCaller extends Assert
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
     * The logger (only used on the client side).
     */
    private Log logger;

    /**
     * The Cactus configuration.
     */
    private Configuration configuration;

    /**
     * Pure JUnit Test Case that we are wrapping (if any)
     */
    private Test wrappedTest;

    /**
     * The test we are delegating for.
     */
    private Test delegatedTest;   

    /**
     * The protocol handler to use to execute the tests on the server side.
     */
    private ProtocolHandler protocolHandler;

    // Constructors ---------------------------------------------------------
    
    /**
     * @param theDelegatedTest the test we are delegating for
     * @param theWrappedTest the test being wrapped by this delegate (or null 
     *        if none)
     * @param theProtocolHandler the protocol handler to use to execute the 
     *        tests on the server side 
     */
    public ClientTestCaseCaller(Test theDelegatedTest, 
        Test theWrappedTest, ProtocolHandler theProtocolHandler)
    {        
        if (theDelegatedTest == null)
        {
            throw new IllegalStateException(
                "The test object passed must not be null");
        }

        setDelegatedTest(theDelegatedTest); 
        setWrappedTest(theWrappedTest);
        this.protocolHandler = theProtocolHandler;
    }

    // Public methods -------------------------------------------------------
    
    /**
     * Execute begin and end methods and calls the different 
     * {@link ProtocolHandler} lifecycle methods to execute the test
     * on the server side.
     * 
     * Note that this method is overriden from the JUnit 
     * {@link junit.framework.TestCase} class in order to prevent JUnit from 
     * calling the {@link junit.framework.TestCase#setUp()} and
     * {@link junit.framework.TestCase#tearDown()} methods on the client side.
     * instead we are calling the server redirector proxy and the setup and
     * teardown methods will be executed on the server side.
     *
     * @exception Throwable if any error happens during the execution of
     *            the test
     */
    public void runTest() throws Throwable
    {
        Request request = this.protocolHandler.createRequest();
        
        // Call the set up and begin methods to fill the request object
        callGlobalBeginMethod(request);
        callBeginMethod(request);

        // Run the server test
        ProtocolState state = this.protocolHandler.runTest(
            getDelegatedTest(), getWrappedTest(), request);
        
        // Call the end method
        Object response = callEndMethod(request, 
            this.protocolHandler.createResponseObjectFactory(state));

        // call the tear down method
        callGlobalEndMethod(request, 
            this.protocolHandler.createResponseObjectFactory(state), 
            response);

        this.protocolHandler.afterTest(state);
    }

    /**
     * @return The logger used by the <code>TestCase</code> class and
     *         subclasses to perform logging.
     */
    public final Log getLogger()
    {
        return this.logger;
    }

    /**
     * Perform client side initializations before each test, such as
     * re-initializating the logger and printing some logging information.
     */
    public void runBareInit()
    {
        // We make sure we reinitialize The logger with the name of the
        // current extending class so that log statements will contain the
        // actual class name (that's why the logged instance is not static).
        this.logger = LogFactory.getLog(this.getClass());

        // Mark beginning of test on client side
        getLogger().debug("------------- Test: " + this.getCurrentTestName());
    }

    /**
     * Call the test case begin method.
     *
     * @param theRequest the request object to pass to the begin method.
     * @exception Throwable any error that occurred when calling the begin
     *            method for the current test case.
     */
    public void callBeginMethod(Request theRequest) throws Throwable
    {
        callGenericBeginMethod(theRequest, getBeginMethodName());
    }

    /**
     * Call the test case end method
     *
     * @param theRequest the request data that were used to open the
     *                   connection.
     * @param theResponseFactory the factory to use to return response objects.
     * @return the created Reponse object
     * @exception Throwable any error that occurred when calling the end method
     *         for the current test case.
     */
    public Object callEndMethod(Request theRequest, 
        ResponseObjectFactory theResponseFactory) throws Throwable
    {
        return callGenericEndMethod(theRequest, theResponseFactory,
            getEndMethodName(), null);
    }

    /**
     * Call the global begin method. This is the method that is called before
     * each test if it exists. It is called on the client side only.
     *
     * @param theRequest the request object which will contain data that will
     *        be used to connect to the Cactus server side redirectors.
     * @exception Throwable any error that occurred when calling the method
     */
    public void callGlobalBeginMethod(Request theRequest) throws Throwable
    {
        callGenericBeginMethod(theRequest, CLIENT_GLOBAL_BEGIN_METHOD);
    }

    /**
     * Call the client tear down up method if it exists.
     *
     * @param theRequest the request data that were used to open the
     *                   connection.
     * @param theResponseFactory the factory to use to return response objects.
     * @param theResponse the Response object if it exists. Can be null in
     *        which case it is created from the response object factory
     * @exception Throwable any error that occurred when calling the method
     */
    private void callGlobalEndMethod(Request theRequest, 
        ResponseObjectFactory theResponseFactory, Object theResponse) 
        throws Throwable
    {
        callGenericEndMethod(theRequest, theResponseFactory,
            CLIENT_GLOBAL_END_METHOD, theResponse);
    }
    
    // Private methods ------------------------------------------------------
    
    /**
     * @param theWrappedTest the pure JUnit test that we need to wrap 
     */
    private void setWrappedTest(Test theWrappedTest)
    {
        this.wrappedTest = theWrappedTest;
    }

    /**
     * @param theDelegatedTest the test we are delegating for
     */
    private void setDelegatedTest(Test theDelegatedTest)
    {
        this.delegatedTest = theDelegatedTest;
    }

    /**
     * @return the wrapped JUnit test
     */
    private Test getWrappedTest()
    {
        return this.wrappedTest;
    }

    /**
     * @return the test we are delegating for
     */
    private Test getDelegatedTest()
    {
        return this.delegatedTest;
    }

    /**
     * @return the test on which we will operate. If there is a wrapped
     *         test then the returned test is the wrapped test. Otherwise we
     *         return the delegated test.
     */
    private Test getTest()
    {
        Test activeTest;
        if (getWrappedTest() != null)
        {
            activeTest = getWrappedTest();
        }
        else
        {
            activeTest = getDelegatedTest();
        }
        return activeTest;
    }

    /**
     * @return the name of the test method to call without the
     *         TEST_METHOD_PREFIX prefix
     */
    private String getBaseMethodName()
    {
        // Sanity check
        if (!getCurrentTestName().startsWith(TEST_METHOD_PREFIX))
        {
            throw new RuntimeException("bad name ["
                + getCurrentTestName()
                + "]. It should start with ["
                + TEST_METHOD_PREFIX + "].");
        }

        return getCurrentTestName().substring(
            TEST_METHOD_PREFIX.length());
    }

    /**
     * @return the name of the test begin method to call that initialize the
     *         test by initializing the <code>WebRequest</code> object
     *         for the test case.
     */
    private String getBeginMethodName()
    {
        return BEGIN_METHOD_PREFIX + getBaseMethodName();
    }

    /**
     * @return the name of the test end method to call when the test has been
     *         run on the server. It can be used to verify returned headers,
     *         cookies, ...
     */
    private String getEndMethodName()
    {
        return END_METHOD_PREFIX + getBaseMethodName();
    }

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
        Method[] methods = getTest().getClass().getMethods();

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
                        + "] must accept a single parameter implementing "
                        + "interface [" + Request.class.getName() + "], "
                        + "but " + parameters.length
                        + " parameters were found");
                }
                else if (!Request.class.isAssignableFrom(parameters[0]))
                {
                    fail("The method [" + methods[i].getName()
                        + "] must accept a single parameter implementing "
                        + "interface [" + Request.class.getName() + "], "
                        + "but found a [" + parameters[0].getName() + "] "
                        + "parameter instead");
                }

                try
                {
                    methods[i].invoke(getTest(), new Object[] {theRequest});

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
     * Call the global end method. This is the method that is called after
     * each test if it exists. It is called on the client side only.
     *
     * @param theRequest the request data that were used to open the
     *        connection.
     * @param theResponseFactory the factory to use to return response objects.
     * @param theMethodName the name of the end method to call
     * @param theResponse the Response object if it exists. Can be null in
     *        which case it is created from the response object factory
     * @return the created Reponse object
     * @exception Throwable any error that occurred when calling the end method
     *            for the current test case.
     */
    private Object callGenericEndMethod(Request theRequest,
        ResponseObjectFactory theResponseFactory, String theMethodName, 
        Object theResponse) throws Throwable
    {
        Method methodToCall = null;
        Object paramObject = null;

        Method[] methods = getTest().getClass().getMethods();

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

                // Verify only one parameter is defined
                if (parameters.length != 1)
                {
                    fail("The method [" + methods[i].getName()
                       + "] must only have a single parameter");
                }

                paramObject = theResponse;

                if (paramObject == null)
                {
                    try
                    {
                        paramObject = theResponseFactory.getResponseObject(
                            parameters[0].getName(), theRequest);
                    }
                    catch (ClientException e)
                    {
                        throw new ClientException("The method ["
                            + methods[i].getName() 
                            + "] has a bad parameter of type ["
                            + parameters[0].getName() + "]", e);
                    }
                }

                // Has a method to call already been found ?
                if (methodToCall != null)
                {
                    fail("There can only be one method ["
                       + methods[i].getName() + "] per test case. "
                       + "Test case [" + this.getCurrentTestName()
                       + "] has two at least !");
                }

                methodToCall = methods[i];
            }
        }

        if (methodToCall != null)
        {
            try
            {
                methodToCall.invoke(getTest(), new Object[] {paramObject});
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

        return paramObject;
    }
    
    /**
     * @see #getCurrentTestName()
     * @deprecated Use {@link #getCurrentTestName()} instead
     */
    private String getCurrentTestMethod()
    {
        return getCurrentTestName();
    }

    /**
     * @return the name of the current test case being executed (it corresponds
     *         to the name of the test method with the "test" prefix removed.
     *         For example, for "testSomeTestOk" would return "someTestOk".
     */
    private String getCurrentTestName()
    {
        return JUnitVersionHelper.getTestCaseName(getDelegatedTest());        
    }

    /**
     * @return The wrapped test name, if any (null otherwise).
     */
    private String getWrappedTestName()
    {
        if (isWrappingATest())
        {
            return getWrappedTest().getClass().getName();
        }
        return null;
    }

    /**
     * @return whether this test case wraps another
     */
    private boolean isWrappingATest()
    {
        return (getWrappedTest() != null);
    }
}
