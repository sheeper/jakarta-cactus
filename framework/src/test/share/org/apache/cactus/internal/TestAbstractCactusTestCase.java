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
package org.apache.cactus.internal;

import java.net.HttpURLConnection;

import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

/**
 * Unit tests of the {@link AbstractCactusTestCase} class.
 *
 * @version $Id$
 */
public class TestAbstractCactusTestCase 
    extends AbstractTestAbstractCactusTestCase
{
    /**
     * @see #testBeginMethodBadReturnType()
     */
    public String beginBeginMethodBadReturnType(WebRequest theRequest)
    {
        // Return anything just to make the compiler happy ...
        return "";
    }

    /**
     * Test that when a begin method for a given test does not have the correct
     * return type (i.e. void), a <code>AssertionFailedError</code> exception
     * is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCaseInterceptorTestCase</code> class.
     */
    public void testBeginMethodBadReturnType()
    {
        // This method only exist so that a test exist and thus the begin
        // method for that test will be called.
        // Should not reach this point
        fail("Should not reach this point");
    }

    //-------------------------------------------------------------------------

    /**
     * @see #testBeginMethodNotPublic()
     */
    private void beginBeginMethodNotPublic(WebRequest theRequest)
    {
    }

    /**
     * Test that when a begin method for a given test is not declared public
     * a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCaseInterceptorTestCase</code> class.
     */
    public void testBeginMethodNotPublic()
    {
        // This method only exist so that a test exist and thus the begin
        // method for that test will be called.
        // Should not reach this point
        fail("Should not reach this point");
    }

    //-------------------------------------------------------------------------

    /**
     * @see #testBeginMethodBadReturnType()
     */
    public void beginBeginMethodBadParamType(String theDummy)
    {
    }

    /**
     * Test that when a begin method for a given test has the wrong type of
     * parameters, a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCaseInterceptorTestCase</code> class.
     */
    public void testBeginMethodBadParamType()
    {
        // This method only exist so that a test exist and thus the begin
        // method for that test will be called.
        // Should not reach this point
        fail("Should not reach this point");
    }

    //-------------------------------------------------------------------------

    /**
     * @see #testBeginMethodBadParamNumber()
     */
    public void beginBeginMethodBadParamNumber(WebRequest theRequest, 
        String theString)
    {
    }

    /**
     * Test that when a begin method for a given test has the wrong number of
     * parameters, a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCaseInterceptorTestCase</code> class.
     */
    public void testBeginMethodBadParamNumber()
    {
        // This method only exist so that a test exist and thus the begin
        // method for that test will be called.
        // Should not reach this point
        fail("Should not reach this point");
    }

    //-------------------------------------------------------------------------

    /**
     * @see #testBeginMethodOK()
     */
    public void beginBeginMethodOK(WebRequest theRequest)
    {
        // We send an exception just to verify that this code has been reached
        // The exception is intercepted in
        // TestAbstractTestCaseInterceptorTestCase
        fail("beginBeginMethodOK");
    }

    /**
     * Verify that the begin method with a
     * <code>WebRequest</code> parameter is called correctly.
     */
    public void testBeginMethodOK()
    {
    }

    //-------------------------------------------------------------------------

    /**
     * Test that when an end method for a given test does not have the correct
     * return type (i.e. void), a <code>AssertionFailedError</code> exception
     * is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCaseInterceptorTestCase</code> class.
     */
    public void testEndMethodBadReturnType()
    {
        // This method only exist so that a test exist and thus the begin
        // method for that test will be called.
    }

    /**
     * @see #testEndMethodBadReturnType()
     */
    public String endEndMethodBadReturnType(WebResponse theResponse)
    {
        // Return anything just to make the compiler happy ...
        return "";
    }

    //-------------------------------------------------------------------------

    /**
     * Test that when an end method for a given test is not declared public
     * a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCaseInterceptorTestCase</code> class.
     */
    public void testEndMethodNotPublic()
    {
    }

    /**
     * @see #testEndMethodNotPublic()
     */
    private void endEndMethodNotPublic(WebResponse theResponse)
    {
    }

    //-------------------------------------------------------------------------

    /**
     * Test that when an end method for a given test has the wrong type of
     * parameters, a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCaseInterceptorTestCase</code> class.
     */
    public void testEndMethodBadParamType()
    {
    }

    /**
     * @see #testEndMethodBadParamType()
     */
    public void endEndMethodBadParamType(String theDummy)
    {
    }

    //-------------------------------------------------------------------------

    /**
     * Test that when an end method for a given test has the wrong number of
     * parameters, a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCaseInterceptorTestCase</code> class.
     */
    public void testEndMethodBadParamNumber()
    {
    }

    /**
     * @see #testEndMethodBadParamNumber()
     */
    public void endEndMethodBadParamNumber(WebResponse theResponse, 
        String theDummy)
    {
    }

    //-------------------------------------------------------------------------

    /**
     * Test that the end method is called correctly when it's signature
     * contains a <code>org.apache.cactus.WebResponse</code> parameter.
     */
    public void testEndMethodOK1()
    {
    }

    /**
     * @see #testEndMethodOK1()
     */
    public void endEndMethodOK1(WebResponse theResponse)
    {
        // We send an exception just to verify that this code has been reached
        // The exception is intercepted in
        // TestAbstractTestCaseInterceptorTestCase
        fail("endEndMethodOK1");
    }

    //-------------------------------------------------------------------------

    /**
     * Test that the end method is called correctly when it's signature
     * contains a <code>com.meterware.httpunit.WebResponse</code> parameter.
     *
     * Note: We need the Httpunit jar and an XML parser jar on the classpath
     * for this test
     */
    public void testEndMethodOK2()
    {
    }

    /**
     * @see #testEndMethodOK2()
     */
    public void endEndMethodOK2(com.meterware.httpunit.WebResponse theResponse)
    {
        // We send an exception just to verify that this code has been reached
        // The exception is intercepted in
        // TestAbstractTestCaseInterceptorTestCase
        fail("endEndMethodOK2");
    }

    //-------------------------------------------------------------------------

    /**
     * Test that the deprecated end method with the
     * <code>HttpURLConnection</code> parameter can still be called correctly.
     */
    public void testEndMethodOK3()
    {
    }

    /**
     * @see #testEndMethodOK3()
     */
    public void endEndMethodOK3(HttpURLConnection theResponse)
    {
        // We send an exception just to verify that this code has been reached
        // The exception is intercepted in
        // TestAbstractTestCaseInterceptorTestCase
        fail("endEndMethodOK3");
    }
}
