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
