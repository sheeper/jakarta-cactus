/* 
 * ========================================================================
 * 
 * Copyright 2001-2005 The Apache Software Foundation.
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
package org.apache.cactus.internal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Various utility methods for string manipulation.
 *
 * @version $Id: StringUtil.java 239169 2005-05-05 09:21:54Z vmassol $
 */
public class StringUtil
{
    /**
     * Returns the stack trace of an exception as String.
     * 
     * @param theThrowable the exception from which to extract the stack trace
     *        as a String
     * @return the exception stack trace as a String
     */
    public static String exceptionToString(Throwable theThrowable)
    {
        return exceptionToString(theThrowable, null);
    }

    /**
     * Returns the stack trace of an exception as String, optionally filtering
     * out line from the stack trac.
     * 
     * @param theThrowable the exception from which to extract the stack trace
     *        as a String
     * @param theFilterPatterns Array containing a list of patterns to filter 
     *        out from the stack trace
     * @return the exception stack trace as a String
     */
    public static String exceptionToString(Throwable theThrowable,
                                           String[] theFilterPatterns)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        theThrowable.printStackTrace(pw);
        String stackTrace = sw.toString();
        return filterStackTrace(stackTrace, theFilterPatterns);
    }

    /**
     * 
     * 
     * @param theStackTrace The original, unfiltered stack trace
     * @param theFilterPatterns The patterns to filter out
     * @return The filtered stack trace
     */
    static String filterStackTrace(String theStackTrace,
                                   String[] theFilterPatterns)
    { 
        if ((theFilterPatterns == null) || (theFilterPatterns.length == 0) 
            || (theStackTrace == null)) 
        {
            return theStackTrace;
        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        StringReader stringReader = new StringReader(theStackTrace);
        BufferedReader bufferedReader = new BufferedReader(stringReader);    

        String line;
        try
        {   
            while ((line = bufferedReader.readLine()) != null)
            {
                if (!filterLine(line, theFilterPatterns))
                {
                    printWriter.println(line);
                }
            }
        }
        catch (IOException e)
        {
            return theStackTrace;
        }
        return stringWriter.toString();
    }

    /**
     * 
     * 
     * @param theLine The line to check
     * @param theFilterPatterns The patterns to filter out
     * @return boolean Whether the specified line should be filtered from the
     *         stack trace
     */
    public static boolean filterLine(String theLine, String[] theFilterPatterns)
    {
        for (int i = 0; i < theFilterPatterns.length; i++)
        {
            if (theLine.indexOf(theFilterPatterns[i]) > 0)
            {
                return true;
            }
        }
        return false;
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

        int pos = theBaseString.indexOf(theChar);
        if (pos < 0)
        {
            return theBaseString;
        }
        
        int lastPos = 0;
        StringBuffer result = new StringBuffer();
        while (pos > -1)
        {
            result.append(theBaseString.substring(lastPos, pos));
            result.append(theNewString);
            
            lastPos = pos + 1;
            pos = theBaseString.indexOf(theChar, lastPos);
        }

        if (lastPos < theBaseString.length())
        {
            result.append(theBaseString.substring(lastPos));
        }
        
        return result.toString();
    }
}