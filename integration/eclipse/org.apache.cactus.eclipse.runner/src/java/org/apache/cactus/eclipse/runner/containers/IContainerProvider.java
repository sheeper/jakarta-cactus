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
package org.apache.cactus.eclipse.runner.containers;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface for container configuration and startup.
 * 
 * @version $Id$
 */
public interface IContainerProvider
{
    /**
     * Starts the container.
     * @param theContainerInfo detail of the container configuration
     * @param thePM the monitor that reflects progress made while starting
     * @throws CoreException when starting fails
     */
    void start(ContainerInfo theContainerInfo, IProgressMonitor thePM)
        throws CoreException;

    /**
     * Deploy a webapp to the container.
     * @param theContextPath path to the webapp (for example "test")
     * @param theDeployableObject war file to be deployed
     * @param theCredentials credentials for deployment (user:pwd)
     * @param thePM the monitor that reflects progress made while deploying
     * @throws CoreException when deployment fails 
     */
    void deploy(
        String theContextPath,
        URL theDeployableObject,
        Credential theCredentials,
        IProgressMonitor thePM)
        throws CoreException;

    /**
     * UnDeploy a webapp to the container.
     * @param theContextPath path to the webapp
     * @param theCredentials credentials for undeployment (user:pwd)
     * @param thePM the monitor that reflects progress made while undeploying
     * @throws CoreException when undeployment fails
     */
    void undeploy(
        String theContextPath,
        Credential theCredentials,
        IProgressMonitor thePM)
        throws CoreException;

    /**
     * Stops the container.
     * @param theContainerInfo detail of the container configuration
     * @param thePM the monitor that reflects progress made while stopping
     * @throws CoreException when stopping fails
     */
    void stop(ContainerInfo theContainerInfo, IProgressMonitor thePM)
        throws CoreException;
}
