/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.cactus.util;

import java.lang.reflect.Method;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import org.apache.cactus.Request;
import org.apache.cactus.WebResponse;
import org.apache.cactus.internal.util.TestCaseImplementChecker;
import org.apache.cactus.internal.util.TestCaseImplementError;

/**
 * Unit tests for the {@link TestCaseImplementChecker} class.
 *
 * @version $Id$
 */
public class TestTestCaseImplementChecker extends TestCase
{
    /**
     * normal test class for {@link #testCheckTestName}
     */
    class NormalTest extends TestCase
    {
        /**
         * @param theName the name of the test
         */
        public NormalTest(String theName)
        {
            super(theName);
        }

        /**
         * dummy test entry
         */
        public void testDummy()
        {
        }
    }

    /**
     * illegal test class for {@link #testCheckTestName}
     */
    class NoNameTest extends TestCase
    {
        /**
         * @param theName the name of the test
         */
        public NoNameTest(String theName)
        {
        }

        /**
         * dummy test entry
         */
        public void testDummy()
        {
        }
    }

    /**
     * declare methods to be used by {@link #testCheckAsBeginMethod} and
     * {@link #testCheckAsEndMethod}
     */
    class MethodHolder
    {
        /**
         */
        public MethodHolder()
        {
        }
        
        // ---- Begin Method ----
        
        /**
         * @param theRequest a Request
         */
        public void beginNormal(Request theRequest)
        {
        }
        
        /**
         * @param theRequest a Request
         * @return a dummy String
         */
        public String beginReturnsString(Request theRequest)
        {
            return "a string";
        }
        
        /**
         * @param theRequest a Request
         */
        protected void beginProtected(Request theRequest)
        {
        }
        
        /**
         * @param theRequest a Request
         */
        private void beginPrivate(Request theRequest)
        {
        }
        
        /**
         */
        public void beginNoParam()
        {
        }
        
        /**
         * @param theRequest a Request
         * @param theObject a Object
         */
        public void beginWithTwoParams(Request theRequest, Object theObject)
        {
        }
        
        /**
         * @param theString a String
         */
        public void beginWithStringParam(String theString)
        {
        }

        // ---- End Method ----
        
        /**
         * @param theResponse a WebResponse
         */
        public void endNormal(WebResponse theResponse)
        {
        }
        
        /**
         * @param theResponse a WebResponse
         * @return a dummy String
         */
        public String endReturnsString(WebResponse theResponse)
        {
            return "a string";
        }
        
        /**
         * @param theResponse a WebResponse
         */
        protected void endProtected(WebResponse theResponse)
        {
        }
        
        /**
         * @param theResponse a WebResponse
         */
        private void endPrivate(WebResponse theResponse)
        {
        }
        
        /**
         */
        public void endNoParam()
        {
        }
        
        /**
         * @param theResponse a WebResponse
         * @param theObject a Object
         */
        public void endWithTwoParams(WebResponse theResponse, Object theObject)
        {
        }
        
        /**
         * @param theString a String
         */
        public void endWithStringParam(String theString)
        {
        }
    }

    /**
     * @return a "Should not be here" message
     */
    private String shouldNotHere()
    {
        return "shold not be here";
    }

    /**
     * @param theThrowable a throwable
     * @return a "Should not be here" message with throwable information
     */
    private String shouldNotHere(Throwable theThrowable)
    {
        return shouldNotHere() + ": " + theThrowable.getClass().getName()
            + "[" + theThrowable.getMessage() + "]";
    }

    /**
     * @see {TestCaseImplementChecker#checkTestName(junit.framework.Test)}
     */
    public void testCheckTestName()
    {
        Test test;
        try
        {
            test = new NormalTest("testDummy");
            TestCaseImplementChecker.checkTestName(test);
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }

        try
        {
            test = new NoNameTest("testDummy");
            TestCaseImplementChecker.checkTestName(test);
            fail(shouldNotHere());
        }
        catch (TestCaseImplementError e)
        {
            assertEquals("No test name found. The test ["
                + "org.apache.cactus.util"
                + ".TestTestCaseImplementChecker$NoNameTest"
                + "] is not properly implemented.", e.getMessage());
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }
    }

