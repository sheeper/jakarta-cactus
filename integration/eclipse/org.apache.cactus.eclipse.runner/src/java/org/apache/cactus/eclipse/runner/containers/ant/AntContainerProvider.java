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

import java.net.URL;
import java.util.Vector;

import org.apache.cactus.eclipse.runner.containers.ContainerInfo;
import org.apache.cactus.eclipse.runner.containers.Credential;
import org.apache.cactus.eclipse.runner.containers.IContainerProvider;
import org.apache.cactus.eclipse.runner.ui.CactusMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

/**
 * Implementation of IContainerProvider that uses ant scripts to set up
 * and launch containers.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * 
 * @version $Id$
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
     * A boolean indicating if the container is running.
     */
    private boolean serverStopped;
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
    public AntContainerProvider(
        AntContainerManager theManager,
        String theTargetMask,
        String theHome)
    {
        this.targetMask = theTargetMask;
        this.home = theHome;
        this.manager = theManager;
    }

    /**
     * @see IContainerProvider#deploy(String, URL, Credential)
     */
    public void deploy(
        String theContextPath,
        URL theDeployableObject,
        Credential theCredentials,
        IProgressMonitor thePM)
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
        ILaunchManager launchManager =
            DebugPlugin.getDefault().getLaunchManager();
        
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
                while (!this.startLaunch.isTerminated())
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
    public void undeploy(
        String theContextPath,
        Credential theCredentials,
        IProgressMonitor thePM)
        throws CoreException
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
     * @param theEclipseRunner the EclipseRunTests instance to associate with
     * this container provider, which will be notified of test run end.
     */
    public void setEclipseRunner(EclipseRunTests theEclipseRunner)
    {
        this.eclipseRunner = theEclipseRunner;
    }

    /**
     * Returns the concatenated String of the prefix and the provider target.
     * @param thePrefix prefix to append to the provider target
     * @return String the concatenated string
     */
    private String getTarget(String thePrefix)
    {
        return thePrefix + this.targetMask;
    }
}