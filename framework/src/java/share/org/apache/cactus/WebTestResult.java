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
package org.apache.cactus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Serializable;

/**
 * Represent the result of the execution of the Test class by the
 * server redirector.If any exception was raised during the test, it
 * is saved by this class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class WebTestResult implements Serializable
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
    public WebTestResult()
    {
    }

    /**
     * Constructor to call when an exception was raised during the test.
     *
     * @param theException the raised exception.
     */
    public WebTestResult(Throwable theException)
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
    public WebTestResult(String theClassName, String theMessage,
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

        if (hasException()) {
            buffer.append("Test failed, Exception message = ["
                + getExceptionMessage() + "]");
        } else {
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

        if (hasException()) {
            xmlText.append("<" + XML_EXCEPTION_ELEMENT + " "
                + XML_EXCEPTION_CLASSNAME_ATTRIBUTE + "=\"");
            xmlText.append(this.exceptionClassName);
            xmlText.append("\">");
            xmlText.append("<" + XML_EXCEPTION_MESSAGE_ELEMENT + "><![CDATA[");
            xmlText.append(this.exceptionMessage);
            xmlText.append("]]></" + XML_EXCEPTION_MESSAGE_ELEMENT + ">");
            xmlText.append("<" + XML_EXCEPTION_STACKTRACE_ELEMENT
                + "><![CDATA[");
            xmlText.append(this.exceptionStackTrace);
            xmlText.append("]]></" + XML_EXCEPTION_STACKTRACE_ELEMENT + ">");
            xmlText.append("</" + XML_EXCEPTION_ELEMENT + ">");
        }

        xmlText.append("</" + XML_ROOT_ELEMENT + ">");

        return xmlText.toString();
    }
}