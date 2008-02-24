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
 * A checked chained exception.
 *
 * @version $Id: ChainedException.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class ChainedException extends Exception
{
    /**
     * Original exception which caused this exception.
     */
    protected Throwable originalException;

    /**
     * Create a <code>ChainedException</code> and set the exception error
     * message.
     *
     * @param theMessage the message of the exception
     */
    public ChainedException(String theMessage)
    {
        this(theMessage, null);
    }

    /**
     * Create a <code>ChainedException</code>, set the exception error
     * message along with the exception object that caused this exception.
     *
     * @param theMessage the detail of the error message
     * @param theException the original exception
     */
    public ChainedException(String theMessage, Throwable theException)
    {
        super(theMessage);
        this.originalException = theException;
    }

    /**
     * Create a <code>ChaineException</code>, and set exception object
     * that caused this exception. The message is set by default to be the one
     * from the original exception.
     *
     * @param theException the original exception
     */
    public ChainedException(Throwable theException)
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
