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

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * Abstract base class for containers that perform the starting and stopping
 * of the server by executing Java classes in a forked JVM.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * 
 * @version $Id$
 */
public abstract class AbstractJavaContainer extends AbstractContainer
{
    // Instance Variables ------------------------------------------------------

    /**
     * The file to which output of the container should be written.
     */
    private File output;

    /**
     * Whether output of the container should be appended to an existing file, 
     * or the existing file should be truncated.
     */
    private boolean append;

    // Public Methods ----------------------------------------------------------

    /**
     * Sets the file to which output of the container should be written.
     * 
     * @param theOutput The output file to set
     */
    public final void setOutput(File theOutput)
    {
        this.output = theOutput;
    }

    /**
     * Sets whether output of the container should be appended to an existing
     * file, or the existing file should be truncated.
     * 
     * @param isAppend Whether output should be appended
     */
    public final void setAppend(boolean isAppend)
    {
        this.append = isAppend;
    }

    // Protected Methods -------------------------------------------------------

    /**
     * Creates a preinitialized instance of the Ant Java task to be used for
     * shutting down the container.
     * 
     * @return The created task instance
     */
    protected final Java createJavaForShutDown()
    {
        Java java = (Java) createAntTask("java");
        java.setFork(true);
        return java;
    }

    /**
     * Creates a preinitialized instance of the Ant Java task to be used for
     * starting down the container.
     * 
     * @return The created task instance
     */
    protected final Java createJavaForStartUp()
    {
        Java java = (Java) createAntTask("java");
        java.setFork(true);
        java.setOutput(this.output);
        java.setAppend(this.append);

        // Add Cactus properties for the server side
        for (int i = 0; i < getSystemProperties().length; i++)
        {
            java.addSysproperty(
                createSysProperty(getSystemProperties()[i].getKey(),
                    getSystemProperties()[i].getValue()));
        }

        return java;
    }

    /**
     * Convenience method to create an Ant environment variable that points to
     * a file.
     * 
     * @param theKey The key or name of the variable
     * @param theFile The file the variable should point to
     * @return The created environment variable
     */
    protected final Variable createSysProperty(String theKey, File theFile)
    {
        Variable var = new Variable();
        var.setKey(theKey);
        var.setFile(theFile);
        return var;
    }

    /**
     * Convenience method to create an Ant environment variable that contains
     * a path.
     * 
     * @param theKey The key or name of the variable
     * @param thePath The path
     * @return The created environment variable
     */
    protected final Variable createSysProperty(String theKey, Path thePath)
    {
        Variable var = new Variable();
        var.setKey(theKey);
        var.setPath(thePath);
        return var;
    }

    /**
     * Convenience method to create an Ant environment variable that contains a 
     * string.
     * 
     * @param theKey The key or name of the variable
     * @param theValue The value
     * @return The created environment variable
     */
    protected final Variable createSysProperty(String theKey, String theValue)
    {
        Variable var = new Variable();
        var.setKey(theKey);
        var.setValue(theValue);
        return var;
    }

    /**
     * Returns the file containing the JDK tools (such as the compiler).
     * 
     * @return The tools.jar file
     * @throws FileNotFoundException If the tools.jar file could not be found
     */
    protected final File getToolsJar()
        throws FileNotFoundException
    {
        String javaHome = System.getProperty("java.home"); 
        // TODO: Fix this as it fails on Max OSX (which doesn't have a 
        // tools.jar file).
        File toolsJar = new File(javaHome, "../lib/tools.jar");
        if (!toolsJar.isFile())
        {
            throw new FileNotFoundException(toolsJar.getAbsolutePath());
        }
        return toolsJar;
    }   
}
