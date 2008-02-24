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
package org.apache.cactus.eclipse.runner.ui;

import java.io.File;
import java.util.Vector;

import org.apache.cactus.eclipse.runner.common.JarFilenameFilter;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action delegate to cactify a project.
 * 
 * @version $Id: CactifyActionDelegate.java 238816 2004-02-29 16:36:46Z vmassol $
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
            String libPath = ""; 
            try {
            	libPath = JavaCore.getClasspathVariable("ECLIPSE_HOME").toFile().toURL().getPath()+
    				CactusPlugin.getDefault().getDescriptor().getInstallURL().getFile().replaceAll("plugin","plugins").replaceAll("_", "-").concat(CactusPlugin.CACTUS_LIBRARY_PATH); 
            } catch (Exception ex) {
            	CactusPlugin.log(ex);
            }
            
            File commonLibDir = new File(libPath.concat(CactusPlugin.CACTUS_COMMON_LIBRARY_PATH));
            File clientLibDir = new File(libPath.concat(CactusPlugin.CACTUS_CLIENT_LIBRARY_PATH));
	    CactusPlugin.log(libPath);
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
            catch (Exception e)
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
                if (selectedResource instanceof org.eclipse.core.internal.resources.Project)
                {
                	Project project = (Project) selectedResource;
                    selectedProject = JavaCore.create(project);
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
