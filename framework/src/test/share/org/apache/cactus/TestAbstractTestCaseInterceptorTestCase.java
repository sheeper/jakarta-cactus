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
package org.apache.cactus;

import java.io.ByteArrayInputStream;

import java.net.URL;

import junit.framework.AssertionFailedError;

import org.apache.cactus.client.ClientException;
import org.apache.cactus.mock.MockHttpURLConnection;
import org.apache.cactus.util.Configuration;
import org.apache.cactus.util.ServletConfiguration;
import org.apache.cactus.util.WebConfiguration;

/**
 * Test <code>TestCase</code> class that intercepts all exceptions (and assert
 * them) coming from test case classes that inherits from it. This class is
 * used to unit test the <code>TestAbstractTestCase</code> class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestAbstractTestCaseInterceptorTestCase
    extends AbstractWebTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param theName the name of the test case
     */
    public TestAbstractTestCaseInterceptorTestCase(String theName)
    {
        super(theName);
    }

    /**
     * Override default method so that configuration checks are not run during
     * these unit tests.
     *
     * @exception Throwable if any exception is thrown during the test. Any
     *            exception will be displayed by the JUnit Test Runner
     * @see AbstractTestCase#runBare()
     */
    public void runBare() throws Throwable
    {
        runTest();
    }

    /**
     * @see AbstractTestCase#createConfiguration()
     */
    protected Configuration createConfiguration()
    {
        return new ServletConfiguration();
    }

    /**
     * Intercepts running test cases to check for normal exceptions.
     *
     * @exception Throwable any error that occurred when calling the test method
     *            for the current test case.
     * @see AbstractTestCase#runTest()
     */
    protected void runTest() throws Throwable
    {
        try
        {
            // Call the begin method
            WebRequest request = new WebRequest(
                (WebConfiguration) getConfiguration());

            callBeginMethod(request);

            // Create a mock HttpURLConnection as it is needed by HttpUnit
            // for creating a WebResponse
            MockHttpURLConnection connection = new MockHttpURLConnection(
                new URL("http://something"));

            // Set the values expected by Http Unit. Note: only the test
            // cases that have an end method with an HttpUnit WebReponse
            // will use the HttpURLConnection.
            connection.setExpectedGetHeaderField("HTTP/1.1 200 OK");
            connection.setExpectedGetInputStream(
                new ByteArrayInputStream("".getBytes()));

            // Create a WebResponse object and call the end method
            callEndMethod(request, connection);
        }
        catch (AssertionFailedError e)
        {
            // Perform asserts
            if (!verifyBeginMethodsOk(e.getMessage())
                && !verifyEndMethodsOk(e.getMessage()))
            {
                throw e;
            }
        }
        catch (ClientException e)
        {
            // Perform asserts
            if (!verifyBeginMethodsOk(e.getMessage())
                && !verifyEndMethodsOk(e.getMessage()))
            {
                throw e;
            }
        }
    }

    /**
     * Assert begin method tests.
     *
     * @param theMessage the error message from the exception
     * @return false is no test matches
     */
    private boolean verifyBeginMethodsOk(String theMessage)
    {
        // Test that when a begin method for a given test does not have the
        // correct return type (i.e. void), a
        // <code>AssertionFailedError</code> exception is returned.
        if (this.getCurrentTestMethod().equals("testBeginMethodBadReturnType"))
        {
            assertEquals("The method "
                + "[beginBeginMethodBadReturnType] should return void and "
                + "not [java.lang.String]", theMessage);

            return true;
        }

        // Test that when a begin method for a given test is not declared
        // public a <code>AssertionFailedError</code> exception is returned.
        if (this.getCurrentTestMethod().equals("testBeginMethodNotPublic"))
        {
            assertEquals("Method [beginBeginMethodNotPublic] should be "
                + "declared public", theMessage);

            return true;
        }

        // Test that when a begin method for a given test has the wrong
        // type of parameters, a <code>AssertionFailedError</code> exception
        // is returned.
        if (this.getCurrentTestMethod().equals("testBeginMethodBadParamType"))
        {
            assertEquals("The method "
                + "[beginBeginMethodBadParamType] must accept a single "
                + "parameter derived from class "
                + "[org.apache.cactus.WebRequest], but "
                + "found a [java.lang.String] parameter instead", theMessage);

            return true;
        }

        // Test that when a begin method for a given test has the wrong
        // number of parameters, a <code>AssertionFailedError</code>
        // exception is returned.
        if (this.getCurrentTestMethod().equals("testBeginMethodBadParamNumber"))
        {
            assertEquals("The method "
                + "[beginBeginMethodBadParamNumber] must accept a single "
                + "parameter derived from class "
                + "[org.apache.cactus.WebRequest], but 2 "
                + "parameters were found", theMessage);

            return true;
        }

        // Verify that the begin method with a
        // <code>WebRequest</code> parameter is called correctly.
        if (this.getCurrentTestMethod().equals("testBeginMethodOK"))
        {
            assertEquals("beginBeginMethodOK", theMessage);

            return true;
        }

        return false;
    }

    /**
     * Assert end method tests.
     *
     * @param theMessage the error message from the exception
     * @return false is no test matches
     */
    private boolean verifyEndMethodsOk(String theMessage)
    {
        // Test that when an end method for a given test does not have the
        // correct return type (i.e. void), a
        // <code>AssertionFailedError</code> exception is returned.
        if (this.getCurrentTestMethod().equals("testEndMethodBadReturnType"))
        {
            assertEquals("The method "
                + "[endEndMethodBadReturnType] should return void and "
                + "not [java.lang.String]", theMessage);

            return true;
        }

        // Test that when an end method for a given test is not declared
        // public a <code>AssertionFailedError</code> exception is returned.
        if (this.getCurrentTestMethod().equals("testEndMethodNotPublic"))
        {
            assertEquals("Method [endEndMethodNotPublic] should be "
                + "declared public", theMessage);

            return true;
        }

        // Test that when an end method for a given test has the wrong
        // type of parameters, a <code>AssertionFailedError</code> exception
        // is returned.
        if (this.getCurrentTestMethod().equals("testEndMethodBadParamType"))
        {
            assertEquals("The method [endEndMethodBadParamType] "
                + "has a bad parameter of type [java.lang.String]", 
                theMessage);

            return true;
        }

        // Test that when an end method for a given test has the wrong
        // number of parameters, a <code>AssertionFailedError</code>
        // exception is returned.
        if (this.getCurrentTestMethod().equals("testEndMethodBadParamNumber"))
        {
            assertEquals("The method [endEndMethodBadParamNumber] "
                + "must only have a single parameter", theMessage);

            return true;
        }

        // Test that the end method is called correctly when it's signature
        // contains a <code>org.apache.cactus.WebResponse</code>
        // parameter.
        if (this.getCurrentTestMethod().equals("testEndMethodOK1"))
        {
            assertEquals("endEndMethodOK1", theMessage);

            return true;
        }

        // Test that the end method is called correctly when it's signature
        // contains a <code>com.meterware.httpunit.WebResponse</code>
        // parameter.
        if (this.getCurrentTestMethod().equals("testEndMethodOK2"))
        {
            assertEquals("endEndMethodOK2", theMessage);

            return true;
        }

        // Test that the deprecated end method with the
        // <code>HttpURLConnection</code> parameter can still be called
        // correctly.
        if (this.getCurrentTestMethod().equals("testEndMethodOK3"))
        {
            assertEquals("endEndMethodOK3", theMessage);

            return true;
        }

        return false;
    }

}