/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
package org.apache.cactus.sample.unit;

import java.io.Serializable;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.client.AssertionFailedErrorWrapper;
import org.apache.cactus.client.ServletExceptionWrapper;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;

/**
 * Verifies the correct handling of exceptions that happen when running
 * inside the server. Specifically verifies that serializable,
 * non-serializable and {@link AssertionFailedError} exceptions are 
 * correctly propagated to the client side.
 *
 * @see
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestServerSideExceptions extends ServletTestCase
{
    /**
     * Not serializable exception.
     */
    public class NotSerializableException extends Exception
    {
        /**
         * @param theMessage the exception message
         */
        public NotSerializableException(String theMessage)
        {
            super(theMessage);
        }
    }

    /**
     * Serializable exception.
     */
    public class SerializableException extends Exception 
        implements Serializable
    {
        /**
         * @param theMessage the exception message
         */
        public SerializableException(String theMessage)
        {
            super(theMessage);
        }
    }

    /**
     * Intercepts running test cases to check for normal exceptions.
     * 
     * @exception Throwable on test failure
     */
    protected void runTest() throws Throwable
    {
        try
        {
            super.runTest();
        }
        catch (AssertionFailedErrorWrapper e)
        {
            // If the test case is "testAssertionFailedError" and the exception
            // is of type AssertionFailedError and contains the text
            // "test assertion failed error", then the test is ok.
            if (this.getCurrentTestMethod().equals("testAssertionFailedError"))
            {
                if (e.instanceOf(AssertionFailedError.class))
                {
                    assertEquals("test assertion failed error", e.getMessage());

                    return;
                }
            }

            // If the test case is "testComparisonFailure" and the exception
            // is of type ComparisonFailure and contains the text
            // "test comparison failure", then the test is ok.
            else if (
                this.getCurrentTestMethod().equals("testComparisonFailure"))
            {
                if (e.instanceOf(AssertionFailedError.class))
                {
                    assertEquals("test comparison failure", e.getMessage());

                    return;
                }
            }

        }
        catch (ServletExceptionWrapper e)
        {
            // If the test case is "testExceptionNotSerializable" and the
            // exception is of type
            // TestServletTestCaseHelper1_ExceptionNotSerializable
            // and contains the text "test non serializable exception", then
            // the test is ok.
            if (this.getCurrentTestMethod().equals(
                "testExceptionNotSerializable"))
            {
                if (e.instanceOf(NotSerializableException.class))
                {
                    assertEquals("test non serializable exception", 
                        e.getMessage());

                    return;
                }
            }

            // If the test case is "testExceptionSerializable" and the exception
            // is of type TestServletTestCaseHelper1_ExceptionSerializable
            // and contains the text "test serializable exception", then
            // the test is ok.
            if (this.getCurrentTestMethod().equals("testExceptionSerializable"))
            {
                assertTrue(e.instanceOf(SerializableException.class));

                assertEquals("test serializable exception", e.getMessage());

                return;
            }

            throw e;
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Raises an <code>AssertionFailedError</code> exception. The exception is
     * caught in
     * <code>TestServletTestCase_InterceptorServletTestCase.runTest()</code>.
     * This is to verify that <code>AssertionFailedError</code> raised on the
     * server side are properly propagated on the client side.
     */
    public void testAssertionFailedError()
    {
        throw new AssertionFailedError("test assertion failed error");
    }

    //-------------------------------------------------------------------------

    /**
     * Raises a non serializable exception. The exception is
     * caught in
     * <code>TestServletTestCase_InterceptorServletTestCase.runTest()</code>.
     * This is to verify that non serializable exceptions raised on the
     * server side are properly propagated on the client side.
     *
     * @exception NotSerializableException the non serializable exception to
     *             throw
     */
    public void testExceptionNotSerializable()
        throws NotSerializableException
    {
        throw new NotSerializableException("test non serializable exception");
    }

    //-------------------------------------------------------------------------

    /**
     * Raises a serializable exception. The exception is
     * caught in
     * <code>TestServletTestCase_InterceptorServletTestCase.runTest()</code>.
     * This is to verify that serializable exceptions raised on the
     * server side are properly propagated on the client side.
     *
     * @exception SerializableException the serializable exception to throw
     */
    public void testExceptionSerializable() throws SerializableException
    {
        throw new SerializableException("test serializable exception");
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that the new {@link ComparisonFailure} introduced in JUnit 3.8.1
     * is correctly reported as a failure and not as an error in the Test 
     * Runner when Cactus is used.
     */
    public void testComparisonFailure()
    {
        throw new ComparisonFailure("test comparison failure", "some value", 
            "other value");
    }

}