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
package org.apache.cactus.internal.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.Test;

import org.apache.cactus.Request;

/**
 * Utilities to check TestCase implementation.
 * @version $Id$
 */
public final class TestCaseImplementChecker
{
    /**
     * Default constructor that requires that 
     * {@link #setConfiguration(Configuration)} be called before the methods
     * requiring a configuration object.
     * 
     */
    private TestCaseImplementChecker()
    {
    }

    /**
     * Check if the Test to run is properly implemented or not.
     * @param theTest the test to check
     * @throws TestCaseImplementError if has no name
     */
    public static void checkTestName(Test theTest)
        throws TestCaseImplementError
    {
        if (theTest == null)
        {
            return;
        }

        if (JUnitVersionHelper.getTestCaseName(theTest) == null)
        {
            throw new TestCaseImplementError("No test name found. The test ["
                + theTest.getClass().getName()
                + "] is not properly implemented.");
        }
    }

    /**
     * @param theNum the number
     * @return a numeric expresion of theNum
     */
    private static String numeric(int theNum)
    {
        switch(theNum)
        {
        case 1: return "1st";
        case 2: return "2nd";
        case 3: return "3rd";
        default: return (theNum + "th");
        }
    }

    /**
     * @param theMethod the method to check
     * @param theType the expected return type
     * @throws TestCaseImplementError if the return-type is not the one expected
     */
    private static void checkReturnType(Method theMethod, Class theType)
        throws TestCaseImplementError
    {
        if (!theMethod.getReturnType().equals(theType))
        {
            throw new TestCaseImplementError("The method ["
                + theMethod.getName()
                + "] should return " + theType
                + " and not [" + theMethod.getReturnType().getName()
                + "]");
        }
    }

    /**
     * @param theMethod the method to test
     * @throws TestCaseImplementError if the method is not public
     */
    private static void isPublic(Method theMethod)
        throws TestCaseImplementError
    {
        if (!Modifier.isPublic(theMethod.getModifiers()))
        {
            throw new TestCaseImplementError("The method ["
                + theMethod.getName()
                + "] should be declared public");
        }
    }

    /**
     * @param theMethod the method to check
     * @param theParams the expected parameters for the method
     * @throws TestCaseImplementError if the number of parameter is not same as
     *                                that of expected
     */
    private static void checkParameterCount(Method theMethod, Class[] theParams)
        throws TestCaseImplementError
    {
        Class[] parameters = theMethod.getParameterTypes();
        if (parameters.length != theParams.length)
        {
            throw new TestCaseImplementError("The method ["
                + theMethod.getName()
                + "] must have " + theParams.length + " parameter(s), "
                + "but " + parameters.length + " parameter(s) were found");
        }
    }

    /**
     * @param theMethod the method to check
     * @param theParams the expected parameters for the method
     * @throws TestCaseImplementError if the number and type of parameter
     *                                 are not same as those of expected
     */
    private static void checkParameterTypes(Method theMethod, Class[] theParams)
        throws TestCaseImplementError
    {
        checkParameterCount(theMethod, theParams);

        Class[] parameters = theMethod.getParameterTypes();
        for (int i = 0; i < parameters.length; i++)
        {
            Class expected = theParams[i];
            Class actual = parameters[i];
            if (!expected.isAssignableFrom(actual))
            {
                throw new TestCaseImplementError("The method ["
                    + theMethod.getName()
                    + "] must accept [" + expected.getName() + "] as "
                    + numeric(i + 1)
                    + " parameter, but found a ["
                    + actual.getName() + "] parameter instead");
            }
        }
    }

    /**
     * Check if the method is suitable for a test/begin/end method.
     * @param theMethod the method to check
     * @throws TestCaseImplementError if the method is not suitable
     *                                for Cactus test method
     */
    private static void checkAsCactusMethod(Method theMethod)
        throws TestCaseImplementError
    {
        checkReturnType(theMethod, Void.TYPE);
        isPublic(theMethod);
    }

    /**
     * Check if the method is suitable for a begin method.
     * Throws <code>AssertionFailedError</code> if at least one of following
     * conditions is failed:
     * <ul>
     *   <li>return type of the method is void</li>
     *   <li>the method is public</li>
     *   <li>the method accept a parameter of type <code>Request</code></li>
     * </ul>
     * @param theMethod the method to check
     * @throws TestCaseImplementError if the method is not suitable
     *                                for Cactus begin method
     */
    public static void checkAsBeginMethod(Method theMethod)
        throws TestCaseImplementError
    {
        checkAsCactusMethod(theMethod);
        checkParameterTypes(theMethod, new Class[]{Request.class});
    }

    /**
     * Check if the method is suitable for a end method.
     * Throws <code>AssertionFailedError</code> if at least one of following
     * conditions is failed:
     * <ul>
     *   <li>return type of the method is void</li>
     *   <li>the method is public</li>
     *   <li>the method accept one parameter</li>
     * </ul>
     * @param theMethod the method to check
     * @throws TestCaseImplementError if the method is not suitable
     *                                for Cactus end method
     */
    public static void checkAsEndMethod(Method theMethod)
        throws TestCaseImplementError
    {
        checkAsCactusMethod(theMethod);
        checkParameterCount(theMethod, new Class[]{Object.class});
    }
}
