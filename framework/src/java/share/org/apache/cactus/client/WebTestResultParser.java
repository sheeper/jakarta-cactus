/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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

import org.apache.cactus.WebTestResult;

/**
 * Parse a string representing a Test result and transform it into a
 * <code>WebTestResult</code> object.
 *
 * @see WebTestResult
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class WebTestResultParser
{
    /**
     * Parsed exception class name
     */
    protected String exceptionClassname;

    /**
     * Parsed exception message
     */
    protected String exceptionMessage;

    /**
     * Parsed exception stack trace
     */
    protected String exceptionStacktrace;

    /**
     * Parse a string and transform it into a <code>WebTestResult</code> object.
     *
     * @param theData the string to parse
     * @return the <code>WebTestResult</code> object corresponding to the data
     *         string
     * @exception ParsingException if an error happens during parsing
     */
    public WebTestResult parse(String theData) throws ParsingException
    {
        String buffer;
        WebTestResult result;

        buffer = readRootElement(theData);

        if (buffer.length() == 0)
        {
            result = new WebTestResult();
        }
        else
        {
            buffer = readExceptionClassname(buffer);
            buffer = readExceptionMessage(buffer);
            buffer = readExceptionStacktrace(buffer);
            result = new WebTestResult(this.exceptionClassname, 
                this.exceptionMessage, this.exceptionStacktrace);
        }

        return result;
    }

    /**
     * Read the {@link WebTestResult#XML_ROOT_ELEMENT} portion.
     *
     * @param theData the string buffer to parse
     * @return the string buffer minus what has been read
     * @exception ParsingException if an error happens during parsing
     */
    protected String readRootElement(String theData) throws ParsingException
    {
        String startRootString = "<" + WebTestResult.XML_ROOT_ELEMENT + ">";
        String endRootString = "</" + WebTestResult.XML_ROOT_ELEMENT + ">";
        String buffer;

        // It is possible that some end of line character are inserted at the
        // end of the string. This is valid, which is why we trim the string
        // before perfoming the checks.
        String trimmedData = theData.trim();

        if (trimmedData.startsWith(startRootString)
            && trimmedData.endsWith(endRootString))
        {
            buffer = trimmedData.substring(startRootString.length(), 
                trimmedData.length() - endRootString.length());
        }
        else
        {
            throw new ParsingException(formatError(theData));
        }

        return buffer;
    }

    /**
     * Read the {@link WebTestResult#XML_EXCEPTION_CLASSNAME_ATTRIBUTE} portion
     * and extract the exception classname.
     *
     * @param theData the string buffer to parse
     * @return the string buffer minus what has been read
     * @exception ParsingException if an error happens during parsing
     */
    protected String readExceptionClassname(String theData)
        throws ParsingException
    {
        String startString = "<" + WebTestResult.XML_EXCEPTION_ELEMENT + " "
            + WebTestResult.XML_EXCEPTION_CLASSNAME_ATTRIBUTE + "=\"";
        String endString = "</" + WebTestResult.XML_EXCEPTION_ELEMENT + ">";
        String buffer;

        if (theData.startsWith(startString) && theData.endsWith(endString))
        {
            int pos = theData.indexOf('\"', startString.length());

            this.exceptionClassname = theData.substring(startString.length(), 
                pos);
            buffer = theData.substring(startString.length()
                + this.exceptionClassname.length() + 2, 
                theData.length() - endString.length());
        }
        else
        {
            throw new ParsingException(formatError(theData));
        }

        return buffer;
    }

    /**
     * Read the {@link WebTestResult#XML_EXCEPTION_MESSAGE_ELEMENT} portion
     * and extract the exception message.
     *
     * @param theData the string buffer to parse
     * @return the string buffer minus what has been read
     * @exception ParsingException if an error happens during parsing
     */
    protected String readExceptionMessage(String theData)
        throws ParsingException
    {
        String startString = "<" + WebTestResult.XML_EXCEPTION_MESSAGE_ELEMENT
            + "><![CDATA[";
        String endString = "]]></"
            + WebTestResult.XML_EXCEPTION_MESSAGE_ELEMENT + ">";
        String buffer;

        if (theData.startsWith(startString))
        {
            int pos = theData.indexOf(endString, startString.length());

            this.exceptionMessage = theData.substring(startString.length(), 
                pos);
            buffer = theData.substring(pos + endString.length());
        }
        else
        {
            throw new ParsingException(formatError(theData));
        }

        return buffer;
    }

    /**
     * Read the {@link WebTestResult#XML_EXCEPTION_STACKTRACE_ELEMENT} portion
     * and extract the exception stacktrace.
     *
     * @param theData the string buffer to parse
     * @return the string buffer minus what has been read
     * @exception ParsingException if an error happens during parsing
     */
    protected String readExceptionStacktrace(String theData)
        throws ParsingException
    {
        String startString = "<"
            + WebTestResult.XML_EXCEPTION_STACKTRACE_ELEMENT + "><![CDATA[";
        String endString = "]]></"
            + WebTestResult.XML_EXCEPTION_STACKTRACE_ELEMENT + ">";
        String buffer;

        if (theData.startsWith(startString))
        {
            int pos = theData.indexOf(endString, startString.length());

            this.exceptionStacktrace = theData.substring(startString.length(), 
                pos);
            buffer = theData.substring(pos + endString.length());
        }
        else
        {
            throw new ParsingException(formatError(theData));
        }

        return buffer;
    }

    /**
     * @param theData the data to format
     * @return the first 100 characters (or less if the data has fewer
     *        characters) of the invalid data as it can be very big
     */
    private String formatError(String theData)
    {
        int nbChars = theData.length() > 100 ? 100 : theData.length();

        return "Not a valid response. First " + nbChars 
            + " characters of the reponse: ["
            + theData.substring(0, nbChars) + "]";        
    }
}