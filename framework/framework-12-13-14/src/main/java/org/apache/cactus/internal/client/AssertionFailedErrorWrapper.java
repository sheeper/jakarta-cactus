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
package org.apache.cactus.internal.client;

import java.io.PrintStream;
import java.io.PrintWriter;

import junit.framework.AssertionFailedError;

/**
 * Same as <code>ServletExceptionWrapper</code> except that this exception class
 * extends JUnit <code>AssertionFailedError</code> so that JUnit will
 * print a different message in it's runner console.
 *
 * @version $Id: AssertionFailedErrorWrapper.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class AssertionFailedErrorWrapper extends AssertionFailedError
{
    /**
     * The stack trace that was sent back from the servlet redirector as a
     * string.
     */
    private String stackTrace;

    /**
     * The class name of the exception that was raised on the server side.
     */
    private String className;

    /**
     * Standard throwable constructor.
     *
     * @param theMessage the exception message
     */
    public AssertionFailedErrorWrapper(String theMessage)
    {
        super(theMessage);
    }

    /**
     * Standard throwable constructor.
     */
    public AssertionFailedErrorWrapper()
    {
        super();
    }

    /**
     * The constructor to use to simulate a real exception.
     *
     * @param theMessage the server exception message
     * @param theClassName the server exception class name
     * @param theStackTrace the server exception stack trace
     */
    public AssertionFailedErrorWrapper(String theMessage, String theClassName, 
        String theStackTrace)
    {
        super(theMessage);
        this.className = theClassName;
        this.stackTrace = theStackTrace;
    }

    /**
     * Simulates a printing of a stack trace by printing the string stack trace.
     *
     * @param thePs the stream to which to output the stack trace
     */
    public void printStackTrace(PrintStream thePs)
    {
        if (this.stackTrace == null)
        {
            thePs.print(getMessage());
        }
        else
        {
            thePs.print(this.stackTrace);
        }
    }

    /**
     * Simulates a printing of a stack trace by printing the string stack trace.
     *
     * @param thePw the writer to which to output the stack trace
     */
    public void printStackTrace(PrintWriter thePw)
    {
        if (this.stackTrace == null)
        {
            thePw.print(getMessage());
        }
        else
        {
            thePw.print(this.stackTrace);
        }
    }

    /**
     * @return the wrapped class name
     */
    public String getWrappedClassName()
    {
        return this.className;
    }
}
