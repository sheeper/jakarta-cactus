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
package org.apache.cactus.util;

import junit.framework.TestCase;

/**
 * Unit tests for the {@link StringUtil} class.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class TestStringUtil extends TestCase
{
    /**
     * Verify package-based stack-trace filtering.
     */
    public void testFilterLinePackageTrue()
    {
        String[] filterPatterns = new String[] {"my.package" };
        assertTrue(StringUtil.filterLine(
            "    at my.package.MyClass.method(MyClass.java:100)",
            filterPatterns));
    }
    
    /**
     * Verify package-based stack-trace filtering.
     */
    public void testFilterLinePackageFalse()
    {
        String[] filterPatterns = new String[] {"my.package" };
        assertTrue(!StringUtil.filterLine(
            "    at other.package.MyClass.method(MyClass.java:100)",
            filterPatterns));
    }

    /**
     * Verify class-based stack-trace filtering.
     */
    public void testFilterLineClassTrue()
    {
        String[] filterPatterns = new String[] {"my.package.MyClass" };
        assertTrue(StringUtil.filterLine(
            "    at my.package.MyClass.method(MyClass.java:100)",
            filterPatterns));
    }

    /**
     * Verify class-based stack-trace filtering.
     */
    public void testFilterLineClassFalse1()
    {
        String[] filterPatterns = new String[] {"my.package.MyClass" };
        assertTrue(!StringUtil.filterLine(
            "    at my.package.OtherClass.method(MyClass.java:100)",
            filterPatterns));
    }

    /**
     * Verify class-based stack-trace filtering.
     */
    public void testFilterLineClassFalse2()
    {
        String[] filterPatterns = new String[] {"my.package.MyClass" };
        assertTrue(!StringUtil.filterLine(
            "    at other.package.MyClass.method(MyClass.java:100)",
            filterPatterns));
    }

}
