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
package org.apache.cactus.eclipse.runner.common;

import java.io.File;
import java.net.URL;

import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

/**
 * Helper class for library access.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * 
 * @version $Id$
 */
public class LibraryHelper
{
    /**
     * Path to the plugin's library directory
     */
    private static final String CACTUS_LIBRARY_PATH = "./lib/";

    /**
     * Name of the common libraries folder
     */
    private static final String CACTUS_COMMON_LIBRARY_PATH = "/common";

    /**
     * Name of the client libraries folder
     */
    private static final String CACTUS_CLIENT_LIBRARY_PATH = "/client";

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
    private static IPath getLibPath()
    {
        CactusPlugin thePlugin = CactusPlugin.getDefault();
        URL antLibURL = thePlugin.find(new Path(CACTUS_LIBRARY_PATH));
        return new Path(antLibURL.getPath());
    }

    /**
     * @return the path to the client library directory
     */
    private static IPath getClientLibPath()
    {
        return getLibPath().append(CACTUS_CLIENT_LIBRARY_PATH);
    }

    /**
     * @return the path to the common library directory
     */
    private static IPath getCommonLibPath()
    {
        return getLibPath().append(CACTUS_COMMON_LIBRARY_PATH);
    }

    /**
     * @param thePath path to convert to an IClasspathEntry
     * @return the IClasspathEntry built from the given path
     */
    private static IClasspathEntry getIClasspathEntry(IPath thePath)
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
