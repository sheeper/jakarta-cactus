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
package org.apache.commons.cactus;

import java.lang.reflect.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;

import junit.framework.*;

import org.apache.commons.cactus.client.*;
import org.apache.commons.cactus.server.*;

/**
 * Abstract class that specific test cases (<code>ServletTestCase</code>,
 * <code>FilterTestCase</code>, ...) must extend. Provides generally useful
 * methods fro writing a specific test case.
 *
 * @version @version@
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
     * The name of the current test method being executed. This name is valid
     * both on the client side and on the server side, meaning you can call it
     * from a <code>testXXX()</code>, <code>setUp()</code> or
     * <code>tearDown()</code> method, as well as from <code>beginXXX()</code>
     * and <code>endXXX()</code> methods.
     */
    public String currentTestMethod;

    /**
     * Constructs a JUnit test case with the given name.
     *
     * @param theName the name of the test case
     */
    public AbstractTestCase(String theName)
    {
        super(theName);
        currentTestMethod = name();
    }

    /**
     * @return the name of the test method to call without the
     *         TEST_METHOD_PREFIX prefix
     */
    private String getBaseMethodName()
    {
        // Sanity check
        if (!name().startsWith(TEST_METHOD_PREFIX)) {
            throw new RuntimeException("bad name [" + name() +
                "]. It should start with [" + TEST_METHOD_PREFIX + "].");
        }

        return name().substring(TEST_METHOD_PREFIX.length());
    }

    /**
     * @return the name of the test begin method to call that initialize the
     *         test by initializing the <code>ServletTestRequest</code> object
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
     * @param theRequest the <code>ServletTestRequest</code> object to
     *                   pass to the begin method.
     */
    protected void callBeginMethod(ServletTestRequest theRequest) throws Throwable
    {
        // First, verify if a begin method exist. If one is found, verify if
        // it has the correct signature. If not, send a warning.
        Method[] methods = getClass().getMethods();
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
                if ((parameters.length != 1) || 
                    (!parameters[0].equals(ServletTestRequest.class))) {

                    fail("The begin method [" + methods[i].getName() +
                        "] must accept a single parameter of type [" +
                        ServletTestRequest.class.getName() + "]");
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
     * @param theConnection the <code>HttpURLConnection</code> that was used
     *        to open the connection to the redirection servlet. The response
     *        codes, headers, cookies can be checked using the get methods of
     *        this object.
     */
    protected void callEndMethod(HttpURLConnection theConnection) throws Throwable
    {
        // First, verify if an end method exist. If one is found, verify if
        // it has the correct signature. If not, send a warning.
        Method[] methods = getClass().getMethods();
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
                if ((parameters.length != 1) || 
                    (!parameters[0].equals(HttpURLConnection.class))) {

                    fail("The end method [" + methods[i].getName() +
                        "] must accept a single parameter of type [" +
                        HttpURLConnection.class.getName() + "]");
                }

                try {

                    methods[i].invoke(this, new Object[] { theConnection });

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
        runTest();
    }

    /**
     * Runs a test case. This method is overriden from the JUnit
     * <code>TestCase</code> class in order to seamlessly call the
     * Cactus redirection servlet.
     */
    protected abstract void runTest() throws Throwable;

	/**
	 * Run the test that was specified in the constructor on the server side,
     * calling <code>setUp()</code> and <code>tearDown()</code>.
	 */
    public void runBareServerTest() throws Throwable
    {
		setUp();
		try {
            runServerTest();
		}
		finally {
	        tearDown();
		}
	}

	/**
	 * Run the test that was specified in the constructor on the server side,
	 */
	protected void runServerTest() throws Throwable
    {
		Method runMethod= null;
		try {
			// use getMethod to get all public inherited
			// methods. getDeclaredMethods returns all
			// methods of this class but excludes the
			// inherited ones.
			runMethod = getClass().getMethod(currentTestMethod, new Class[0]);
		} catch (NoSuchMethodException e) {
            fail("Method [" + currentTestMethod +
                "()] does not exist for class [" + 
                this.getClass().getName() + "].");
		}
		if (runMethod != null && !Modifier.isPublic(runMethod.getModifiers())) {
			fail("Method [" + currentTestMethod + "()] should be public");
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
	}
	
}
