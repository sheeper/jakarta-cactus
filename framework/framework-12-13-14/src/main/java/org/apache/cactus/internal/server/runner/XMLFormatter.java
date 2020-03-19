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
package org.apache.cactus.internal.server.runner;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestListener;
import junit.framework.TestResult;
import org.apache.cactus.internal.util.JUnitVersionHelper;
import org.apache.cactus.internal.util.StringUtil;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Format the test results in XML.
 *
 * @version $Id: XMLFormatter.java 239169 2005-05-05 09:21:54Z vmassol $
 */
public class XMLFormatter implements XMLConstants, TestListener
{
    /**
     * Default stack filter patterns.
     */
    private static final String[] DEFAULT_STACK_FILTER_PATTERNS = new String[]
        {
            "org.apache.cactus.AbstractTestCase",
            "org.apache.cactus.AbstractWebTestCase",
            "org.apache.cactus.FilterTestCase",
            "org.apache.cactus.JspTestCase",
            "org.apache.cactus.ServletTestCase",
            "junit.framework.TestCase",
            "junit.framework.TestResult",
            "junit.framework.TestSuite",
            "junit.framework.Assert.", // don't filter AssertionFailure
            "java.lang.reflect.Method.invoke("
        };

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
     * Encoding to use for the returned XML. Defaults to "UTF-8". 
     */
    private String encoding = "UTF-8";
    
    /**
     * Time current test was started.
     */
    private long currentTestStartTime;

    /**
     * The number format used to convert durations into strings. Don't use the
     * default locale for that, because the resulting string needs to use 
     * dotted decimal notation for an XSLT transformation to work correctly.
     */
    private NumberFormat durationFormat = NumberFormat.getInstance(Locale.US);

    /**
     * XML string containing executed test case results.
     */
    private StringBuilder currentTestCaseResults = new StringBuilder();

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
     * @param theEncoding the encoding to use for the returned XML.
     */
    public void setEncoding(String theEncoding)
    {
        this.encoding = theEncoding;
    }

    /**
     * @return the encoding to use for the returned XML
     */
    public String getEncoding()
    {
        return this.encoding;
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
        return durationFormat.format((double) theDuration / 1000);
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
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"")
                .append(getEncoding())
                .append("\"?>");

        if (this.xslFileName != null)
        {
            xml.append("<?xml-stylesheet type=\"text/xsl\" href=\"")
                    .append(this.xslFileName)
                    .append("\"?>");
        }

        xml.append("<").append(TESTSUITES).append(">");

        xml.append("<").append(TESTSUITE).append(" ")
                .append(ATTR_NAME).append("=\"")
                .append(getSuiteClassName()).append("\" ")
                .append(ATTR_TESTS).append("=\"")
                .append(theResult.runCount()).append("\" ")
                .append(ATTR_FAILURES).append("=\"")
                .append(theResult.failureCount()).append("\" ")
                .append(ATTR_ERRORS).append("=\"")
                .append(theResult.errorCount()).append("\" ")
                .append(ATTR_TIME).append("=\"")
                .append(getTotalDurationAsString()).append("\">");

        xml.append(this.currentTestCaseResults);

        xml.append("</").append(TESTSUITE).append(">");
        xml.append("</").append(TESTSUITES).append(">");

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
        StringBuilder xml = new StringBuilder();

        xml.append("<").append(ERROR).append(" ")
                .append(ATTR_MESSAGE).append("=\"")
                .append(xmlEncode(failure.thrownException().getMessage()))
                .append("\" ")
                .append(ATTR_TYPE).append("=\"")
                .append(failure.thrownException().getClass().getName())
                .append("\">");
        // stacktrace serialization
        xml.append(xmlEncode(StringUtil.exceptionToString(
            failure.thrownException(), DEFAULT_STACK_FILTER_PATTERNS)));
        xml.append("</").append(ERROR).append(">");

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
        StringBuilder xml = new StringBuilder();

        xml.append("<").append(FAILURE).append(" ")
                .append(ATTR_MESSAGE).append("=\"")
                .append(xmlEncode(failure.thrownException().getMessage()))
                .append("\" ")
                .append(ATTR_TYPE).append("=\"")
                .append(failure.thrownException().getClass().getName())
                .append("\">");
        // stacktrace
        xml.append(xmlEncode(StringUtil.exceptionToString(
            failure.thrownException(), DEFAULT_STACK_FILTER_PATTERNS)));
        xml.append("</").append(FAILURE).append(">");

        this.currentTestFailure = xml.toString();
    }

    /**
     * Event called by the base test runner when the test ends.
     *
     * @param theTest the test object being executed
     */
    public void endTest(Test theTest)
    {
        StringBuilder xml = new StringBuilder();
        String duration = getDurationAsString(System.currentTimeMillis()
            - this.currentTestStartTime);

        xml.append("<").append(TESTCASE).append(" ")
                .append(ATTR_NAME).append("=\"")
                .append(JUnitVersionHelper.getTestCaseName(theTest)).append("\" ")
                .append(ATTR_TIME).append("=\"").append(duration).append("\">");

        if (this.currentTestFailure != null)
        {
            xml.append(this.currentTestFailure);
        }

        xml.append("</").append(TESTCASE).append(">");

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
        newString = StringUtil.replace(theString, '&', "&amp;");

        newString = StringUtil.replace(newString, '<', "&lt;");
        newString = StringUtil.replace(newString, '>', "&gt;");
        newString = StringUtil.replace(newString, '\"', "&quot;");

        return newString;
    }
}
