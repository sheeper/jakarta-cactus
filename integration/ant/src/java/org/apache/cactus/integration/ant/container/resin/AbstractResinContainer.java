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
 * @version $Id$
 */
public abstract class AbstractResinContainer extends AbstractJavaContainer
{
    // Instance Variables ------------------------------------------------------

    /**
     * The Resin installation directory.
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
        if (!getDir().isDirectory())
        {
            throw new BuildException(getDir() + " is not a directory");
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
            fileSet.setDir(getDir());
            fileSet.createInclude().setName("lib/*.jar");
            classpath.addFileset(fileSet);
            java.setClassname(ResinRun.class.getName());
            java.createArg().setValue("-start");
            java.createArg().setValue("-conf");
            java.createArg().setFile(new File(tmpDir, "resin.conf"));

            // Add settings specific to a given container version
            startUpAdditions(java, classpath);
            
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
        fileSet.setDir(getDir());
        fileSet.createInclude().setName("lib/*.jar");
        classpath.addFileset(fileSet);
        java.setClassname(ResinRun.class.getName());
        java.createArg().setValue("-stop");
        java.execute();
    }
    
    // Protected Methods -------------------------------------------------------

    /**
     * Allow specific version implementations to add custom settings to the 
     * Java container that will be started.
     * 
     * @param theJavaContainer the Ant Java object that will start the container
     * @param theClasspath the classpath that will be used to start the 
     *        container
     */
    protected abstract void startUpAdditions(Java theJavaContainer,
        Path theClasspath);

    /**
     * Allow specific version implementations to add custom preparation steps
     * before the container is started.
     * 
     * @param theFilterChain the filter chain used to replace Ant tokens in 
     *        configuration
     * @exception IOException in case of an error
     */
    protected abstract void prepareAdditions(FilterChain theFilterChain)
        throws IOException;

    /**
     * @return the name of the directory where the container will be set up
     */
    protected abstract String getContainerDirName();
    
    /**
     * @return the directory where Resin is installed
     */
    protected final File getDir()
    {
        return this.dir;
    }

    /**
     * @return The temporary directory from which the container will be 
     *         started.
     */
    protected final File getTmpDir()
    {
        return this.tmpDir;
    }
    
    // Private Methods ---------------------------------------------------------

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

        this.tmpDir = prepareTempDirectory(this.tmpDir, theDirName);

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

        // deploy the web-app by copying the WAR file into the webapps
        // directory
        File webappsDir = createDirectory(tmpDir, "webapps");
        fileUtils.copyFile(getDeployableFile().getFile(),
            new File(webappsDir, getDeployableFile().getFile().getName()), 
            null, true);

        // Add preparation steps specific to a given container version
        prepareAdditions(filterChain);
    }
}
