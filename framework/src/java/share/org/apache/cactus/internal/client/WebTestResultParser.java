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

import org.apache.cactus.WebTestResult;

/**
 * Parse a string representing a Test result and transform it into a
 * <code>WebTestResult</code> object.
 *
 * @see WebTestResult
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
