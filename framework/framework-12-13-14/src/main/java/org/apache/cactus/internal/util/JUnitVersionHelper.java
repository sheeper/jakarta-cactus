/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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

import org.apache.cactus.util.ChainedRuntimeException;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Work around for some changes to the public JUnit API between
 * different JUnit releases.
 *
 * @version $Id: JUnitVersionHelper.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class JUnitVersionHelper
{
    /**
     * The <code>Method</code> to use to get the test name from a
     * <code>TestCase</code> object.
     */
    private static Method testCaseName = null;

    static
    {
        try
        {
            testCaseName = TestCase.class.getMethod("getName", new Class[0]);
        }
        catch (NoSuchMethodException e)
        {
            // pre JUnit 3.7
            try
            {
                testCaseName = TestCase.class.getMethod("name", new Class[0]);
            }
            catch (NoSuchMethodException e2)
            {
                throw new ChainedRuntimeException("Cannot find method name()");
            }
        }
    }

    /**
     * JUnit 3.7 introduces TestCase.getName() and subsequent versions
     * of JUnit remove the old name() method.  This method provides
     * access to the name of a TestCase via reflection that is
     * supposed to work with version before and after JUnit 3.7.
     *
     * @param theTest the test case for which to retrieve the name
     * @return the test case name
     */
    public static String getTestCaseName(Test theTest)
    {
        String name;
        
        if (theTest instanceof TestCase && (testCaseName != null))
        {
            try
            {
                name = (String) testCaseName.invoke(theTest, new Object[0]);
            }
            catch (Throwable e)
            {
                name = "unknown";
            }
        }
        else
        {
            name = "unknown";
        }

        return name;
    }
}
