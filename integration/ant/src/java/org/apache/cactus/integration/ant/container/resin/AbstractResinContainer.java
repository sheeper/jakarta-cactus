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
package org.apache.cactus.integration.ant.container.resin;

import java.io.File;
import java.io.IOException;

import org.apache.cactus.integration.ant.container.AbstractJavaContainer;
import org.apache.cactus.integration.ant.util.ResourceUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/**
 * Common support for all Resin container versions.
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * 
 * @version $Id$
 */
public abstract class AbstractResinContainer extends AbstractJavaContainer
{
    // Instance Variables ------------------------------------------------------

    /**
     * The Resin 2.x installation directory.
     */
    private File dir;

    /**
     * A user-specific resin.conf configuration file. If this variable is not
     * set, the default configuration file from the JAR resources will be used.
     */
    private File resinConf;

    /**
     * The port to which the container should be bound.
     */
    private int port = 8080;

    /**
     * The temporary directory from which the container will be started.
     */
    private File tmpDir;

    // Public Methods ----------------------------------------------------------

    /**
     * Sets the Resin installation directory.
     * 
     * @param theDir The directory to set
     */
    public final void setDir(File theDir)
    {
        this.dir = theDir;
    }

    /**
     * Sets the configuration file to use for the test installation of Resin
     * 2.x.
     * 
     * @param theResinConf The resin.conf file
     */
    public final void setResinConf(File theResinConf)
    {
        this.resinConf = theResinConf;
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
     * Sets the temporary directory from which the container is run.
     * 
     * @param theTmpDir The temporary directory to set
     */
    public final void setTmpDir(File theTmpDir)
    {
        this.tmpDir = theTmpDir;
    }

    // AbstractContainer Implementation ----------------------------------------

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
     * @see org.apache.cactus.integration.ant.container.Container#init
     */
    public final void init()
    {
        if (!this.dir.isDirectory())
        {
            throw new BuildException(this.dir + " is not a directory");
        }
    }

    /**
     * @see org.apache.cactus.integration.ant.container.Container#startUp
     */
    public final void startUp()
    {
        try
        {
            prepare("cactus/" + getContainerDirName());
            
            // invoke the main class
            Java java = createJavaForStartUp();           
            java.addSysproperty(createSysProperty("resin.home", this.tmpDir));
            Path classpath = java.createClasspath();
            classpath.createPathElement().setLocation(
                ResourceUtils.getResourceLocation("/"
                    + ResinRun.class.getName().replace('.', '/') + ".class"));
            FileSet fileSet = new FileSet();
            fileSet.setDir(this.dir);
            fileSet.createInclude().setName("lib/*.jar");
            classpath.addFileset(fileSet);
            java.setClassname(ResinRun.class.getName());
            java.createArg().setValue("-start");
            java.createArg().setValue("-conf");
            java.createArg().setFile(new File(tmpDir, "resin.conf"));
            java.execute();
        }
        catch (IOException ioe)
        {
            getLog().error("Failed to startup the container", ioe);
            throw new BuildException(ioe);
        }
    }

    /**
     * @see org.apache.cactus.integration.ant.container.Container#shutDown
     */
    public final void shutDown()
    {
        // invoke the main class
        Java java = createJavaForShutDown();
        java.setFork(true);
        java.addSysproperty(createSysProperty("resin.home", this.tmpDir));
        Path classpath = java.createClasspath();
        classpath.createPathElement().setLocation(
            ResourceUtils.getResourceLocation("/"
                + ResinRun.class.getName().replace('.', '/') + ".class"));
        FileSet fileSet = new FileSet();
        fileSet.setDir(this.dir);
        fileSet.createInclude().setName("lib/*.jar");
        classpath.addFileset(fileSet);
        java.setClassname(ResinRun.class.getName());
        java.createArg().setValue("-stop");
        java.execute();
    }
    
    // Private Methods ---------------------------------------------------------

    /**
     * @return the name of the directory where the container will be set up
     */
    protected abstract String getContainerDirName();
    
    /**
     * Prepares a temporary installation of the container and deploys the 
     * web-application.
     * 
     * @param theDirName The name of the temporary container installation
     *        directory
     * @throws IOException If an I/O error occurs
     */
    private void prepare(String theDirName) throws IOException
    {
        FileUtils fileUtils = FileUtils.newFileUtils();
        FilterChain filterChain = createFilterChain();
        
        if (this.tmpDir == null)
        {
            this.tmpDir = createTempDirectory(theDirName);
        }

        // copy configuration files into the temporary container directory
        if (this.resinConf != null)
        {
            fileUtils.copyFile(this.resinConf, new File(tmpDir, "resin.conf"));
        }
        else
        {
            ResourceUtils.copyResource(getProject(),
                RESOURCE_PATH + getContainerDirName() + "/resin.conf",
                new File(tmpDir, "resin.conf"), filterChain);
        }
            
        fileUtils.copyFile(getDeployableFile().getFile(),
            new File(tmpDir, getDeployableFile().getFile().getName()), 
            null, true);
    }
}