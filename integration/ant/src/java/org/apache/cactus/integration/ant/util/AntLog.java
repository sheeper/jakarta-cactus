/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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
