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
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.cactus.eclipse.runner.containers.IContainerManager;
import org.apache.cactus.eclipse.runner.containers.IContainerProvider;
import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

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
     * Constructor.
     * @param theBuildFilePath path to the Ant build file for this manager
     * @param thePort the port that will be used when setting up the containers
     * @param theTargetDir temporary directory to use for
     *     containers configuration
     * @param theHomes ContainerHome array for container config
     * @throws CoreException if an argument is invalid
     */
    public AntContainerManager(
        String theBuildFilePath,
        int thePort,
        String theTargetDir,
        Hashtable theHomes)
        throws CoreException
    {
        this.buildFilePath = theBuildFilePath;
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
        this.containerHomes = theHomes;
        antArguments.add("-Dcactus.port=" + thePort);
        antArguments.add("-Dcactus.target.dir=" + theTargetDir);
        // Avoid Ant console popups on win32 platforms
        if (BootLoader.getOS().equals(BootLoader.OS_WIN32))
        {
            antArguments.add("-Dcactus.jvm=javaw");
        }
    }

    /**
     * @see IContainerManager#getContainerProvider()
     */
    public IContainerProvider[] getContainerProviders()
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
     * returns an AntRunner for a container provider.
     * @param theTarget the ant target to be called (in that order)
     * @param theProviderArguments the Ant arguments specific for
     *     the container provider
     * @return the AntRunner for the script
     * @throws CoreException if an AntRunner cannot be created
     */
    public AntRunner createAntRunner(
        String[] theProviderArguments,
        String theTarget)
        throws CoreException
    {
        AntRunner runner = new AntRunner();
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
        runner.setBuildFileLocation(buildFileLocation.getAbsolutePath());
        runner.setArguments(getAllAntArguments(theProviderArguments));
        runner.setExecutionTargets(new String[] {theTarget});
        return runner;
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
}
