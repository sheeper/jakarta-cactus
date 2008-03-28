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
package org.apache.cactus.eclipse.runner.containers.jetty;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.cactus.eclipse.runner.containers.IContainerManager;
import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.apache.cactus.eclipse.runner.ui.CactusPreferences;
import org.apache.cactus.eclipse.webapp.internal.WarBuilder;
import org.apache.cactus.eclipse.webapp.internal.Webapp;
import org.apache.cactus.eclipse.webapp.internal.ui.WebappPlugin;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Implementation of IContainerManager for the Jetty container.
 * 
 * @version $Id: JettyContainerManager.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class JettyContainerManager implements IContainerManager
{

    /**
     * The name of the path to the Jetty webapp
     */
    private static final String JETTY_WEBAPP_PATH = "jetty.webapp";

    /**
     * The name of the jspRedirector.jsp file
     */
    private static final String JSPREDIRECTOR_PATH =
		 new String("lib/confs/jspRedirector.jsp");

    /**
     * Directory containg the web application for Jetty
     */
    private File jettyWebappDir =
        new File(
            CactusPreferences.getTempDir()
                + File.separator
                + JETTY_WEBAPP_PATH);
    /**
     * @see IContainerManager#prepare(org.eclipse.jdt.core.IJavaProject)
     */
    public void prepare(IJavaProject theJavaProject)
    {
        try
        {
            cactifyWebapp(theJavaProject);
        }
        catch (CoreException e)
        {
        	CactusPlugin.log("Failed"+e.getMessage());
        }
    }

    /**
     * @see IContainerManager#tearDown()
     */
    public void tearDown()
    {
        //WarBuilder.delete(jettyWebappDir);
    }

    /**
     * @param theJavaProject the project to cactify
     * @throws CoreException if the cactification could not occur
     */
    private void cactifyWebapp(IJavaProject theJavaProject)
        throws CoreException
    {
        WarBuilder.delete(jettyWebappDir);
        jettyWebappDir.mkdir();
        copyCactusWebappResources(jettyWebappDir);
        Webapp webapp = WebappPlugin.getWebapp(theJavaProject);
        webapp.init();
        File webappDir = webapp.getAbsoluteDir();
        if (webappDir != null && webappDir.exists())
        {
            copyContents(webappDir, jettyWebappDir);
        }
    }

    /**
     * Copies Cactus webapp resources (jspredirector.jsp) to the given
     * directory
     * @param theDir the directory to copy resources to
     * @throws CoreException if we cannot copy the resources
     */
    private void copyCactusWebappResources(File theDir) throws CoreException
    {
        Project antProject = new Project();
        antProject.init();
        Copy copy = new Copy();
        copy.setProject(antProject);
        copy.setTodir(theDir);
        CactusPlugin thePlugin = CactusPlugin.getDefault();
        URL jspRedirectorURL = null;
		try {
			jspRedirectorURL = Platform.asLocalURL(thePlugin.getBundle().getEntry(JSPREDIRECTOR_PATH));
		} catch (IOException e1) {
			//do nothing the exception is called later.
		}
        if (jspRedirectorURL == null)
        {
            throw CactusPlugin.createCoreException(
                "CactusLaunch.message.prepare.error.plugin.file",
                " : " + JSPREDIRECTOR_PATH,
                null);
        }
        try{
        	jspRedirectorURL = Platform.asLocalURL(jspRedirectorURL);
        } catch(IOException e) {
            throw CactusPlugin.createCoreException(
                    "CactusLaunch.message.prepare.error.plugin.file",
                    " : " + e.getMessage(),
                    null);
        }
        //CactusPlugin.log(jspRedirectorURL.getPath());
        File jspRedirector = new File(jspRedirectorURL.getPath());
        FileSet fileSet = new FileSet();
        fileSet.setFile(jspRedirector);
        copy.addFileset(fileSet);
        copy.execute();
    }

    /**
     * copies files from a directory to another directory
     * @param theSourceDir directory to copy files from
     * @param theTargetDir directory to copy files to
     */
    private void copyContents(File theSourceDir, File theTargetDir)
    {
        Project antProject = new Project();
        antProject.init();
        Copy copy = new Copy();
        copy.setProject(antProject);
        copy.setTodir(theTargetDir);
        FileSet fileSet = new FileSet();
        fileSet.setDir(theSourceDir);
        copy.addFileset(fileSet);
        copy.execute();
    }

    /**
     * @return the path name element of the web application directory 
     */
    public static String getJettyWebappPath()
    {
        return JETTY_WEBAPP_PATH;
    }
}
