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
package org.apache.cactus.integration.ant.container.weblogic;

import java.io.File;
import java.io.IOException;

import org.apache.cactus.integration.ant.container.AbstractJavaContainer;
import org.apache.cactus.integration.ant.util.ResourceUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.FileUtils;

/**
 * Special container support for the Bea WebLogic 7.x application server.
 * 
 * TODO: this doesn't work for me on JDK 1.3.1 and WL 7.0 SP2
 * 
 * @version $Id$
 */
public class WebLogic7xContainer extends AbstractJavaContainer
{

    // Instance Variables ------------------------------------------------------

    /**
     * The Bea home directory.
     */
    private File beaHome;

    /**
     * The WebLogic 7.x installation directory. For example:
     * "c:\bea\weblogic700".
     */
    private File dir;

    /**
     * The port to which the container should be bound.
     */
    private int port = 8080;

    /**
     * A user-specific <code>config.xml</code> WebLogic configuration file. 
     * If this variable is not set, the default configuration file from the 
     * JAR resources will be used.
     */
    private File configXml;
    
    /**
     * The temporary directory from which the container will be started.
     */
    private File tmpDir;

    // Public Methods ----------------------------------------------------------

    /**
     * Sets the Bea home directory.
     * 
     * @param theBeaHome The BEA home directory
     */
    public final void setBeaHome(File theBeaHome)
    {
        this.beaHome = theBeaHome;
    }

    /**
     * Sets the WebLogic 7.x installation directory.
     * 
     * @param theDir The directory to set
     */
    public final void setDir(File theDir)
    {
        this.dir = theDir;
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
     * Sets the configuration file to use for the test installation of 
     * WebLogic.
     * 
     * @param theConfigXml The custom <code>config.xml</code> file
     */
    public final void setConfigXml(File theConfigXml)
    {
        this.configXml = theConfigXml;
    }
    
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
        return "WebLogic 7.x";
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
        if (!this.dir.isDirectory())
        {
            throw new BuildException(this.dir + " is not a directory");
        }

        // If the beaHome attribute is not set, guess the bea home
        // directory using the parent directory of this.dir 
        if (this.beaHome == null)
        {
            getLog().debug("Extrapolating beaHome to be ["
                + this.dir.getParentFile() + "]");
            this.beaHome = this.dir.getParentFile();
        }
    }

    /**
     * @see org.apache.cactus.integration.ant.container.Container#startUp
     */
    public final void startUp()
    {
        try
        {
            prepare("cactus/weblogic7x");
            
            Java java = createJavaForStartUp();
            java.setDir(new File(this.tmpDir, "testdomain"));
            
            java.createJvmarg().setValue("-hotspot");
            java.createJvmarg().setValue("-Xms32m");
            java.createJvmarg().setValue("-Xmx200m");
            
            File serverDir = new File(this.dir, "server");
            
            java.addSysproperty(
                createSysProperty("weblogic.Name", "testserver"));
            java.addSysproperty(
                createSysProperty("bea.home", this.beaHome));
            java.addSysproperty(
                createSysProperty("weblogic.management.username", "weblogic"));
            java.addSysproperty(
                createSysProperty("weblogic.management.password", "weblogic"));

            // Note: The "=" in the call below is on purpose. It is left so that
            // we end up with: 
            //   -Djava.security.policy==./server/lib/weblogic.policy
            // (otherwise, we would end up with:
            //   -Djava.security.policy=./server/lib/weblogic.policy, which
            //  will not add to the security policy but instead replace it).
            java.addSysproperty(
                createSysProperty("java.security.policy",
                    "=./server/lib/weblogic.policy"));

            Path classpath = java.createClasspath();
            classpath.createPathElement().setLocation(
                new File(serverDir, "lib/weblogic_sp.jar"));
            classpath.createPathElement().setLocation(
                new File(serverDir, "lib/weblogic.jar"));

            java.setClassname("weblogic.Server");
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
        Java java = createJavaForShutDown();
            
        File serverDir = new File(this.dir, "server");

        Path classpath = java.createClasspath();
        classpath.createPathElement().setLocation(
            new File(serverDir, "lib/weblogic_sp.jar"));
        classpath.createPathElement().setLocation(
            new File(serverDir, "lib/weblogic.jar"));

        java.setClassname("weblogic.Admin");
        java.createArg().setValue("-url");
        java.createArg().setValue("t3://localhost:" + getPort());
        java.createArg().setValue("-username");
        java.createArg().setValue("weblogic");
        java.createArg().setValue("-password");
        java.createArg().setValue("weblogic");
        
        // Forcing WebLogic shutdown to speed up the shutdown process
        java.createArg().setValue("FORCESHUTDOWN");

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
        FilterChain filterChain = createFilterChain();
        
        this.tmpDir = prepareTempDirectory(this.tmpDir, theDirName);

        File testDomainDir = createDirectory(this.tmpDir, "testdomain");

        if (this.configXml != null)
        {
            fileUtils.copyFile(this.configXml,
                new File(testDomainDir, "config.xml"));
        }
        else
        {
            ResourceUtils.copyResource(getProject(),
                RESOURCE_PATH + "weblogic7x/config.xml",
                new File(testDomainDir, "config.xml"),
                filterChain);
        }
        
        ResourceUtils.copyResource(getProject(),
            RESOURCE_PATH + "weblogic7x/DefaultAuthenticatorInit.ldift",
            new File(testDomainDir, "DefaultAuthenticatorInit.ldift"),
            filterChain);

        // TODO: For improvement, do not copy the weblogic.xml file
        // in the tmpdir.
        
        // Extract the weblogic.xml descriptor
        File weblogicXml = new File(this.tmpDir, "weblogic.xml");
        ResourceUtils.copyResource(getProject(),
            RESOURCE_PATH + "weblogic7x/weblogic.xml",
            weblogicXml, filterChain);

        // deploy the web-app by copying the WAR file into the applications
        // directory, adding the weblogic.xml descriptor in WEB-INF
        File applicationsDir =
            createDirectory(testDomainDir, "applications");
        Jar jar = (Jar) createAntTask("jar");
        jar.setDestFile(new File(applicationsDir,
            getDeployableFile().getFile().getName()));
        ZipFileSet zip = new ZipFileSet();
        zip.setSrc(getDeployableFile().getFile());
        jar.addZipfileset(zip);
        ZipFileSet fileSet = new ZipFileSet();
        fileSet.setDir(this.tmpDir);
        fileSet.createInclude().setName("weblogic.xml");
        fileSet.setPrefix("WEB-INF");
        jar.addZipfileset(fileSet);
        jar.execute();
    }

}
