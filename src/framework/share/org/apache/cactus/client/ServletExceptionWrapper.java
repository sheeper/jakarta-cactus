/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */
package org.apache.cactus.client;

import java.io.*;

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
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
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
        if (this.stackTrace == null) {
            thePs.print(getMessage());
        } else {
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
        if (this.stackTrace == null) {
            thePw.print(getMessage());
        } else {
            thePw.print(this.stackTrace);
        }
    }

    /**
     * As all the server exceptions are wrapped into this
     * <code>ServletExceptionWrapper</code> class, we need to be able to
     * know the original server exception class.
     *
     * @param theClass the class to compare with the server exception class
     */
    public boolean instanceOf(Class theClass)
    {
        if (this.className == null) {
            return false;
        }

        return theClass.getName().equals(this.className);
    }

}