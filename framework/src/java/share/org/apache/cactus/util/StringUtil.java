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
package org.apache.cactus.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Various utility methods for string manipulation.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
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
     * out line from the stack trac
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
    static boolean filterLine(String theLine, String[] theFilterPatterns)
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

}
