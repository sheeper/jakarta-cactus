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
package org.apache.cactus.integration.ant.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.taskdefs.CallTarget;

/**
 * A generic container that can be nested in the
 * {@link org.apache.cactus.integration.ant.CactusTask} to support complete 
 * customization of the container lifecycle from a build file.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class GenericContainer extends AbstractContainer
{

    // Inner Classes -----------------------------------------------------------

    /**
     * Class that represents the nested 'startup' and 'shutdown' elements. It
     * supports either an Ant target to delegate to, or a list of nested tasks
     * that are to be executed in order to perform the operation. 
     */
    public class Hook implements TaskContainer
    {
        
        // Instance Variables --------------------------------------------------
        
        /**
         * The target to call when the hook is executed. 
         */
        private String target;

        /**
         * Ordered list of the contained tasks that should be invoked when the
         * hook is executed.
         */
        private List tasks = new ArrayList();

        // Public Methods ------------------------------------------------------
        
        /**
         * Sets the target to call.
         * 
         * @param theTarget The name of the target
         */
        public void setTarget(String theTarget)
        {
            if (!this.tasks.isEmpty())
            {
                throw new BuildException("The generic element supports either "
                    + "a [target] attribute or nested tasks, but not both");
            }
            this.target = theTarget;
        }

        /**
         * @see org.apache.tools.ant.TaskContainer#addTask
         */
        public void addTask(Task theTask) throws BuildException
        {
            if (this.target != null)
            {
                throw new BuildException("The generic element supports either "
                    + "a [target] attribute or nested tasks, but not both");
            }
            this.tasks.add(theTask);
        }

        /**
         * Executes the hook by either calling the specified target, or invoking
         * all nested tasks.
         * 
         * @throws BuildException If thrown by the called target or one of the
         *         nested tasks
         */
        public void execute() throws BuildException
        {
            if (this.target != null)
            {
                CallTarget antCall = (CallTarget) createAntTask("antcall");
                antCall.setInheritAll(true);
                antCall.setInheritRefs(true);
                antCall.init();
                antCall.setTarget(this.target);
                antCall.execute();
            }
            else
            {
                for (Iterator i = this.tasks.iterator(); i.hasNext();)
                {
                    Task task = (Task) i.next();
                    task.perform();
                }
            }
        }

    }

    // Instance Variables ------------------------------------------------------

    /**
     * Name of the container for logging purposes.
     */
    private String name = "Unknown Container";

    /**
     * The hook that is called when the container should be started.
     */
    private Hook startUpHook;

    /**
     * The hook that is called when the container should be shut down.
     */
    private Hook shutDownHook;

    /**
     * The port to which the container should be bound.
     */
    private int port = 8080;

    // Public Methods ----------------------------------------------------------

    /**
     * Creates a nested 'startup' element.
     * 
     * @return The new hook element
     * @throws BuildException If a startup hook has already been added
     */
    public final Hook createStartUp() throws BuildException
    {
        if (isStartUpSet())
        {
            throw new BuildException("The container element supports only one"
                + "nested [startup] element");
        }
        this.startUpHook = new Hook();
        return this.startUpHook;
    }

    /**
     * Creates a nested 'shutdown' element.
     *  
     * @return The new hook element
     * @throws BuildException If a shutdown hook has already been added
     */
    public final Hook createShutDown() throws BuildException
    {
        if (isShutDownSet())
        {
            throw new BuildException("The container element supports only one"
                + "nested [shutdown] element");
        }
        this.shutDownHook = new Hook();
        return this.shutDownHook;
    }

    /**
     * Returns whether a way to start the container has already been set, either
     * as a target, or as a nested task container.
     * 
     * @return <code>true</code> if the shut down procedure has been set
     */
    public boolean isShutDownSet()
    {
        return (this.shutDownHook != null);
    }

    /**
     * Returns whether a way to stop the container has already been set, either
     * as a target, or as a nested task container.
     * 
     * @return <code>true</code> if the start up procedure has been set
     */
    public boolean isStartUpSet()
    {
        return (this.startUpHook != null);
    }

    /**
     * Sets the name of the container for logging purposes.
     * 
     * @param theName The container name
     */
    public final void setName(String theName)
    {
        this.name = theName;
    }

    /**
     * Sets the port to which the container should listen.
     * 
     * @param thePort The port to set
     */
    public final void setPort(int thePort)
    {
        this.port = thePort;
    }

    /**
     * Sets the target to call to start the server.
     *
     * @param theStartTarget the Ant target to call
     */
    public void setStartUpTarget(String theStartUpTarget)
    {
        if (isStartUpSet())
        {
            throw new BuildException("Either specify the [startuptarget] "
                + "attribute or the nested [startup] element, but not both");
        }
        this.startUpHook = new Hook();
        this.startUpHook.setTarget(theStartUpTarget);
    }

    /**
     * Sets the target to call to stop the server.
     *
     * @param theStopTarget the Ant target to call
     */
    public void setShutDownTarget(String theShutDownTarget)
    {
        if (isShutDownSet())
        {
            throw new BuildException("Either specify the [shutdowntarget] "
                + "attribute or the nested [shutdown] element, but not both");
        }
        this.shutDownHook = new Hook();
        this.shutDownHook.setTarget(theShutDownTarget);
    }

    // AbstractContainer Implementation ----------------------------------------

    /**
     * @see org.apache.cactus.integration.ant.container.Container#getName
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * Returns the port to which the container should listen.
     * 
     * @return The port
     */
    public final int getPort()
    {
        return this.port;
    }

    /**
     * Starts up the container by delegating to the startup hook.
     * 
     * @throws BuildException If thrown by the startup hook
     */
    public final void startUp() throws BuildException
    {
        if (this.startUpHook != null)
        {
            this.startUpHook.execute();
        }
    }

    /**
     * Shuts down the container by delegating to the shutdown hook.
     * 
     * @throws BuildException If thrown by the shutdown hook
     */
    public final void shutDown() throws BuildException
    {
        if (this.shutDownHook != null)
        {
            this.shutDownHook.execute();
        }
    }

}
