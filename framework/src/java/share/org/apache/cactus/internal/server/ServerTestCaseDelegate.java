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
package org.apache.cactus.internal.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.Assert;
import junit.framework.Test;

import org.apache.cactus.util.JUnitVersionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Delegate class that allows executing JUnit test on the server side by
 * calling <code>setUp()</code>, <code>testXXX()</code> and 
 * <code>tearDown()</code>.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ServerTestCaseDelegate extends Assert
{
    /**
     * The logger.
     */
    private Log logger;

    /**
     * The test we are delegating for.
     */
    private Test delegatedTest;   

    /**
     * Pure JUnit Test Case that we are wrapping (if any)
     */
    private Test wrappedTest;

    /**
     * @param theDelegatedTest the test we are delegating for
     * @param theWrappedTest the test being wrapped by this delegate (or null 
     *        if none)
     */
    public ServerTestCaseDelegate(Test theDelegatedTest, Test theWrappedTest) 
    {        
        if (theDelegatedTest == null)
        {
            throw new IllegalStateException(
                "The test object passed must not be null");
        }

        setDelegatedTest(theDelegatedTest); 
        setWrappedTest(theWrappedTest);
    }

    /**
     * @param theWrappedTest the pure JUnit test that we need to wrap 
     */
    public void setWrappedTest(Test theWrappedTest)
    {
        this.wrappedTest = theWrappedTest;
    }

    /**
     * @return the wrapped JUnit test
     */
    public Test getWrappedTest()
    {
        return this.wrappedTest;
    }

    /**
     * @param theDelegatedTest the test we are delegating for
     */
    public void setDelegatedTest(Test theDelegatedTest)
    {
        this.delegatedTest = theDelegatedTest;
    }

    /**
     * @return the test we are delegating for
     */
    public Test getDelegatedTest()
    {
        return this.delegatedTest;
    }

    /**
     */
    public void runBareInit()
    {
        // Initialize the logging system. As this class is instanciated both
        // on the server side and on the client side, we need to differentiate
        // the logging initialisation. This method is only called on the server
        // side, so we instanciate the log for server side here.
        if (getLogger() == null)
        {
            setLogger(LogFactory.getLog(getDelegatedTest().getClass()));
        }        
    }
    
    /**
     * @return the logger pointing to the wrapped test case that use to perform
     *         logging on behalf of the wrapped test.
     */
    private Log getLogger()
    {
        return this.logger;
    }

    /**
     * @param theLogger the logger to use 
     */
    private void setLogger(Log theLogger)
    {
        this.logger = theLogger;
    }

    /**
     * Run the test that was specified in the constructor on the server side
     * by executing the testXXX method.
     *
     * @exception Throwable any error that occurred when calling the test method
     *         for the current test case, on the server side.
     */
    public void runServerTest() throws Throwable
    {
        Method runMethod = null;

        try
        {
            // Use getMethod to get all public inherited
            // methods. getDeclaredMethods returns all
            // methods of this class but excludes the
            // inherited ones.
            runMethod = getWrappedTest().getClass().getMethod(
                JUnitVersionHelper.getTestCaseName(getWrappedTest()),
                new Class[0]);
        }
        catch (NoSuchMethodException e)
        {
            fail("Method [" 
                + JUnitVersionHelper.getTestCaseName(getWrappedTest())
                + "()] does not exist for class [" 
                + getWrappedTest().getClass().getName() + "].");
        }

        if ((runMethod != null) && !Modifier.isPublic(runMethod.getModifiers()))
        {
            fail("Method [" 
                + JUnitVersionHelper.getTestCaseName(getWrappedTest())
                + "()] should be public");
        }

        try
        {
            runMethod.invoke(getWrappedTest(), new Class[0]);
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
