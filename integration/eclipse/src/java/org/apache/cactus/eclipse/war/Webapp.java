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

import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.cactus.eclipse.ui.CactusPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;

/**
 * Helper class for creating War files.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @version $Id$
 */
public class Webapp
{
    /**
     * Delimiter for classpaths in the String that will be persisted. 
     */
    private static final String CLASSPATH_DELIMITER = ";";
    /**
     * QualifiedName of the output war property
     * used for persistence of project properties. 
     */
    private QualifiedName outputQN =
        new QualifiedName(CactusPlugin.getPluginId(), "output");
    /**
     * QualifiedName of the webapp directory property
     * used for persistence of project properties. 
     */
    private QualifiedName dirQN =
        new QualifiedName(CactusPlugin.getPluginId(), "dir");
    /**
     * QualifiedName of the temporary directory property
     * used for persistence of project properties. 
     */
    private QualifiedName tempDirQN =
        new QualifiedName(CactusPlugin.getPluginId(), "tempDir");
    /**
     * QualifiedName of the classpath property
     * used for persistence of project properties. 
     */
    private QualifiedName classpathQN =
        new QualifiedName(CactusPlugin.getPluginId(), "webappClasspath");
    /**
     * Full path to the webapp War.
     */
    private String output;
    /**
     * Directory of the webapp relative to the user's project. 
     */
    private String dir;
    /**
     * Temporary directory for jars copy. 
     */
    private String tempDir;
    /**
     * Paths to the webapp libraries
     */
    private IClasspathEntry[] classpath;
    /**
     * The current project to which this webapp refers.  
     */
    private IJavaProject javaProject;

    /**
     * Constructor.
     * @param theProject the project this webapp is linked to
     */
    public Webapp(IProject theProject)
    {
        javaProject = JavaCore.create(theProject);
    }

    /**
     * Initialize the web app properties with default values or stored values
     * if they exist. 
     * @return boolean true if we loaded the default values
     */
    public boolean init()
    {
        try
        {
            loadValues();
            return false;
        }
        catch (CoreException e)
        {
            loadDefaultValues();
            return true;
        }
    }

    /**
     * Loads this webapp from the project properties.
     * @throws CoreException if an error occurs while loading the properties 
     */
    public void loadValues() throws CoreException
    {
        IProject theProject = javaProject.getProject();
        output = theProject.getPersistentProperty(outputQN);
        dir = theProject.getPersistentProperty(dirQN);
        tempDir = theProject.getPersistentProperty(tempDirQN);
        classpath =
            toClasspathEntryArray(
                theProject.getPersistentProperty(classpathQN));
        if (output == null
            || dir == null
            || tempDir == null
            || classpath == null)
        {
            loadDefaultValues();
        }
    }

    /**
     *  Loads the default values of a webapp.
     */
    public void loadDefaultValues()
    {
        output = "c:/temp/webapp.war";
        dir = "src/webapp";
        tempDir = System.getProperty("java.io.tmpdir");
        try
        {
            classpath = javaProject.getRawClasspath();
        }
        catch (JavaModelException e)
        {
            classpath = new IClasspathEntry[0];
        }
    }

    /**
     * Saves this webapp in the project's properties
     * @throws CoreException if an error occurs while saving 
     */
    public void persist() throws CoreException
    {
        IProject project = javaProject.getProject();
        project.setPersistentProperty(outputQN, output);
        project.setPersistentProperty(dirQN, dir);
        project.setPersistentProperty(tempDirQN, tempDir);
        project.setPersistentProperty(classpathQN, toString(classpath));
    }

    /**
     * Converts a String classpath to an array of library classpath entries
     * @param theClasspathEntriesString string of delimiter-separated classpaths
     * @return an array of library entries
     */
    private IClasspathEntry[] toClasspathEntryArray(
        String theClasspathEntriesString)
    {
        if (theClasspathEntriesString == null)
        {
            return null;
        }
        Vector result = new Vector();
        StringTokenizer cpTokenizer =
            new StringTokenizer(theClasspathEntriesString, CLASSPATH_DELIMITER);
        while (cpTokenizer.hasMoreElements())
        {
            String element = cpTokenizer.nextToken();
            try
            {
                JavaProject jp = (JavaProject) javaProject;
                IClasspathEntry newEntry =
                    JavaCore.newLibraryEntry(new Path(element), null, null);
                result.add(newEntry);
            }
            catch (Exception e)
            {
                // Do not add the entry
            }
        }
        return (IClasspathEntry[]) result.toArray(
            new IClasspathEntry[result.size()]);
    }

    /**
     * Converts an array of library classpath entries to a String 
     * @param theClasspathEntries an array of library entries
     * @return String string of delimiter-separated classpaths
     */
    private String toString(IClasspathEntry[] theClasspathEntries)
    {
        String result = "";
        for (int i = 0; i < theClasspathEntries.length; i++)
        {
            IClasspathEntry current = theClasspathEntries[i];
            result += current.getPath() + CLASSPATH_DELIMITER;
        }
        return result;
    }
    /**
     * Sets the classpath.
     * @param theClasspath The classpath to set
     */
    public void setClasspath(IClasspathEntry[] theClasspath)
    {
        this.classpath = theClasspath;
    }

    /**
     * Sets the dir.
     * @param theDir The dir to set
     */
    public void setDir(String theDir)
    {
        this.dir = theDir;
    }

    /**
     * Sets the temporary directory.
     * @param theTempDir The temporary dir to set
     */
    public void setTempDir(String theTempDir)
    {
        this.tempDir = theTempDir;
    }

    /**
     * Sets the output.
     * @param theOutput The output to set
     */
    public void setOutput(String theOutput)
    {
        this.output = theOutput;
    }

    /**
     * @return IClasspathEntry[]
     */
    public IClasspathEntry[] getClasspath()
    {
        return classpath;
    }

    /**
     * @return String
     */
    public String getDir()
    {
        return dir;
    }

    /**
     * @return String
     */
    public String getTempDir()
    {
        return tempDir;
    }

    /**
     * @return String
     */
    public String getOutput()
    {
        return output;
    }

}
