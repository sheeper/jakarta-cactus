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
package org.apache.cactus.integration.ant.container.jboss;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.cactus.integration.ant.container.AbstractJavaContainer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/**
 * Special container support for the JBoss application server.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * 
 * @version $Id$
 */
public class JBoss3xContainer extends AbstractJavaContainer
{

    // Instance Variables ------------------------------------------------------

    /**
     * The JBoss 3.x installation directory.
     */
    private File dir;

    /**
     * The name of the server configuration to use for running the tests.
     */
    private String config = "default";

    /**
     * The port to which the container should be bound.
     */
    private transient int port = 8080;

    /**
     * The JBoss version detected by reading the Manifest file in the
     * installation directory.
     */
    private transient String version;

    // Public Methods ----------------------------------------------------------

    /**
     * Sets the JBoss installation directory.
     * 
     * @param theDir The directory to set
     * @throws BuildException If the specified directory doesn't contain a valid
     *         JBoss 3.x installation
     */
    public final void setDir(File theDir) throws BuildException
    {
        this.dir = theDir;
    }

    /**
     * Sets the name of the server configuration to use for running the tests.
     * 
     * @param theConfig The configuration name
     */
    public final void setConfig(String theConfig)
    {
        this.config = theConfig;
    }

    // Container Implementation ------------------------------------------------

    /**
     * @see org.apache.cactus.integration.ant.container.Container#getName
     */
    public final String getName()
    {
        return "JBoss " + this.version;
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
     * @see org.apache.cactus.integration.ant.container.Container#init
     */
    public final void init()
    {
        // Verify the installation directory
        this.version = getVersion(this.dir);
        if (this.version == null)
        {
            throw new BuildException(this.dir
                + " not recognized as a JBoss 3.x installation");
        }
        if (!this.version.startsWith("3"))
        {
            throw new BuildException(
                "This element doesn't support version " + this.version
                + " of JBoss");
        }

        // TODO: as long as we don't have a way to set the port on the JBoss 
        // instance, we'll at least need to extract the port from a config file
        // in the installation directory
    }

    /**
     * @see org.apache.cactus.integration.ant.container.Container#startUp
     */
    public final void startUp()
    {
        try
        {
            prepare("cactus/jboss3x");
            
            File binDir = new File(this.dir, "bin");
            File configDir = new File(this.dir, "server");
            
            Java java = createJavaForStartUp();
            java.setDir(binDir);
            
            java.addSysproperty(
                createSysProperty("program.name",
                    new File(binDir, "run.bat")));
            java.addSysproperty(
                createSysProperty("jboss.server.home.dir",
                    new File(configDir, this.config)));
            java.addSysproperty(
                createSysProperty("jboss.server.home.url",
                    new File(configDir, this.config).toURL().toString()));

            Path classPath = java.createClasspath();
            classPath.createPathElement().setLocation(
                new File(binDir, "run.jar"));
            try
            {
                classPath.createPathElement().setLocation(getToolsJar());
            }
            catch (FileNotFoundException fnfe)
            {
                getLog().warn(
                    "Couldn't find tools.jar (needed for JSP compilation)");
            }

            java.setClassname("org.jboss.Main");
            java.createArg().setValue("-c");
            java.createArg().setValue(this.config);
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
        File binDir = new File(this.dir, "bin");
            
        Java java = createJavaForShutDown();
        java.setFork(true);

        Path classPath = java.createClasspath();
        classPath.createPathElement().setLocation(
            new File(binDir, "shutdown.jar"));

        java.setClassname("org.jboss.Shutdown");
        if (this.version.startsWith("3.2"))
        {
            java.createArg().setValue("--shutdown");
        }
        else
        {
            java.createArg().setValue("localhost");
            java.createArg().setValue(String.valueOf(getPort()));
        }
        java.execute();
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

        // TODO: Find out how to create a valid default server configuration.
        // Copying the server directory does not seem to be enough
            
        // deploy the web-app by copying the WAR file into the webapps
        // directory
        File configDir = new File(this.dir, "server");
        File deployDir = new File(configDir, this.config + "/deploy");
        fileUtils.copyFile(getDeployableFile(),
            new File(deployDir, getDeployableFile().getName()), null, true);
    }

    /**
     * Returns the version of the JBoss installation.
     * 
     * @param theDir The JBoss installation directory 
     * @return The JBoss version, or <code>null</code> if the verion number
     *         could not be retrieved
     */
    private String getVersion(File theDir)
    {
        // Extract version information from the manifest in run.jar
        String retVal = null;
        try
        {
            JarFile runJar = new JarFile(new File(theDir, "bin/run.jar"));
            Manifest mf = runJar.getManifest();
            if (mf != null)
            {
                Attributes attrs = mf.getMainAttributes();
                retVal = attrs.getValue(Attributes.Name.SPECIFICATION_VERSION);
            }
            else
            {
                getLog().warn("Couldn't find MANIFEST.MF in " + runJar);
            }
        }
        catch (IOException ioe)
        {
            getLog().warn("Couldn't retrieve JBoss version information", ioe);
        }
        return retVal;
    }

}
