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
package org.apache.cactus.eclipse.webapp.internal;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.cactus.eclipse.webapp.internal.ui.WebappPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Represents a web application for a given project.
 * It knows how to load its values from project properties and
 * how to persist them.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @version $Id$
 */
public class Webapp
{
    /**
     * Delimiter for classpaths entries in the String that will be used
     * for persisting the webapp settings. 
     */
    private static final String CLASSPATH_DELIMITER = ";";

    /**
     * Qualified name of the output war property. Used for persistence of 
     * project properties. 
     */
    private static final QualifiedName OUTPUT_QN =
        new QualifiedName(WebappPlugin.getPluginId(), "output");

    /**
     * Qualified name of the webapp directory property. Used for persistence 
     * of project properties. 
     */
    private static final QualifiedName DIR_QN =
        new QualifiedName(WebappPlugin.getPluginId(), "dir");

    /**
     * Qualified name of the classpath property. Used for persistence of 
     * project properties. 
     */
    private static final QualifiedName CLASSPATH_QN =
        new QualifiedName(WebappPlugin.getPluginId(), "webappClasspath");

    /**
     * Default path for the generated war 
     */
    private static final String DEFAULT_OUTPUT =
        System.getProperty("java.io.tmpdir") + "webapp.war";

    /**
     * Default directory of where the webapp is located. 
     */
    private static final String DEFAULT_DIR =
        "src" + File.separator + "webapp";
            
    /**
     * Full path to the webapp War.
     */
    private String output;

    /**
     * Directory of the webapp relative to the user's project. 
     */
    private String dir;

    /**
     * Paths to the webapp libraries
     */
    private IClasspathEntry[] classpath;

    /**
     * The current project to which this webapp refers.  
     */
    private IJavaProject javaProject;

    /**
     * @param theJavaProject the project this webapp is linked to
     */
    public Webapp(IJavaProject theJavaProject)
    {
        this.javaProject = theJavaProject;
    }

    /**
     * Initialize the web app properties with default values or stored values
     * if they exist. 
     * @return boolean true if we loaded the default values
     */
    public boolean init()
    {
        return loadValues();
    }

    /**
     * Loads this webapp from the project properties. If the persistent
     * properties cannot be loaded or if a value is not set, we load the 
     * default values.
     * 
     * @return true if the default values were loaded or false if the 
     *         persistent ones were loaded
     */
    public boolean loadValues()
    {
        boolean isDefaultValues;

        try
        {
            loadPersistentValues();
            isDefaultValues = false;
        }
        catch (CoreException ce)
        {
            loadDefaultValues();
            isDefaultValues = true;
        }

        if (output == null
            || dir == null
            || classpath == null)
        {
            loadDefaultValues();
            isDefaultValues = true;
        }

        return isDefaultValues;
    }

    /**
     * Loads the persistent properties for this webapp.
     * @throws CoreException if we fail to load a persistent property
     */
    public void loadPersistentValues() throws CoreException
    {
        IProject theProject = javaProject.getProject();

        this.output = theProject.getPersistentProperty(OUTPUT_QN);
        this.dir = theProject.getPersistentProperty(DIR_QN);
        this.classpath = toClasspathEntryArray(
            theProject.getPersistentProperty(CLASSPATH_QN));
    }

    /**
     *  Loads the default values of a webapp.
     */
    public void loadDefaultValues()
    {
        this.output = DEFAULT_OUTPUT;
        this.dir = DEFAULT_DIR;

        try
        {
            this.classpath = javaProject.getRawClasspath();
        }
        catch (JavaModelException e)
        {
            this.classpath = new IClasspathEntry[0];
        }
    }

    /**
     * Saves this webapp in the project's properties
     * @throws CoreException if an error occurs while saving 
     */
    public void persist() throws CoreException
    {
        IProject project = javaProject.getProject();
        project.setPersistentProperty(OUTPUT_QN, output);
        project.setPersistentProperty(DIR_QN, dir);
        project.setPersistentProperty(CLASSPATH_QN, toString(classpath));
    }

    /**
     * Converts a String classpath to an array of library classpath entries.
     * @param theClasspathEntriesString string of delimiter-separated 
     *        classpaths
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
            new StringTokenizer(theClasspathEntriesString, 
            CLASSPATH_DELIMITER);
            
        while (cpTokenizer.hasMoreElements())
        {
            String element = cpTokenizer.nextToken();
            try
            {
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
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < theClasspathEntries.length; i++)
        {
            IClasspathEntry current = theClasspathEntries[i];
            result.append(current.getPath());
            result.append(CLASSPATH_DELIMITER);
        }
        return result.toString();
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
     * Sets the output.
     * @param theOutput The output to set
     */
    public void setOutput(String theOutput)
    {
        this.output = theOutput;
    }

    /**
     * @return IClasspathEntry[] the array of jar entries for this webapp
     */
    public IClasspathEntry[] getClasspath()
    {
        return this.classpath;
    }

    /**
     * @return String directory of this webapp source files
     * relative to the project path 
     */
    public String getDir()
    {
        return this.dir;
    }
    
    /**
     * @return the absolute directory to this webapp source files
     */
    public File getAbsoluteDir()
    {
        File result = null;

        if (this.dir != null)
        {
            IPath projectPath = javaProject.getProject().getLocation();
            result = projectPath.append(this.dir).toFile();
        } 
        return result; 
    }

    /**
     * @return String location of the generated war
     */
    public String getOutput()
    {
        return this.output;
    }
}
