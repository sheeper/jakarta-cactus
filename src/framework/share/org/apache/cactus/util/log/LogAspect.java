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
package org.apache.cactus.util.log;

import org.aspectj.lang.reflect.*;
import org.aspectj.lang.*;

/**
 * Log every entry and exit of methods.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public aspect LogAspect
{
    /**
     * Saves the call depth to print indented logs
     * TODO: move this feature to the log4j wrapper so that all other log
     * message in the code will benefit from it.
     */
    protected static int callDepth = 0;

    /**
     * All calls that take longer than this default duration will have their
     * exact duration logged. Value is in ms.
     */
    protected static long duration = 200;

    /**
     * Log all entries and exits of methods. Also logs returned values when
     * concerned method returns a value.
     */
    Object around() :
        !within(org.apache.cactus.util.log.*) && target(org.apache.cactus.*) && call(* *(..))
    {
        // The class name that uses the method that has been called
        String targetName = thisJoinPoint.getTarget().getClass().getName();

        // The class name that declares the method called (can be different from the class that
        // uses the method - think inheritance).
        String declaringName = thisJoinPoint.getSignature().getDeclaringType().getName();

        Class declaringReturnType = ((MethodSignature)(thisJoinPoint.getSignature())).getReturnType();
        Object result;

        // Only log methods that belong to the cactus codebase
        if (declaringName.startsWith("org.apache.cactus")) {
            Log log = LogService.getInstance().getLog(targetName);
            log.entry(getIndentations() + getFullSignature(thisJoinPoint));

            callDepth++;

            long entryTime = System.currentTimeMillis();

            // Execute the method
            result = proceed();

            long exitTime = System.currentTimeMillis();

            callDepth--;

            // Compute the exit string to print
            StringBuffer exitString = new StringBuffer(getIndentations());
            exitString.append(getFullSignature(thisJoinPoint));

            // Log the result if the declaring method is returning a value
            if (declaringReturnType != Void.TYPE) {
                exitString.append(" = [" + result + "]");
            }

            // Add the time if > default duration
            if ((exitTime - entryTime) >= duration) {
                exitString.append(", duration = " + (exitTime - entryTime) + "ms");
            }

            log.exit(exitString.toString());

        } else {
            result = proceed();
        }

        return result;
    }

    /**
     * @return the full signature of a method
     */
    private String getFullSignature(JoinPoint jp)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(jp.getSignature().getName());
        buffer.append('(');
        Object[] objs = jp.getArgs();
        if (objs.length > 0) {
            for (int i = 0; i < objs.length - 1; i++) {
                buffer.append('[');
                buffer.append(objs[i]);
                buffer.append(']');
                buffer.append(',');
                buffer.append(' ');
            }
            buffer.append('[');
            buffer.append(objs[objs.length - 1]);
            buffer.append(']');
        }
        buffer.append(')');
        return buffer.toString();
    }

    /**
     * Prints spaces for log indentation.
     */
    private static String getIndentations()
    {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < callDepth; i++) {
            buffer.append(' ');
            buffer.append(' ');
        }
        return buffer.toString();
    }

}
