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
import org.apache.cactus.eclipse.ui.CactusMessages;
import org.apache.cactus.eclipse.ui.CactusPlugin;
import org.apache.tools.ant.BuildException;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

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
     * @throws CoreException if an argument is invalid
     */
    public GenericAntProvider(
        int thePort,
        String theTargetDir,
        ContainerHome[] theHomes)
        throws CoreException
    {
        if (thePort <= 0)
        {
            CactusPlugin.throwCoreException(
                "CactusLaunch.message.invalidproperty.port", null);
        }
        if (theTargetDir.equalsIgnoreCase(""))
        {
            CactusPlugin.throwCoreException(
                "CactusLaunch.message.invalidproperty.tempdir", null);
        }
        if (theHomes.length == 0)
        {
            CactusPlugin.throwCoreException(
                "CactusLaunch.message.invalidproperty.containers", null);
        }
        port = thePort;
        containerHomes = theHomes;
        antArguments = new Vector();
        antArguments.add("-Dcactus.port=" + thePort);
        for (int i = 0; i < containerHomes.length; i++)
        {
            ContainerHome currentContainerHome = containerHomes[i];
            antArguments.add(
                "-Dcactus.home."
                    + currentContainerHome.getTargetMask()
                    + "="
                    + currentContainerHome.getDirectory());
        }
        CactusPlugin thePlugin = CactusPlugin.getDefault();
        //File antFilesLocation =
        //    new File(thePlugin.find(new Path("./ant")).getPath());
        buildFileLocation =
            new File(
                thePlugin.find(new Path("./ant/build.xml")).getPath());
        //antArguments.add("-Dbase.dir=" + antFilesLocation.getAbsolutePath());
        antArguments.add("-Dcactus.target.dir=" + theTargetDir);
        // Avoid Ant console popups on win32 platforms
        if (BootLoader.getOS().equals(BootLoader.OS_WIN32))
        {
            antArguments.add("-Dcactus.jvm=javaw");
        }
    }
    
    /**
     * @see IContainerProvider#start(ContainerInfo)
     */
    public void start(ContainerInfo theContainerInfo, IProgressMonitor thePM)
        throws CoreException
    {
        thePM.subTask(CactusMessages.getString("CactusLaunch.message.start"));
        String[] targets = getMasked("cactus.start.");
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
            CactusPlugin.throwCoreException(
                "CactusLaunch.message.start.error",
                e);
        }
        startHelper.setTestURL(testURL);
        startHelper.setProgressMonitor(new SubProgressMonitor(thePM, 4));
        try
        {
            startHelper.execute();
        }
        catch (BuildException e)
        {
            CactusPlugin.throwCoreException(
                "CactusLaunch.message.start.error",
                e);
        }
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
        contextPath = theContextPath;
        String warPath = theDeployableObject.getPath();
        antArguments.add("-Dcactus.war=" + warPath);
        antArguments.add("-Dcactus.context=" + theContextPath);
        String[] warTarget = { "cactus.war.framework" };
        String[] setupTargets = getMasked("cactus.setup.");
        String[] deployTargets = getMasked("cactus.deploy.");
        createAntRunner(warTarget).run(new SubProgressMonitor(thePM, 1));
        createAntRunner(setupTargets).run(new SubProgressMonitor(thePM, 1));
        createAntRunner(deployTargets).run(new SubProgressMonitor(thePM, 1));
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
        String[] targets = getMasked("cactus.clean.");
        createAntRunner(targets).run(/*thePM*/);
    }

    /**
     * @see IContainerProvider#stop(ContainerInfo)
     */
    public void stop(ContainerInfo theContainerInfo, IProgressMonitor thePM)
        throws CoreException
    {
        String[] targets = getMasked("cactus.stop.");
        createAntRunner(targets).run(/*thePM*/);
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
