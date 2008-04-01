/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cactus.integration.maven;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.net.URL;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * Process {@link FileSet} and extracts classes that are Cactus tests. As
 * a Cactus test can be a simple JUnit test case wrapped in a Cactus suite,
 * it is very difficult to find out only Cactus tests. Thus, in this version,
 * we are only finding JUnit tests.
 *
 * A class is considered to be a JUnit Test Case if:
 * <ul>
 *   <li>It extends {@link TestCase}</li>
 *   <li>It is not abstract</li>
 *   <li>It has at least one method that starts with "test", returns void and
 *   takes no parameters</li>
 * </ul>
 *
 * @version $Id: CactusScanner.java 238815 2004-02-29 16:34:44Z vmassol $
 */
public class CactusScanner
{
    /**
     * Log instance.
     */
    private Log log = LogFactory.getLog(CactusScanner.class);

    /**
     * The Ant project.
     */
    private Project project;

    /**
     * Lists of Cactus class names that were found in the {@link FileSet}.
     */
    private List cactusTests = new ArrayList();

    /**
     * @param theProject the Ant project that is currently executing
     */
    public void setProject(Project theProject)
    {
        this.project = theProject;
    }

    /**
     * Remove all Cactus class names that were found in the {@link Fileset}.
     */
    public void clear()
    {
        this.cactusTests.clear();
    }

    /**
     * @return the list of valid Cactus test cases
     */
    public Iterator iterator()
    {
        return this.cactusTests.iterator();
    }

    /**
     * Finds the Cactus test cases from a list of files.
     *
     * @param theFileset the list of files in which to look for Cactus tests
     * @param theClasspath the classpaths needed to load the test classes
     */
    public void processFileSet(FileSet theFileset, Path theClasspath)
    {
        DirectoryScanner ds = theFileset.getDirectoryScanner(this.project);
        ds.scan();
        String[] files = ds.getIncludedFiles();

        for (int i = 0; i < files.length; i++)
        {
            // The path is supposed to be a relative path that matches the
            // package directory structure. Thus we only need to replace
            // the directory separator char by a "." and remove the file
            // extension to get the FQN java class name.

            // Is it a java class file?
            if (files[i].endsWith(".class"))
            {
                String fqn = files[i]
                    .substring(0, files[i].length() - ".class".length())
                    .replace(File.separatorChar, '.');

                log.debug("Found candidate class: [" + fqn + "]");

                // Is it a Cactus test case?
                if (isJUnitTestCase(fqn, theClasspath))
                {
                    log.debug("Found Cactus test case: [" + fqn + "]");
                    this.cactusTests.add(fqn);
                }
            }
        }
    }

    /**
     * @param theClassName the fully qualified name of the class to check
     * @param theClasspath the classpaths needed to load the test classes
     * @return true if the class is a JUnit test case
     */
    private boolean isJUnitTestCase(String theClassName, Path theClasspath)
    {
        Class clazz = loadClass(theClassName, theClasspath);
        if (clazz == null)
        {
            return false;
        }

        Class testCaseClass = null;
        try
        {
            testCaseClass = clazz.getClassLoader().loadClass(
                TestCase.class.getName());
        }
        catch (ClassNotFoundException e)
        {
            log.debug("Cannot load class", e);
            return false;
        }

        if (!testCaseClass.isAssignableFrom(clazz))
        {
            log.debug("Not a JUnit test as class [" + theClassName + "] does "
                + "not inherit from [" + TestCase.class.getName()
                + "]");
            return false;
        }

        // the class must not be abstract
        if (Modifier.isAbstract(clazz.getModifiers()))
        {
            log.debug("Not a JUnit test as class [" + theClassName + "] is "
                + "abstract");
            return false;
        }

        // the class must have at least one test, i.e. a public method
        // starting with "test" and that takes no parameters
        boolean hasTestMethod = false;
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++)
        {
            if (methods[i].getName().startsWith("test")
                && (methods[i].getReturnType() == Void.TYPE)
                && (methods[i].getParameterTypes().length == 0))
            {
                hasTestMethod = true;
                break;
            }
        }

        if (!hasTestMethod)
        {
            log.debug("Not a JUnit test as class [" + theClassName + "] has "
                + "no method that start with \"test\", returns void and has "
                + "no parameters");
            return false;
        }

        return true;
    }

    /**
     * @param theClassName the fully qualified name of the class to check
     * @param theClasspath the classpaths needed to load the test classes
     * @return the class object loaded by reflection from its string name
     */
    private Class loadClass(String theClassName, Path theClasspath)
    {
        Class clazz = null;
        try
        {
            clazz = createClassLoader(theClasspath).loadClass(theClassName);
        }
        catch (ClassNotFoundException e)
        {
            log.error("Failed to load class [" + theClassName + "]", e);
        }
        return clazz;
    }

    /**
     * @param theClasspath the classpaths needed to load the test classes
     * @return a ClassLoader that has all the needed classpaths for loading
     *         the Cactus tests classes
     */
    private ClassLoader createClassLoader(Path theClasspath)
    {
        URL[] urls = new URL[theClasspath.size()];

        try
        {
            for (int i = 0; i < theClasspath.size(); i++)
            {
                log.debug("Adding ["
                    + new File(theClasspath.list()[i]).toURL() + "] "
                    + "to class loader classpath");
                urls[i] = new File(theClasspath.list()[i]).toURL();
            }
        }
        catch (MalformedURLException e)
        {
            log.debug("Invalid URL", e);
        }

        return new URLClassLoader(urls);
    }
}
