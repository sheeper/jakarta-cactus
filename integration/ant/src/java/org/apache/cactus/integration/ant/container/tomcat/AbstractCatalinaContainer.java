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
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
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
        java.addSysproperty(createSysProperty("catalina.base", this.tmpDir));
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
    protected final void prepare(String theResourcePrefix, String theDirName)
        throws IOException
    {
        FileUtils fileUtils = FileUtils.newFileUtils();
        FilterChain filterChain = createFilterChain();

        if (this.tmpDir == null)
        {
            this.tmpDir = createTempDirectory(theDirName);
        }

        File confDir = createDirectory(tmpDir, "conf");
        
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
        
        // Copy user-provided configuration files into the temporary container 
        // directory

        copyConfFiles(confDir);
           
        // deploy the web-app by copying the WAR file into the webapps
        // directory
        File webappsDir = createDirectory(tmpDir, "webapps");
        fileUtils.copyFile(getDeployableFile().getFile(),
            new File(webappsDir, getDeployableFile().getFile().getName()), 
            null, true);
    }

}
