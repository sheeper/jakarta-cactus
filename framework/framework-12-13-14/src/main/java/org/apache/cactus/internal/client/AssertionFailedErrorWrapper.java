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

import junit.framework.AssertionFailedError;

import java.io.PrintStream;
import java.io.PrintWriter;

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
     * The class name of the exception that was raised on the server side.
     */
    private final String className;

    /**
     * Standard throwable constructor.
     *
     * @param theMessage the exception message
     */
    public AssertionFailedErrorWrapper(String theMessage)
    {
        super(theMessage);
        this.className = getClass().getName();
    }

    /**
     * Standard throwable constructor.
     */
    public AssertionFailedErrorWrapper()
    {
        super();
        this.className = getClass().getName();
    }

    /**
     * The constructor to use to simulate a real exception.
     *
     * @param theMessage the server exception message
     * @param theClassName the server exception class name
     * @param theStackTrace the server exception stack trace
     */
    public AssertionFailedErrorWrapper(String theMessage, String theClassName, 
        StackTraceElement[] theStackTrace, Throwable cause)
    {
        super(theMessage);
        initCause(cause);
        setStackTrace(theStackTrace);
        this.className = theClassName;
    }

    /**
     * @return the wrapped class name
     */
    public String getWrappedClassName()
    {
        return this.className;
    }

    public String toString() {
        String s = getWrappedClassName();
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
