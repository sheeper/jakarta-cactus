/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
     * All objects in the log package. We don't want to log these as they are the object that
     * perform the logging and thus at execution time we would enter an infinite recursive loop.
     */
    pointcut logObjectCalls() :
        execution(public * org.apache.cactus.util.log..*(..));

    /**
     * All public static methods that have parameters.
     */
    pointcut publicStaticMethodsWithParameterCalls() :
        !execution(public static * org.apache.cactus..*()) &&
        execution(public static * org.apache.cactus..*(..));

    /**
     * All public methods that have parameters.
     */
    pointcut publicMethodsWithParameterCalls() :
        !execution(public * org.apache.cactus..*()) &&
        execution(public * org.apache.cactus..*(..));

    /**
     * All public methods that return values
     */
    pointcut publicMethodsWithReturnValueCalls() :
        !execution(public void org.apache.cactus..*(..)) &&
        execution(public * org.apache.cactus..*(..));

    /**
     * Log all entries and exits of static methods that have no return values.
     */
    Object around() :
        !logObjectCalls() && publicMethodsWithParameterCalls() &&
        publicStaticMethodsWithParameterCalls() && !publicMethodsWithReturnValueCalls()
    {
// Get the LOGGER to perform logging
        Log logger = LogService.getInstance().getLog(
            thisJoinPoint.getSignature().getDeclaringType().getName());

        if (logger.isDebugEnabled()) {
            // Log the entry
            logger.entry(getFullSignature(thisJoinPoint));

            // Execute the method
            final Object result = proceed();

            // Log the exit
            logger.exit(thisJoinPoint.getSignature().getName());
            return result;
        }

        return proceed();
    }

    /**
     * Log all entries and exits of non-static methods that have no return values.
     */
    Object around() :
        !logObjectCalls() && publicMethodsWithParameterCalls() &&
        !publicStaticMethodsWithParameterCalls() && !publicMethodsWithReturnValueCalls()
    {
        // The class name that uses the method that has been called
        final String targetName = thisJoinPoint.getTarget().getClass().getName();

// Get the LOGGER to perform logging
        Log logger = LogService.getInstance().getLog(targetName);

        if (logger.isDebugEnabled()) {
            // Log the entry
            logger.entry(getFullSignature(thisJoinPoint));

            // Execute the method
            final Object result = proceed();

            // Log the exit
            logger.exit(thisJoinPoint.getSignature().getName());
            return result;
        }

        return proceed();
    }

    /**
     * Log all entries and exits of static methods that have return values.
     */
    Object around() :
        !logObjectCalls() && publicMethodsWithParameterCalls() &&
        publicMethodsWithReturnValueCalls() && publicStaticMethodsWithParameterCalls()
    {
// Get the LOGGER to perform logging
        Log logger = LogService.getInstance().getLog(
            thisJoinPoint.getSignature().getDeclaringType().getName());

        if (logger.isDebugEnabled()) {
            // Log the entry
            logger.entry(getFullSignature(thisJoinPoint));

            // Execute the method
            final Object result = proceed();

            // Compute the exit string to print
            final StringBuffer exitString = new StringBuffer(thisJoinPoint.getSignature().getName());

            exitString.append(' ');
            exitString.append('=');
            exitString.append(' ');
            exitString.append('[');
            exitString.append(result);
            exitString.append(']');

            // Log the exit
            logger.exit(exitString.toString());
            return result;
        }

        return proceed();
    }

    /**
     * Log all entries and exits of non-static methods that have return values.
     */
    Object around() :
        !logObjectCalls() && publicMethodsWithParameterCalls() &&
        publicMethodsWithReturnValueCalls() && !publicStaticMethodsWithParameterCalls()
    {
        // The class name that uses the method that has been called
        final String targetName = thisJoinPoint.getTarget().getClass().getName();

// Get the LOGGER to perform logging
        Log logger = LogService.getInstance().getLog(targetName);

        if (logger.isDebugEnabled()) {
            // Log the entry
            logger.entry(getFullSignature(thisJoinPoint));

            // Execute the method
            final Object result = proceed();

            // Compute the exit string to print
            final StringBuffer exitString = new StringBuffer(thisJoinPoint.getSignature().getName());

            exitString.append(' ');
            exitString.append('=');
            exitString.append(' ');
            exitString.append('[');
            exitString.append(result);
            exitString.append(']');

            // Log the exit
            logger.exit(exitString.toString());
            return result;
        }

        return proceed();
    }

    /**
     * @return the full signature of a method
     */
    private final String getFullSignature(final JoinPoint jp)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(jp.getSignature().getName());
        buffer.append('(');
        final Object[] objs = jp.getArgs();
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

}
