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
import java.util.Vector;

import org.apache.cactus.eclipse.ui.CactusPlugin;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Helper class for creating War files.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @version $Id: $
 */
public class WarBuilder
{
    /**
     * the directory where to find classes
     */
    private File classFilesDir;
    /**
     * the web.xml file
     */
    private File webXML;
    /**
     * directory where to find jars for the webapp
     */
    private File jarFilesDir;
    /**
     * directory where to find web files
     */
    private File webFilesDir;
    /**
     * the location of the Ant build file for creating wars
     */
    private File buildFileLocation;

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
        this.classFilesDir = theClassFilesDir;
        this.webXML = theWebXML;
        this.jarFilesDir = theJarFilesDir;
        this.webFilesDir = theWebFilesDir;
    }

    /**
     * Constructor.
     * @param theJavaProject the Java project which Java classes will be used
     * @param theJarFilesDir the directory where the Jars are located
     * @throws JavaModelException if we can't get the ouput location
     */
    public WarBuilder(IJavaProject theJavaProject, File theJarFilesDir)
        throws JavaModelException
    {
        jarFilesDir = theJarFilesDir;
        IPath projectPath = theJavaProject.getProject().getLocation();

        IPath thePath =
            projectPath.removeLastSegments(1).append(
                theJavaProject.getOutputLocation());
        classFilesDir = thePath.toFile();
        CactusPlugin thePlugin = CactusPlugin.getDefault();
        buildFileLocation =
            new File(thePlugin.find(new Path("./ant/build-war.xml")).getPath());
        webXML =
            new File(
                thePlugin.find(new Path("./ant/conf/test/web.xml")).getPath());
        // copy any web folder situated in the user's project
        webFilesDir = projectPath.append("web").toFile();
    }

    /**
     * Creates the war file in the Java temp directory.
     * @return File the location where the war file was created
     * @throws CoreException if we can't create the file
     */
    public File createWar() throws CoreException
    {
        File testWar = null;
        try
        {
            testWar = File.createTempFile("test", ".war");
        }
        catch (IOException e)
        {
            throw new CoreException(
                new Status(
                    IStatus.ERROR,
                    CactusPlugin.getPluginId(),
                    IStatus.OK,
                    e.getMessage(),
                    e));
        }
        Vector arguments = new Vector();
        String jarFilesPath = jarFilesDir.getAbsolutePath();
        arguments.add("-Djars.dir=" + jarFilesPath);
        String webXMLPath = webXML.getAbsolutePath();
        arguments.add("-Dwebxml.path=" + webXMLPath);
        String classFilesPath = classFilesDir.getAbsolutePath();
        arguments.add("-Dclasses.dir=" + classFilesPath);
        String warFilePath = testWar.getAbsolutePath();
        arguments.add("-Dwar.path=" + warFilePath);
        // If a web dir is present in the user's project
        // we use it for the War file, otherwise we use a blank one
        boolean userWebExists = webFilesDir.exists();
        if (!userWebExists)
        {
            String tempPath = System.getProperty("java.io.tmpdir");
            webFilesDir = new File(tempPath, "web");
            webFilesDir.mkdir();
        }
        String webFilesPath = webFilesDir.getAbsolutePath();
        arguments.add("-Dwebfiles.dir=" + webFilesPath);
        String[] antArguments = (String[]) arguments.toArray(new String[0]);
        AntRunner runner = new AntRunner();
        runner.setBuildFileLocation(buildFileLocation.getAbsolutePath());
        runner.setArguments(antArguments);
        String[] targets = { "testwar" };
        runner.setExecutionTargets(targets);
        runner.run();
        // Delete the created Web dir, if any.
        // Could not use deleteOnExit on this dir because the VM launched by
        // Ant seems to be crashing when shut down by the 'stop' task
        //  
        if (!userWebExists)
        {
            webFilesDir.delete();
        }
        return testWar;
    }
}
