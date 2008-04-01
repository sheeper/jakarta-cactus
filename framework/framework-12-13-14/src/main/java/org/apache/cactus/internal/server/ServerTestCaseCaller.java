/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cactus.internal.server;

import junit.framework.Assert;
import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provide the ability to execute Cactus test case classes on the server side.
 * It mimics the JUnit behavior by calling <code>setUp()</code>, 
 * <code>testXXX()</code> and <code>tearDown()</code> methods on the server
 * side.
 *
 * @version $Id: ServerTestCaseCaller.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class ServerTestCaseCaller extends Assert
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
     * Pure JUnit Test Case that we are wrapping (if any).
     */
    private Test wrappedTest;

    /**
     * @param theDelegatedTest the test we are delegating for
     * @param theWrappedTest the test being wrapped by this delegate (or null 
     *        if none)
     */
    public ServerTestCaseCaller(Test theDelegatedTest, Test theWrappedTest) 
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
     * Perform server side initializations before each test, such as
     * initializating the logger.
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
}
