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
package org.apache.cactus.eclipse.runner.containers.ant;

import java.net.URL;
import java.util.Vector;

import org.apache.cactus.eclipse.runner.containers.ContainerInfo;
import org.apache.cactus.eclipse.runner.containers.Credential;
import org.apache.cactus.eclipse.runner.containers.IContainerProvider;
import org.apache.cactus.eclipse.runner.ui.CactusMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

/**
 * Implementation of IContainerProvider that uses ant scripts to set up
 * and launch containers.
 * 
 * @version $Id: AntContainerProvider.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class AntContainerProvider implements IContainerProvider
{
    /**
     * the manager of this provider
     */
    private AntContainerManager manager;

    /**
     * the Ant arguments specific to this provider
     */
    private Vector antArguments = new Vector();

    /**
     * The Eclipse runner associated to the Ant container provider.
     */
    private EclipseRunTests eclipseRunner;

    /**
     * The home directory of this provider.
     */
    private String home;

    /**
     * The Ant target mask for this provider, for example "tomcat4x".
     */
    private String targetMask;

    /**
     * Reference to the launch created to start the container.
     */    
    private ILaunch startLaunch;
    
    /**
     * @param theManager the manager of this provider
     * @param theTargetMask target mask for this provider
     * @param theHome home directory for this provider's container
     */
    public AntContainerProvider(AntContainerManager theManager,
        String theTargetMask, String theHome)
    {
        this.targetMask = theTargetMask;
        this.home = theHome;
        this.manager = theManager;
    }

    /**
     * @see IContainerProvider#deploy(String, URL, Credential)
     */
    public void deploy(String theContextPath, URL theDeployableObject,
        Credential theCredentials, IProgressMonitor thePM)
        throws CoreException
    {
        thePM.subTask(CactusMessages.getString("CactusLaunch.message.deploy"));
        String warPath = theDeployableObject.getPath();
        antArguments.add("-Dcactus.war=" + warPath);
        antArguments.add("-Dcactus.context=" + theContextPath);
    }

    /**
     * @see IContainerProvider#start(ContainerInfo)
     */
    public void start(ContainerInfo theContainerInfo, IProgressMonitor thePM)
        throws CoreException
    {
        thePM.subTask(CactusMessages.getString("CactusLaunch.message.start"));
        String target = getTarget("cactus.run.");
        antArguments.add(
            "-Dcactus.test.task=" + EclipseRunTests.class.getName());
        antArguments.add("-Dcactus.home." + this.targetMask + "=" + home);
        String[] arguments =
            (String[]) antArguments.toArray(new String[antArguments.size()]);
        ILaunchConfigurationWorkingCopy antCopy =
            this.manager.createAntLaunchConfiguration(arguments, target);
        this.startLaunch = antCopy.launch(
            ILaunchManager.RUN_MODE,
            new SubProgressMonitor(thePM, 8));
    }

    /**
     * @see IContainerProvider#stop(ContainerInfo)
     */
    public void stop(ContainerInfo theContainerInfo, IProgressMonitor thePM)
        throws CoreException
    {
        thePM.subTask(CactusMessages.getString("CactusLaunch.message.stop"));
        if (eclipseRunner != null)
        {
            eclipseRunner.finish();
            try
            {
                thePM.worked(30);
                while (this.startLaunch == null
                    || !this.startLaunch.isTerminated())
                {
                    Thread.sleep(300);
                    thePM.worked(7);
                }
            }
            catch (InterruptedException e)
            {
                // Do nothing
            }
        }
    }

    /**
     * @see IContainerProvider#undeploy (String, Credential)
     */
    public void undeploy(String theContextPath, Credential theCredentials,
        IProgressMonitor thePM) throws CoreException
    {
        thePM.subTask(
            CactusMessages.getString("CactusLaunch.message.undeploy"));
        String antTarget = getTarget("cactus.clean.");
        String[] arguments =
            (String[]) antArguments.toArray(new String[antArguments.size()]);
        ILaunchConfigurationWorkingCopy antCopy =
            this.manager.createAntLaunchConfiguration(arguments, antTarget);
        antCopy.launch(
            ILaunchManager.RUN_MODE,
            new SubProgressMonitor(thePM, 50));
    }
    
    /**
     * @param theEclipseRunner The EclipseRunTests instance to associate with
     *        this container provider, which will be notified of test run end
     */
    public void setEclipseRunner(EclipseRunTests theEclipseRunner)
    {
        this.eclipseRunner = theEclipseRunner;
    }

    /**
     * Returns the concatenated String of the prefix and the provider target.
     * 
     * @param thePrefix The prefix to append to the provider target
     * @return The concatenated string
     */
    private String getTarget(String thePrefix)
    {
        return thePrefix + this.targetMask;
    }
}
