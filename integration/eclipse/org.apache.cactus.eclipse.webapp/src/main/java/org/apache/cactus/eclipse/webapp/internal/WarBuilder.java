/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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
package org.apache.cactus.eclipse.webapp.internal;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.cactus.eclipse.webapp.internal.ui.WebappMessages;
import org.apache.cactus.eclipse.webapp.internal.ui.WebappPlugin;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;

/**
 * Helper class for creating War files.
 * 
 * @version $Id: WarBuilder.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class WarBuilder
{
    /**
     * The Java project to build the war from
     */
    private IJavaProject javaProject;

    /**
     * The Webapp object that stores webapp preferences 
     */
    private Webapp webapp;

    /**
     * Name of the WEB-INF directory
     */
    public static final String WEBINF = "WEB-INF";

    /**
     * Name of the lib directory
     */
    public static final String LIB = "lib";

    /**
     * Name of the web.xml file
     */
    public static final String WEBXML = "web.xml";

    /**
     * @param theJavaProject the Java project for which the webapp will be 
     *        created
     * @throws JavaModelException if we can't get the output location
     */
    public WarBuilder(final IJavaProject theJavaProject) 
        throws JavaModelException
    {
        this.javaProject = theJavaProject;
        this.webapp = new Webapp(theJavaProject);
    }

    /**
     * @param theWebFilesDir webapp directory to get the web.xml from
     * @return the web.xml file in the given webapp directory,
     *  or null if none
     */
    public static File getWebXML(final File theWebFilesDir)
    {
        if (theWebFilesDir == null)
        {
            return null;
        }
        else
        {
            String userWebXMLPath =
                theWebFilesDir.getAbsolutePath()
                    + File.separator
                    + WEBINF
                    + File.separator
                    + WEBXML;
            return new File(userWebXMLPath);
        }
    }
    /**
     * For each IClasspathEntry transform the path in an absolute path.
     * @param theEntries array of IClasspathEntry to render asbolute
     * @return an array of absolute IClasspathEntries
     */
    private static IClasspathEntry[] getAbsoluteEntries(
        final IClasspathEntry[] theEntries)
    {
        if (theEntries == null)
        {
            return new IClasspathEntry[0];
        }
        Vector result = new Vector();
        for (int i = 0; i < theEntries.length; i++)
        {
            IClasspathEntry currentEntry = theEntries[i];
            if (currentEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
            {
                IPath path = currentEntry.getPath();
                Object target =
                    JavaModel.getTarget(
                        ResourcesPlugin.getWorkspace().getRoot(),
                        path,
                        true);
                if (target instanceof IFile)
                {
                    IFile file = (IFile) target;
                    IPath absolutePath = file.getLocation();
                    result.add(
                        JavaCore.newLibraryEntry(absolutePath, null, null));
                }
                else
                    if (target instanceof File)
                    {
                        File file = (File) target;
                        result.add(
                            JavaCore.newLibraryEntry(
                                new Path(file.getAbsolutePath()),
                                null,
                                null));
                    }
            }
        }
        return (IClasspathEntry[]) result.toArray(
            new IClasspathEntry[result.size()]);
    }

    /**
     * @param theJavaProject the java project for which we want to get the
     *        absolute output path 
     * @return the absolute project output path
     * @throws JavaModelException if we fail to get the project relative 
     *         output location
     */
    private static File getAbsoluteOutputLocation(
        final IJavaProject theJavaProject) throws JavaModelException
    {
        IPath projectPath = theJavaProject.getProject().getLocation();
        IPath outputLocation = theJavaProject.getOutputLocation();
        IPath classFilesPath =
            projectPath.append(outputLocation.removeFirstSegments(1));
        return classFilesPath.toFile();
    }

    /**
     * Creates the war file.
     * @param thePM a monitor that reflects the overall progress,
     *  or null if none is to be used.
     * @return File the location where the war file was created
     * @throws CoreException if we can't create the file
     */
    public final File createWar(final IProgressMonitor thePM) 
        throws CoreException
    {
        IProgressMonitor progressMonitor = thePM;
        if (progressMonitor == null)
        {
            progressMonitor = new NullProgressMonitor();
        }
        
        progressMonitor.subTask(
            WebappMessages.getString("WarBuilder.message.createwar.monitor"));
        this.webapp.loadValues();
        File outputWar = getOutputWar();
        File userWebFilesDir = getUserWebFilesDir();
        File userWebXML = getWebXML(userWebFilesDir);
        IClasspathEntry[] jarEntries = getJarEntries();
        File userClassFilesDir = getAbsoluteOutputLocation(this.javaProject);
        
        outputWar.delete();
        War warTask = new War();
        progressMonitor.worked(1);
        Project antProject = new Project();
        antProject.init();
        warTask.setProject(antProject);
        warTask.setDestFile(outputWar);
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
            try
            {
                // A file is needed for war creation
                File voidFile = File.createTempFile("void", null);
                createZipFile(outputWar, voidFile);
                voidFile.delete();
            }
            catch (IOException e)
            {
                throw new CoreException(
                    new Status(
                        IStatus.ERROR,
                        WebappPlugin.getPluginId(),
                        IStatus.OK,
                        WebappMessages.getString(
                            "WarBuilder.message.createwar.temp"),
                        e));
            }

            warTask.setUpdate(true);
        }

        ZipFileSet[] jarFS = getZipFileSets(jarEntries);
        for (int i = 0; i < jarFS.length; i++)
        {
            warTask.addLib(jarFS[i]);
        }
        warTask.execute();
        progressMonitor.worked(2);
        return outputWar;
    }

    /**
     * @param theJarEntries the jars to build ZipFileSets from
     * @return an array of ZipFileSet corresponding to the given jars
     */
    private static ZipFileSet[] getZipFileSets(
        final IClasspathEntry[] theJarEntries)
    {
        Vector result = new Vector();
        for (int i = 0; i < theJarEntries.length; i++)
        {

            IClasspathEntry currentEntry = theJarEntries[i];
            if (currentEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
            {
                File currentJar = currentEntry.getPath().toFile();
                ZipFileSet zipFS = new ZipFileSet();
                zipFS.setFile(currentJar);
                result.add(zipFS);
            }
        }
        return (ZipFileSet[]) result.toArray(new ZipFileSet[result.size()]);
    }

    /**
     * @return the web application folder situated in the user's project
     */
    private File getUserWebFilesDir()
    {
        // path to the web directory relative to the user's project
        String userWebFilesPath = this.webapp.getDir();
        if (userWebFilesPath == null || userWebFilesPath.equals(""))
        {
            return null;
        }
        else
        {
            IPath projectPath = this.javaProject.getProject().getLocation();

            // web application folder situated in the user's project
            return projectPath.append(userWebFilesPath).toFile();
        }
    }

    /**
     * @return the jar entries
     */
    private IClasspathEntry[] getJarEntries()
    {
        return getAbsoluteEntries(this.webapp.getClasspath());
    }

    /**
     * @return the output war file
     */
    private File getOutputWar()
    {
        return new File(this.webapp.getOutput());
    }

    /**
     * Removes the specified file or directory, and all subdirectories
     * @param theFile the file or directory to delete
     */
    public static final void delete(final File theFile)
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

    /**
     * Creates a zip file containing the given existing file. 
     * @param theZipFile the zip file to create
     * @param theExistingFile the file to include in the zip
     */
    private void createZipFile(final File theZipFile, 
        final File theExistingFile)
    {
        Project antProject = new Project();
        antProject.init();
        Zip zip = new Zip();
        zip.setProject(antProject);
        zip.setDestFile(theZipFile);
        FileSet existingFileSet = new FileSet();
        existingFileSet.setFile(theExistingFile);
        zip.addFileset(existingFileSet);
        zip.execute();
    }

}
