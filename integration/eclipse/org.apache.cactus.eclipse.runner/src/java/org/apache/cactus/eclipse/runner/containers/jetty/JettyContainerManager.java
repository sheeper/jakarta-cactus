/*
 * Created on Apr 10, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.cactus.eclipse.runner.containers.jetty;

import java.io.File;
import java.net.URL;

import org.apache.cactus.eclipse.runner.containers.IContainerManager;
import org.apache.cactus.eclipse.runner.launcher.CactusLaunchShortcut;
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
 * @author Julien Ruaux
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
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
            CactusLaunchShortcut launchShortcut =
                CactusPlugin.getDefault().getCactusLaunchShortcut();
            launchShortcut.launchJunitTests();
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
