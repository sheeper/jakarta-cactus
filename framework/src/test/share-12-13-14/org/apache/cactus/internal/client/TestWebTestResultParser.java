/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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
package org.apache.cactus.internal.client;

import junit.framework.TestCase;

import org.apache.cactus.internal.WebTestResult;

/**
 * Unit tests of the <code>WebTestResultParser</code> class.
 *
 * @version $Id$
 */
public class TestWebTestResultParser extends TestCase
{
    /**
     * Verify parsing when the test result contains no exception.
     *
     * @exception ParsingException if error
     */
    public void testParseNoException() throws ParsingException
    {
        WebTestResult initialResult = new WebTestResult();
        WebTestResultParser parser = new WebTestResultParser();
        WebTestResult result = parser.parse(initialResult.toXml());

        assertNotNull(result);
        assertTrue(!result.hasException());
        assertNull(result.getExceptionClassName());
        assertNull(result.getExceptionMessage());
        assertNull(result.getExceptionStackTrace());
    }

    /**
     * Verify parsing when the test result contains an exception.
     *
     * @exception ParsingException if error
     */
    public void testParseWithException() throws ParsingException
    {
        Exception e = new Exception("test exception");
        WebTestResult initialResult = new WebTestResult(e);
        WebTestResultParser parser = new WebTestResultParser();
        WebTestResult result = parser.parse(initialResult.toXml());

        assertNotNull(result);
        assertTrue("There is no exception in the test result !", 
            result.hasException());
        assertEquals("java.lang.Exception", result.getExceptionClassName());
        assertEquals("test exception", result.getExceptionMessage());
        assertTrue("Should not be empty", 
            result.getExceptionStackTrace().length() > 0);
    }

    /**
     * Verify the correct parsing behaviour to extract the ROOT element when
     * there is no exception returned in the test result.
     *
     * @exception ParsingException if error
     */
    public void testReadRootElementEmpty() throws ParsingException
    {
        WebTestResult initialResult = new WebTestResult();
        WebTestResultParser parser = new WebTestResultParser();

        String buffer = parser.readRootElement(initialResult.toXml());

        assertEquals("", buffer);
    }

    /**
     * Verify the correct parsing behaviour to extract the ROOT element when
     * there is an exception returned in the test result.
     *
     * @exception ParsingException if error
     */
    public void testReadRootElementFull() throws ParsingException
    {
        String expectedStart = "<exception classname=\""
            + "java.lang.Exception\"><message><![CDATA[test exception]]>"
            + "</message><stacktrace><![CDATA[";
        String expectedEnd = "]]></stacktrace></exception>";

        Exception e = new Exception("test exception");
        WebTestResult initialResult = new WebTestResult(e);
        WebTestResultParser parser = new WebTestResultParser();

        String buffer = parser.readRootElement(initialResult.toXml());

        assertTrue("Should have started with [" + expectedStart + "]", 
            buffer.startsWith(expectedStart));
        assertTrue("Should have ended with [" + expectedEnd + "]", 
            buffer.endsWith(expectedEnd));
    }

    /**
     * Verify the correct parsing behaviour to extract the exception classname.
     *
     * @exception ParsingException if error
     */
    public void testReadExceptionClassName() throws ParsingException
    {
        String expectedStart = "<message><![CDATA[test exception]]>"
            + "</message><stacktrace><![CDATA[";
        String expectedEnd = "]]></stacktrace>";

        Exception e = new Exception("test exception");
        WebTestResult initialResult = new WebTestResult(e);
        WebTestResultParser parser = new WebTestResultParser();
        String buffer = parser.readRootElement(initialResult.toXml());

        buffer = parser.readExceptionClassname(buffer);
        assertEquals("java.lang.Exception", parser.exceptionClassname);
        assertTrue("Should have started with [" + expectedStart + "]", 
            buffer.startsWith(expectedStart));
        assertTrue("Should have ended with [" + expectedEnd + "]", 
            buffer.endsWith(expectedEnd));
    }

    /**
     * Verify the correct parsing behaviour to extract the exception message.
     *
     * @exception ParsingException if error
     */
    public void testReadExceptionMessage() throws ParsingException
    {
        String expectedStart = "<stacktrace><![CDATA[";
        String expectedEnd = "]]></stacktrace>";

        Exception e = new Exception("test exception");
        WebTestResult initialResult = new WebTestResult(e);
        WebTestResultParser parser = new WebTestResultParser();
        String buffer = parser.readRootElement(initialResult.toXml());

        buffer = parser.readExceptionClassname(buffer);

        buffer = parser.readExceptionMessage(buffer);
        assertEquals("test exception", parser.exceptionMessage);
        assertTrue("Should have started with [" + expectedStart + "]", 
            buffer.startsWith(expectedStart));
        assertTrue("Should have ended with [" + expectedEnd + "]", 
            buffer.endsWith(expectedEnd));
    }
}
