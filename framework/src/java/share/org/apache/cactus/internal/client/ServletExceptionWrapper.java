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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Wrapper around a <code>Throwable</code> object. Whenever an exception occurs
 * in a test case executed on the server side, the text of this exception
 * along with the stack trace as a String are sent back in the HTTP response.
 * This is because some exceptions are not serializable and because the stack
 * trace is implemented as a <code>transient</code> variable by the JDK so it
 * cannot be transported in the response. However, we need to send a real
 * exception object to JUnit so that the exception stack trace will be printed
 * in the JUnit console. This class does this by being a <code>Throwable</code>
 * and overloading the <code>printStackTrace()</code> methods to print a
 * text stack trace.
 *
 * @version $Id$
 */
public class ServletExceptionWrapper extends Throwable
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
    public ServletExceptionWrapper(String theMessage)
    {
        super(theMessage);
    }

    /**
     * Standard throwable constructor.
     */
    public ServletExceptionWrapper()
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
    public ServletExceptionWrapper(String theMessage, String theClassName, 
        String theStackTrace)
    {
        super(theMessage);
        this.className = theClassName;
        this.stackTrace = theStackTrace;
    }

    /**
     * Simulates a printing of a stack trace by printing the string stack trace
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
     * Simulates a printing of a stack trace by printing the string stack trace
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
