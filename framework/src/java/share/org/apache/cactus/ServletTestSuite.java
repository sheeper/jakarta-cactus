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
package org.apache.cactus;

import junit.framework.Test;

/**
 * {@link junit.framework.TestSuite} wrapper that wraps all the tests of the 
 * suite in Cactus {@link ServletTestCase} objects.
 *
 * @version $Id$
 * @since 1.5
 */
public class ServletTestSuite extends AbstractTestSuite
{
    /**
     * @see AbstractTestSuite#AbstractTestSuite()
     */
    public ServletTestSuite()
    {
    }

    /**
     * @see AbstractTestSuite#AbstractTestSuite(Class)
     */
    public ServletTestSuite(final Class theClass)
    {
        super(theClass);
    }

    /**
     * @see AbstractTestSuite#AbstractTestSuite(String)
     */
    public ServletTestSuite(String theName)
    {
        super(theName);
    }

    /**
     * @see AbstractTestSuite#createTestSuite(Class)
     */
    protected Test createTestSuite(Class theTestClass)
    {
        return new ServletTestSuite(theTestClass);
    }

    /**
     * @see AbstractTestSuite#createCactusTestCase(String, Test)
     */
    protected Test createCactusTestCase(String theName, Test theTest)
    {
        return new ServletTestCase(theName, theTest); 
    }
}
