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

import org.apache.cactus.configuration.ConfigurationInitializer;
import org.apache.cactus.server.runner.TestXMLFormatter;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all the unit tests of Cactus that do not need a servlet
 * environment to run. These other tests will be exercised in the sample
 * application.
 *
 * @version $Id$
 */
public class TestAll
{
    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     * @exception Exception on failure to load the cactus properties file
     */
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite(
            "Cactus unit tests not needing servlet engine");

        // Make sure logging configuration properties are initialized so
        // that it is possible to control logging from the outside of the
        // tests.
        ConfigurationInitializer.initialize();
        
        suite.addTestSuite(TestAbstractTestCase.class);
        suite.addTestSuite(TestServletURL.class);
        suite.addTestSuite(TestServletUtil.class);
        suite.addTestSuite(TestWebTestResult.class);
        suite.addTestSuite(TestWebRequest.class);

        suite.addTest(org.apache.cactus.internal.client.TestAll.suite());
        suite.addTest(org.apache.cactus.util.TestAll.suite());

        suite.addTestSuite(TestXMLFormatter.class);
        
        return suite;
    }
}
