/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.cactus;

import java.io.ByteArrayInputStream;

import java.net.URL;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.cactus.client.WebResponseObjectFactory;
import org.apache.cactus.client.connector.http.HttpProtocolHandler;
import org.apache.cactus.configuration.ServletConfiguration;
import org.apache.cactus.internal.WebRequestImpl;
import org.apache.cactus.internal.client.ClientException;
import org.apache.cactus.internal.client.ClientTestCaseCaller;
import org.apache.cactus.mock.MockHttpURLConnection;
import org.apache.cactus.util.JUnitVersionHelper;

/**
 * Test <code>TestCase</code> class that intercepts all exceptions (and assert
 * them) coming from test case classes that inherits from it. This class is
 * used to unit test the <code>TestAbstractTestCase</code> class.
 *
 * @version $Id$
 */
public abstract class AbstractTestAbstractTestCase extends TestCase 
{   
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
     * Intercepts running test cases to check for normal exceptions.
     *
     * @exception Throwable any error that occurred when calling the test method
     *            for the current test case.
     * @see AbstractTestCase#runTest()
     */
    protected void runTest() throws Throwable
    {
        ClientTestCaseCaller delegator = new ClientTestCaseCaller(
            this, this, new HttpProtocolHandler(new ServletConfiguration()));

        try
        {
            // Call the begin method
            WebRequest request = new WebRequestImpl(new ServletConfiguration());

            delegator.callBeginMethod(request);

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
            delegator.callEndMethod(request, 
                new WebResponseObjectFactory(connection));
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
     * @param theTestName the test name to verify
     * @return true if the test name to verify corresponds to the currently
     *         executing test
     */
    private boolean checkName(String theTestName)
    {
        return JUnitVersionHelper.getTestCaseName(this).equals(
            theTestName);
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
        if (checkName("testBeginMethodBadReturnType"))
        {
            assertEquals("The method "
                + "[beginBeginMethodBadReturnType] should return void and "
                + "not [java.lang.String]", theMessage);

            return true;
        }

        // Test that when a begin method for a given test is not declared
        // public a <code>AssertionFailedError</code> exception is returned.
        if (checkName("testBeginMethodNotPublic"))
        {
            assertEquals("Method [beginBeginMethodNotPublic] should be "
                + "declared public", theMessage);

            return true;
        }

        // Test that when a begin method for a given test has the wrong
        // type of parameters, a <code>AssertionFailedError</code> exception
        // is returned.
        if (checkName("testBeginMethodBadParamType"))
        {
            assertEquals("The method "
                + "[beginBeginMethodBadParamType] must accept "
                + "[org.apache.cactus.Request] as 1st parameter, but "
                + "found a [java.lang.String] parameter instead", theMessage);

            return true;
        }

        // Test that when a begin method for a given test has the wrong
        // number of parameters, a <code>AssertionFailedError</code>
        // exception is returned.
        if (checkName("testBeginMethodBadParamNumber"))
        {
            assertEquals("The method "
                + "[beginBeginMethodBadParamNumber] must have "
                + "1 parameter(s), but 2 parameter(s) were found",
                theMessage);

            return true;
        }

        // Verify that the begin method with a
        // <code>WebRequest</code> parameter is called correctly.
        if (checkName("testBeginMethodOK"))
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
        if (checkName("testEndMethodBadReturnType"))
        {
            assertEquals("The method "
                + "[endEndMethodBadReturnType] should return void and "
                + "not [java.lang.String]", theMessage);

            return true;
        }

        // Test that when an end method for a given test is not declared
        // public a <code>AssertionFailedError</code> exception is returned.
        if (checkName("testEndMethodNotPublic"))
        {
            assertEquals("Method [endEndMethodNotPublic] should be "
                + "declared public", theMessage);

            return true;
        }

        // Test that when an end method for a given test has the wrong
        // type of parameters, a <code>AssertionFailedError</code> exception
        // is returned.
        if (checkName("testEndMethodBadParamType"))
        {
            assertEquals("The method [endEndMethodBadParamType] "
                + "has a bad parameter of type [java.lang.String]", 
                theMessage);

            return true;
        }

        // Test that when an end method for a given test has the wrong
        // number of parameters, a <code>AssertionFailedError</code>
        // exception is returned.
        if (checkName("testEndMethodBadParamNumber"))
        {
            assertEquals("The method [endEndMethodBadParamNumber] "
                + "must have 1 parameter(s), but 2 parameter(s) were found",
                theMessage);

            return true;
        }

        // Test that the end method is called correctly when it's signature
        // contains a <code>org.apache.cactus.WebResponse</code>
        // parameter.
        if (checkName("testEndMethodOK1"))
        {
            assertEquals("endEndMethodOK1", theMessage);

            return true;
        }

        // Test that the end method is called correctly when it's signature
        // contains a <code>com.meterware.httpunit.WebResponse</code>
        // parameter.
        if (checkName("testEndMethodOK2"))
        {
            assertEquals("endEndMethodOK2", theMessage);

            return true;
        }

        // Test that the deprecated end method with the
        // <code>HttpURLConnection</code> parameter can still be called
        // correctly.
        if (checkName("testEndMethodOK3"))
        {
            assertEquals("endEndMethodOK3", theMessage);

            return true;
        }

        return false;
    }

}
