package org.apache.cactus.internal;
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
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Prototype of EJBRedirector for Cactus.
 * @author Siddhartha P. Chandurkar (siddhartha@visioncodified.com)
 */
public class EJBTestResult 
{
    /**
         * Name of the exception class if an error occurred
         */
    private String exceptionClassName;

    /**
     * Save the stack trace as text because otherwise it will not be
     * transmitted back to the client (the stack trac field in the
     * <code>Throwable</code> class is transient).
     */
    private String exceptionStackTrace;

    /**
     * The exception message if an error occurred
     */
    private String exceptionMessage;

    /**
     * Name of Root XML tag (see {@link #toXml()}).
     */
    public static final String XML_ROOT_ELEMENT = "webresult";

    /**
     * Name of Exception XML tag (see {@link #toXml()}).
     */
    public static final String XML_EXCEPTION_ELEMENT = "exception";

    /**
     * Name of Exception XML attribute that contains the exception classname
     * (see {@link #toXml()}).
     */
    public static final String XML_EXCEPTION_CLASSNAME_ATTRIBUTE = "classname";

    /**
     * Name of Exception Message XML tag (see {@link #toXml()}).
     */
    public static final String XML_EXCEPTION_MESSAGE_ELEMENT = "message";

    /**
     * Name of Exception Stacktrace XML tag (see {@link #toXml()}).
     */
    public static final String XML_EXCEPTION_STACKTRACE_ELEMENT = "stacktrace";

    /**
     * Constructor to call when the test was ok and no error was raised.
     */
    public EJBTestResult() 
    {
    }

    /**
     * Constructor to call when an exception was raised during the test.
     *
     * @param theException the raised exception.
     */
    public EJBTestResult(Throwable theException) 
    {
        this.exceptionClassName = theException.getClass().getName();
        this.exceptionMessage = theException.getMessage();

        // Save the stack trace as text
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        theException.printStackTrace(pw);
        this.exceptionStackTrace = sw.toString();
    }

    /**
     * Constructor used to reconstruct a WebTestResult object from its String
     * representation.
     *
     * @param theClassName the class name of the exception thrown on the server
     *        side
     * @param theMessage the message of the exception thrown on the server side
     * @param theStackTrace the stack trace of the exception thrown on the
     *        server side
     */
    public EJBTestResult(
        String theClassName,
        String theMessage,
        String theStackTrace) 
    {
        this.exceptionClassName = theClassName;
        this.exceptionMessage = theMessage;
        this.exceptionStackTrace = theStackTrace;
    }

    /**
     * @return the exception class name if an exception was raised or
     *         <code>null</code> otherwise.
     */
    public String getExceptionClassName() 
    {
        return this.exceptionClassName;
    }

    /**
     * @return the exception message if an exception was raised or
     *         <code>null</code> otherwise.
     */
    public String getExceptionMessage() 
    {
        return this.exceptionMessage;
    }

    /**
     * @return true if an exception was raised during the test, false otherwise.
     */
    public boolean hasException() 
    {
        return (this.exceptionClassName != null);
    }

    /**
     * @return the stack trace as a string
     */
    public String getExceptionStackTrace() 
    {
        return this.exceptionStackTrace;
    }

    /**
     * @see Object#toString()
     */
    public String toString() 
    {
        StringBuffer buffer = new StringBuffer();

        if (hasException()) 
        {
            buffer.append(
                "Test failed, Exception message = ["
                    + getExceptionMessage()
                    + "]");
        } 
        else 
        {
            buffer.append("Test ok");
        }

        return buffer.toString();
    }

    /**
     * @return an XML representation of the test result to be sent in the
     *         HTTP response to the Cactus client.
     */
    public String toXml() 
    {
        StringBuffer xmlText = new StringBuffer();
        xmlText.append("<" + XML_ROOT_ELEMENT + ">");

        if (hasException()) 
        {
            xmlText.append(
                "<"
                    + XML_EXCEPTION_ELEMENT
                    + " "
                    + XML_EXCEPTION_CLASSNAME_ATTRIBUTE
                    + "=\"");
            xmlText.append(this.exceptionClassName);
            xmlText.append("\">");
            xmlText.append("<" + XML_EXCEPTION_MESSAGE_ELEMENT + "><![CDATA[");
            xmlText.append(this.exceptionMessage);
            xmlText.append("]]></" + XML_EXCEPTION_MESSAGE_ELEMENT + ">");
            xmlText.append(
                "<" + XML_EXCEPTION_STACKTRACE_ELEMENT + "><![CDATA[");
            xmlText.append(this.exceptionStackTrace);
            xmlText.append("]]></" + XML_EXCEPTION_STACKTRACE_ELEMENT + ">");
            xmlText.append("</" + XML_EXCEPTION_ELEMENT + ">");
        }

        xmlText.append("</" + XML_ROOT_ELEMENT + ">");

        return xmlText.toString();
    }
}
