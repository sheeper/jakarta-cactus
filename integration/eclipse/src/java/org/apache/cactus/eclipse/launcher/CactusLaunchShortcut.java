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
package org.apache.cactus.eclipse.launcher;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.cactus.eclipse.containers.IContainerProvider;
import org.apache.cactus.eclipse.ui.CactusMessages;
import org.apache.cactus.eclipse.ui.CactusPlugin;
import org.apache.cactus.eclipse.ui.CactusPreferences;
import org.apache.cactus.eclipse.war.WarBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.internal.junit.util.TestSearchEngine;
import org.eclipse.jdt.junit.ITestRunListener;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

/**
 * Launch shortcut used to start the Cactus launch configuration on the
 * current workbench selection.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @version $Id$
 */
public class CactusLaunchShortcut
    extends JUnitLaunchShortcut
    implements ITestRunListener
{
    /**
     * Indicates whether we already went through the launch cycle.
     */
    private boolean launchEnded;
    /**
     * Reference to the War file so that we can delete it on tearDown()
     */
    private File war;
    /**
     * The provider to use for container setup.
     */
    private IContainerProvider provider;
    /**
     * The current search to launch on.
     */
    private Object[] search;
    /**
     * The current mode to launch with.
     */
    private String mode;
    /**
     * @return the Cactus launch configuration type. This method overrides
     *         the one in {@link JUnitLaunchShortcut} so that we can return
     *         a Cactus configuration type and not a JUnit one
     */
    protected ILaunchConfigurationType getJUnitLaunchConfigType()
    {
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        return lm.getLaunchConfigurationType(
            CactusLaunchConfiguration.ID_CACTUS_APPLICATION);
    }

    /**
     * Launches a Java type from the Object array.
     * If many test items exist in the array, we call chooseType
     * to select the type to launch. In all cases we prepare and launch
     * the type to test : we prepare for testing (war creation, container
     * setup), then we register the current CactusLaunchShortcut instance to
     * the JUnit plugin as a listener to test events. Finally we run the JUnit
     * method for tests launch.
     * @param theSearch array of possible types to launch
     * @param theMode mode for the configuration
     */
    protected void launchType(Object[] theSearch, String theMode)
    {
        IType[] types = null;
        try
        {
            types =
                TestSearchEngine.findTests(
                    new ProgressMonitorDialog(getShell()),
                    theSearch);
        }
        catch (InterruptedException e)
        {
            CactusPlugin.displayErrorMessage(
                CactusMessages.getString("CactusLaunch.message.error"),
                e.getMessage(),
                null);
            return;
        }
        catch (InvocationTargetException e)
        {
            CactusPlugin.displayErrorMessage(
                CactusMessages.getString("CactusLaunch.message.error"),
                e.getMessage(),
                null);
            return;
        }
        IType type = null;
        if (types.length == 0)
        {
            CactusPlugin.displayErrorMessage(
                CactusMessages.getString("CactusLaunch.dialog.title"),
                CactusMessages.getString("CactusLaunch.message.notests"),
                null);
        }
        else
            if (types.length > 1)
            {
                type = chooseType(types, theMode);
            }
            else
            {
                type = types[0];
            }
        if (type != null)
        {
            launchEnded = false;
            // Register the instance of CactusLaunchShortcut to the JUnitPlugin
            // for TestRunEnd notification.            
            JUnitPlugin.getDefault().addTestRunListener(this);
            this.search = theSearch;
            this.mode = theMode;
            CactusPlugin.getDefault().addCactusLaunchShortcut(this);
            final IJavaProject theJavaProject = type.getJavaProject();
            final IRunnableWithProgress runnable = new IRunnableWithProgress()
            {
                public void run(IProgressMonitor thePM)
                    throws InterruptedException
                {
                    try
                    {
                        CactusPlugin.log("Preparing cactus tests");
                        prepareCactusTests(theJavaProject, thePM);
                    }
                    catch (CoreException e)
                    {
                        throw new InterruptedException(e.getMessage());
                    }
                }
            };
            Display.getDefault().asyncExec(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        ProgressMonitorDialog dialog =
                            new ProgressMonitorDialog(getShell());
                        dialog.run(true, true, runnable);
                    }
                    catch (InvocationTargetException e)
                    {
                        CactusPlugin.displayErrorMessage(
                            CactusMessages.getString(
                                "CactusLaunch.message.prepare.error"),
                            e.getTargetException().getMessage(),
                            null);
                        cancelPreparation();
                        return;
                    }
                    catch (InterruptedException e)
                    {
                        CactusPlugin.displayErrorMessage(
                            CactusMessages.getString(
                                "CactusLaunch.message.prepare.error"),
                            e.getMessage(),
                            null);
                        cancelPreparation();
                        return;
                    }
                }
            });
        }
    }

    /**
     * Launches the Junit tests.
     */
    public void launchJunitTests()
    {
        CactusPlugin.log("Launching tests");
        super.launchType(search, mode);
    }
    /**
     * Returns the provider associated with this CactusLaunchShortcut instance.
     * @return the provider associated with this instance
     */
    public IContainerProvider getContainerProvider()
    {
        return provider;
    }
    /**
     * Launches a new progress dialog for preparation cancellation.
     */
    private void cancelPreparation()
    {
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
        IRunnableWithProgress tearDownRunnable = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor thePM) throws InterruptedException
            {
                try
                {
                    teardownCactusTests(thePM);
                }
                catch (CoreException e)
                {
                    throw new InterruptedException(e.getMessage());
                }
            }
        };
        try
        {
            dialog.run(true, true, tearDownRunnable);
        }
        catch (InvocationTargetException tearDownE)
        {
            CactusPlugin.displayErrorMessage(
                CactusMessages.getString("CactusLaunch.message.teardown.error"),
                tearDownE.getTargetException().getMessage(),
                null);
        }
        catch (InterruptedException tearDownE)
        {
            CactusPlugin.displayErrorMessage(
                CactusMessages.getString("CactusLaunch.message.teardown.error"),
                tearDownE.getMessage(),
                null);
        }
    }

    /**
     * creates the war file, deploys and launches the container.
     * @param theJavaProject the Java file
     * @param thePM the progress monitor to report to
     * @throws CoreException if anything goes wrong during preparation
     */
    private void prepareCactusTests(
        IJavaProject theJavaProject,
        IProgressMonitor thePM)
        throws CoreException
    {
        thePM.beginTask(
            CactusMessages.getString("CactusLaunch.message.prepare"),
            10);
        provider = CactusPlugin.getContainerProvider();
        try
        {
            WarBuilder newWar = new WarBuilder(theJavaProject);
            File tempDir = new File(CactusPreferences.getTempDir());
            war = newWar.createWar(thePM);
            URL warURL = war.toURL();
            String contextURLPath = CactusPreferences.getContextURLPath();
            if (contextURLPath.equals(""))
            {
                throw CactusPlugin.createCoreException(
                    "CactusLaunch.message.invalidproperty.contextpath",
                    null);
            }
            provider.deploy(contextURLPath, warURL, null, thePM);
            provider.start(null, thePM);
        }
        catch (MalformedURLException e)
        {
            CactusPlugin.log(e);
            throw CactusPlugin.createCoreException(
                "CactusLaunch.message.war.malformed",
                e);
        }
        thePM.done();
    }

    /**
     * Stops the container and undeploys (cleans) it.
     * @param thePM a progress monitor that reflects progress made while tearing
     * down the container setup
     * @throws CoreException if an error occurs when tearing down
     */
    private void teardownCactusTests(IProgressMonitor thePM)
        throws CoreException
    {
        thePM.beginTask(
            CactusMessages.getString("CactusLaunch.message.teardown"),
            100);
        if (provider != null)
        {
            provider.stop(null, thePM);
            provider.undeploy(null, null, thePM);
            war.delete();
        }
        thePM.done();
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
        if (launchEnded)
        {
            return;
        }
        CactusPlugin.log("Test run ended");
        final IRunnableWithProgress runnable = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor thePM) throws InterruptedException
            {
                CactusPlugin.log("Tearing down cactus tests");
                try
                {
                    teardownCactusTests(thePM);
                }
                catch (CoreException e)
                {
                    throw new InterruptedException(e.getMessage());
                }
            }
        };
        Display.getDefault().asyncExec(new Runnable()
        {
            public void run()
            {
                try
                {
                    ProgressMonitorDialog dialog =
                        new ProgressMonitorDialog(getShell());
                    dialog.run(true, true, runnable);
                }
                catch (InvocationTargetException e)
                {
                    CactusPlugin.displayErrorMessage(
                        CactusMessages.getString(
                            "CactusLaunch.message.teardown.error"),
                        e.getTargetException().getMessage(),
                        null);
                    cancelPreparation();
                    return;
                }
                catch (InterruptedException e)
                {
                    CactusPlugin.displayErrorMessage(
                        CactusMessages.getString(
                            "CactusLaunch.message.teardown.error"),
                        e.getMessage(),
                        null);
                    cancelPreparation();
                    return;
                }
            }

        });
        launchEnded = true;
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
     * @see ITestRunListener#testStarted(java.lang.String)
     */
    public void testStarted(String theTestId, String theTestName)
    {
    }

    /**
     * @see ITestRunListener#testEnded(java.lang.String)
     */
    public void testEnded(String theTestId, String theTestName)
    {
    }

    /**
     * @see ITestRunListener#testFailed (int, String, String)
     */
    public void testFailed(
        int theStatus,
        String theTestId,
        String theTestName,
        String theTrace)
    {
    }

    /**
     * @see ITestRunListener#testTreeEntry(java.lang.String)
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
     * @see ITestRunListener#testReran(String, String, int, String)
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
