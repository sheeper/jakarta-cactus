/*
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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */
package org.apache.cactus;

import java.io.ByteArrayInputStream;
import java.net.URL;

import junit.framework.AssertionFailedError;

import org.apache.cactus.mock.MockHttpURLConnection;

/**
 * Test <code>TestCase</code> class that intercepts all exceptions (and assert
 * them) coming from test case classes that inherits from it. This class is
 * used to unit test the <code>TestAbstractTestCase</code> class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestAbstractTestCase_InterceptorTestCase
    extends AbstractTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param theName the name of the test case
     */
    public TestAbstractTestCase_InterceptorTestCase(String theName)
    {
        super(theName);
    }

    /**
     * Override default method so that configuration checks are not run during
     * these unit tests.
     */
    public void runBare() throws Throwable
    {
        runTest();
    }

    /**
     * Intercepts running test cases to check for normal exceptions.
     */
    protected void runTest() throws Throwable
    {
        try {
            // Call the begin method
            WebRequest request = new WebRequest();
            callBeginMethod(request);

            // Create a mock HttpURLConnection as it is needed by HttpUnit
            // for creating a WebResponse
            MockHttpURLConnection connection = new MockHttpURLConnection(
                new URL("http://something"));

            // Set the values expected by Http Unit. Note: only the test
            // cases that have an end method with an HttpUnit WebReponse
            // will use the HttpURLConnection.
            connection.setExpectedGetHeaderField("HTTP/1.1 200 OK");
            connection.setExpectedGetInputStream(new ByteArrayInputStream(
                "".getBytes()));

            // Create a WebResponse object and call the end method
            callEndMethod(request, connection);

        } catch (AssertionFailedError e) {

            // ------ Tests for begin methods --------------------------------

            // Test that when a begin method for a given test does not have the
            // correct return type (i.e. void), a
            // <code>AssertionFailedError</code> exception is returned.
            if (this.getCurrentTestMethod().equals("testBeginMethodBadReturnType")) {
                assertEquals("The begin method " +
                    "[beginBeginMethodBadReturnType] should return void and " +
                    "not [java.lang.String]", e.getMessage());
                return;
            }

            // Test that when a begin method for a given test is not declared
            // public a <code>AssertionFailedError</code> exception is returned.
            if (this.getCurrentTestMethod().equals("testBeginMethodNotPublic")) {
                assertEquals("Method [beginBeginMethodNotPublic] should be " +
                    "declared public", e.getMessage());
                return;
            }

            // Test that when a begin method for a given test has the wrong
            // type of parameters, a <code>AssertionFailedError</code> exception
            // is returned.
            if (this.getCurrentTestMethod().equals("testBeginMethodBadParamType")) {
                assertEquals("The begin method " +
                    "[beginBeginMethodBadParamType] must accept a single " +
                    "parameter derived from class " +
                    "[org.apache.cactus.WebRequest], but " +
                    "found a [java.lang.String] parameter instead",
                    e.getMessage());
                return;
            }

            // Test that when a begin method for a given test has the wrong
            // number of parameters, a <code>AssertionFailedError</code>
            // exception is returned.
            if (this.getCurrentTestMethod().equals("testBeginMethodBadParamNumber")) {
                assertEquals("The begin method " +
                    "[beginBeginMethodBadParamNumber] must accept a single " +
                    "parameter derived from class " +
                    "[org.apache.cactus.WebRequest], but 2 " +
                    "parameters were found",
                    e.getMessage());
                return;
            }

            // Verify that the begin method with a
            // <code>WebRequest</code> parameter is called correctly.
            if (this.getCurrentTestMethod().equals("testBeginMethodOK")) {
                assertEquals("beginBeginMethodOK", e.getMessage());
                return;
            }

            // ------ Tests for end methods ----------------------------------

            // Test that when an end method for a given test does not have the
            // correct return type (i.e. void), a
            // <code>AssertionFailedError</code> exception is returned.
            if (this.getCurrentTestMethod().equals("testEndMethodBadReturnType")) {
                assertEquals("The end method " +
                    "[endEndMethodBadReturnType] should return void and " +
                    "not [java.lang.String]", e.getMessage());
                return;
            }

            // Test that when an end method for a given test is not declared
            // public a <code>AssertionFailedError</code> exception is returned.
            if (this.getCurrentTestMethod().equals("testEndMethodNotPublic")) {
                assertEquals("Method [endEndMethodNotPublic] should be " +
                    "declared public", e.getMessage());
                return;
            }

            // Test that when an end method for a given test has the wrong
            // type of parameters, a <code>AssertionFailedError</code> exception
            // is returned.
            if (this.getCurrentTestMethod().equals("testEndMethodBadParamType")) {
                assertEquals("The end method [endEndMethodBadParamType] " +
                    "has a bad parameter of type [java.lang.String]",
                    e.getMessage());
                return;
            }

            // Test that when an end method for a given test has the wrong
            // number of parameters, a <code>AssertionFailedError</code>
            // exception is returned.
            if (this.getCurrentTestMethod().equals("testEndMethodBadParamNumber")) {
                assertEquals("The end method [endEndMethodBadParamNumber] " +
                    "must only have a single parameter", e.getMessage());
                return;
            }

            // Test that the end method is called correctly when it's signature
            // contains a <code>org.apache.cactus.WebResponse</code>
            // parameter.
            if (this.getCurrentTestMethod().equals("testEndMethodOK1")) {
                assertEquals("endEndMethodOK1", e.getMessage());
                return;
            }

            // Test that the end method is called correctly when it's signature
            // contains a <code>com.meterware.httpunit.WebResponse</code>
            // parameter.
            if (this.getCurrentTestMethod().equals("testEndMethodOK2")) {
                assertEquals("endEndMethodOK2", e.getMessage());
                return;
            }

            // Test that the deprecated end method with the
            // <code>HttpURLConnection</code> parameter can still be called
            // correctly.
            if (this.getCurrentTestMethod().equals("testEndMethodOK3")) {
                assertEquals("endEndMethodOK3", e.getMessage());
                return;
            }

            // If the exception is unknown, let it be raised ...
            throw e;

        }

    }
}
