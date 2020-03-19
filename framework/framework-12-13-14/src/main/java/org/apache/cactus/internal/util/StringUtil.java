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
package org.apache.cactus.internal.util;

import org.apache.cactus.util.ChainedRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Various utility methods for string manipulation.
 *
 * @version $Id: StringUtil.java 239169 2005-05-05 09:21:54Z vmassol $
 */
public class StringUtil
{
    private StringUtil() {
        // hide utility class constructor
    }

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
     * Returns a exception wrapper parsed from the given stacktrace.
     *
     * @param wrapperClazz the exception in which the trace should be wrapped
     * @param stackTrace the stacktrace that as been serialized by printStackTrace
     * @return the exception stack trace as a ExceptionWrapper
     * @throws ChainedRuntimeException if no valid constructor can be found or it could not be used
     */
    public static Throwable stringToException(Class<? extends Throwable> wrapperClazz,
                                              String sourceClass,
                                              String sourceMessage,
                                              String stackTrace) {
        if(stackTrace == null || stackTrace.length() == 0){
            return createThrowable(wrapperClazz,
                    null,
                    sourceClass,
                    sourceMessage,
                    Collections.<StackTraceElement> emptyList());
        } else {
            return parseStackTrace(wrapperClazz, stackTrace);
        }
    }

    /**
     * parse the stack trace to extract all informations about StackTraceElements
     * classes and messages and causes.
     *
     * @param wrapperClazz target wrapper class
     * @param stackTrace   the stackTrace String (created with printStackTrace)
     * @return a wrapper containing all the informations
     */
    private static Throwable parseStackTrace(Class<? extends Throwable> wrapperClazz,
                                             String stackTrace) {
        Throwable result = null;
        // splitting the stack in "Caused by: "
        // separates all the Exceptions in the cause hierarchy
        String[] causeTraces = stackTrace.split("\nCaused by: ");
        for (int i = causeTraces.length - 1; i >= 0; i--) {
            // first lines should always be the exception and the message
            String[] stackLines = causeTraces[i].split("\n");
            int classIdx = stackLines[0].indexOf(": ");
            String sourceClass;
            StringBuilder sourceMessage;
            if (classIdx == -1) {
                sourceClass = stackLines[0];
                sourceMessage = new StringBuilder();
            } else {
                sourceClass = stackLines[0].substring(0, classIdx);
                sourceMessage = new StringBuilder(stackLines[0].substring(classIdx + 2));
            }

            // parsing the stacktrace elements
            List<StackTraceElement> stackTraceList = new ArrayList<StackTraceElement>();
            for (int s = 1; s < stackLines.length; s++) {
                final String line = stackLines[s];
                // stacktrace
                if (line.startsWith("\tat ")) {
                    int sourceStartIdx = line.indexOf('(');
                    int sourceEndIdx = line.indexOf(')', sourceStartIdx);
                    String classAndMethod = line.substring(4, sourceStartIdx);
                    int methodIdx = classAndMethod.lastIndexOf('.');
                    String declaringClass = classAndMethod.substring(0, methodIdx);
                    String methodName = classAndMethod.substring(methodIdx + 1);

                    int lineStartIdx = line.indexOf(':', sourceStartIdx);
                    String fileName;
                    int lineNumber;
                    if (lineStartIdx == -1) {
                        // (File name or Unknown Source or Native Method)
                        fileName = line.substring(sourceStartIdx + 1, sourceEndIdx);
                        lineNumber = -1;
                    } else {
                        fileName = line.substring(sourceStartIdx + 1, lineStartIdx);

                        lineNumber = Integer.parseInt(
                                line.substring(lineStartIdx + 1, sourceEndIdx));
                    }
                    stackTraceList.add(
                            new StackTraceElement(
                                    declaringClass,
                                    methodName,
                                    fileName,
                                    lineNumber));
                } else // extended message
                {
                    sourceMessage.append("\n").append(line);
                }
            }
            result = createThrowable(wrapperClazz,
                    result,
                    sourceClass,
                    sourceMessage.toString(),
                    stackTraceList);
        }
        return result;
    }

    /**
     * Create a Exception with the given informations.
     *
     * @param wrapperClazz   targeted class
     * @param cause          exception cause
     * @param sourceClass    original exception class name
     * @param sourceMessage  original exception message
     * @param stackTraceList original exception stack trace
     * @return the exception wrapper object
     */
    private static Throwable createThrowable(Class<? extends Throwable> wrapperClazz,
                                             Throwable cause,
                                             String sourceClass,
                                             String sourceMessage,
                                             List<StackTraceElement> stackTraceList) {
        try
        {
            Constructor<? extends Throwable> wrapperConstructor =
                    wrapperClazz.getConstructor(
                            String.class, // message
                            String.class, // class
                            StackTraceElement[].class, // stacktrace
                            Throwable.class); // cause

            cause = wrapperConstructor.newInstance(
                    sourceMessage.length() == 0 ?
                            null : sourceMessage
                    , sourceClass
                    , stackTraceList.toArray(
                            new StackTraceElement[stackTraceList.size()])
                    , cause);
        }
        catch (NoSuchMethodException e) {
            throw new ChainedRuntimeException("Failed to get the client "
                    + "exception constructor, for [" +  wrapperClazz + "]", e);
        }
        catch (InstantiationException e) {
            throw new ChainedRuntimeException("Failed to init the client "
                    + "exception [" +  wrapperClazz + "]", e);
        }
        catch (IllegalAccessException e) {
            throw new ChainedRuntimeException("Failed to access the client "
                    + "exception constructor [" +  wrapperClazz + "]", e);
        }
        catch (InvocationTargetException e) {
            throw new ChainedRuntimeException("Failed to invoke the client "
                    + "exception constructor [" +  wrapperClazz + "]", e);
        }
        return cause;
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
        StringBuilder result = new StringBuilder();
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
