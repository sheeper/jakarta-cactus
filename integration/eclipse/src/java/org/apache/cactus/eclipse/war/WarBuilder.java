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
package org.apache.cactus.eclipse.war;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.cactus.eclipse.ui.CactusMessages;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
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
     * directory where to find classes
     */
    private File userClassFilesDir;
    /**
     * web.xml file
     */
    private File userWebXML;
    /**
     * directory where to find user's web files
     */
    private File userWebFilesDir;
    /**
     * location of generated war file
     */
    private File war;
    /**
     * location of the temporary directory for jar copy
     */
    private File tempDir;
    /**
     * jar entries to include in the war
     */
    private IClasspathEntry[] jarEntries;
    /**
     * Cactus plug-in relative path to the web.xml file
     */
    private static final String WEB_XML_PATH = "./ant/confs/web.xml";
    /**
     * name of the WEB-INF directory
     */
    public static final String WEBINF = "WEB-INF";
    /**
     * name of the lib directory
     */
    public static final String LIB = "lib";
    /**
     * name of the web.xml file
     */
    public static final String WEBXML = "web.xml";
    /**
     * name of the temporary directory for jar copy 
     */
    private static final String JARS_PATH =
        "org.apache.cactus.eclipse.war.jars.temp";

    /**
     * Constructor.
     * @param theJavaProject the Java project which Java classes will be used
     * @throws JavaModelException if we can't get the ouput location
     */
    public WarBuilder(IJavaProject theJavaProject) throws JavaModelException
    {
        IPath projectPath = theJavaProject.getProject().getLocation();
        IPath classFilesPath =
            projectPath.removeLastSegments(1).append(
                theJavaProject.getOutputLocation());
        userClassFilesDir = classFilesPath.toFile();
        Webapp webapp = new Webapp(theJavaProject.getProject());
        try
        {
            webapp.loadValues();
        }
        catch (CoreException e)
        {
            throw new JavaModelException(e);
        }
        war = new File(webapp.getOutput());
        tempDir = new File(webapp.getTempDir());
        jarEntries = webapp.getClasspath();
        // path to the web directory relative to the user's project
        String userWebFilesPath = webapp.getDir();
        if (userWebFilesPath.equals("") || userWebFilesPath == null)
        {
            userWebFilesDir = null;
            userWebXML = null;
        }
        else
        {
            // web application folder situated in the user's project
            userWebFilesDir = projectPath.append(userWebFilesPath).toFile();
            // path to the web.xml file relative to the user's project
            String userWebXMLPath =
                userWebFilesPath + "/" + WEBINF + "/" + WEBXML;
            userWebXML = projectPath.append(userWebXMLPath).toFile();
        }

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
        Vector arguments = new Vector();
        IPath tempJarsPath =
            new Path(tempDir.getAbsolutePath()).append(JARS_PATH);
        File tempJarsDir = tempJarsPath.toFile();
        if (tempJarsDir.exists())
        {
            delete(tempJarsDir);
        }
        tempJarsDir.mkdir();
        copyJars(jarEntries, tempJarsDir);
        thePM.worked(1);
        Project antProject = new Project();
        antProject.init();
        War warTask = new War();
        warTask.setProject(antProject);
        warTask.setDestFile(war);
        ZipFileSet classes = new ZipFileSet();
        classes.setDir(userClassFilesDir);
        warTask.addClasses(classes);
        classes = new ZipFileSet();
        classes.setDir(userClassFilesDir);
        classes.setIncludes("log4j.properties");
        warTask.addClasses(classes);
        if (userWebFilesDir != null && userWebFilesDir.exists())
        {
            FileSet webFiles = new FileSet();
            webFiles.setDir(userWebFilesDir);
            webFiles.setExcludes(WEBINF);
            warTask.addFileset(webFiles);
        }
        if (userWebXML != null && userWebXML.exists())
        {
            warTask.setWebxml(userWebXML);
        }
        else
        {
            // Without a webxml attribute the Ant war task
            // requires the update attribute set to true
            // That's why we actually need an existing war file. 
            war.delete();
            try
            {
                war.createNewFile();
            }
            catch (IOException e)
            {
                throw new JavaModelException(
                    e,
                    IJavaModelStatusConstants.IO_EXCEPTION);
            }
            warTask.setUpdate(true);
        }
        ZipFileSet lib = new ZipFileSet();
        lib.setDir(tempJarsDir);
        warTask.addLib(lib);
        warTask.execute();
        delete(tempJarsDir);
        thePM.worked(2);
        return war;
    }

    /**
     * Copies a set of Jar files to the destination directory.
     * @param theEntries set of Jars
     * @param theDestination the destination directory 
     */
    private void copyJars(IClasspathEntry[] theEntries, File theDestination)
    {
        if (!theDestination.isDirectory())
        {
            return;
        }
        Project antProject = new Project();
        antProject.init();
        Copy jarCopy = new Copy();
        jarCopy.setProject(antProject);
        jarCopy.setTodir(theDestination);
        for (int i = 0; i < theEntries.length; i++)
        {
            IClasspathEntry currentEntry = theEntries[i];
            if (currentEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
            {
                File currentJar = currentEntry.getPath().toFile();
                FileSet fileSet = new FileSet();
                fileSet.setFile(currentJar);
                jarCopy.addFileset(fileSet);
            }
        }
        jarCopy.execute();
    }
    /**
     * Removes the specified file or directory, and all subdirectories
     * @param theFile the file or directory to delete
     */
    public static void delete(File theFile)
    {
        if (theFile.isDirectory())
        {
            File[] dir = theFile.listFiles();
            for (int i = 0; i < dir.length; i++)
            {
                delete(dir[i]);
            }
            theFile.delete();
        }
        else
            if (theFile.exists())
            {
                theFile.delete();
            }
    }

}
