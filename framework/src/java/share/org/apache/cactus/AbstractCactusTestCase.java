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

import org.apache.cactus.client.connector.ProtocolHandler;
import org.apache.cactus.configuration.ConfigurationInitializer;
import org.apache.cactus.internal.client.ClientTestCaseCaller;
import org.apache.cactus.internal.server.ServerTestCaseCaller;

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
     * Provides all client side Cactus calling logic.
     * Note that we are using a delegate class instead of inheritance in order 
     * to hide non public API to the users and thus to be able to easily change
     * the implementation.
     */
    private ClientTestCaseCaller clientCaller;

    /**
     * Provides all server side Cactus calling logic. 
     * Note that we are using a delegate class instead of inheritance in order 
     * to hide non public API to the users and thus to be able to easily change
     * the implementation.
     */
    private ServerTestCaseCaller serverCaller;

    // Abstract methods -----------------------------------------------------

    /**
     * Create a protocol handler instance that will be used to connect to the
     * server side.
     * 
     * @return the protocol handler instance
     */
    protected abstract ProtocolHandler createProtocolHandler();
    
    // Constructors ---------------------------------------------------------

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
    
    // Public methods -------------------------------------------------------

    /**
     * JUnit method that is used to run the tests. However, we're intercepting
     * it so that we can call the server side of Cactus where the tests will
     * be run (instead of on the client side).
     *
     * @exception Throwable if any exception is thrown during the test. Any
     *            exception will be displayed by the JUnit Test Runner
     */
    public void runBare() throws Throwable
    {
        runBareClient();
    }

    /**
     * @see CactusTestCase#runBareServer()
     */
    public void runBareServer() throws Throwable
    {
        getServerCaller().runBareInit();            

        // Note: We cannot delegate this piece of code in the
        // ServerTestCaseDelegate class as it requires to call
        // super.runBare()

        if (getServerCaller().getWrappedTest() != null)
        {
            ((TestCase) getServerCaller().getWrappedTest()).runBare();
        }
        else
        {
            super.runBare();            
        }
    }
    
    // Private methods ------------------------------------------------------
    
    /**
     * @param theCaller the client test case calling class
     */
    private void setClientCaller(ClientTestCaseCaller theCaller)
    {
        this.clientCaller = theCaller;
    }

    /**
     * @param theCaller the server test case calling class
     */
    private void setServerCaller(ServerTestCaseCaller theCaller)
    {
        this.serverCaller = theCaller;
    }

    /**
     * @return the client test case caller
     */
    private ClientTestCaseCaller getClientCaller()
    {
        return this.clientCaller;
    }

    /**
     * @return the server test case caller
     */
    private ServerTestCaseCaller getServerCaller()
    {
        return this.serverCaller;
    }
    
    /**
     * Initializations common to all constructors.
     *  
     * @param theTest a pure JUnit Test that Cactus will wrap
     */
    private void init(Test theTest)
    {
        setClientCaller(new ClientTestCaseCaller(this, theTest,
            createProtocolHandler()));
        setServerCaller(new ServerTestCaseCaller(this, theTest));
    }

    /**
     * Introduced for symmetry with {@link #runBareServer()}.
     * 
     * @see #runBare()
     */
    private void runBareClient() throws Throwable
    {    
        getClientCaller().runBareInit();            

        // Catch the exception just to have a chance to log it
        try
        {
            getClientCaller().runTest();
        }
        catch (Throwable t)
        {
            // TODO: Move getLogger to this class instead
            getClientCaller().getLogger().debug("Exception in test", t);
            throw t;
        }
    }   
}
