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
package org.apache.cactus.eclipse.containers.ant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.cactus.eclipse.containers.ContainerInfo;
import org.apache.cactus.eclipse.containers.Credential;
import org.apache.cactus.eclipse.containers.IContainerProvider;
import org.apache.cactus.eclipse.ui.CactusPlugin;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

/**
 * Implementation of a container provider that uses ant scripts to set up
 * and launch containers.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * 
 * @version $Id: $
 */
public class GenericAntProvider implements IContainerProvider
{
    /**
     * the arguments given to the AntRunner
     */
    private Vector antArguments;
    /**
     * location of the Ant build file to be used 
     */
    private File buildFileLocation;
    /**
     * the port that will be used when setting up the container
     */
    private int port;
    /**
     * the context path for the test web app
     */
    private String contextPath = null;

    /**
     * Array containing all the information needed
     * (target mask & container home directory)
     * to call the scripts
     */
    private ContainerHome[] containerHomes;

    /**
     * Constructor.
     * @param thePort the port that will be used when setting up the container
     * @param theTargetDir the directory to be used for container configuration
     * @param theHomes ContainerHome array for container config
     */
    public GenericAntProvider(
        int thePort,
        String theTargetDir,
        ContainerHome[] theHomes)
    {
        port = thePort;
        containerHomes = theHomes;
        antArguments = new Vector();
        antArguments.add("-Dtest.port=" + thePort);
        for (int i = 0; i < containerHomes.length; i++)
        {
            ContainerHome currentContainerHome = containerHomes[i];
            antArguments.add(
                "-Dhome."
                    + currentContainerHome.getTargetMask()
                    + "="
                    + currentContainerHome.getDirectory());
        }
        CactusPlugin thePlugin = CactusPlugin.getDefault();
        File antFilesLocation =
            new File(thePlugin.find(new Path("./ant")).getPath());
        buildFileLocation =
            new File(
                thePlugin.find(new Path("./ant/build/build.xml")).getPath());
        antArguments.add("-Dbase.dir=" + antFilesLocation.getAbsolutePath());
        antArguments.add("-Dtarget.dir=" + theTargetDir);
    }
    /**
     * @see org.apache.cactus.eclipse.containers.IContainerProvider#start(org.apache.cactus.eclipse.containers.ContainerInfo)
     */
    public void start(ContainerInfo theContainerInfo) throws CoreException
    {
        String[] targets = getMasked("start.");
        AntRunner runner = createAntRunner(targets);
        StartServerHelper startHelper = new StartServerHelper(runner);
        URL testURL = null;
        try
        {
            testURL =
                new URL(
                    "http://localhost:"
                        + port
                        + "/"
                        + contextPath
                        + "/ServletRedirector"
                        + "?Cactus_Service=RUN_TEST");
        }
        catch (MalformedURLException e)
        {
            throw new CoreException(
                new Status(
                    IStatus.ERROR,
                    CactusPlugin.getPluginId(),
                    IStatus.OK,
                    e.getMessage(),
                    e));
        }
        startHelper.setTestURL(testURL);
        startHelper.execute();
    }

    /**
     * @see org.apache.cactus.eclipse.containers.IContainerProvider#deploy(java.lang.String, java.net.URL, org.apache.cactus.eclipse.containers.Credential)
     */
    public void deploy(
        String theContextPath,
        URL theDeployableObject,
        Credential theCredentials)
        throws CoreException
    {
        contextPath = theContextPath;
        String warPath = theDeployableObject.getPath();
        antArguments.add("-Dwar.path=" + warPath);
        antArguments.add("-Dcontext.path=" + theContextPath);
        String[] targets = getMasked("prepare.");
        createAntRunner(targets).run();

    }

    /**
     * @see org.apache.cactus.eclipse.containers.IContainerProvider#undeploy(java.lang.String, org.apache.cactus.eclipse.containers.Credential)
     */
    public void undeploy(String theContextPath, Credential theCredentials)
        throws CoreException
    {
        String[] targets = { "clean" };
        createAntRunner(targets).run();
    }

    /**
     * @see org.apache.cactus.eclipse.containers.IContainerProvider#stop(org.apache.cactus.eclipse.containers.ContainerInfo)
     */
    public void stop(ContainerInfo theContainerInfo) throws CoreException
    {
        String[] targets = getMasked("stop.");
        createAntRunner(targets).run();
    }

    /**
     * returns an AntRunner for this provider.
     * @param theTargets the ant target to be called (in that order)
     * @return the AntRunner for the script
     */
    private AntRunner createAntRunner(String[] theTargets)
    {
        AntRunner runner = new AntRunner();
        runner.setBuildFileLocation(buildFileLocation.getAbsolutePath());
        runner.setArguments((String[]) antArguments.toArray(new String[0]));
        runner.setExecutionTargets(theTargets);
        return runner;
    }

    /**
     * Returns a String array adding the containerHomes masks to the elements.
     * @param thePrefix prefix to add to the mask
     * @return String[] the masked array
     */
    private String[] getMasked(String thePrefix)
    {
        String[] result = new String[containerHomes.length];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = thePrefix + containerHomes[i].getTargetMask();
        }
        return result;
    }
}
