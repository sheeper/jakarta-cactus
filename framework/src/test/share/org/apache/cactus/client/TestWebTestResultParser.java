/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Cactus" and "Apache Software
 *    Foundation" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.cactus.client;

import junit.framework.TestCase;

import org.apache.cactus.WebTestResult;

/**
 * Unit tests of the <code>WebTestResultParser</code> class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestWebTestResultParser extends TestCase
{
    // Make sure logging is disabled
    static {
        System.setProperty("org.apache.commons.logging.Log",
            "org.apache.commons.logging.impl.NoOpLog");
    }

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestWebTestResultParser(String theName)
    {
        super(theName);
    }

    //-------------------------------------------------------------------------

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