/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.cactus.configuration.ConfigurationInitializer;
import org.apache.cactus.internal.client.ClientTestCaseDelegate;
import org.apache.cactus.internal.server.ServerTestCaseDelegate;

/**
 * Base class for all Cactus test case extensions.
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 * @since 1.6 
 */
public abstract class AbstractCactusTestCase extends TestCase
{
    /**
     * As this class is the first one loaded on the client side, we ensure
     * that the Cactus configuration has been initialized. In the future,
     * this block will be removed as all initialization will be done in Cactus
     * test suites. However, as we still support using Cactus TestCase classes
     * we don't have a proper initialization hook and thus we need this hack.
     */
    static
    {
        ConfigurationInitializer.initialize();
    }

    /**
     * Delegate that provides all client side Cactus related test case logic. 
     * We are using a delegate in order to hide non public API to the users 
     * and thus to be able to easily change the implementation.
     */
    private ClientTestCaseDelegate clientDelegate;

    /**
     * Delegate that provides all server side Cactus related test case logic. 
     * We are using a delegate in order to hide non public API to the users 
     * and thus to be able to easily change the implementation.
     */
    private ServerTestCaseDelegate serverDelegate;

    /**
     * Default constructor defined in order to allow creating Test Case
     * without needing to define constructor (new feature in JUnit 3.8.1).
     * Should only be used with JUnit 3.8.1 or greater. 
     */
    public AbstractCactusTestCase()
    {
        init(null);
    }

    /**
     * Constructs a JUnit test case with the given name.
     *
     * @param theName the name of the test case
     */
    public AbstractCactusTestCase(String theName)
    {
        super(theName);
        init(null);
    }

    /**
     * Wraps a pure JUnit Test Case in a Cactus Test Case.
     *  
     * @param theName the name of the test
     * @param theTest the Test Case class to wrap
     */
    public AbstractCactusTestCase(String theName, Test theTest)
    {
        super(theName);
        init(theTest);
    }

    /**
     * Create a client side test case delegate.
     * 
     * @param theTest the JUnit test to wrap or null if there is no test to 
     *        wrap
     * @return the client side test case delegate to use
     */
    protected abstract ClientTestCaseDelegate createClientTestCaseDelegate(
        Test theTest);

    /**
     * Create a server side test case delegate.
     * 
     * @param theTest the JUnit test to wrap or null if there is no test to 
     *        wrap
     * @return the server side test case delegate to use
     */
    protected abstract ServerTestCaseDelegate createServerTestCaseDelegate(
        Test theTest);
    
    /**
     * @param theDelegate the client test case delegate
     */
    void setClientDelegate(ClientTestCaseDelegate theDelegate)
    {
        this.clientDelegate = theDelegate;
    }

    /**
     * @param theDelegate the client test case delegate
     */
    void setServerDelegate(ServerTestCaseDelegate theDelegate)
    {
        this.serverDelegate = theDelegate;
    }

    /**
     * @return the client test case delegate
     */
    ClientTestCaseDelegate getClientDelegate()
    {
        return this.clientDelegate;
    }

    /**
     * @return the server test case delegate
     */
    private ServerTestCaseDelegate getServerDelegate()
    {
        return this.serverDelegate;
    }
    
    /**
     * Initializations common to all constructors.
     *  
     * @param theTest a pure JUnit Test that Cactus will wrap
     */
    void init(Test theTest)
    {
        setClientDelegate(createClientTestCaseDelegate(theTest));
        setServerDelegate(createServerTestCaseDelegate(theTest));
    }

    /**
     * @return true if this test class has been instanciated on the server
     *         side or false otherwise 
     */
    protected abstract boolean isServerSide();

    /**
     * Runs the bare test (either on the client side or on the server side). 
     * This method is overridden from the JUnit 
     * {@link TestCase} class in order to prevent the latter to immediatly
     * call the <code>setUp()</code> and <code>tearDown()</code> methods 
     * which, in our case, need to be executed on the server side.
     *
     * @exception Throwable if any exception is thrown during the test. Any
     *            exception will be displayed by the JUnit Test Runner
     */
    public void runBare() throws Throwable
    {
        if (isServerSide())
        {
            getServerDelegate().runBareInit();            
        }
        else
        {
            getClientDelegate().runBareInit();            
        }

        // Catch the exception just to have a chance to log it
        try
        {
            runCactusTest();
        }
        catch (Throwable t)
        {
            if (!isServerSide())
            {
                getClientDelegate().getLogger().debug("Exception in test", t);
            }
            throw t;
        }
    }   

    /**
     * Runs a Cactus test case.
     *
     * @exception Throwable if any error happens during the execution of
     *            the test
     */
    protected void runCactusTest() throws Throwable
    {
        if (isServerSide())
        {
            // Note: We cannot delegate this piece of code in the
            // ServerTestCaseDelegate class as it requires to call
            // super.runBare()

            if (getServerDelegate().getWrappedTest() != null)
            {
                ((TestCase) getServerDelegate().getWrappedTest()).runBare();
            }
            else
            {
                super.runBare();            
            }
        }
        else
        {
            getClientDelegate().runTest();
        }
    }
}
