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
package org.apache.cactus.eclipse.runner.launcher;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
import org.eclipse.jdt.core.JavaCore;

/**
 * Provides a launcher to start Cactus tests. This is done by extending
 * the JUnit Plugin launch configuration and adding Cactus specific VM
 * configuration (Cactus jars, VM parameters) and by registering this
 * class as an 
 * <code>"org.eclipse.debug.core.launchConfigurationTypes"</code> Eclipse
 * extension point.
 * 
 * @version $Id: JettyCactusLaunchConfiguration.java 238816 2004-02-29 16:36:46Z vmassol $
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
        try {
        	cactusClasspath =
                LibraryHelper.concatenateEntries(
                    cactusClasspath,
                    getJettyClasspath());
        } catch (MalformedURLException me) {
        	throw CactusPlugin.createCoreException(
                    "CactusLaunch.message.prepare.error.plugin.file",
                    " : " + me.toString(),
                    null);
        }
        return cactusClasspath;
    }

    /**
     * @return an array of Jar paths needed for Cactus
     * @throws CoreException if a Jar cannot be found
     * @throws MalformedURLException 
     */
    private IClasspathEntry[] getJettyClasspath() throws CoreException, MalformedURLException
    {
        CactusPlugin thePlugin = CactusPlugin.getDefault();
        //CactusPlugin.log("Just here:"+LibraryHelper.getLibPath().toString());
        
        //URL libURL = thePlugin.find(LibraryHelper.getLibPath());
        
        IPath libURL = LibraryHelper.getLibPath();
        
        if (libURL == null)
        {
            throw CactusPlugin.createCoreException(
                "CactusLaunch.message.prepare.error.plugin.file",
                " : " + libURL.toString(),
                null);
        }
        File libDir = new File(libURL.toString());
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
     * @throws MalformedURLException 
     */
    private IClasspathEntry[] getLibrariesPaths(
        IPluginDescriptor theDescriptor, String thePackagePrefix) throws MalformedURLException
    {
    	CactusPlugin.log("PPrefix"+thePackagePrefix);
        Vector result = new Vector();
        URL root = new URL(("file://"+JavaCore.getClasspathVariable("ECLIPSE_HOME").toFile().toURL().getPath()+theDescriptor.getInstallURL().getPath()).replaceAll("plugin","plugins"));
        CactusPlugin.log("Root"+root.getPath());
        ILibrary[] libraries = theDescriptor.getRuntimeLibraries();
        
        for (int i = 0; i < libraries.length; i++)
        {
        	
            ILibrary currentLib = libraries[i];
            //CactusPlugin.log("Current library "+currentLib.getPath().toString());
            if (thePackagePrefix == null
                || isContained(thePackagePrefix, currentLib))
            {
                try
                {
                    URL url = new URL(root, currentLib.getPath().toString());
                    result.add(
                        LibraryHelper.getIClasspathEntry(
                            Platform.asLocalURL(url).getPath()));
                    CactusPlugin.log("Added");
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
        
    	//CactusPlugin.log("Path of the lib:"+theCurrentLib.getPath().toString());
    	
    	String[] prefixes = theCurrentLib.getPackagePrefixes();
        if(prefixes==null || prefixes.length==0) {
        	//CactusPlugin.log("lib: "+theCurrentLib.getPath().toString()+" is not");
        	return false;
        }
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
