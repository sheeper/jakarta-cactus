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
package org.apache.cactus.eclipse.runner.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import org.apache.cactus.eclipse.runner.common.JarFilenameFilter;
import org.apache.cactus.eclipse.runner.common.LibraryHelper;
import org.apache.cactus.eclipse.runner.containers.jetty.JettyContainerManager;
import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.apache.cactus.eclipse.runner.ui.CactusPreferences;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILibrary;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Provides a launcher to start Cactus tests. This is done by extending
 * the JUnit Plugin launch configuration and adding Cactus specific VM
 * configuration (Cactus jars, VM parameters) and by registering this
 * class as an 
 * <code>"org.eclipse.debug.core.launchConfigurationTypes"</code> Eclipse
 * extension point.
 * 
 * @version $Id$
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 */
public class JettyCactusLaunchConfiguration
    extends CactusLaunchConfiguration
{
    /**
     * Id under which the Cactus launch configuration has been registered. 
     */
    public static final String ID_CACTUS_APPLICATION_JETTY =
        "org.apache.cactus.eclipse.runner.launchconfig.jetty";

    /**
     * Path to the Jetty library directory in the Cactus plugin
     * structure
     */
    private static final String JETTY_LIBRARY_PATH = "./lib/";

    /**
     * @return an array of classpaths needed for Cactus
     * @throws CoreException when an error occurs while
     * trying to build the classpath
     */
    protected IClasspathEntry[] getCactusClasspath() throws CoreException
    {
        IClasspathEntry[] cactusClasspath = super.getCactusClasspath();
        cactusClasspath =
                LibraryHelper.concatenateEntries(
                    cactusClasspath,
                    getJettyClasspath());
        return cactusClasspath;
    }

    /**
     * @return an array of Jar paths needed for Cactus
     * @throws CoreException if a Jar cannot be found
     */
    private IClasspathEntry[] getJettyClasspath() throws CoreException
    {
        CactusPlugin thePlugin = CactusPlugin.getDefault();
        URL libURL = thePlugin.find(new Path(JETTY_LIBRARY_PATH));
        if (libURL == null)
        {
            throw CactusPlugin.createCoreException(
                "CactusLaunch.message.prepare.error.plugin.file",
                " : " + libURL.getPath(),
                null);
        }
        File libDir = new File(libURL.getPath());
        IClasspathEntry[] jettyJarPaths = getJarPaths(libDir);
        IClasspathEntry[] apacheJarPaths = new IClasspathEntry[0];
        Plugin tomcatPlugin = Platform.getPlugin("org.eclipse.tomcat");
        if (tomcatPlugin != null)
        {
            IPluginDescriptor descriptor = tomcatPlugin.getDescriptor();
            apacheJarPaths = getLibrariesPaths(descriptor, "org.apache.jasper");
        }
        return LibraryHelper.concatenateEntries(jettyJarPaths, apacheJarPaths);
    }

    /**
     * @param theJavaProject the Java project to get the arguments for
     * @return an array of the specific Cactus VM arguments
     */
    protected String getCactusVMArgs(IJavaProject theJavaProject)
    {
        String cactusVMArgs = super.getCactusVMArgs(theJavaProject);
        String jettyResourcePath =
            CactusPreferences.getTempDir()
                + File.separator
                + JettyContainerManager.getJettyWebappPath();
        cactusVMArgs += "-Dcactus.jetty.resourceDir="
            + jettyResourcePath
            + VM_ARG_SEPARATOR;
        IPath projectLocation = theJavaProject.getProject().getLocation();
        File jettyXML =
            projectLocation.append(CactusPreferences.getJettyXML()).toFile();
        if (jettyXML.exists())
        {
            cactusVMArgs += "-Dcactus.jetty.config="
                + jettyXML.getAbsolutePath()
                + VM_ARG_SEPARATOR;
        }
        cactusVMArgs += "-Dcactus.initializer="
            + "org.apache.cactus.extension.jetty.JettyInitializer";
        return cactusVMArgs;
    }

    /**
     * @param theDirectory the directory to list jars from
     * @return the array of jar paths in the given directory
     */
    private IClasspathEntry[] getJarPaths(File theDirectory)
    {
        File[] jars = theDirectory.listFiles(new JarFilenameFilter());
        IClasspathEntry[] jarPaths = new IClasspathEntry[jars.length];
        for (int i = 0; i < jarPaths.length; i++)
        {
            jarPaths[i] =
                LibraryHelper.getIClasspathEntry(jars[i].getAbsolutePath());
        }
        return jarPaths;
    }

    /**
     * @param theDescriptor the plug-in descriptor to get libraries from
     * @param thePackagePrefix package prefix used to filter libraries 
     * @return an array of jar paths exposed by the plug-in
     */
    private IClasspathEntry[] getLibrariesPaths(
        IPluginDescriptor theDescriptor, String thePackagePrefix)
    {
        Vector result = new Vector();
        URL root = theDescriptor.getInstallURL();
        ILibrary[] libraries = theDescriptor.getRuntimeLibraries();
        for (int i = 0; i < libraries.length; i++)
        {
            ILibrary currentLib = libraries[i];
            if (thePackagePrefix == null
                || isContained(thePackagePrefix, currentLib))
            {
                try
                {
                    URL url = new URL(root, currentLib.getPath().toString());
                    result.add(
                        LibraryHelper.getIClasspathEntry(
                            Platform.asLocalURL(url).getPath()));
                }
                catch (IOException e)
                {
                    // if the URL is not valid we don't add it
                    CactusPlugin.log(e);
                    continue;
                }
            }
        }
        return (IClasspathEntry[]) result.toArray(
            new IClasspathEntry[result.size()]);
    }

    /**
     * @param thePackagePrefix prefix which presence is to be tested
     * @param theCurrentLib the library in which the prefix will be searched 
     * @return true if the library declares the given package prefix
     */
    private boolean isContained(String thePackagePrefix, ILibrary theCurrentLib)
    {
        String[] prefixes = theCurrentLib.getPackagePrefixes();
        for (int i = 0; i < prefixes.length; i++)
        {
            if (prefixes[i].equals(thePackagePrefix))
            {
                return true;
            }
        }
        return false;
    }
}
