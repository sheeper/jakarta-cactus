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

import org.apache.cactus.internal.AbstractTestSuite;

import junit.framework.Test;

/**
 * {@link junit.framework.TestSuite} wrapper that wraps all the tests of the 
 * suite in Cactus {@link ServletTestCase} objects.
 *
 * @version $Id: ServletTestSuite.java 238991 2004-05-22 11:34:50Z vmassol $
 * @since 1.5
 */
public class ServletTestSuite extends AbstractTestSuite
{
    /**
     * {@inheritDoc}
     * @see AbstractTestSuite#AbstractTestSuite()
     */
    public ServletTestSuite()
    {
    }

    /**
     * {@inheritDoc}
     * @see AbstractTestSuite#AbstractTestSuite(Class)
     */
    public ServletTestSuite(final Class theClass)
    {
        super(theClass);
    }

    /**
     * {@inheritDoc}
     * @see AbstractTestSuite#AbstractTestSuite(String)
     */
    public ServletTestSuite(String theName)
    {
        super(theName);
    }

    /** 
     * {@inheritDoc} 
     * @see AbstractTestSuite#addTest(Test)
     * 
     * Note: This method is overriden from {@link AbstractTestSuite} because 
     * we do not want to create a binary dependency on end user classes
     * with {@link AbstractTestSuite}.
     */
    public void addTest(Test theTest)
    {
        super.addTest(theTest);
    }

    /**
     * {@inheritDoc}
     * @see AbstractTestSuite#addTestSuite(Class)
     * 
     * Note: This method is overriden from {@link AbstractTestSuite} because 
     * we do not want to create a binary dependency on end user classes
     * with {@link AbstractTestSuite}.
     */
    public void addTestSuite(Class theTestClass)
    {
        super.addTestSuite(theTestClass);
    }
    
    /**
     * {@inheritDoc}
     * @see AbstractTestSuite#createTestSuite(Class)
     */
    protected Test createTestSuite(Class theTestClass)
    {
        return new ServletTestSuite(theTestClass);
    }

    /**
     * {@inheritDoc}
     * @see AbstractTestSuite#createCactusTestCase(String, Test)
     */
    protected Test createCactusTestCase(String theName, Test theTest)
    {
        return new ServletTestCase(theName, theTest); 
    }
}
