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
package org.apache.cactus;

import junit.framework.TestCase;
import org.apache.cactus.client.connector.ProtocolHandler;
import org.apache.cactus.client.connector.http.HttpProtocolHandler;
import org.apache.cactus.configuration.BaseConfiguration;
import org.apache.cactus.configuration.ServletConfiguration;
import org.apache.cactus.util.TestCaseImplementError;

/**
 * Unit tests of the <code>AbstractTestCase</code> class and its subclasses.
 *
 * @version $Id$
 */
public class TestNoNameTestCase extends TestCase
{
    /**
     * Sample subclass of AbstractCactusTestCase of which the constructor
     * and setName() don't set test name.
     */
    class NoNameTestCase extends AbstractCactusTestCase
    {
        /**
         * default constructor
         */
        public NoNameTestCase()
        {
        }

        /**
         * Construct without super(theName)
         * @param theName name of the test
         */
        public NoNameTestCase(String theName)
        {
        }

        /**
         * override junit.framework.TestCase#setName(String).
         * @param theName name of the test
         */
        public void setName(String theName)
        {
        }

        /**
         * dummy implementation
         * @return ProtocolHandler always return null
         */
        protected ProtocolHandler createProtocolHandler()
        {
            return new HttpProtocolHandler(new ServletConfiguration());
        }

        /**
         * dummy test entry
         */
        public void testNoName()
        {
        }
    }

    /**
     * Sample subclass of ServletTestCase of which the constructor
     * and setName() don't set test name.
     */
    class NoNameServletTestCase extends ServletTestCase
    {
        /**
         * default constructor
         */
        public NoNameServletTestCase()
        {
        }

        /**
         * Construct without super(theName)
         * @param theName name of the test
         */
        public NoNameServletTestCase(String theName)
        {
        }

        /**
         * override junit.framework.TestCase#setName(String).
         * @param theName name of the test
         */
        public void setName(String theName)
        {
        }

        /**
         * dummy test entry
         */
        public void testNoName()
        {
        }
    }

    /**
     * Sample subclass of JspTestCase of which the constructor
     * and setName() don't set test name.
     */
    class NoNameJspTestCase extends JspTestCase
    {
        /**
         * default constructor
         */
        public NoNameJspTestCase()
        {
        }

        /**
         * Construct without super(theName)
         * @param theName name of the test
         */
        public NoNameJspTestCase(String theName)
        {
        }

        /**
         * override junit.framework.TestCase#setName(String).
         * @param theName name of the test
         */
        public void setName(String theName)
        {
        }

        /**
         * dummy test entry
         */
        public void testNoName()
        {
        }
    }

    /**
     * set cactus.contextURL as a system property.
     */
    public void setUp()
    {
        System.setProperty(BaseConfiguration.CACTUS_CONTEXT_URL_PROPERTY,
            "http://localhost/dummy");
    }

    /**
     * @param theName name of the test
     */
    public TestNoNameTestCase(String theName)
    {
        super(theName);
    }

    /**
     * @param theTest the test to test
     */
    private void executeRunBare(TestCase theTest)
    {
        try
        {
            theTest.runBare();
            fail("test should fail");
        }
        catch (Throwable t)
        {
            assertEquals(TestCaseImplementError.class.getName(),
                t.getClass().getName());
            String message = t.getMessage();
            assertNotNull("no message", message);
            assertEquals("No test name found. "
                + "The test [" + theTest.getClass().getName()
                + "] is not properly implemented.", message);
        }
    }

    /**
     * Test subclass of AbstractCactusTestCase.
     * Set the test name by constructor NoNameTestCase(String).
     */
    public void testNoNameTestCase()
    {
        TestCase test; 
        test = new NoNameTestCase("testNoName");
        executeRunBare(test);
    }

    /**
     * Test subclass of AbstractCactusTestCase.
     * Set the test name by TestCase#setName(String).
     */
    public void testNoNameTestCaseWithSetName()
    {
        TestCase test; 
        test = new NoNameTestCase();
        test.setName("testNoName");
        executeRunBare(test);
    }

    /**
     * Test subclass of ServletTestCase.
     * Set the test name by constructor NoNameTestCase(String).
     */
    public void testNoNameServletTestCase()
    {
        TestCase test; 
        test = new NoNameServletTestCase("testNoName");
        executeRunBare(test);
    }

    /**
     * Test subclass of ServletTestCase.
     * Set the test name by TestCase#setName(String).
     */
    public void testNoNameServletTestCaseWithSetName()
    {
        TestCase test; 
        test = new NoNameServletTestCase();
        test.setName("testNoName");
        executeRunBare(test);
    }

    /**
     * Test subclass of JspTestCase.
     * Set the test name by constructor NoNameTestCase(String).
     */
    public void testNoNameJspTestCase()
    {
        TestCase test; 
        test = new NoNameJspTestCase("testNoName");
        executeRunBare(test);
    }

    /**
     * Test subclass of JspTestCase.
     * Set the test name by TestCase#setName(String).
     */
    public void testNoNameJspTestCaseWithSetName()
    {
        TestCase test; 
        test = new NoNameJspTestCase();
        test.setName("testNoName");
        executeRunBare(test);
    }
}
