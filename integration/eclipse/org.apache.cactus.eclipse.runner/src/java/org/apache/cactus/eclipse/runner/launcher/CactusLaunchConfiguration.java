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

import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.apache.cactus.eclipse.runner.common.LibraryHelper;
import org.apache.cactus.eclipse.runner.ui.CactusMessages;
import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.apache.cactus.eclipse.runner.ui.CactusPreferences;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfiguration;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.junit.ITestRunListener;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
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
     * Separator between VM arguments
     */
    protected static final String VM_ARG_SEPARATOR = " ";

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
        final IJavaProject javaProject = getJavaProject(theConfiguration);
        Vector userClasspath =
            toMemento(
                JavaRuntime.computeUnresolvedRuntimeClasspath(javaProject));
        Vector cactusClasspath = toMemento(getCactusClasspath());
        List classpath =
            theConfiguration.getAttribute(
                IJavaLaunchConfigurationConstants.ATTR_CLASSPATH,
                (List) null);
        if (classpath == null)
        {
            classpath = userClasspath;
            classpath.addAll(0, cactusClasspath);
        }
        else
        {
            classpath.addAll(0, userClasspath);
            classpath.addAll(0, cactusClasspath);
        }
        final ILaunchConfigurationWorkingCopy cactusConfig =
            theConfiguration.getWorkingCopy();
        cactusConfig.setAttribute(
            IJavaLaunchConfigurationConstants.ATTR_CLASSPATH,
            classpath);
        cactusConfig.setAttribute(
            IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
            false);
        String jUnitArgs = getVMArguments(theConfiguration);
        String cactusVMArgs = getCactusVMArgs(javaProject);
        String globalArgs = jUnitArgs + VM_ARG_SEPARATOR + cactusVMArgs;
        cactusConfig.setAttribute(
            IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
            globalArgs);
        this.launchEnded = false;
        // Register the instance of CactusLaunchShortcut to the JUnitPlugin
        // for TestRunEnd notification.
        JUnitPlugin.getDefault().addTestRunListener(this);
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
                                    cactusConfig,
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
     * @param theEntries the array of IClasspathEntry to build mementos from 
     * @return a Vector of mementos from the given array
     */
    private Vector toMemento(IClasspathEntry[] theEntries)
    {
        Vector result = new Vector();
        for (int i = 0; i < theEntries.length; i++)
        {
            try
            {
                result.add(
                    JavaRuntime
                        .newArchiveRuntimeClasspathEntry(
                            theEntries[i].getPath())
                        .getMemento());
            }
            catch (CoreException e)
            {
                // Do nothing
            }
        }
        return result;
    }

    /**
     * @param theEntries the array of IRuntimeClasspathEntry to build
     * mementos from 
     * @return a Vector of mementos from the given array
     */
    private Vector toMemento(IRuntimeClasspathEntry[] theEntries)
    {
        Vector result = new Vector();
        for (int i = 0; i < theEntries.length; i++)
        {
            try
            {
                result.add(theEntries[i].getMemento());
            }
            catch (CoreException e)
            {
                // Do nothing
            }
        }
        return result;
    }

    /**
     * @return an array of classpaths needed for Cactus
     * @throws CoreException when an error occurs while
     * trying to build the classpath
     */
    protected IClasspathEntry[] getCactusClasspath() throws CoreException
    {
        IClasspathEntry[] cactusClasspath =
            LibraryHelper.getClientSideEntries();
        URL[] antURLs = AntCorePlugin.getPlugin().getPreferences().getAntURLs();
        IClasspathEntry[] apacheClasspath = getClasspathEntryArray(antURLs);
        cactusClasspath =
            LibraryHelper.concatenateEntries(cactusClasspath, apacheClasspath);
        return cactusClasspath;
    }

    /**
     * @param theJavaProject the Java project to get the arguments for
     * @return an array of the specific Cactus VM arguments
     */
    protected String getCactusVMArgs(IJavaProject theJavaProject)
    {
        String cactusVMArgs = "";
        cactusVMArgs += "-Dcactus.contextURL="
            + CactusPreferences.getContextURL()
            + VM_ARG_SEPARATOR;
        return cactusVMArgs;
    }

    /**
     * @param theAntURLs array of URLs to convert to Jar paths
     * @return the array of jar paths from the given URLs
     */
    private IClasspathEntry[] getClasspathEntryArray(URL[] theAntURLs)
    {
        IClasspathEntry[] result = new IClasspathEntry[theAntURLs.length];
        for (int i = 0; i < theAntURLs.length; i++)
        {
            result[i] =
                LibraryHelper.getIClasspathEntry(theAntURLs[i].getPath());
        }
        return result;
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
