/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
import java.io.IOException;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.cactus.integration.ant.AbstractJavaContainer;
import org.apache.cactus.integration.ant.util.ResourceUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/**
 * Special container support for the Apache Tomcat 4.x servlet container.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * 
 * @version $Id$
 */
public class Tomcat4xContainer extends AbstractJavaContainer
{

    // Instance Variables ------------------------------------------------------

    /**
     * The Tomcat 4.x installation directory.
     */
    private File dir;

    /**
     * A user-specific server.xml configuration file. If this variable is not
     * set, the default configuration file from the JAR resources will be used.
     */
    private File serverXml;

    /**
     * The port to which the container should be bound.
     */
    private int port = 8080;

    /**
     * The temporary directory from which the container will be started.
     */
    private transient File tmpDir;

    /**
     * The Tomcat version detected by reading the Manifest file in the
     * installation directory.
     */
    private transient String version;

    // Public Methods ----------------------------------------------------------

    /**
     * Sets the Tomcat 4.x installation directory.
     * 
     * @param theDir The directory to set
     */
    public final void setDir(File theDir)
    {
        this.dir = theDir;
    }

    /**
     * Sets the server configuration file to use for the test installation of
     * Tomcat 4.x.
     * 
     * @param theServerXml The server.xml file
     */
    public final void setServerXml(File theServerXml)
    {
        this.serverXml = theServerXml;
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

    // AbstractContainer Implementation ----------------------------------------

    /**
     * @see org.apache.cactus.integration.ant.Container#getName
     */
    public final String getName()
    {
        return "Tomcat " + this.version;
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
     * @see org.apache.cactus.integration.ant.Container#init
     */
    public final void init()
    {
        // Check the installation directory
        this.version = getVersion(this.dir);
        if (this.version == null)
        {
            throw new BuildException(this.dir
                + " not recognized as a Tomcat 4.x installation");
        }
        if (!this.version.startsWith("4"))
        {
            throw new BuildException(
                "This element doesn't support version " + this.version
                + " of JBoss");
        }
    }

    /**
     * @see org.apache.cactus.integration.ant.Container#startUp
     */
    public final void startUp()
    {
        try
        {
            prepare("cactus/tomcat4x");
            invoke("start");
        }
        catch (IOException ioe)
        {
            throw new BuildException(ioe);
        }
    }

    /**
     * @see org.apache.cactus.integration.ant.Container#shutDown
     */
    public final void shutDown()
    {
        invoke("stop");
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Returns the version of the Tomcat installation.
     * 
     * @param theDir The Tomcat installation directory 
     * @return The Tomcat version, or <code>null</code> if the verion number
     *         could not be retrieved
     */
    private String getVersion(File theDir)
    {
        String retVal = null;
        try
        {
            // Unfortunately, there's no safe way to find out the version of a
            // Tomcat 4.x installation, so we need to try multiple paths here
            
            // Tomcat 4.1.0 and later includes a ServerInfo.properties resource 
            // in catalina.jar that contains the version number. If that
            // resource doesn't exist, we're on Tomcat 4.0.x
            JarFile catalinaJar = new JarFile(
                new File(theDir, "server/lib/catalina.jar"));
            ZipEntry entry = catalinaJar.getEntry(
                "org/apache/catalina/util/ServerInfo.properties");
            if (entry != null)
            {
                Properties props = new Properties();
                props.load(catalinaJar.getInputStream(entry));
                String serverInfo = props.getProperty("server.info");
                if (serverInfo.indexOf('/') > 0)
                {
                    retVal = serverInfo.substring(serverInfo.indexOf('/') + 1);
                }
            }
            else
            {
                retVal = "4.0.x";
            }
        }
        catch (IOException ioe)
        {
            getLog().warn("Couldn't retrieve Tomcat version information", ioe);
        }
        return retVal;
    }

    /**
     * Invokes the Catalina Bootstrap class to start or stop the container, 
     * depending on the value of the provided argument.
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
        java.addSysproperty(createSysProperty("catalina.home", this.dir));
        java.addSysproperty(createSysProperty("catalina.base", this.tmpDir));
        Path classpath = java.createClasspath();
        classpath.createPathElement().setLocation(
            new File(this.dir, "bin/bootstrap.jar"));
        try
        {
            classpath.createPathElement().setLocation(getToolsJar());
        }
        catch (FileNotFoundException fnfe)
        {
            getLog().warn(
                "Couldn't find tools.jar (needed for JSP compilation)");
        }
        java.setClassname("org.apache.catalina.startup.Bootstrap");
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

        this.tmpDir = createTempDirectory(theDirName);

        // copy configuration files into the temporary container directory
        File confDir = createDirectory(tmpDir, "conf");
        if (this.serverXml != null)
        {
            fileUtils.copyFile(this.serverXml, new File(confDir, "server.xml"));
        }
        else
        {
            ResourceUtils.copyResource(getProject(),
                RESOURCE_PATH + "tomcat4x/server.xml",
                new File(confDir, "server.xml"), filterChain);
        }
        ResourceUtils.copyResource(getProject(),
            RESOURCE_PATH + "tomcat4x/tomcat-users.xml",
            new File(confDir, "tomcat-users.xml"));
        fileUtils.copyFile(new File(dir, "conf/web.xml"),
            new File(confDir, "web.xml"));
            
        // deploy the web-app by copying the WAR file into the webapps
        // directory
        File webappsDir = createDirectory(tmpDir, "webapps");
        fileUtils.copyFile(getWarFile(),
            new File(webappsDir, getWarFile().getName()), null, true);
    }

}
