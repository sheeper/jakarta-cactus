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
import org.apache.cactus.eclipse.runner.ui.CactusMessages;
import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.apache.cactus.eclipse.runner.ui.CactusPreferences;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILibrary;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfiguration;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.junit.ITestRunListener;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.swt.widgets.Display;

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
public class CactusLaunchConfiguration
    extends JUnitLaunchConfiguration
    implements ITestRunListener
{
    /**
     * Indicates whether we already went through the launch cycle.
     * This is used because there currently is no way to unregister
     * an ITestRunListener from the JUnit plugin.
     */
    private boolean launchEnded;

    /**
     * Id under which the Cactus launch configuration has been registered. 
     */
    public static final String ID_CACTUS_APPLICATION =
        "org.apache.cactus.eclipse.runner.launchconfig";

    /**
     * Path to the Jetty library directory in the Cactus plugin
     * structure
     */
    private static final String JETTY_LIBRARY_PATH = "./lib/";

    /**
     * Returns a valid VM configuration for Cactus. This method overrides
     * the JUnit plugin one in order to add Cactus required VM parameters. 
     *
     * @param theConfiguration the launch configuration
     * @param theMode the mode (debug, run)
     * @param theTests the JUnit tests that will be run
     * @param thePort the JUnit remote port
     * @return the configuration for the VM in which to run the tests. It
     *         includes both the JUnit plugin configuration and the Cactus
     *         required configuration 
     * 
     * @exception CoreException on critical failures
     */
    protected VMRunnerConfiguration launchTypes(
        ILaunchConfiguration theConfiguration,
        String theMode,
        IType[] theTests,
        int thePort)
        throws CoreException
    {
        CactusPlugin.log("creating VMRunnerConfiguration for Cactus");
        VMRunnerConfiguration configuration =
            super.launchTypes(theConfiguration, theMode, theTests, thePort);
        String[] jUnitArgs = configuration.getVMArguments();
        String[] cactusVMArgs = getCactusVMArgs(theTests);
        String[] globalArgs = concatenateStringArrays(jUnitArgs, cactusVMArgs);
        configuration.setVMArguments(globalArgs);
        CactusPlugin.log("Cactus VM arguments : [" + cactusVMArgs + "]");

        String[] junitClasspath = configuration.getClassPath();
        String[] cactusClasspath = getCactusClasspath();
        String[] globalClasspath =
            concatenateStringArrays(cactusClasspath, junitClasspath);
        VMRunnerConfiguration cactusConfig =
            new VMRunnerConfiguration(
                configuration.getClassToLaunch(),
                globalClasspath);
        CactusPlugin.log(
            "Cactus VM classpath : ["
                + getRepresentation(cactusClasspath)
                + "]");

        cactusConfig.setBootClassPath(configuration.getBootClassPath());
        cactusConfig.setProgramArguments(configuration.getProgramArguments());
        cactusConfig.setVMArguments(configuration.getVMArguments());
        cactusConfig.setWorkingDirectory(configuration.getWorkingDirectory());
        return cactusConfig;
    }

    /**
     * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String)
     */
    public void launch(
        final ILaunchConfiguration theConfiguration,
        final String theMode,
        final ILaunch theLaunch,
        final IProgressMonitor thePM)
        throws CoreException
    {
        this.launchEnded = false;
        // Register the instance of CactusLaunchShortcut to the JUnitPlugin
        // for TestRunEnd notification.
        JUnitPlugin.getDefault().addTestRunListener(this);
        final IJavaProject javaProject = getJavaProject(theConfiguration);
        // Run the preparation in a new thread so that the UI thread which is
        // the current thread be not blocked by the sleep of
        // IContainerManager.prepare().
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    CactusPlugin.getContainerManager(true).prepare(javaProject);
                    Display.getDefault().asyncExec(new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                CactusLaunchConfiguration.super.launch(
                                    theConfiguration,
                                    theMode,
                                    theLaunch,
                                    thePM);
                            }
                            catch (CoreException e)
                            {
                                CactusPlugin.displayErrorMessage(
                                    CactusMessages.getString(
                            "CactusLaunch.message.containerManager.error"),
                                    e.getMessage(),
                                    null);
                                return;
                            }
                        }
                    });
                }
                catch (CoreException e)
                {
                    CactusPlugin.displayErrorMessage(
                        CactusMessages.getString(
                            "CactusLaunch.message.containerManager.error"),
                        e.getMessage(),
                        null);
                    return;
                }
            }
        }).start();




    }

    /**
     * @param theClasspath an array of classpaths
     * @return a representation of the given classpath array
     */
    private String getRepresentation(String[] theClasspath)
    {
        String cactusClasspathRepresentation = "";
        for (int i = 0; i < theClasspath.length; i++)
        {
            cactusClasspathRepresentation += theClasspath[i] + ";";
        }
        return cactusClasspathRepresentation;
    }

    /**
     * @return an array of classpaths needed for Cactus
     * @throws CoreException when an error occurs while
     * trying to build the classpath
     */
    private String[] getCactusClasspath() throws CoreException
    {
        String[] clientClasspath = LibraryHelper.getClientJarPaths();
        String[] commonClasspath = LibraryHelper.getCommonJarPaths();
        String[] cactusClasspath =
            concatenateStringArrays(clientClasspath, commonClasspath);
        URL[] antURLs = AntCorePlugin.getPlugin().getPreferences().getAntURLs();
        String[] apacheJarPaths = getJarPaths(antURLs);
        cactusClasspath =
            concatenateStringArrays(cactusClasspath, apacheJarPaths);
        if (CactusPreferences.getJetty())
        {
            cactusClasspath =
                concatenateStringArrays(cactusClasspath, getJettyClasspath());
        }
        return cactusClasspath;
    }

    /**
     * @return an array of Jar paths needed for Cactus
     * @throws CoreException if a Jar cannot be found
     */
    private String[] getJettyClasspath() throws CoreException
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
        String[] jettyJarPaths = getJarPaths(libDir);
        String[] apacheJarPaths = new String[0];
        Plugin tomcatPlugin = Platform.getPlugin("org.eclipse.tomcat");
        if (tomcatPlugin != null)
        {
            IPluginDescriptor descriptor = tomcatPlugin.getDescriptor();
            apacheJarPaths = getLibrariesPaths(descriptor, "org.apache.jasper");
        }
        return concatenateStringArrays(jettyJarPaths, apacheJarPaths);
    }

    /**
     * @param theTests the types to get the arguments for
     * @return an array of the specific Cactus VM arguments
     */
    private String[] getCactusVMArgs(IType[] theTests)
    {
        Vector cactusVMArgs = new Vector();
        cactusVMArgs.add(
            "-Dcactus.contextURL=" + CactusPreferences.getContextURL());
        if (CactusPreferences.getJetty())
        {
            if (theTests.length > 0)
            {
                String jettyResourcePath =
                    CactusPreferences.getTempDir()
                        + File.separator
                        + JettyContainerManager.getJettyWebappPath();
                cactusVMArgs.add(
                    "-Dcactus.jetty.resourceDir=" + jettyResourcePath);
                IPath projectLocation =
                    theTests[0].getJavaProject().getProject().getLocation();
                File jettyXML =
                    projectLocation
                        .append(CactusPreferences.getJettyXML())
                        .toFile();
                if (jettyXML.exists())
                {
                    cactusVMArgs.add(
                        "-Dcactus.jetty.config=" + jettyXML.getAbsolutePath());
                }
            }
            cactusVMArgs.add(
                "-Dcactus.initializer="
                    + "org.apache.cactus.extension.jetty.JettyInitializer");
        }
        return (String[]) cactusVMArgs.toArray(new String[cactusVMArgs.size()]);
    }

    /**
     * Concatenate two string arrays.
     * 
     * @param theArray1 the first array
     * @param theArray2 the second array
     * @return a string array containing the first array followed by the second
     *         one
     */
    private String[] concatenateStringArrays(
        String[] theArray1,
        String[] theArray2)
    {
        String[] newArray = new String[theArray1.length + theArray2.length];
        System.arraycopy(theArray1, 0, newArray, 0, theArray1.length);
        System.arraycopy(
            theArray2,
            0,
            newArray,
            theArray1.length,
            theArray2.length);
        return newArray;
    }

    /**
     * @param theDirectory the directory to list jars from
     * @return the array of jar paths in the given directory
     */
    private String[] getJarPaths(File theDirectory)
    {
        File[] jars = theDirectory.listFiles(new JarFilenameFilter());
        String[] jarPaths = new String[jars.length];
        for (int i = 0; i < jarPaths.length; i++)
        {
            jarPaths[i] = jars[i].getAbsolutePath();
        }
        return jarPaths;
    }

    /**
     * @param theAntURLs array of URLs to convert to Jar paths
     * @return the array of jar paths from the given URLs
     */
    private String[] getJarPaths(URL[] theAntURLs)
    {
        String[] jarPaths = new String[theAntURLs.length];
        for (int i = 0; i < theAntURLs.length; i++)
        {
            jarPaths[i] = theAntURLs[i].getFile();
        }
        return jarPaths;
    }

    /**
     * @param theDescriptor the plug-in descriptor to get libraries from
     * @param thePackagePrefix package prefix used to filter libraries 
     * @return an array of jar paths exposed by the plug-in
     */
    private String[] getLibrariesPaths(
        IPluginDescriptor theDescriptor,
        String thePackagePrefix)
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
                    result.add(Platform.asLocalURL(url).getFile());
                }
                catch (IOException e)
                {
                    // if the URL is not valid we don't add it
                    CactusPlugin.log(e);
                    continue;
                }
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    /**
     * @param thePackagePrefix prefix which presence is to be tested
     * @param theCurrentLib the library in which the prefix will be searched 
     * @return true if the library declares the given package prefix
     */
    private boolean isContained(
        String thePackagePrefix,
        ILibrary theCurrentLib)
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

    /**
     * @see ITestRunListener#testRunStarted(int)
     */
    public void testRunStarted(int theTestCount)
    {
    }

    /**
     * Test run has ended so we tear down the container setup.
     * @param theElapsedTime not used here
     */
    public void testRunEnded(long theElapsedTime)
    {
        // If we already finished the launch (i.e. we already went here)
        // we do nothing. 
        if (this.launchEnded)
        {
            return;
        }
        CactusPlugin.log("Test run ended");
        try
        {
            CactusPlugin.getContainerManager(false).tearDown();
        }
        catch (CoreException e)
        {
            CactusPlugin.displayErrorMessage(
                CactusMessages.getString(
                    "CactusLaunch.message.containerManager.error"),
                e.getMessage(),
                null);
            return;
        }
        this.launchEnded = true;
    }

    /**
     * If test run has stopped we have to do the same thing
     * as if the test run had ended normally.
     * @param theElapsedTime not used here
     */
    public void testRunStopped(long theElapsedTime)
    {
        testRunEnded(0);
    }

    /**
     * @see ITestRunListener#testStarted(String, String)
     */
    public void testStarted(String theTestId, String theTestName)
    {
    }

    /**
     * @see ITestRunListener#testEnded(String, String)
     */
    public void testEnded(String theTestId, String theTestName)
    {
    }

    /**
     * @see ITestRunListener#testFailed (int, String, String, String)
     */
    public void testFailed(
        int theStatus,
        String theTestId,
        String theTestName,
        String theTrace)
    {
    }

    /**
     * @see ITestRunListener#testTreeEntry(String)
     */
    public void testTreeEntry(String theEntry)
    {
    }

    /**
     * If test run has been terminated we have to do the same thing
     * as if the test run had ended normally.
     */
    public void testRunTerminated()
    {
        testRunEnded(0);
    }

    /**
     * @see ITestRunListener#testReran(String, String, String, int, String)
     */
    public void testReran(
        String theTestId,
        String theTestClass,
        String theTestName,
        int theStatus,
        String theTrace)
    {
    }

}
