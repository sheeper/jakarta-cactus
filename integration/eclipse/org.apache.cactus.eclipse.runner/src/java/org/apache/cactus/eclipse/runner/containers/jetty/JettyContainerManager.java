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
package org.apache.cactus.eclipse.runner.containers.jetty;

import java.io.File;
import java.net.URL;

import org.apache.cactus.eclipse.runner.containers.IContainerManager;
import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.apache.cactus.eclipse.runner.ui.CactusPreferences;
import org.apache.cactus.eclipse.webapp.WarBuilder;
import org.apache.cactus.eclipse.webapp.Webapp;
import org.apache.cactus.eclipse.webapp.ui.WebappPlugin;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Implementation of IContainerManager for the Jetty container.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * 
 * @version $Id$
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
        "./ant/confs/jspRedirector.jsp";

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
//
        }
    }

    /**
     * @see IContainerManager#tearDown()
     */
    public void tearDown()
    {
        WarBuilder.delete(jettyWebappDir);
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
        URL jspRedirectorURL = thePlugin.find(new Path(JSPREDIRECTOR_PATH));
        if (jspRedirectorURL == null)
        {
            throw CactusPlugin.createCoreException(
                "CactusLaunch.message.prepare.error.plugin.file",
                " : " + JSPREDIRECTOR_PATH,
                null);
        }
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
