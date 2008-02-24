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
package org.apache.cactus.internal.server.runner;

/**
 * XML constants for outputting the JUnit test results in XML.
 *
 * Note: This class was copied from the Jakarta Ant project and heavily
 * adapted for Cactus.
 *
 * @version $Id: XMLConstants.java 238991 2004-05-22 11:34:50Z vmassol $
 *
 * @see XMLFormatter
 */
public interface XMLConstants
{
    /**
     * Root element for all test suites.
     */
    String TESTSUITES = "testsuites";

    /**
     * A single test suite results.
     */
    String TESTSUITE = "testsuite";

    /**
     * A single testcase element.
     */
    String TESTCASE = "testcase";

    /**
     * The error element (for a test case).
     */
    String ERROR = "error";

    /**
     * The failure element (for a test case).
     */
    String FAILURE = "failure";

    /**
     * Name attribute for property, testcase and testsuite elements.
     */
    String ATTR_NAME = "name";

    /**
     * Time attribute for testcase and testsuite elements.
     */
    String ATTR_TIME = "time";

    /**
     * Errors attribute for testsuite elements.
     */
    String ATTR_ERRORS = "errors";

    /**
     * Failures attribute for testsuite elements.
     */
    String ATTR_FAILURES = "failures";

    /**
     * Tests attribute for testsuite elements (number of tests executed).
     */
    String ATTR_TESTS = "tests";

    /**
     * Type attribute for failure and error elements.
     */
    String ATTR_TYPE = "type";

    /**
     * Message attribute for failure elements (message of the exception).
     */
    String ATTR_MESSAGE = "message";
}
