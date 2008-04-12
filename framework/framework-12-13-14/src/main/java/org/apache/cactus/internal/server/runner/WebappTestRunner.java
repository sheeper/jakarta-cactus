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
package org.apache.cactus.internal.server.runner;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.runner.BaseTestRunner;

/**
 * JUnit Test Runner that can load test cases that are in the classpath of
 * a webapp. This test runner is supposed to be executed from within the
 * webapp.
 *
 * @version $Id: WebappTestRunner.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class WebappTestRunner extends BaseTestRunner
{
    /**
     * Error message if the suite failed to load.
     */
    private String errorMessage;

    /**
     * Overridden from BaseTestRunner in order to use either the context
     * class loader or the webapp one.
     *
     * @return a loader that loads classes using the context class loader or
     *         the webapp class loader.
     */
    //public TestSuiteLoader getLoader()
    //{
    //    return new WebappTestSuiteLoader();
    //}
    /**
     * Returns the loaded Class for a suite name.
     */
    public Class loadSuiteClass(String theSuiteClassName) 
        throws ClassNotFoundException 
    {
        WebappTestSuiteLoader loader =  new WebappTestSuiteLoader();
        return loader.load(theSuiteClassName);
    }

    /**
     * Event called by the base test runner when it fails to load a test suite.
     *
     * @param theMessage the message of the failure
     */
    protected void runFailed(String theMessage)
    {
        this.errorMessage = theMessage;
    }

    /**
     * @return the error message provided by <code>BaseTestRunner</code> if it
     *         failed to load the test suite
     */
    public String getErrorMessage()
    {
        return this.errorMessage;
    }

    /**
     * Event called by the base test runner when the test ends.
     *
     * @param theTestName the test case name
     */
    public void testEnded(String theTestName)
    {
        // not used
    }

    /**
     * Event called by the base test runner when the test fails.
     *
     * @param theStatus the status code of the error
     * @param theTest the test object that failed
     * @param theThrowable the exception that was thrown
     */
    public void testFailed(int theStatus, Test theTest, Throwable theThrowable)
    {
        // not used
    }

    /**
     * Event called by the base test runner when the test starts.
     *
     * @param theTestName the test case name
     */
    public void testStarted(String theTestName)
    {
        // not used
    }

    /**
     * {@inheritDoc}
     * @see BaseTestRunner#addError(Test, Throwable)
     */
    public void addError(Test theTest, Throwable theThrowable)
    {
        // not used
    }

    /**
     * {@inheritDoc}
     * @see BaseTestRunner#addFailure(Test, AssertionFailedError)
     */
    public void addFailure(Test theTest, 
        AssertionFailedError theAssertionFailedError)
    {
        // not used
    }

    /**
     * {@inheritDoc}
     * @see BaseTestRunner#endTest(Test)
     */
    public void endTest(Test theTest)
    {
        // not used
    }

    /**
     * {@inheritDoc}
     * @see BaseTestRunner#startTest(Test)
     */
    public void startTest(Test theTest)
    {
        // not used
    }
}
