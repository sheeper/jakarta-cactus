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

import org.apache.cactus.eclipse.containers.IContainerProvider;
import org.apache.cactus.eclipse.ui.CactusMessages;
import org.apache.cactus.eclipse.ui.CactusPlugin;
import org.apache.cactus.eclipse.ui.CactusPreferences;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jdt.internal.junit.runner.ITestRunListener;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.internal.junit.util.TestSearchEngine;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

/**
 * Launch shortcut used to start the Cactus launch configuration on the
 * current workbench selection.
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @version $Id: $
 */
public class CactusLaunchShortcut
    extends JUnitLaunchShortcut
    implements ITestRunListener
{

    /**
     * Reference to the War file so that we can delete it on tearDown()
     */
    private File war;
    /**
     * The provider to use for container setup.
     */
    private IContainerProvider provider;

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
                "Error when launching tests",
                e.getMessage(),
                null);
            return;
        }
        catch (InvocationTargetException e)
        {
            CactusPlugin.displayErrorMessage(
                "Error when launching tests",
                e.getMessage(),
                null);
            return;
        }
        IType type = null;
        if (types.length == 0)
        {
            MessageDialog.openInformation(
                getShell(),
                CactusMessages.getString("LaunchTestAction.dialog.title"),
                CactusMessages.getString("LaunchTestAction.message.notests"));
        }
        else if (types.length > 1)
        {
            type = chooseType(types, theMode);
        }
        else
        {
            type = types[0];
        }
        if (type != null)
        {
			prepareCactusTests(type);
			JUnitPlugin.getDefault().addTestRunListener(this);
            super.launchType(theSearch, theMode);
        }
    }

    /**

     * @param theType test or test suite to launch
     * @param theMode mode for launch configuration
     */

    /**
     * creates the war file, deploys and launches the container.
     * @param theType the Java file     
     */
    private void prepareCactusTests(IType theType)
    {

        provider = CactusPlugin.getContainerProvider();
        try
        {
            WarBuilder newWar =
                new WarBuilder(
                    theType.getJavaProject(),
                    new File(CactusPreferences.getJarsDir()));
            war = newWar.createWar();
            provider.deploy(
                CactusPreferences.getContextURLPath(),
                war.toURL(),
                null);
            provider.start(null);
        }
        catch (CoreException e)
        {
            CactusPlugin.displayErrorMessage(
                "Error when preparing tests",
                e.getMessage(),
                e.getStatus());
        }
        catch (MalformedURLException e)
        {
            CactusPlugin.displayErrorMessage(
                "Error when preparing tests",
                e.getMessage(),
                null);
        }

    }

    /**
     * Stops the container and undeploys (cleans) it.
     */
    private void teardownCactusTests()
    {
        try
        {
            provider.stop(null);
            provider.undeploy(null, null);
            war.delete();
        }
        catch (CoreException e)
        {
            CactusPlugin.displayErrorMessage(
                "Error when tearing down tests",
                e.getMessage(),
                e.getStatus());
        }
    }
    /**
     * @see org.eclipse.jdt.internal.junit.runner.ITestRunListener#testRunStarted(int)
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
        teardownCactusTests();
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
     * @see org.eclipse.jdt.internal.junit.runner.ITestRunListener#testStarted(java.lang.String)
     */
    public void testStarted(String theTestName)
    {
    }

    /**
     * @see org.eclipse.jdt.internal.junit.runner.ITestRunListener#testEnded(java.lang.String)
     */
    public void testEnded(String theTestName)
    {
    }

    /**
     * @see org.eclipse.jdt.internal.junit.runner.ITestRunListener#testFailed(int, java.lang.String, java.lang.String)
     */
    public void testFailed(int theStatus, String theTestName, String theTrace)
    {
    }

    /**
     * @see org.eclipse.jdt.internal.junit.runner.ITestRunListener#testTreeEntry(java.lang.String)
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
     * @see org.eclipse.jdt.internal.junit.runner.ITestRunListener#testReran(java.lang.String, java.lang.String, int, java.lang.String)
     */
    public void testReran(
        String theTestClass,
        String theTestName,
        int theStatus,
        String theTrace)
    {
    }

}
