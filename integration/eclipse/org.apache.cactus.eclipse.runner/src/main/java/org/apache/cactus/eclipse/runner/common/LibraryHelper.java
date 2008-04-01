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
package org.apache.cactus.eclipse.runner.common;

import java.io.File;
import java.io.IOException;

import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

/**
 * Helper class for library access.
 * 
 * @version $Id: LibraryHelper.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class LibraryHelper
{
    /**
     * Returns an array of jar paths contained
     * in the given directory.
     * @param theDirectory the directory to list jars from
     * @return an array of jar paths
     */
    private static IPath[] getJarPathArray(File theDirectory)
    {
        File[] files = theDirectory.listFiles(new JarFilenameFilter());
        IPath[] result = new IPath[files.length];
        for (int i = 0; i < files.length; i++)
        {
            result[i] = new Path(files[i].getAbsolutePath());
        }
        return result;
    }

    /**
     * @return an IPath array of the jar files in client lib directory
     */
    private static IPath[] getClientJarPathArray()
    {
        File clientLibDir = getClientLibPath().toFile();
        return getJarPathArray(clientLibDir);
    }

    /**
     * @return an IPath array of the jar files in client lib directory
     */
    private static IPath[] getCommonJarPathArray()
    {
        File commmonLibDir = getCommonLibPath().toFile();
        return getJarPathArray(commmonLibDir);
    }

    /**
     * @return an array of IClasspathEntry contained in the client dir
     */
    public static IClasspathEntry[] getClientEntries()
    {
        IPath[] clientJars = getClientJarPathArray();
        IClasspathEntry[] result = new IClasspathEntry[clientJars.length];
        for (int i = 0; i < clientJars.length; i++)
        {
            result[i] = getIClasspathEntry(clientJars[i]);
        }
        return result;
    }

    /**
     * @return an array of IClasspathEntry contained in the common dir
     */
    public static IClasspathEntry[] getCommonEntries()
    {
        IPath[] commonJars = getCommonJarPathArray();
        IClasspathEntry[] result = new IClasspathEntry[commonJars.length];
        for (int i = 0; i < commonJars.length; i++)
        {
            result[i] =  getIClasspathEntry(commonJars[i]);
        }
        return result;
    }

    /**
     * @return an array of IClasspathEntry contained in the common dir
     */
    public static IClasspathEntry[] getClientSideEntries()
    {
        IClasspathEntry[] clientEntries = getClientEntries();
        IClasspathEntry[] commonEntries = getCommonEntries();
        return concatenateEntries(clientEntries, commonEntries);
    }

    /**
     * Concatenate two IClasspathEntry arrays.
     * 
     * @param theArray1 the first array
     * @param theArray2 the second array
     * @return an IClasspathEntry array containing the first array
     *         followed by the second one
     */
    public static IClasspathEntry[] concatenateEntries(
        IClasspathEntry[] theArray1, IClasspathEntry[] theArray2)
    {
        IClasspathEntry[] newArray =
            new IClasspathEntry[theArray1.length + theArray2.length];
        System.arraycopy(theArray1, 0, newArray, 0, theArray1.length);
        System.arraycopy(
            theArray2,
            0,
            newArray,
            theArray1.length,
            theArray2.length);
        return newArray;
    }

    /**
     * @return the path to the library directory
     */
    public static IPath getLibPath()
    {
        CactusPlugin thePlugin = CactusPlugin.getDefault();
        
        try {
				return new Path(Platform.asLocalURL(thePlugin.getBundle().getEntry("/" +CactusPlugin.CACTUS_LIBRARY_PATH)).getPath());
        }  catch(IOException ex) {
			   CactusPlugin.log(ex);	// throwing an exception would be preferable
        }
        return new Path(CactusPlugin.CACTUS_LIBRARY_PATH);
    }

    /**
     * @return the path to the client library directory
     */
    private static IPath getClientLibPath()
    {
        return getLibPath().append(CactusPlugin.CACTUS_CLIENT_LIBRARY_PATH);
    }

    /**
     * @return the path to the common library directory
     */
    private static IPath getCommonLibPath()
    {
        return getLibPath().append(CactusPlugin.CACTUS_COMMON_LIBRARY_PATH);
    }

    /**
     * @param thePath path to convert to an IClasspathEntry
     * @return the IClasspathEntry built from the given path
     */
    public static IClasspathEntry getIClasspathEntry(IPath thePath)
    {
        return JavaCore.newLibraryEntry(thePath, null, null);
    }
    
    /**
     * @param thePath path to convert to an IClasspathEntry
     * @return the IClasspathEntry built from the given path
     */
    public static IClasspathEntry getIClasspathEntry(String thePath)
    {
        return getIClasspathEntry(new Path(thePath));
    }
}
