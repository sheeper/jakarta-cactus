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
    public Webapp(final IJavaProject theJavaProject)
    {
        this.javaProject = theJavaProject;
    }

    /**
     * Initialize the web app properties with default values or stored values
     * if they exist. 
     * @return boolean true if we loaded the default values
     */
    public final boolean init()
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
    public final boolean loadValues()
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
    public final void loadPersistentValues() throws CoreException
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
    public final void loadDefaultValues()
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
    public final void persist() throws CoreException
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
        final String theClasspathEntriesString)
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
    private String toString(final IClasspathEntry[] theClasspathEntries)
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
    public final void setClasspath(final IClasspathEntry[] theClasspath)
    {
        this.classpath = theClasspath;
    }

    /**
     * Sets the dir.
     * @param theDir The dir to set
     */
    public final void setDir(final String theDir)
    {
        this.dir = theDir;
    }

    /**
     * Sets the output.
     * @param theOutput The output to set
     */
    public final void setOutput(final String theOutput)
    {
        this.output = theOutput;
    }

    /**
     * @return IClasspathEntry[] the array of jar entries for this webapp
     */
    public final IClasspathEntry[] getClasspath()
    {
        return this.classpath;
    }

    /**
     * @return String directory of this webapp source files
     * relative to the project path 
     */
    public final String getDir()
    {
        return this.dir;
    }
    
    /**
     * @return the absolute directory to this webapp source files
     */
    public final File getAbsoluteDir()
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
    public final String getOutput()
    {
        return this.output;
    }
}
