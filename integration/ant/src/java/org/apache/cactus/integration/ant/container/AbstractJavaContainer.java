/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation.
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


    /**
     * The arguments for JVM.
     */
    private String jvmArgs;

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


    /**
     * Sets the arguments for JVM.
     *
     * @param theJVMArgs The arguments
     */
    public final void setJVMArgs(String theJVMArgs)
    {
        this.jvmArgs = theJVMArgs;
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

        // Add extra container classpath entries specified by the user.
        addExtraClasspath(java);
        
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

        // pass arguments to the JVM
        if (this.jvmArgs != null) 
        {
            getLog().trace(
                "Passing arguments to the container JVM: " + this.jvmArgs);
            java.setJvmargs(this.jvmArgs);
        }

        // Add extra container classpath entries specified by the user.
        addExtraClasspath(java);
       
        // Add Cactus properties for the server side
        if (getSystemProperties() != null)
        {
            for (int i = 0; i < getSystemProperties().length; i++)
            {
                java.addSysproperty(
                    createSysProperty(getSystemProperties()[i].getKey(),
                        getSystemProperties()[i].getValue()));
            }
        }
        
        return java;
    }

    /**
     * Add extra container classpath entries specified by the user.
     * 
     * @param theJavaCommand the java command used to start/stop the container
     */
    private void addExtraClasspath(Java theJavaCommand)
    {
        Path classpath = theJavaCommand.createClasspath();
        if (getContainerClasspath() != null)
        {
            classpath.addExisting(getContainerClasspath());
        }        
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
     * Returns the file containing the JDK tools (such as the compiler). This
     * method must not be called on Mac OSX as there is no tools.jar file on
     * that platform (everything is included in classes.jar).
     * 
     * @return The tools.jar file
     * @throws FileNotFoundException If the tools.jar file could not be found
     */
    protected final File getToolsJar() throws FileNotFoundException
    {
        String javaHome = System.getProperty("java.home"); 
        File toolsJar = new File(javaHome, "../lib/tools.jar");
        if (!toolsJar.isFile())
        {
            throw new FileNotFoundException(toolsJar.getAbsolutePath());
        }
        return toolsJar;
    }   
 
    /**
     * Adds the tools.jar to the classpath, except for Mac OSX as it is not
     * needed.
     * 
     * @param theClasspath the classpath object to which to add the tools.jar
     */
    protected final void addToolsJarToClasspath(Path theClasspath)
    {
        // On OSX, the tools.jar classes are included in the classes.jar so 
        // there is no need to include any tools.jar file to the cp.
        if (!isOSX())
           {    
            try
            {
                theClasspath.createPathElement().setLocation(getToolsJar());
            }            
            catch (FileNotFoundException fnfe)
            {
                getLog().warn(
                    "Couldn't find tools.jar (needed for JSP compilation)");
            }
        }
    }

    /**
     * @return The arguments for JVM.
     */
    protected final String getJVMArgs()
    {
        return this.jvmArgs;
    }

    // Private Methods -------------------------------------------------------

    /**
     * Is the user running on a Macintosh OS X system?  Heuristic derived from
     * <a href="http://developer.apple.com/technotes/tn/tn2042.html#Section0_1">
     * Apple Tech Note 2042</a>.
     *
     * @return true if the user's system is determined to be Mac OS X.
     */
    private boolean isOSX()
    {
        return (System.getProperty("mrj.version") != null);
    }    

}
