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

/**
 * Special container support for the Bea WebLogic 7.x application server.
 * 
 * TODO: this doesn't work for me on JDK 1.3.1 and WL 7.0 SP2
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
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
        java.createArg().setValue("SHUTDOWN");
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
        FilterChain filterChain = createFilterChain();
        
        if (this.tmpDir == null)
        {
            this.tmpDir = createTempDirectory(theDirName);
        }

        File testDomainDir = createDirectory(this.tmpDir, "testdomain");
        ResourceUtils.copyResource(getProject(),
            RESOURCE_PATH + "weblogic7x/config.xml",
            new File(testDomainDir, "config.xml"),
            filterChain);
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
