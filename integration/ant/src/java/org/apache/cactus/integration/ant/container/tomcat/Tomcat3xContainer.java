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
package org.apache.cactus.integration.ant.container.tomcat;

import java.io.File;
import java.io.IOException;

import org.apache.cactus.integration.ant.util.ResourceUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/**
 * Special container support for the Apache Tomcat 3.x servlet container.
 * 
 * @version $Id$
 */
public class Tomcat3xContainer extends AbstractTomcatContainer
{

    // Instance Variables ------------------------------------------------------

    /**
     * The temporary directory from which the container will be started.
     */
    private File tmpDir;

    // Public Methods ----------------------------------------------------------

    /**
     * Sets the temporary installation directory.
     * 
     * @param theTmpDir The temporary directory to set
     */
    public final void setTmpDir(File theTmpDir)
    {
        this.tmpDir = theTmpDir;
    }

    // AbstractContainer Implementation ----------------------------------------

    /**
     * @see org.apache.cactus.integration.ant.container.Container#getName
     */
    public final String getName()
    {
        return "Tomcat 3.x";
    }

    /**
     * @see org.apache.cactus.integration.ant.container.Container#startUp
     */
    public final void startUp()
    {
        try
        {
            prepare("cactus/tomcat3x");
            invoke("start");
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
        invoke("stop");
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Invokes the Tomcat Main class to start or stop the container, depending
     * on the value of the provided argument.
     * 
     * @param theArg Either 'start' or 'stop'
     */
    private void invoke(String theArg)
    {
        Java java = null;
        if ("start".equals(theArg))
        {
            java = createJavaForStartUp();
        }
        else
        {
            java = createJavaForShutDown();
        }
        java.addSysproperty(createSysProperty("tomcat.install", getDir()));
        java.addSysproperty(createSysProperty("tomcat.home", this.tmpDir));
        Path classpath = java.createClasspath();
        classpath.createPathElement().setLocation(
            new File(getDir(), "lib/tomcat.jar"));

        // It seems that since Tomcat 3.3.2, the commons-logging jar is 
        // required in the Tomcat bootstrap classpath...
        File commonsLoggingJarFile = 
            new File(getDir(), "lib/common/commons-logging-api.jar");
        if (commonsLoggingJarFile.exists())
        {
            classpath.createPathElement().setLocation(commonsLoggingJarFile);
        }
        
        java.setClassname("org.apache.tomcat.startup.Main");
        java.createArg().setValue(theArg);
        java.execute();
    }
    
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
        
        this.tmpDir = setupTempDirectory(this.tmpDir, theDirName);
        cleanTempDirectory(this.tmpDir);

        // copy configuration files into the temporary container directory
        File confDir = createDirectory(tmpDir, "conf");
        copyConfFiles(confDir);
        if (getServerXml() == null)
        {
            ResourceUtils.copyResource(getProject(),
                RESOURCE_PATH + "tomcat3x/server.xml",
                new File(confDir, "server.xml"), filterChain);
        }
        // TODO: only copy these files if they haven't been provided by the
        // user as a conf fileset
        ResourceUtils.copyResource(getProject(),
            RESOURCE_PATH + "tomcat3x/tomcat-users.xml",
            new File(confDir, "tomcat-users.xml"));
        ResourceUtils.copyResource(getProject(),
            RESOURCE_PATH + "tomcat3x/modules.xml",
            new File(confDir, "modules.xml"));
        
        // deploy the web-app by copying the WAR file
        File webappsDir = createDirectory(tmpDir, "webapps");
        fileUtils.copyFile(getDeployableFile().getFile(),
            new File(webappsDir, getDeployableFile().getFile().getName()), 
            null, true);
    }

}
