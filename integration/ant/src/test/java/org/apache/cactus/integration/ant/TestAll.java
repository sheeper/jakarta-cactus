/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cactus.integration.ant;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all the unit tests for the web.xml support classes.
 *
 * @version $Id: TestAll.java 394301 2006-04-15 15:07:26Z felipeal $
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
            "Unit tests for the Ant integration classes");
        suite.addTest(
            org.apache.cactus.integration.ant.deployment.TestAll.suite());

        suite.addTestSuite(TestCactifyWarTask.class);
        suite.addTestSuite(TestCactusTestTask.class);
        //suite.addTestSuite(TestRunServerTestsTask.class);

        return suite;
    }
}
