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
package org.apache.cactus.server.runner;

import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 * Unit tests for {@link XMLFormatter}.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public final class TestXMLFormatter extends TestCase
{   
    /**
     * Instance to unit test 
     */
    private XMLFormatter formatter;
   
    /**
     * TestResult object used for testing
     */
    private TestResult testResult;
    
    /**
     * Set up common mock behaviors.
     */
    public void setUp()
    {
        formatter = new XMLFormatter();
        testResult = new TestResult();
    }

    /**
     * Verify that calling {@link XMLFormatter#toXML(TestResult)} works
     * when using the default encoding. 
     */
    public void testToXmlEmptyWithDefaultEncoding()
    {
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<testsuites><testsuite name=\"null\" tests=\"0\" failures=\"0\""
            + " errors=\"0\" time=\"0\"></testsuite></testsuites>";
        
        String result = formatter.toXML(testResult);
        assertEquals(expected, result);
    }

    /**
     * Verify that calling {@link XMLFormatter#toXML(TestResult)} works
     * when using a custom encoding of ISO-8859-1. 
     */
    public void testToXmlEmptyWithCustomEncoding()
    {
        String expected = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
        + "<testsuites><testsuite name=\"null\" tests=\"0\" failures=\"0\""
        + " errors=\"0\" time=\"0\"></testsuite></testsuites>";
        
        formatter.setEncoding("ISO-8859-1");
        String result = formatter.toXML(testResult);
        assertEquals(expected, result);
    }
}
