/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Cactus" and "Apache Software
 *    Foundation" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
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
 *
 */
package org.apache.cactus.integration.ant.util;

import org.apache.commons.logging.Log;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 * Support class that lets classes log to Ant using the Commons Logging API. 
 * 
 * This is not intended to be a general solution, rather as a thin separation
 * layer to not have to pass around full-blown Ant <code>Project</code>, 
 * <code>Target</code> or <code>Task</code> objects just to enable logging.
 * 
 * Note that as there is no log level in Commons-Logging that corresponds to
 * Ant's <em>VERBOSE</em> level (the level between <em>INFO</em> and
 * <em>DEBUG</em>), the <em>TRACE</em> level of Commons-Logging gets mapped to
 * <em>VERBOSE</em>, which is probably inappropriate for components that do not
 * know they are using the <code>AntLog</code> class. 
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public final class AntLog implements Log
{

    // Constants ---------------------------------------------------------------

    /**
     * Singleton log implementation that simply ignores all log requests.
     */
    public static final Log NULL = new Log()
    {

        // Log Implementation --------------------------------------------------

        /**
         * @see org.apache.commons.logging.Log#isFatalEnabled()
         */
        public boolean isFatalEnabled()
        {
            return false;
        }

        /**
         * @see org.apache.commons.logging.Log#fatal(Object)
         */
        public void fatal(Object theMessage)
        {
            // do nothing
        }

        /**
         * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
         */
        public void fatal(Object theMessage, Throwable theThrowable)
        {
            // do nothing
        }

        /**
         * @see org.apache.commons.logging.Log#isErrorEnabled()
         */
        public boolean isErrorEnabled()
        {
            return false;
        }

        /**
         * @see org.apache.commons.logging.Log#error(Object)
         */
        public void error(Object theMessage)
        {
            // do nothing
        }

        /**
         * @see org.apache.commons.logging.Log#error(Object, Throwable)
         */
        public void error(Object theMessage, Throwable theThrowable)
        {
            // do nothing
        }

        /**
         * @see org.apache.commons.logging.Log#isWarnEnabled()
         */
        public boolean isWarnEnabled()
        {
            return false;
        }

        /**
         * @see org.apache.commons.logging.Log#warn(Object)
         */
        public void warn(Object theMessage)
        {
            // do nothing
        }

        /**
         * @see org.apache.commons.logging.Log#warn(Object, Throwable)
         */
        public void warn(Object theMessage, Throwable theThrowable)
        {
            // do nothing
        }

        /**
         * @see org.apache.commons.logging.Log#isInfoEnabled()
         */
        public boolean isInfoEnabled()
        {
            return false;
        }

        /**
         * @see org.apache.commons.logging.Log#info(Object)
         */
        public void info(Object theMessage)
        {
            // do nothing
        }

        /**
         * @see org.apache.commons.logging.Log#info(Object, Throwable)
         */
        public void info(Object theMessage, Throwable theThrowable)
        {
            // do nothing
        }

        /**
         * @see org.apache.commons.logging.Log#isDebugEnabled()
         */
        public boolean isDebugEnabled()
        {
            return false;
        }

        /**
         * @see org.apache.commons.logging.Log#debug(Object)
         */
        public void debug(Object theMessage)
        {
            // do nothing
        }

        /**
         * @see org.apache.commons.logging.Log#debug(Object, Throwable)
         */
        public void debug(Object theMessage, Throwable theThrowable)
        {
            // do nothing
        }

        /**
         * @see org.apache.commons.logging.Log#isTraceEnabled()
         */
        public boolean isTraceEnabled()
        {
            return false;
        }

        /**
         * @see org.apache.commons.logging.Log#trace(Object)
         */
        public void trace(Object theMessage)
        {
            // do nothing
        }

        /**
         * @see org.apache.commons.logging.Log#trace(Object, Throwable)
         */
        public void trace(Object theMessage, Throwable theThrowable)
        {
            // do nothing
        }

    };

    // Instance Variables ------------------------------------------------------

    /**
     * The Ant project.
     */
    private Project project;

    /**
     * The current target, or <code>null</code> if used outside of a target.
     */
    private Target target;

    /**
     * The task, or <code>null</code> if not used by a task.
     */
    private Task task;

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param theTask The Ant task
     */
    public AntLog(Task theTask)
    {
        this.project = theTask.getProject();
        this.task = theTask;
    }

    /**
     * Constructor.
     * 
     * @param theTarget The current target
     */
    public AntLog(Target theTarget)
    {
        this.project = theTarget.getProject();
        this.target = theTarget;
    }

    /**
     * Constructor.
     * 
     * @param theProject The Ant project
     */
    public AntLog(Project theProject)
    {
        this.project = theProject;
    }

    // Log Implementation ------------------------------------------------------

    /**
     * @see org.apache.commons.logging.Log#isFatalEnabled()
     */
    public boolean isFatalEnabled()
    {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    public void fatal(Object theMessage)
    {
        log(theMessage, null, Project.MSG_ERR);
    }

    /**
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    public void fatal(Object theMessage, Throwable theThrowable)
    {
        log(theMessage, theThrowable, Project.MSG_ERR);
    }

    /**
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    public boolean isErrorEnabled()
    {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#error(Object)
     */
    public void error(Object theMessage)
    {
        log(theMessage, null, Project.MSG_ERR);
    }

    /**
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    public void error(Object theMessage, Throwable theThrowable)
    {
        log(theMessage, theThrowable, Project.MSG_ERR);
    }

    /**
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public boolean isWarnEnabled()
    {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    public void warn(Object theMessage)
    {
        log(theMessage, null, Project.MSG_WARN);
    }

    /**
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    public void warn(Object theMessage, Throwable theThrowable)
    {
        log(theMessage, theThrowable, Project.MSG_WARN);
    }

    /**
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    public boolean isInfoEnabled()
    {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#info(Object)
     */
    public void info(Object theMessage)
    {
        log(theMessage, null, Project.MSG_INFO);
    }

    /**
     * @see org.apache.commons.logging.Log#info(Object, Throwable)
     */
    public void info(Object theMessage, Throwable theThrowable)
    {
        log(theMessage, theThrowable, Project.MSG_INFO);
    }

    /**
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public boolean isDebugEnabled()
    {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    public void debug(Object theMessage)
    {
        log(theMessage, null, Project.MSG_DEBUG);
    }

    /**
     * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    public void debug(Object theMessage, Throwable theThrowable)
    {
        log(theMessage, theThrowable, Project.MSG_DEBUG);
    }

    /**
     * @see org.apache.commons.logging.Log#isTraceEnabled()
     */
    public boolean isTraceEnabled()
    {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#trace(Object)
     */
    public void trace(Object theMessage)
    {
        log(theMessage, null, Project.MSG_VERBOSE);
    }

    /**
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    public void trace(Object theMessage, Throwable theThrowable)
    {
        log(theMessage, theThrowable, Project.MSG_VERBOSE);
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Helper method to log to a certain Ant log level.
     * 
     * @param theMessage The log message
     * @param theThrowable The excecption to be logged, or <code>null</code>
     * @param theLogLevel The Ant log level
     */
    private void log(Object theMessage, Throwable theThrowable, int theLogLevel)
    {
        String message = String.valueOf(theMessage);
        if (theThrowable != null)
        {
            message += " (" + theThrowable.getMessage() + ")";
        }
        if (this.task != null)
        {
            this.project.log(this.task, message, theLogLevel);
        }
        else if (this.target != null)
        {
            this.project.log(this.target, message, theLogLevel);
        }
        else
        {
            this.project.log(message, theLogLevel);
        }
    }

}
