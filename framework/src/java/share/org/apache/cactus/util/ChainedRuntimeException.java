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
package org.apache.cactus.util;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Represent an exception that should stop the running test. It is a runtime
 * exception but it will be caught by JUnit so the application will not stop.
 * The test will be reported as failed. It implements chaining.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ChainedRuntimeException extends RuntimeException
{
    /**
     * Original exception which caused this exception.
     */
    protected Throwable originalException;

    /**
     * Create a <code>ChainedRuntimeException</code> and set the exception
     * error message.
     *
     * @param theMessage the message of the exception
     */
    public ChainedRuntimeException(String theMessage)
    {
        this(theMessage, null);
    }

    /**
     * Create a <code>ChainedRuntimeException</code>, set the exception error
     * message along with the exception object that caused this exception.
     *
     * @param theMessage the detail of the error message
     * @param theException the original exception
     */
    public ChainedRuntimeException(String theMessage, Throwable theException)
    {
        super(theMessage);
        this.originalException = theException;
    }

    /**
     * Create a <code>ChainedRuntimeException</code>, and set exception object
     * that caused this exception. The message is set by default to be the one
     * from the original exception.
     *
     * @param theException the original exception
     */
    public ChainedRuntimeException(Throwable theException)
    {
        super(theException.getMessage());
        this.originalException = theException;
    }

    /**
     * Print the full stack trace, including the original exception.
     */
    public void printStackTrace()
    {
        printStackTrace(System.err);
    }

    /**
     * Print the full stack trace, including the original exception.
     *
     * @param thePs the byte stream in which to print the stack trace
     */
    public void printStackTrace(PrintStream thePs)
    {
        super.printStackTrace(thePs);

        if (this.originalException != null)
        {
            this.originalException.printStackTrace(thePs);
        }
    }

    /**
     * Print the full stack trace, including the original exception.
     *
     * @param thePw the character stream in which to print the stack trace
     */
    public void printStackTrace(PrintWriter thePw)
    {
        super.printStackTrace(thePw);

        if (this.originalException != null)
        {
            this.originalException.printStackTrace(thePw);
        }
    }
}
