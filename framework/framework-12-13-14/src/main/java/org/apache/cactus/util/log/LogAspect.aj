/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.cactus.util.log;

import org.aspectj.lang.reflect.*;
import org.aspectj.lang.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Log every entry and exit of methods.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id: LogAspect.aj 238902 2004-04-10 18:21:30Z vmassol $
 */
public aspect LogAspect
{
    /**
     * All objects in the log package. We don't want to log these as they are
     * the object that perform the logging and thus at execution time we would
     * enter an infinite recursive loop.
     */
    pointcut logObjectCalls() :
        execution(public * org.apache.cactus.internal.util.log..*(..))
        || execution(public * org.apache.cactus.internal.util.ClassLoaderUtils.loadPropertyResourceBundle(..));

    /**
     * All public static methods that have parameters.
     */
    pointcut publicStaticMethodsWithParameterCalls() :
        !execution(public static * org.apache.cactus..*())
        && execution(public static * org.apache.cactus..*(..));

    /**
     * All public methods that have parameters.
     */
    pointcut publicMethodsWithParameterCalls() :
        !execution(public * org.apache.cactus..*())
        && execution(public * org.apache.cactus..*(..));

    /**
     * All public methods that return values
     */
    pointcut publicMethodsWithReturnValueCalls() :
        !execution(public void org.apache.cactus..*(..))
        && execution(public * org.apache.cactus..*(..));

    /**
     * Log all entries and exits of static methods that have no return values.
     */
    Object around() :
        !logObjectCalls()
        && publicMethodsWithParameterCalls()
        && publicStaticMethodsWithParameterCalls()
        && !publicMethodsWithReturnValueCalls()
    {
        // Get The logger to perform logging
        Log logger =
            LogFactory.getLog(thisJoinPoint.getSignature().getDeclaringType());

        if (logger.isDebugEnabled())
        {
            // Log the entry
            logger.debug('<' + getFullSignature(thisJoinPoint));

            // Execute the method
            final Object result = proceed();

            // Log the exit
            logger.debug('>' + thisJoinPoint.getSignature().getName());
            return result;
        }

        return proceed();
    }

    /**
     * Log all entries and exits of non-static methods that have no return
     * values.
     */
    Object around() :
        !logObjectCalls()
        && publicMethodsWithParameterCalls()
        && !publicStaticMethodsWithParameterCalls()
        && !publicMethodsWithReturnValueCalls()
    {
        // The class that uses the method that has been called
        final Class target = thisJoinPoint.getTarget().getClass();

        // Get The logger to perform logging
        Log logger = LogFactory.getLog(target);

        if (logger.isDebugEnabled())
        {
            // Log the entry
            logger.debug('<' + getFullSignature(thisJoinPoint));

            // Execute the method
            final Object result = proceed();

            // Log the exit
            logger.debug('>' + thisJoinPoint.getSignature().getName());
            return result;
        }

        return proceed();
    }

    /**
     * Log all entries and exits of static methods that have return values.
     */
    Object around() :
        !logObjectCalls()
        && publicMethodsWithParameterCalls()
        && publicMethodsWithReturnValueCalls()
        && publicStaticMethodsWithParameterCalls()
    {
        // Get The logger to perform logging
        Log logger =
            LogFactory.getLog(thisJoinPoint.getSignature().getDeclaringType());

        if (logger.isDebugEnabled())
        {
            // Log the entry
            logger.debug('<' + getFullSignature(thisJoinPoint));

            // Execute the method
            final Object result = proceed();

            // Compute the exit string to print
            final StringBuffer exitString =
                new StringBuffer(thisJoinPoint.getSignature().getName());

            exitString.append(' ');
            exitString.append('=');
            exitString.append(' ');
            exitString.append('[');
            exitString.append(result);
            exitString.append(']');

            // Log the exit
            logger.debug('>' + exitString.toString());
            return result;
        }

        return proceed();
    }

    /**
     * Log all entries and exits of non-static methods that have return values.
     */
    Object around() :
        !logObjectCalls()
        && publicMethodsWithParameterCalls()
        && publicMethodsWithReturnValueCalls()
        && !publicStaticMethodsWithParameterCalls()
    {
        // The class that uses the method that has been called
        final Class target = thisJoinPoint.getTarget().getClass();

        // Get The logger to perform logging
        Log logger = LogFactory.getLog(target);

        if (logger.isDebugEnabled())
        {
            // Log the entry
            logger.debug('<' + getFullSignature(thisJoinPoint));

            // Execute the method
            final Object result = proceed();

            // Compute the exit string to print
            final StringBuffer exitString =
                new StringBuffer(thisJoinPoint.getSignature().getName());

            exitString.append(' ');
            exitString.append('=');
            exitString.append(' ');
            exitString.append('[');
            exitString.append(result);
            exitString.append(']');

            // Log the exit
            logger.debug('>' + exitString.toString());
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
        if (objs.length > 0)
        {
            for (int i = 0; i < objs.length - 1; i++)
            {
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
