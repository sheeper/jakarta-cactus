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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.cactus.configuration.ServletConfiguration;
import org.apache.cactus.internal.client.WebClientTestCaseDelegator;
import org.apache.cactus.server.ServletConfigWrapper;

/**
 * Test classes that need access to valid Servlet implicit objects (such as the
 * the HTTP request, the HTTP response, the servlet config, ...) must subclass
 * this class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ServletTestCase extends TestCase
{
    /**
     * Valid <code>HttpServletRequest</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public org.apache.cactus.server.HttpServletRequestWrapper request;

    /**
     * Valid <code>HttpServletResponse</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public HttpServletResponse response;

    /**
     * Valid <code>HttpSession</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public HttpSession session;

    /**
     * Valid <code>ServletConfig</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public ServletConfigWrapper config;

    /**
     * Delegator that provides all Cactus related test case logic. We are using
     * a delegator in order to hide non public API to the users and thus to be 
     * able to easily change the implementation.
     */
    private WebClientTestCaseDelegator delegator;

    /**
     * Default constructor defined in order to allow creating Test Case
     * without needing to define constructor (new feature in JUnit 3.8.1).
     * Should only be used with JUnit 3.8.1 or greater. 
     * 
     * @since 1.5 
     */
    public ServletTestCase()
    {
        init(this);
    }

    /**
     * Constructs a JUnit test case with the given name.
     *
     * @param theName the name of the test case
     */
    public ServletTestCase(String theName)
    {
        super(theName);
        init(this);
    }

    /**
     * Wraps a pure JUnit Test Case in a Cactus Test Case.
     *  
     * @param theName the name of the test
     * @param theTest the Test Case class to wrap
     * @since 1.5
     */
    public ServletTestCase(String theName, Test theTest)
    {
        super(theName);
        init(theTest);
    }

    /**
     * Initializations common to all constructors.
     *  
     * @param theTest a pure JUnit Test that Cactus will wrap
     */
    void init(Test theTest)
    {
        setDelegator(new WebClientTestCaseDelegator(
            this, theTest, new ServletConfiguration()));        
    }

    /**
     * @param theDelegator the client test case delegator
     */
    void setDelegator(WebClientTestCaseDelegator theDelegator)
    {
        this.delegator = theDelegator;
    }

    /**
     * @return the client test case delegator
     */
    WebClientTestCaseDelegator getDelegator()
    {
        return this.delegator;
    }

    /**
     * Runs the bare test. This method is overridden from the JUnit 
     * {@link TestCase} class in order to prevent the latter to call the 
     * <code>setUp()</code> and <code>tearDown()</code> methods which, in 
     * our case, need to be executed on the server side.
     *
     * @exception Throwable if any exception is thrown during the test. Any
     *            exception will be displayed by the JUnit Test Runner
     */
    public void runBare() throws Throwable
    {
        getDelegator().runBareInit();

        // Catch the exception just to have a chance to log it
        try
        {
            // Give back control to JUnit
            runTest();
        }
        catch (Throwable t)
        {
            getDelegator().getLogger().debug("Exception in test", t);
            throw t;
        }
    }   

    /**
     * Runs a test case. This method is overriden from the JUnit
     * {@link TestCase} class in order to seamlessly call the
     * Cactus redirector.
     *
     * @exception Throwable if any error happens during the execution of
     *            the test
     */
    protected void runTest() throws Throwable
    {
        getDelegator().runTest();        
    }
}
