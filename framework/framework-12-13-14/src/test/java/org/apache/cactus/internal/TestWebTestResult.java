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
package org.apache.cactus.internal;

import junit.framework.TestCase;

/**
 * Unit tests of the <code>WebTestResult</code> class.
 *
 * @version $Id: TestWebTestResult.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class TestWebTestResult extends TestCase
{
    /**
     * Verify the correctness of the XML representation for a test result with
     * no error.
     */
    public void testToXmlNoException()
    {
        WebTestResult result = new WebTestResult();

        assertEquals("<webresult></webresult>", result.toXml());
    }

    /**
     * Verify the correctness of the XML representation for a test result with
     * an exception.
     */
    public void testToXmlWithException()
    {
        String expectedStart = "<webresult><exception classname=\""
            + "java.lang.Exception\"><message><![CDATA[test exception]]>"
            + "</message><stacktrace><![CDATA[";
        String expectedEnd = "]]></stacktrace></exception></webresult>";

        Exception e = new Exception("test exception");
        WebTestResult result = new WebTestResult(e);

        assertTrue("Should have started with [" + expectedStart + "]", 
            result.toXml().startsWith(expectedStart));
        assertTrue("Should have ended with [" + expectedEnd + "]", 
            result.toXml().endsWith(expectedEnd));
    }
}
