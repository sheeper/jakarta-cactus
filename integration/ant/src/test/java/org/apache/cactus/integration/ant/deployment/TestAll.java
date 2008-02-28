/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.deployment;

import org.apache.cactus.integration.ant.deployment.application.TestApplicationXml;
import org.apache.cactus.integration.ant.deployment.application.TestApplicationXmlVersion;
import org.apache.cactus.integration.ant.deployment.webapp.TestWarArchive;
import org.apache.cactus.integration.ant.deployment.webapp.TestWebXml;
import org.apache.cactus.integration.ant.deployment.webapp.TestWebXmlMerger;
import org.apache.cactus.integration.ant.deployment.webapp.TestWebXmlVersion;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all the unit tests for the deployment support classes.
 *
 * @version $Id: TestAll.java 239003 2004-05-31 20:05:27Z vmassol $
 */
public final class TestAll
{
    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(
            "Unit tests for the deployment support classes");

        suite.addTestSuite(TestApplicationXml.class);
        suite.addTestSuite(TestApplicationXmlVersion.class);

        suite.addTestSuite(TestWebXml.class);
        suite.addTestSuite(TestWebXmlMerger.class);
        suite.addTestSuite(TestWebXmlVersion.class);
        suite.addTestSuite(TestWarArchive.class);

        suite.addTestSuite(TestJarArchive.class);
        suite.addTestSuite(TestEarParser.class);

        return suite;
    }
}
