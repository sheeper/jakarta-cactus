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

import org.apache.cactus.internal.TestAbstractCactusTestCase;
import org.apache.cactus.internal.TestWebTestResult;
import org.apache.cactus.internal.client.TestWebTestResultParser;
import org.apache.cactus.internal.configuration.ConfigurationInitializer;
import org.apache.cactus.internal.server.TestServletUtil;
import org.apache.cactus.internal.server.runner.TestXMLFormatter;
import org.apache.cactus.internal.util.TestCookieUtil;
import org.apache.cactus.internal.util.TestIoUtil;
import org.apache.cactus.internal.util.TestStringUtil;
import org.apache.cactus.internal.util.TestTestCaseImplementChecker;
import org.apache.cactus.internal.util.TestUniqueGenerator;
import org.apache.cactus.server.runner.TestServletTestRunner;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all the unit tests of Cactus that do not need a servlet
 * environment to run. These other tests will be exercised in the sample
 * application.
 *
 * @version $Id$
 */
public class TestShareAll
{
    /**
     * @return a test suite (<code>TestSuite</code>) that includes all shared
     *         tests
     * @exception Exception on failure to load the cactus properties file
     */
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite(
            "Cactus unit tests for all J2EE APIs");

        // Make sure logging configuration properties are initialized so
        // that it is possible to control logging from the outside of the
        // tests.
        ConfigurationInitializer.initialize();
        
        suite.addTestSuite(TestNoNameTestCase.class);
        suite.addTestSuite(TestServletURL.class);
        suite.addTestSuite(TestWebRequest.class);

        suite.addTestSuite(TestAbstractCactusTestCase.class);
        suite.addTestSuite(TestWebTestResult.class);

        suite.addTestSuite(TestWebTestResultParser.class);
                
        suite.addTestSuite(TestServletUtil.class);

        suite.addTestSuite(TestXMLFormatter.class);

        suite.addTestSuite(TestCookieUtil.class);
        suite.addTestSuite(TestIoUtil.class);
        suite.addTestSuite(TestStringUtil.class);
        suite.addTestSuite(TestTestCaseImplementChecker.class);
        suite.addTestSuite(TestUniqueGenerator.class);

        suite.addTestSuite(TestServletTestRunner.class);

        return suite;
    }
}
