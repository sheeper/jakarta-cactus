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
package org.apache.cactus.eclipse.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import org.apache.cactus.eclipse.ui.CactusMessages;
import org.apache.cactus.eclipse.ui.CactusPlugin;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Helper class for creating War files.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @version $Id$
 */
public class WarBuilder
{
    /**
     * the directory where to find classes
     */
    private File userClassFilesDir;
    /**
     * the web.xml file
     */
    private File userWebXML;
    /**
     * directory where to find jars for the webapp
     */
    private File userJarFilesDir;
    /**
     * directory where to find user's web files
     */
    private File userWebFilesDir;
    /**
     * the location of the Ant build file for creating wars
     */
    private File buildFileLocation;
    /**
     * Cactus plug-in relative path to the war build file
     */
    private String buildFilePath = "./ant/build-war.xml";
    /**
     * Cactus plug-in relative path to the web.xml file
     */    
    private String webXMLPath = "./ant/confs/web.xml";
    /**
     * User's project relative path to the web directory
     */    
    private String userWebFilesPath = "web";
    /**
     * User's project relative path to the web.xml file
     */    
    private String userWebXMLPath = userWebFilesPath + "/WEB-INF/web.xml";
    /**
     * User's project relative path to the lib directory
     */    
    private String userJarFilesPath = userWebFilesPath + "/WEB-INF/lib";
    /**
     * Constructor.
     * @param theBuildFileLocation the build file for war creation
     * @param theClassFilesDir classes to include in the war file
     * @param theWebXML web.xml file to include in the war file
     * @param theJarFilesDir jars to include in the war file
     * @param theWebFilesDir web files to include in the war file
     */
    public WarBuilder(
        File theBuildFileLocation,
        File theClassFilesDir,
        File theWebXML,
        File theJarFilesDir,
        File theWebFilesDir)
    {
        this.buildFileLocation = theBuildFileLocation;
        this.userClassFilesDir = theClassFilesDir;
        this.userWebXML = theWebXML;        
        this.userJarFilesDir = theJarFilesDir;
        this.userWebFilesDir = theWebFilesDir;
    }

    /**
     * Constructor.
     * @param theJavaProject the Java project which Java classes will be used
     * @throws JavaModelException if we can't get the ouput location
     */
    public WarBuilder(IJavaProject theJavaProject)
        throws JavaModelException
    {
        CactusPlugin thePlugin = CactusPlugin.getDefault();
        URL buildFileURL = thePlugin.find(new Path(buildFilePath));
        if (buildFileURL == null)
        {
            throw new JavaModelException(
                CactusPlugin.createCoreException(
                    "CactusLaunch.message.prepare.error.plugin.file",
                    " : " + buildFilePath,
                    null));
        }
        buildFileLocation =
            new File(buildFileURL.getPath());
        IPath projectPath = theJavaProject.getProject().getLocation();
        IPath classFilesPath =
            projectPath.removeLastSegments(1).append(
                theJavaProject.getOutputLocation());
        userClassFilesDir = classFilesPath.toFile();
        userWebXML = projectPath.append(userWebXMLPath).toFile();
        if (!userWebXML.exists())
        {
            URL webXMLURL = thePlugin.find(new Path(webXMLPath));
            if (webXMLURL == null)
            {
                throw new JavaModelException(
                    CactusPlugin.createCoreException(
                        "CactusLaunch.message.prepare.error.plugin.file",
                        " : " + webXMLPath,
                        null));
            }
            userWebXML = new File(webXMLURL.getPath());
        }
        userJarFilesDir = projectPath.append(userJarFilesPath).toFile();
        // copy any web folder situated in the user's project
        userWebFilesDir = projectPath.append(userWebFilesPath).toFile();
    }

    /**
     * Creates the war file in the Java temp directory.
     * @param thePM a monitor that reflects the overall progress 
     * @return File the location where the war file was created
     * @throws CoreException if we can't create the file
     */
    public File createWar(IProgressMonitor thePM) throws CoreException
    {
        thePM.subTask(CactusMessages.getString("CactusLaunch.message.war"));
        File testWar = null;
        try
        {
            testWar = File.createTempFile("test", ".war");
        }
        catch (IOException e)
        {
            throw CactusPlugin.createCoreException(
                "CactusLaunch.message.war.error",
                e);
        }
        Vector arguments = new Vector();
        
        String jarFilesPath = userJarFilesDir.getAbsolutePath();
        arguments.add("-Djars.dir=" + jarFilesPath);

        String webXMLPath = userWebXML.getAbsolutePath();
        arguments.add("-Dwebxml.path=" + webXMLPath);
        
        String classFilesPath = userClassFilesDir.getAbsolutePath();
        arguments.add("-Dclasses.dir=" + classFilesPath);
        
        String warFilePath = testWar.getAbsolutePath();
        arguments.add("-Dwar.path=" + warFilePath);

        if (userWebFilesDir.exists())
        {
            String webFilesPath = userWebFilesDir.getAbsolutePath();
            arguments.add("-Dwebfiles.dir=" + webFilesPath);
        }
        String[] antArguments = (String[]) arguments.toArray(new String[0]);
        AntRunner runner = new AntRunner();
        runner.setBuildFileLocation(buildFileLocation.getAbsolutePath());
        runner.setArguments(antArguments);
        String[] targets = { "testwar" };
        runner.setExecutionTargets(targets);
        runner.run(new SubProgressMonitor(thePM, 3));
        return testWar;
    }
}
