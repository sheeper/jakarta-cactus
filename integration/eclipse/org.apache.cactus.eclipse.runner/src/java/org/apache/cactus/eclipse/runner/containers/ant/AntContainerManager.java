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
package org.apache.cactus.eclipse.runner.containers.ant;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.cactus.eclipse.runner.containers.IContainerManager;
import org.apache.cactus.eclipse.runner.containers.IContainerProvider;
import org.apache.cactus.eclipse.runner.ui.CactusMessages;
import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.apache.cactus.eclipse.webapp.WarBuilder;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

/**
 * Implementation of IContainerManager based on Ant.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * 
 * @version $Id$
 */
public class AntContainerManager implements IContainerManager
{
    /**
     * The progress monitor associated with the current action. 
     */
    private IProgressMonitor currentPM;

    /**
     * True if the provider has successfully been deployed. 
     */
    private boolean stateProviderDeployed = false;

    /**
     * True if the war has successfully been created. 
     */
    private boolean stateWarCreated = false;

    /**
     * True if the container has been prepared. 
     */
    private boolean prepared = false;

    /**
     * Provider currently used 
     */
    private AntContainerProvider provider;

    /**
     * Table containing all the information needed
     * (key = target mask & value = container home directory)
     * to call the scripts
     */
    private Hashtable containerHomes;

    /**
     * Ant arguments common to all container providers 
     */
    private Vector antArguments = new Vector();

    /**
     * Path to the Ant build file for the manager
     */
    private String buildFilePath;

    /**
     * Context path for the container provider
     */
    private String contextURLPath;
    /**
     * Reference to the War file so that we can delete it on tearDown()
     */
    private File war;

    /**
     * Constructor.
     * @param theBuildFilePath path to the Ant build file for this manager
     * @param thePort the port that will be used when setting up the containers
     * @param theTargetDir temporary directory to use for
     *     containers configuration
     * @param theHomes ContainerHome array for container config
     * @param theContextURLPath context path of the container provider
     * @throws CoreException if an argument is invalid 
     */
    public AntContainerManager(
        String theBuildFilePath,
        int thePort,
        String theTargetDir,
        Hashtable theHomes,
        String theContextURLPath)
        throws CoreException
    {
        this.buildFilePath = theBuildFilePath;
        init(thePort, theTargetDir, theHomes, theContextURLPath);
    }

    /**
     * Initializer.
     * @param thePort the port that will be used when setting up the containers
     * @param theTargetDir temporary directory to use for
     *     containers configuration
     * @param theHomes ContainerHome array for container config
     * @param theContextURLPath context path of the container provider 
     * @throws CoreException if an argument is invalid
     */
    public void init(
        int thePort,
        String theTargetDir,
        Hashtable theHomes,
        String theContextURLPath)
        throws CoreException
    {
        if (thePort <= 0)
        {
            throw CactusPlugin.createCoreException(
                "CactusLaunch.message.invalidproperty.port",
                null);
        }
        if (theTargetDir.equalsIgnoreCase(""))
        {
            throw CactusPlugin.createCoreException(
                "CactusLaunch.message.invalidproperty.tempdir",
                null);
        }
        if (theHomes.size() == 0)
        {
            throw CactusPlugin.createCoreException(
                "CactusLaunch.message.invalidproperty.containers",
                null);
        }
        if (theContextURLPath.equalsIgnoreCase(""))
        {
            throw CactusPlugin.createCoreException(
                "CactusLaunch.message.invalidproperty.contextpath",
                null);
        }
        this.contextURLPath = theContextURLPath;
        this.containerHomes = theHomes;
        antArguments.add("-Dcactus.port=" + thePort);
        antArguments.add("-Dcactus.target.dir=" + theTargetDir);
        // Avoid Ant console popups on win32 platforms
        if (BootLoader.getOS().equals(BootLoader.OS_WIN32))
        {
            antArguments.add("-Dcactus.jvm=javaw");
        }
        this.provider = (AntContainerProvider) getContainerProviders()[0];
    }

    /**
     * @return an array of provider containers supported by the manager
     */
    private IContainerProvider[] getContainerProviders()
    {
        String[] ids =
            (String[]) containerHomes.keySet().toArray(
                new String[containerHomes.size()]);
        IContainerProvider[] providers = new IContainerProvider[ids.length];
        for (int i = 0; i < providers.length; i++)
        {
            providers[i] =
                new AntContainerProvider(
                    this,
                    ids[i],
                    (String) containerHomes.get(ids[i]));
        }
        return providers;
    }

