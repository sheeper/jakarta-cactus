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

import org.apache.commons.logging.LogFactory;

/**
 * Abstract test case class that extends {@link AbstractTestCase} by
 * adding generic code to run the tests on the server side.
 *
 * This class is independent of the protocol used to communicate from the
 * Cactus client side and the Cactus server side; it can be HTTP, JMS, etc.
 * Subclasses will define additional behaviour that depends on the protocol.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public abstract class AbstractServerTestCase extends AbstractTestCase
{
    /**
     * Constructs a JUnit test case with the given name.
     *
     * @param theName the name of the test case
     */
    public AbstractServerTestCase(String theName)
    {
        super(theName);
    }

    /**
     * Runs a test case. This method is overriden from the JUnit
     * <code>TestCase</code> class in order to perform different
     * use cases such as seamlessly calling the Cactus Redirector, etc.
     *
     * @exception Throwable any error that occurred when calling the test method
     *            for the current test case.
     */
    protected abstract void runTest() throws Throwable;

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

}