/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
package org.apache.cactus.server.runner;

import junit.runner.BaseTestRunner;
import junit.runner.TestSuiteLoader;
import junit.framework.Test;
import junit.framework.AssertionFailedError;

/**
 * JUnit Test Runner that can load test cases that are in the classpath of
 * a webapp. This test runner is supposed to be executed from within the
 * webapp.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
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
    public TestSuiteLoader getLoader()
    {
        return new WebappTestSuiteLoader();
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
     * @see BaseTestRunner#addError(Test, Throwable)
     */
    public void addError(Test theTest, Throwable theThrowable)
    {
        // not used
    }

    /**
     * @see BaseTestRunner#addFailure(Test, AssertionFailedError)
     */
    public void addFailure(Test theTest,
        AssertionFailedError theAssertionFailedError)
    {
        // not used
    }

    /**
     * @see BaseTestRunner#endTest(Test)
     */
    public void endTest(Test theTest)
    {
        // not used
    }

    /**
     * @see BaseTestRunner#startTest(Test)
     */
    public void startTest(Test theTest)
    {
        // not used
    }
}