    /**
     * @param theTarget the Ant target to be called
     * @param theProviderArguments the Ant arguments specific for
     *     the container provider
     * @return a launch configuration copy for Ant build
     * @throws CoreException if the launch configuration cannot be created
     */
    public ILaunchConfigurationWorkingCopy createAntLaunchConfiguration(
        String[] theProviderArguments,
        String theTarget)
        throws CoreException
    {
        CactusPlugin thePlugin = CactusPlugin.getDefault();
        URL buildFileURL = thePlugin.find(new Path(buildFilePath));
        if (buildFileURL == null)
        {
            throw CactusPlugin.createCoreException(
                "CactusLaunch.message.prepare.error.plugin.file",
                " : " + buildFilePath,
                null);
        }
        File buildFileLocation = new File(buildFileURL.getPath());

        ILaunchManager launchManager =
            DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType antType =
            launchManager.getLaunchConfigurationType(
                IExternalToolConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
                
        String name = "Cactus container start-up";
        String uniqueName =
            launchManager.generateUniqueLaunchConfigurationNameFrom(name);
        ILaunchConfigurationWorkingCopy antConfig =
            antType.newInstance(null, uniqueName);

        antConfig.setAttribute(
            IExternalToolConstants.ATTR_LOCATION,
            buildFileLocation.getAbsolutePath());
        antConfig.setAttribute(
            IExternalToolConstants.ATTR_TOOL_ARGUMENTS,
            getString(getAllAntArguments(theProviderArguments)));
        antConfig.setAttribute(
            IExternalToolConstants.ATTR_ANT_TARGETS,
            theTarget);
        antConfig.setAttribute(
            IExternalToolConstants.ATTR_RUN_IN_BACKGROUND,
            false);
        return antConfig;
    }

    /**
     * @param theStringArray an array of String
     * @return the concatenation of the String elements 
     */
    private String getString(String[] theStringArray)
    {
        String result = "";
        for (int i = 0; i < theStringArray.length; i++)
        {
            result += theStringArray[i] + " ";
        }
        return result;
    }

    /**
     * @param theProviderArguments the container provider specific arguments
     * @return an array of arguments for the container build
     */
    private String[] getAllAntArguments(String[] theProviderArguments)
    {
        String[] managerArguments =
            (String[]) antArguments.toArray(new String[antArguments.size()]);
        String[] allArguments =
            new String[theProviderArguments.length + managerArguments.length];
        System.arraycopy(
            theProviderArguments,
            0,
            allArguments,
            0,
            theProviderArguments.length);
        System.arraycopy(
            managerArguments,
            0,
            allArguments,
            theProviderArguments.length,
            managerArguments.length);
        return allArguments;
    }

    /**
     * @see IContainerManager#prepare(org.eclipse.jdt.core.IJavaProject)
     */
    public void prepare(final IJavaProject theJavaProject)
    {
        this.prepared = false;
        final IRunnableWithProgress runnable = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor thePM) throws InterruptedException
            {
                try
                {
                    CactusPlugin.log("Preparing cactus tests");
                    prepareCactusTests(theJavaProject, thePM, provider);
                }
                catch (CoreException e)
                {
                    throw new InterruptedException(e.getMessage());
                }
            }
        };
        Display.getDefault().asyncExec(new Runnable()
        {
            public void run()
            {
                try
                {
                    ProgressMonitorDialog dialog =
                        new ProgressMonitorDialog(getShell());
                    dialog.run(true, true, runnable);
                }
                catch (InvocationTargetException e)
                {
                    CactusPlugin.displayErrorMessage(
                        CactusMessages.getString(
                            "CactusLaunch.message.prepare.error"),
                        e.getTargetException().getMessage(),
                        null);
                    cancelPreparation(provider);
                    return;
                }
                catch (InterruptedException e)
                {
                    CactusPlugin.displayErrorMessage(
                        CactusMessages.getString(
                            "CactusLaunch.message.prepare.error"),
                        e.getMessage(),
                        null);
                    cancelPreparation(provider);
                    return;
                }
            }
        });
        while (!prepared)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                CactusPlugin.log(e);
            }
        }
    }

    /**
     * creates the war file, deploys and launches the container.
     * @param theJavaProject the Java file
     * @param thePM the progress monitor to report to
     * @param theProvider the provider to prepare
     * @throws CoreException if anything goes wrong during preparation
     */
    private void prepareCactusTests(
        IJavaProject theJavaProject,
        IProgressMonitor thePM,
        IContainerProvider theProvider)
        throws CoreException
    {
        this.currentPM = thePM;
        thePM.beginTask(
            CactusMessages.getString("CactusLaunch.message.prepare"),
            10);
        try
        {
            URL warURL = createWar(theJavaProject, thePM);
            this.stateWarCreated = true;
            theProvider.deploy(this.contextURLPath, warURL, null, thePM);
            this.stateProviderDeployed = true;
            theProvider.start(null, thePM);
        }
        catch (MalformedURLException e)
        {
            CactusPlugin.log(e);
            throw CactusPlugin.createCoreException(
                "CactusLaunch.message.war.malformed",
                e);
        }
        thePM.done();
    }

    /**
     * @param theJavaProject the project to build the war from
     * @param thePM monitor that tracks the progression
     * @return the URL to the war file
     * @throws JavaModelException if we cannot create the war
     * @throws CoreException if we cannot create the war
     * @throws MalformedURLException if we cannot create the war
     */
    private URL createWar(IJavaProject theJavaProject, IProgressMonitor thePM)
        throws JavaModelException, CoreException, MalformedURLException
    {
        WarBuilder newWar = new WarBuilder(theJavaProject);
        this.war = newWar.createWar(thePM);
        return war.toURL();
    }

    /**
     * Launches a new progress dialog for preparation cancellation.
     * @param theProvider the provider which preparation to cancel
     */
    private void cancelPreparation(final IContainerProvider theProvider)
    {
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
        IRunnableWithProgress tearDownRunnable = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor thePM) throws InterruptedException
            {
                try
                {
                    teardownCactusTests(thePM, theProvider);
                }
                catch (CoreException e)
                {
                    throw new InterruptedException(e.getMessage());
                }
            }
        };
        try
        {
            dialog.run(true, true, tearDownRunnable);
        }
        catch (InvocationTargetException tearDownE)
        {
            CactusPlugin.displayErrorMessage(
                CactusMessages.getString("CactusLaunch.message.teardown.error"),
                tearDownE.getTargetException().getMessage(),
                null);
        }
        catch (InterruptedException tearDownE)
        {
            CactusPlugin.displayErrorMessage(
                CactusMessages.getString("CactusLaunch.message.teardown.error"),
                tearDownE.getMessage(),
                null);
        }
    }

    /**
     * Tears down the Cactus tests
     */
    public void tearDown()
    {
        final IRunnableWithProgress runnable = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor thePM) throws InterruptedException
            {
                CactusPlugin.log("Tearing down cactus tests");
                try
                {
                    teardownCactusTests(thePM, provider);
                }
                catch (CoreException e)
                {
                    throw new InterruptedException(e.getMessage());
                }
            }
        };
        Display.getDefault().asyncExec(new Runnable()
        {
            public void run()
            {
                try
                {
                    ProgressMonitorDialog dialog =
                        new ProgressMonitorDialog(getShell());
                    dialog.run(true, true, runnable);
                }
                catch (InvocationTargetException e)
                {
                    CactusPlugin.displayErrorMessage(
                        CactusMessages.getString(
                            "CactusLaunch.message.teardown.error"),
                        e.getTargetException().getMessage(),
                        null);
                    cancelPreparation(provider);
                    return;
                }
                catch (InterruptedException e)
                {
                    CactusPlugin.displayErrorMessage(
                        CactusMessages.getString(
                            "CactusLaunch.message.teardown.error"),
                        e.getMessage(),
                        null);
                    cancelPreparation(provider);
                    return;
                }
            }

        });
    }

    /**
     * Stops the container and undeploys (cleans) it.
     * @param thePM a progress monitor that reflects progress made while tearing
     * down the container setup
     * @param theProvider the provider of the container to stop and undeploy
     * @throws CoreException if an error occurs when tearing down
     */
    private void teardownCactusTests(
        IProgressMonitor thePM,
        IContainerProvider theProvider)
        throws CoreException
    {
        thePM.beginTask(
            CactusMessages.getString("CactusLaunch.message.teardown"),
            100);
        theProvider.stop(null, thePM);
        if (stateProviderDeployed)
        {
            theProvider.undeploy(null, null, thePM);
        }
        if (stateWarCreated)
        {
            this.war.delete();
        }
        thePM.done();
    }

    /**
     * Convenience method to get the active Shell.
     * @return the active shell 
     */
    protected Shell getShell()
    {
        return CactusPlugin.getActiveWorkbenchShell();
    }

    /**
     * @param theRunner the Eclipse runner to call when tests are done
     */
    public void setEclipseRunner(EclipseRunTests theRunner)
    {
        provider.setEclipseRunner(theRunner);
    }

    /**
     * 
     */
    public void preparationDone()
    {
        this.prepared = true;
    }

}
