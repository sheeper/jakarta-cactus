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
package org.apache.cactus.integration.ant.container.tomcat;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.cactus.integration.ant.util.ResourceUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/**
 * Base support for Catalina based containers.
 * 
 * @version $Id$
 */
public abstract class AbstractCatalinaContainer extends AbstractTomcatContainer
{
    // Instance Variables ------------------------------------------------------

    /**
     * The temporary directory from which the container will be started.
     */
    private File tmpDir;

    /**
     * The Catalina version detected by reading a property file in the
     * installation directory.
     */
    private String version;
    
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
        return "Tomcat " + this.version;
    }

    /**
     * @see org.apache.cactus.integration.ant.container.Container#init
     */
    public void init()
    {
        // Check the installation directory
        this.version = getVersion();
        if (this.version == null)
        {
            throw new BuildException(getDir()
                + " not recognized as a Tomcat 4.x installation");
        }
    }

    // Protected Methods -------------------------------------------------------

    /**
     * Returns the version of the Tomcat installation.
     * 
     * @return The Tomcat version, or <code>null</code> if the verion number
     *         could not be retrieved
     */
    protected final String getVersion()
    {
        if (this.version == null)
        {
            try
            {
                // Unfortunately, there's no safe way to find out the version of
                // a Catalina installation, so we need to try multiple paths
                // here
                
                // Tomcat 4.1.0 and later includes a ServerInfo.properties
                // resource in catalina.jar that contains the version number. If
                // that resource doesn't exist, we're on Tomcat 4.0.x
                JarFile catalinaJar = new JarFile(
                    new File(getDir(), "server/lib/catalina.jar"));
                ZipEntry entry = catalinaJar.getEntry(
                    "org/apache/catalina/util/ServerInfo.properties");
                if (entry != null)
                {
                    Properties props = new Properties();
                    props.load(catalinaJar.getInputStream(entry));
                    String serverInfo = props.getProperty("server.info");
                    if (serverInfo.indexOf('/') > 0)
                    {
                        this.version =
                            serverInfo.substring(serverInfo.indexOf('/') + 1);
                    }
                }
                else
                {
                    this.version = "4.0.x";
                }
            }
            catch (IOException ioe)
            {
                getLog().warn("Couldn't retrieve Tomcat version information",
                    ioe);
            }
        }
        return this.version;
    }

    /**
     * Invokes the Catalina Bootstrap class to start or stop the container, 
     * depending on the value of the provided argument.
     * 
     * @param theArg Either 'start' or 'stop'
     */
    protected final void invokeBootstrap(String theArg)
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
        java.addSysproperty(createSysProperty("catalina.home", getDir()));
        java.addSysproperty(createSysProperty("catalina.base", getTmpDir()));
        Path classpath = java.createClasspath();
        classpath.createPathElement().setLocation(
            new File(getDir(), "bin/bootstrap.jar"));
        addToolsJarToClasspath(classpath);
        java.setClassname("org.apache.catalina.startup.Bootstrap");
        java.createArg().setValue(theArg);
        java.execute();
    }

    /**
     * Prepares a temporary installation of the container and deploys the 
     * web-application.
     * 
     * @param theResourcePrefix The prefix to use when looking up container
     *        resource in the JAR
     * @param theDirName The name of the temporary container installation
     *        directory
     * @throws IOException If an I/O error occurs
     */
    protected void prepare(String theResourcePrefix, String theDirName)
        throws IOException
    {
        FileUtils fileUtils = FileUtils.newFileUtils();
        FilterChain filterChain = createFilterChain();

        setTmpDir(prepareTempDirectory(getTmpDir(), theDirName));

        File confDir = createDirectory(getTmpDir(), "conf");
        
        // Copy first the default configuration files so that they can be
        // overriden by the user-provided ones.

        if (getServerXml() == null)
        {
            ResourceUtils.copyResource(getProject(),
                RESOURCE_PATH + theResourcePrefix + "/server.xml",
                new File(confDir, "server.xml"), filterChain);
        }
        
        ResourceUtils.copyResource(getProject(),
            RESOURCE_PATH + theResourcePrefix + "/tomcat-users.xml",
            new File(confDir, "tomcat-users.xml"));
        fileUtils.copyFile(new File(getDir(), "conf/web.xml"),
            new File(confDir, "web.xml"));

        // deploy the web-app by copying the WAR file into the webapps
        // directory
        File webappsDir = createDirectory(getTmpDir(), "webapps");
        fileUtils.copyFile(getDeployableFile().getFile(),
            new File(webappsDir, getDeployableFile().getFile().getName()), 
            null, true);
        
        // Copy user-provided configuration files into the temporary conf/ 
        // container directory.
        copyConfFiles(confDir);
    }

    /**
     * @return The temporary directory from which the container will be 
     *         started.
     */
    protected final File getTmpDir()
    {
        return this.tmpDir;
    }
}
