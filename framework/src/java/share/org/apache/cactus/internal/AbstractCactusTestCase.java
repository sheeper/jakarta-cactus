/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
 */
package org.apache.cactus.internal;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.cactus.internal.client.ClientTestCaseCaller;
import org.apache.cactus.internal.configuration.ConfigurationInitializer;
import org.apache.cactus.internal.server.ServerTestCaseCaller;
import org.apache.cactus.spi.client.connector.ProtocolHandler;
import org.apache.cactus.util.TestCaseImplementChecker;

/**
 * Base class for all Cactus test case extensions.
 * 
 * Note: We must not add any method that can be called by the end user to this
 * class as users will those methods and it will create a runtime dependency to 
 * this class. We will then have to break binary compatibility if we wish to
 * move this class around or change its implementation.
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
        TestCaseImplementChecker.checkTestName(this);
        TestCaseImplementChecker.checkTestName(
            getServerCaller().getWrappedTest());

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
