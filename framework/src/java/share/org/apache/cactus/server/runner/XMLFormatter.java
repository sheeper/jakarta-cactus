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
package org.apache.cactus.server.runner;

import java.text.NumberFormat;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestListener;
import junit.framework.TestResult;

import org.apache.cactus.util.JUnitVersionHelper;
import org.apache.cactus.util.StringUtil;

/**
 * Format the test results in XML.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class XMLFormatter implements XMLConstants, TestListener
{
    /**
     * (optional) Name of the XSL stylesheet to put in the returned XML string
     * so that the browser will try to apply it (IE at least, I don't know
     * about the others).
     */
    private String xslFileName;

    /**
     * The name of the test suite class.
     */
    private String suiteClassName;

    /**
     * Duration it took to execute all the tests.
     */
    private long totalDuration;

    /**
     * Time current test was started
     */
    private long currentTestStartTime;

    /**
     * XML string containing executed test case results
     */
    private StringBuffer currentTestCaseResults = new StringBuffer();

    /**
     * Current test failure (XML string) : failure or error.
     */
    private String currentTestFailure;

    /**
     * Sets the XSL stylesheet file name to put in the returned XML string
     * so that the browser will try to apply it (IE at least, I don't know
     * about the others).
     *
     * @param theXslFileName the file name (relative to the webapp root)
     */
    public void setXslFileName(String theXslFileName)
    {
        this.xslFileName = theXslFileName;
    }

    /**
     * @return the suite class name
     */
    public String getSuiteClassName()
    {
        return this.suiteClassName;
    }

    /**
     * Sets the suite class name that was executed.
     *
     * @param theSuiteClassName the suite class name
     */
    public void setSuiteClassName(String theSuiteClassName)
    {
        this.suiteClassName = theSuiteClassName;
    }

    /**
     * @return the total duration as a string
     */
    public String getTotalDurationAsString()
    {
        return getDurationAsString(this.totalDuration);
    }

    /**
     * Comvert a duration expressed as a long into a string.
     *
     * @param theDuration the duration to convert to string
     * @return the total duration as a string
     */
    private String getDurationAsString(long theDuration)
    {
        return NumberFormat.getInstance().format((double) theDuration / 1000);
    }

    /**
     * Sets the duration it took to execute all the tests.
     *
     * @param theDuration the time it took
     */
    public void setTotalDuration(long theDuration)
    {
        this.totalDuration = theDuration;
    }

    /**
     * Formats the test result as an XML string.
     *
     * @param theResult the test result object
     * @return the XML string representation of the test results
     */
    public String toXML(TestResult theResult)
    {
        StringBuffer xml = new StringBuffer();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");

        if (this.xslFileName != null)
        {
            xml.append("<?xml-stylesheet type=\"text/xsl\" " + "href=\""
                + this.xslFileName + "\"?>");
        }

        xml.append("<" + TESTSUITES + ">");

        xml.append("<" + TESTSUITE + " " + ATTR_NAME + "=\""
            + getSuiteClassName() + "\" " + ATTR_TESTS + "=\""
            + theResult.runCount() + "\" " + ATTR_FAILURES + "=\""
            + theResult.failureCount() + "\" " + ATTR_ERRORS + "=\""
            + theResult.errorCount() + "\" " + ATTR_TIME + "=\""
            + getTotalDurationAsString() + "\">");

        xml.append(this.currentTestCaseResults.toString());

        xml.append("</" + TESTSUITE + ">");
        xml.append("</" + TESTSUITES + ">");

        return xml.toString();
    }

    /**
     * Event called by the base test runner when the test starts.
     *
     * @param theTest the test object being executed
     */
    public void startTest(Test theTest)
    {
        this.currentTestStartTime = System.currentTimeMillis();
        this.currentTestFailure = null;
    }

    /**
     * Event called by the base test runner when the test fails with an error.
     *
     * @param theTest the test object that failed
     * @param theThrowable the exception that was thrown
     */
    public void addError(Test theTest, Throwable theThrowable)
    {
        TestFailure failure = new TestFailure(theTest, theThrowable);
        StringBuffer xml = new StringBuffer();

        xml.append("<" + ERROR + " " + ATTR_MESSAGE + "=\""
            + xmlEncode(failure.thrownException().getMessage()) + "\" "
            + ATTR_TYPE + "=\""
            + failure.thrownException().getClass().getName() + "\">");
        xml.append(xmlEncode(StringUtil.exceptionToString(
            failure.thrownException())));
        xml.append("</" + ERROR + ">");

        this.currentTestFailure = xml.toString();
    }

    /**
     * Event called by the base test runner when the test fails with a failure.
     *
     * @param theTest the test object that failed
     * @param theError the exception that was thrown
     */
    public void addFailure(Test theTest, AssertionFailedError theError)
    {
        TestFailure failure = new TestFailure(theTest, theError);
        StringBuffer xml = new StringBuffer();

        xml.append("<" + FAILURE + " " + ATTR_MESSAGE + "=\""
            + xmlEncode(failure.thrownException().getMessage()) + "\" "
            + ATTR_TYPE + "=\""
            + failure.thrownException().getClass().getName() + "\">");
        xml.append(xmlEncode(StringUtil.exceptionToString(
            failure.thrownException())));
        xml.append("</" + FAILURE + ">");

        this.currentTestFailure = xml.toString();
    }

    /**
     * Event called by the base test runner when the test ends.
     *
     * @param theTest the test object being executed
     */
    public void endTest(Test theTest)
    {
        StringBuffer xml = new StringBuffer();
        String duration = getDurationAsString(System.currentTimeMillis()
            - this.currentTestStartTime);

        xml.append("<" + TESTCASE + " " + ATTR_NAME + "=\""
            + JUnitVersionHelper.getTestCaseName(theTest) + "\" "
            + ATTR_TIME + "=\"" + duration + "\">");

        if (this.currentTestFailure != null)
        {
            xml.append(this.currentTestFailure);
        }

        xml.append("</" + TESTCASE + ">");

        this.currentTestCaseResults.append(xml.toString());
    }

    /**
     * Escapes reserved XML characters.
     *
     * @param theString the string to escape
     * @return the escaped string
     */
    private String xmlEncode(String theString)
    {
        String newString;

        // It is important to replace the "&" first as the other replacements
        // also introduces "&" chars ...
        newString = XMLFormatter.replace(theString, '&', "&amp;");

        newString = XMLFormatter.replace(newString, '<', "&lt;");
        newString = XMLFormatter.replace(newString, '>', "&gt;");
        newString = XMLFormatter.replace(newString, '\"', "&quot;");

        return newString;
    }

    /**
     * Replaces a character in a string by a substring.
     *
     * @param theBaseString the base string in which to perform replacements
     * @param theChar the char to look for
     * @param theNewString the string with which to replace the char
     * @return the string with replacements done or null if the input string
     *          was null
     */
    public static String replace(String theBaseString, char theChar, 
        String theNewString)
    {
        if (theBaseString == null)
        {
            return null;
        }

        final int len = theBaseString.length() - 1;
        int pos = -1;

        while ((pos = theBaseString.indexOf(theChar, pos + 1)) > -1)
        {
            if (pos == 0)
            {
                final String after = theBaseString.substring(1);

                theBaseString = theNewString + after;
            }
            else if (pos == len)
            {
                final String before = theBaseString.substring(0, pos);

                theBaseString = before + theNewString;
            }
            else
            {
                final String before = theBaseString.substring(0, pos);
                final String after = theBaseString.substring(pos + 1);

                theBaseString = before + theNewString + after;
            }
        }

        return theBaseString;
    }
}