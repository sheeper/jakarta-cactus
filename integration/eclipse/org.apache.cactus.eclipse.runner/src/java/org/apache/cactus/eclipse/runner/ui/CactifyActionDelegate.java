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
package org.apache.cactus.eclipse.runner.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Vector;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action delegate to cactify a project.
 * 
 * @version $Id$
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 */
public class CactifyActionDelegate implements IObjectActionDelegate
{
    /**
     * The project selected by the user
     */
    private IJavaProject selectedProject;
    /**
     * The active part
     */
    private IWorkbenchPart part;
    /**
     * Path to the plugin's library directory
     */
    private static final String CACTUS_LIBRARY_PATH = "./ant/lib/";
    /**
     * Name of the common libraries folder
     */
    private static final String CACTUS_COMMON_LIBRARY_PATH = "common";
    /**
     * Name of the client libraries folder
     */
    private static final String CACTUS_CLIENT_LIBRARY_PATH = "client";
    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    public void setActivePart(IAction theAction, IWorkbenchPart theTargetPart)
    {
        this.part = theTargetPart;
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction theAction)
    {
        if (part != null && selectedProject != null)
        {
            CactusPlugin thePlugin = CactusPlugin.getDefault();
            URL commonLibURL =
                thePlugin.find(
                    new Path(CACTUS_LIBRARY_PATH + CACTUS_COMMON_LIBRARY_PATH));
            URL clientLibURL =
                thePlugin.find(
                    new Path(CACTUS_LIBRARY_PATH + CACTUS_CLIENT_LIBRARY_PATH));
            File commonLibDir = new File(commonLibURL.getPath());
            File clientLibDir = new File(clientLibURL.getPath());
            IClasspathEntry[] commonEntries =
                getLibClassPathEntries(commonLibDir);
            IClasspathEntry[] clientEntries =
                getLibClassPathEntries(clientLibDir);
            IClasspathEntry[] allNewEntries =
                new IClasspathEntry[commonEntries.length
                    + clientEntries.length];
            System.arraycopy(
                commonEntries,
                0,
                allNewEntries,
                0,
                commonEntries.length);
            System.arraycopy(
                clientEntries,
                0,
                allNewEntries,
                commonEntries.length,
                clientEntries.length);
            try
            {

                IClasspathEntry[] existingEntries =
                    selectedProject.getRawClasspath();
                selectedProject.setRawClasspath(
                    merge(existingEntries, allNewEntries),
                    null);
            }
            catch (JavaModelException e)
            {
                CactusPlugin.displayErrorMessage(
                    CactusMessages.getString("Cactify.message.error"),
                    e.getMessage(),
                    null);
            }
        }
    }

    /**
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction theAction, ISelection theSelection)
    {
        selectedProject = null;
        if (theSelection instanceof IStructuredSelection)
        {
            IStructuredSelection structuredSelection =
                (IStructuredSelection) theSelection;
            if (structuredSelection.size() == 1)
            {
                Object selectedResource = structuredSelection.getFirstElement();
                if (selectedResource instanceof IJavaProject)
                {
                    selectedProject = (IJavaProject) selectedResource;
                }
            }
        }
    }

    /**
     * Returns a list of entries of jars files contained
     * in the given directory. 
     * @param theDirectory the directory to list jars from
     * @return an array of jar entries
     */
    private IClasspathEntry[] getLibClassPathEntries(File theDirectory)
    {
        File[] jarFiles = theDirectory.listFiles(new JarFilenameFilter());
        IClasspathEntry[] result = new IClasspathEntry[jarFiles.length];
        for (int i = 0; i < jarFiles.length; i++)
        {
            File currentJarFile = jarFiles[i];
            result[i] =
                JavaCore.newLibraryEntry(
                    new Path(currentJarFile.getAbsolutePath()),
                    null,
                    null);
        }
        return result;
    }
    /**
     * Filter for jar files.
     * i.e. accepts files like 'library.jar'
     * 
     * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
     * 
     * @version $Id$
     */
    static class JarFilenameFilter implements FilenameFilter
    {
        /**
         * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        public boolean accept(File theDir, String theFilename)
        {
            return theFilename.endsWith(".jar");
        }
    }

    /**
     * @param theFirstArray an array of classpath entries
     * @param theSecondArray an array of classpath entries
     * @return the fusion of the two given arrays 
     */
    private static IClasspathEntry[] merge(
        IClasspathEntry[] theFirstArray,
        IClasspathEntry[] theSecondArray)
    {

        Vector result = new Vector();
        for (int i = 0; i < theFirstArray.length; i++)
        {
            result.add(theFirstArray[i]);
        }
        for (int i = 0; i < theSecondArray.length; i++)
        {
            IClasspathEntry currentEntry = theSecondArray[i];
            boolean entryAlreadyExists = false;
            for (int j = 0; j < theFirstArray.length; j++)
            {
                IClasspathEntry comparedEntry = theFirstArray[j];
                if (comparedEntry.getPath().equals(currentEntry.getPath()))
                {
                    entryAlreadyExists = true;
                    break;
                }
            }
            if (!entryAlreadyExists)
            {
                result.add(currentEntry);
            }
        }
        return (IClasspathEntry[]) result.toArray(
            new IClasspathEntry[result.size()]);
    }
}
