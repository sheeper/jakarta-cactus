/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all the unit tests for the web.xml support classes.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
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
            org.apache.cactus.integration.ant.container.TestAll.suite());
        suite.addTest(
            org.apache.cactus.integration.ant.deployment.TestAll.suite());

        suite.addTestSuite(TestCactifyWarTask.class);
        suite.addTestSuite(TestCactusTask.class);

        return suite;
    }
}
