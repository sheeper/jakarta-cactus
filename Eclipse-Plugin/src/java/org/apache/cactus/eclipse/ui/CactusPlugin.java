/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
package org.apache.cactus.eclipse.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractSet;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.junit.launcher.JUnitBaseLaunchConfiguration;
import org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author Julien Ruaux
 *
 * The plug-in runtime class for the Cactus plug-in.
 * 
 * @version $Id: $
 */
public class CactusPlugin extends AbstractUIPlugin implements ILaunchListener
{
    /**
     * The single instance of this plug-in runtime class.
     */
    private static CactusPlugin fgPlugin = null;

    public static final String PLUGIN_ID = "org.apache.cactus.eclipse"; //$NON-NLS-1$

    public final static String TEST_SUPERCLASS_NAME = "junit.framework.TestCase"; //$NON-NLS-1$
    public final static String TEST_INTERFACE_NAME = "junit.framework.Test"; //$NON-NLS-1$

    private static URL fgIconBaseURL;

    /**
     * Use to track new launches. We need to do this
     * so that we only attach a TestRunner once to a launch.
     * Once a test runner is connected it is removed from the set.
     */
    private AbstractSet fTrackedLaunches = new HashSet(20);

    public CactusPlugin(IPluginDescriptor desc)
    {
        super(desc);
        fgPlugin = this;
        String pathSuffix = "icons/"; //$NON-NLS-1$
        try
        {
            fgIconBaseURL =
                new URL(getDescriptor().getInstallURL(), pathSuffix);
        } catch (MalformedURLException e)
        {
            // do nothing
        }
    }

    public static CactusPlugin getDefault()
    {
        return fgPlugin;
    }

    public static Shell getActiveWorkbenchShell()
    {
        IWorkbenchWindow workBenchWindow = getActiveWorkbenchWindow();
        if (workBenchWindow == null)
            return null;
        return workBenchWindow.getShell();
    }

    /**
     * Returns the active workbench window
     * 
     * @return the active workbench window
     */
    public static IWorkbenchWindow getActiveWorkbenchWindow()
    {
        if (fgPlugin == null)
            return null;
        IWorkbench workBench = fgPlugin.getWorkbench();
        if (workBench == null)
            return null;
        return workBench.getActiveWorkbenchWindow();
    }

    public IWorkbenchPage getActivePage()
    {
        return getActiveWorkbenchWindow().getActivePage();
    }

    public static String getPluginId()
    {
        return getDefault().getDescriptor().getUniqueIdentifier();
    }

    /*
     * @see AbstractUIPlugin#initializeDefaultPreferences
     */
    protected void initializeDefaultPreferences(IPreferenceStore store)
    {
        super.initializeDefaultPreferences(store);
        //		JUnitPreferencePage.initializeDefaults(store);
    }

    public static void log(Throwable e)
    {
        log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, "Error", e)); //$NON-NLS-1$
    }

    public static void log(IStatus status)
    {
        getDefault().getLog().log(status);
    }

//    public static URL makeIconFileURL(String name) throws MalformedURLException
//    {
//        if (JUnitPlugin.fgIconBaseURL == null)
//            throw new MalformedURLException();
//        return new URL(JUnitPlugin.fgIconBaseURL, name);
//    }
//    static ImageDescriptor getImageDescriptor(String relativePath)
//    {
//        try
//        {
//            return ImageDescriptor.createFromURL(makeIconFileURL(relativePath));
//        } catch (MalformedURLException e)
//        {
//            // should not happen
//            return ImageDescriptor.getMissingImageDescriptor();
//        }
//    }

    /*
     * @see ILaunchListener#launchRemoved(ILaunch)
     */
    public void launchRemoved(ILaunch launch)
    {
        fTrackedLaunches.remove(launch);
    }

    /*
     * @see ILaunchListener#launchAdded(ILaunch)
     */
    public void launchAdded(ILaunch launch)
    {
        fTrackedLaunches.add(launch);
    }

    public void connectTestRunner(ILaunch launch, IType launchedType, int port)
    {
        IWorkbench workbench = getWorkbench();
        if (workbench == null)
            return;

        IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        TestRunnerViewPart testRunner = null;
        if (page != null)
        {
            try
            { // show the result view if it isn't shown yet
                testRunner =
                    (TestRunnerViewPart) page.findView(TestRunnerViewPart.NAME);
                if (testRunner == null)
                {
                    IWorkbenchPart activePart = page.getActivePart();
                    testRunner =
                        (TestRunnerViewPart) page.showView(
                            TestRunnerViewPart.NAME);
                    //restore focus stolen by the creation of the result view
                    page.activate(activePart);
                }
            } catch (PartInitException pie)
            {
                log(pie);
            }
        }
        if (testRunner != null)
            testRunner.startTestRunListening(launchedType, port, launch);
    }

    /*
     * @see ILaunchListener#launchChanged(ILaunch)
     */
    public void launchChanged(final ILaunch launch)
    {
        if (!fTrackedLaunches.contains(launch))
            return;

        ILaunchConfiguration config = launch.getLaunchConfiguration();
        IType launchedType = null;
        int port = -1;
        if (config != null)
        {
            // test whether the launch defines the JUnit attributes
            String portStr =
                launch.getAttribute(JUnitBaseLaunchConfiguration.PORT_ATTR);
            String typeStr =
                launch.getAttribute(JUnitBaseLaunchConfiguration.TESTTYPE_ATTR);
            if (portStr != null && typeStr != null)
            {
                port = Integer.parseInt(portStr);
                IJavaElement element = JavaCore.create(typeStr);
                if (element instanceof IType)
                    launchedType = (IType) element;
            }
        }
        if (launchedType != null)
        {
            fTrackedLaunches.remove(launch);
            final int finalPort = port;
            final IType finalType = launchedType;
            getDisplay().asyncExec(new Runnable()
            {
                public void run()
                {
                    connectTestRunner(launch, finalType, finalPort);
                }
            });
        }
    }

    /*
     * @see Plugin#startup()
     */
    public void startup() throws CoreException
    {
        super.startup();
        ILaunchManager launchManager =
            DebugPlugin.getDefault().getLaunchManager();
        launchManager.addLaunchListener(this);
    }

    /*
     * @see Plugin#shutdown()
     */
    public void shutdown() throws CoreException
    {
        super.shutdown();
        ILaunchManager launchManager =
            DebugPlugin.getDefault().getLaunchManager();
        launchManager.removeLaunchListener(this);
    }

    public static Display getDisplay()
    {
        Shell shell = getActiveWorkbenchShell();
        if (shell != null)
        {
            return shell.getDisplay();
        }
        Display display = Display.getCurrent();
        if (display == null)
        {
            display = Display.getDefault();
        }
        return display;
    }

}