    /**
     * @see {TestCaseImplementChecker#checkAsBeginMethod}
     */
    public void testCheckAsBeginMethod()
    {
        //--------------------------------------------------------------------
        
        try
        {
            Method method = MethodHolder.class.getMethod(
                "beginNormal", new Class[]{Request.class});
            TestCaseImplementChecker.checkAsBeginMethod(method);
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }

        //---------------------------------------------------------------------

        try
        {
            Method method = MethodHolder.class.getMethod(
                "beginReturnsString", new Class[]{Request.class});
            TestCaseImplementChecker.checkAsBeginMethod(method);
            fail(shouldNotHere());
        }
        catch (AssertionFailedError e)
        {
            assertEquals("The method [beginReturnsString] "
                + "should return void and not [java.lang.String]",
                e.getMessage());
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }

        //---------------------------------------------------------------------

        try
        {
            Method method = MethodHolder.class.getDeclaredMethod(
                "beginProtected", new Class[]{Request.class});
            TestCaseImplementChecker.checkAsBeginMethod(method);
            fail(shouldNotHere());
        }
        catch (AssertionFailedError e)
        {
            assertEquals("The method [beginProtected] "
                + "should be declared public", e.getMessage());
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }

        //---------------------------------------------------------------------

        try
        {
            Method method = MethodHolder.class.getDeclaredMethod(
                "beginPrivate", new Class[]{Request.class});
            TestCaseImplementChecker.checkAsBeginMethod(method);
            fail(shouldNotHere());
        }
        catch (AssertionFailedError e)
        {
            assertEquals("The method [beginPrivate] "
                + "should be declared public", e.getMessage());
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }

        //---------------------------------------------------------------------

        try
        {
            Method method = MethodHolder.class.getMethod(
                "beginNoParam", new Class[]{});
            TestCaseImplementChecker.checkAsBeginMethod(method);
            fail(shouldNotHere());
        }
        catch (AssertionFailedError e)
        {
            assertEquals("The method [beginNoParam] must have 1 parameter(s), "
                + "but 0 parameter(s) were found", e.getMessage());
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }

        //---------------------------------------------------------------------

        try
        {
            Method method = MethodHolder.class.getMethod(
                "beginWithTwoParams",
                new Class[]{Request.class, Object.class});
            TestCaseImplementChecker.checkAsBeginMethod(method);
            fail(shouldNotHere());
        }
        catch (AssertionFailedError e)
        {
            assertEquals("The method [beginWithTwoParams] "
                + "must have 1 parameter(s), "
                + "but 2 parameter(s) were found", e.getMessage());
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }

        //---------------------------------------------------------------------
        try
        {
            Method method = MethodHolder.class.getMethod(
                "beginWithStringParam", new Class[]{String.class});
            TestCaseImplementChecker.checkAsBeginMethod(method);
            fail(shouldNotHere());
        }
        catch (AssertionFailedError e)
        {
            assertEquals("The method [beginWithStringParam] "
                + "must accept [org.apache.cactus.Request] "
                + "as 1st parameter, but found a "
                + "[java.lang.String] parameter instead",
                e.getMessage());
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }
    }


    /**
     * @see {TestCaseImplementChecker#checkAsEndMethod
     *       (java.lang.reflect.Method)}
     */
    public void testCheckAsEndMethod()
    {
        //---------------------------------------------------------------------

        try
        {
            Method method = MethodHolder.class.getMethod(
                "endNormal", new Class[]{WebResponse.class});
            TestCaseImplementChecker.checkAsEndMethod(method);
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }

        //---------------------------------------------------------------------

        try
        {
            Method method = MethodHolder.class.getMethod(
                "endReturnsString", new Class[]{WebResponse.class});
            TestCaseImplementChecker.checkAsEndMethod(method);
            fail(shouldNotHere());
        }
        catch (AssertionFailedError e)
        {
            assertEquals("The method [endReturnsString] "
                + "should return void and not [java.lang.String]",
                e.getMessage());
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }

        //---------------------------------------------------------------------

        try
        {
            Method method = MethodHolder.class.getDeclaredMethod(
                "endProtected", new Class[]{WebResponse.class});
            TestCaseImplementChecker.checkAsEndMethod(method);
            fail(shouldNotHere());
        }
        catch (AssertionFailedError e)
        {
            assertEquals("The method [endProtected] "
                + "should be declared public", e.getMessage());
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }

        //---------------------------------------------------------------------

        try
        {
            Method method = MethodHolder.class.getDeclaredMethod(
                "endPrivate", new Class[]{WebResponse.class});
            TestCaseImplementChecker.checkAsEndMethod(method);
            fail(shouldNotHere());
        }
        catch (AssertionFailedError e)
        {
            assertEquals("The method [endPrivate] "
                + "should be declared public", e.getMessage());
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }

        //---------------------------------------------------------------------

        try
        {
            Method method = MethodHolder.class.getMethod(
                "endNoParam", new Class[]{});
            TestCaseImplementChecker.checkAsEndMethod(method);
            fail(shouldNotHere());
        }
        catch (AssertionFailedError e)
        {
            assertEquals("The method [endNoParam] must have 1 parameter(s), "
                + "but 0 parameter(s) were found", e.getMessage());
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }

        //---------------------------------------------------------------------

        try
        {
            Method method = MethodHolder.class.getMethod(
                "endWithTwoParams",
                new Class[]{WebResponse.class, Object.class});
            TestCaseImplementChecker.checkAsEndMethod(method);
            fail(shouldNotHere());
        }
        catch (AssertionFailedError e)
        {
            assertEquals("The method [endWithTwoParams] "
                + "must have 1 parameter(s), "
                + "but 2 parameter(s) were found", e.getMessage());
        }
        catch (Throwable t)
        {
            fail(shouldNotHere(t));
        }
    }
}
