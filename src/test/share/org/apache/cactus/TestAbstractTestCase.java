/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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

import java.util.*;
import java.net.*;

import junit.framework.*;

import org.apache.cactus.*;
import org.apache.cactus.util.log.*;

/**
 * Unit tests of the <code>AbstractTestCase</code> class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestAbstractTestCase extends
    TestAbstractTestCase_InterceptorTestCase
{
    // Initialize logging system first
    static {
        LogService.getInstance().init(null);
    }

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestAbstractTestCase(String theName)
    {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs)
    {
        junit.ui.TestRunner.main(
            new String[] {TestAbstractTestCase.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite()
    {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestAbstractTestCase.class);
    }

    //-------------------------------------------------------------------------

    /**
     * Test that when a begin method for a given test does not have the correct
     * return type (i.e. void), a <code>AssertionFailedError</code> exception
     * is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
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
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
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
     * Test that when a begin method for a given test is not declared public
     * a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
     */
    private void beginBeginMethodNotPublic(WebRequest theRequest)
    {
    }

    /**
     * Test that when a begin method for a given test is not declared public
     * a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
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
     * Test that when a begin method for a given test has the wrong type of
     * parameters, a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
     */
    public void beginBeginMethodBadParamType(String theDummy)
    {
    }

    /**
     * Test that when a begin method for a given test has the wrong type of
     * parameters, a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
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
     * Test that when a begin method for a given test has the wrong number of
     * parameters, a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
     */
    public void beginBeginMethodBadParamNumber(WebRequest theRequest,
        String theString)
    {
    }

    /**
     * Test that when a begin method for a given test has the wrong number of
     * parameters, a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
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
     * Verify that the begin method with a
     * <code>WebRequest</code> parameter is called correctly.
     */
    public void beginBeginMethodOK(WebRequest theRequest)
    {
        // We send an exception just to verify that this code has been reached
        // The exception is intercepted in
        // TestAbstractTestCase_InterceptorTestCase
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
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
     */
    public void testEndMethodBadReturnType()
    {
        // This method only exist so that a test exist and thus the begin
        // method for that test will be called.
    }

    /**
     * Test that when an end method for a given test does not have the correct
     * return type (i.e. void), a <code>AssertionFailedError</code> exception
     * is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
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
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
     */
    public void testEndMethodNotPublic()
    {
    }

    /**
     * Test that when an end method for a given test is not declared public
     * a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
     */
    private void endEndMethodNotPublic(WebResponse theResponse)
    {
    }

    //-------------------------------------------------------------------------

    /**
     * Test that when an end method for a given test has the wrong type of
     * parameters, a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
     */
    public void testEndMethodBadParamType()
    {
    }

    /**
     * Test that when an end method for a given test has the wrong type of
     * parameters, a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
     */
    public void endEndMethodBadParamType(String theDummy)
    {
    }

    //-------------------------------------------------------------------------

    /**
     * Test that when an end method for a given test has the wrong number of
     * parameters, a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
     */
    public void testEndMethodBadParamNumber()
    {
    }

    /**
     * Test that when an end method for a given test has the wrong number of
     * parameters, a <code>AssertionFailedError</code> exception is returned.
     * Note: the assert is done in the
     * <code>TestAbstractTestCase_InterceptorTestCase</code> class.
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
     * Test that the end method is called correctly when it's signature
     * contains a <code>org.apache.cactus.WebResponse</code> parameter.
     */
    public void endEndMethodOK1(WebResponse theResponse)
    {
        // We send an exception just to verify that this code has been reached
        // The exception is intercepted in
        // TestAbstractTestCase_InterceptorTestCase
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
     * Test that the end method is called correctly when it's signature
     * contains a <code>com.meterware.httpunit.WebResponse</code> parameter.
     *
     * Note: We need the Httpunit jar and an XML parser jar on the classpath
     * for this test
     */
    public void endEndMethodOK2(com.meterware.httpunit.WebResponse theResponse)
    {
        // We send an exception just to verify that this code has been reached
        // The exception is intercepted in
        // TestAbstractTestCase_InterceptorTestCase
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
     * Test that the deprecated end method with the
     * <code>HttpURLConnection</code> parameter can still be called correctly.
     */
    public void endEndMethodOK3(HttpURLConnection theResponse)
    {
        // We send an exception just to verify that this code has been reached
        // The exception is intercepted in
        // TestAbstractTestCase_InterceptorTestCase
        fail("endEndMethodOK3");
    }

}