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

import org.apache.cactus.integration.ant.util.ResourceUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/**
 * Special container support for the Apache Tomcat 3.x servlet container.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class Tomcat3xContainer extends AbstractTomcatContainer
{

    // Instance Variables ------------------------------------------------------

    /**
     * The temporary directory from which the container will be started.
     */
    private transient File tmpDir;

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
        
        tmpDir = createTempDirectory(theDirName);
        
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
        fileUtils.copyFile(getWarFile(),
            new File(webappsDir, getWarFile().getName()), null, true);
    }

}
