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
package org.apache.cactus.eclipse.ui;

import org.apache.cactus.eclipse.containers.IContainerProvider;
import org.apache.cactus.eclipse.containers.ant.GenericAntProvider;
import org.apache.cactus.eclipse.launcher.CactusLaunchShortcut;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The plug-in runtime class for the Cactus plug-in.
 * 
 * @version $Id$
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 */
public class CactusPlugin extends AbstractUIPlugin
{
    /**
     * The single instance of this plug-in runtime class.
     */
    private static CactusPlugin plugin;
    /**
     * The current instance of CactusLaunchShortcut.
     */
    private CactusLaunchShortcut launchShortcut;

    /**
     * @see org.eclipse.core.runtime.Plugin#Plugin(IPluginDescriptor)
     */
    public CactusPlugin(IPluginDescriptor theDescription)
    {
        super(theDescription);
        plugin = this;
    }

    /**
     * @return the single instance of this plug-in runtime class
     */
    public static CactusPlugin getDefault()
    {
        return plugin;
    }

    /**
     * @return the active workbench shell
     */
    public static Shell getActiveWorkbenchShell()
    {
        IWorkbenchWindow workBenchWindow = getActiveWorkbenchWindow();
        if (workBenchWindow == null)
        {
            return null;
        }
        return workBenchWindow.getShell();
    }

    /**
     * @return the active workbench window
     */
    public static IWorkbenchWindow getActiveWorkbenchWindow()
    {
        if (plugin == null)
        {
            return null;
        }
        IWorkbench workBench = plugin.getWorkbench();
        if (workBench == null)
        {
            return null;
        }
        return workBench.getActiveWorkbenchWindow();
    }

    /**
     * @return the plugin identifier
     */
    public static String getPluginId()
    {
        return getDefault().getDescriptor().getUniqueIdentifier();
    }

    /**
     * @param theMessage the message to log
     */
    public static void log(String theMessage)
    {
        log(
            new Status(
                IStatus.INFO,
                getPluginId(),
                IStatus.OK,
                theMessage,
                null));
    }

    /**
     * @param theThrowable throwable to log
     */
    public static void log(Throwable theThrowable)
    {
        log(
            new Status(
                IStatus.ERROR,
                getPluginId(),
                IStatus.ERROR,
                "Error",
                theThrowable));
    }

    /**
     * @param theStatus status to log
     */
    public static void log(IStatus theStatus)
    {
        getDefault().getLog().log(theStatus);
    }

    /**
     * @return the current display
     */
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
    /**
     * Initializes all preferences to their default values.
     * 
     * @param theStore the preference store
     */
    protected void initializeDefaultPreferences(IPreferenceStore theStore)
    {
        theStore.setDefault(
            CactusPreferences.CONTEXT_URL_SCHEME,
            CactusMessages.getString(
                "CactusPreferencePage.label.protocol.init"));
        theStore.setDefault(
            CactusPreferences.CONTEXT_URL_HOST,
            CactusMessages.getString("CactusPreferencePage.label.host.init"));
        theStore.setDefault(
            CactusPreferences.CONTEXT_URL_PORT,
            CactusMessages.getString("CactusPreferencePage.label.port.init"));
        theStore.setDefault(
            CactusPreferences.CONTEXT_URL_PATH,
            CactusMessages.getString(
                "CactusPreferencePage.label.context.init"));
        theStore.setDefault(
            CactusPreferences.JARS_DIR,
            CactusMessages.getString("CactusPreferencePage.label.jars.init"));
        theStore.setDefault(
            CactusPreferences.TEMP_DIR,
            System.getProperty("java.io.tmpdir"));
    }

    /**
     * Returns a container provider.
     * @return IContainerProvider a container provider to use for Cactus tests.
     * @throws CoreException if the container provider can't be contructed
     */
    public static IContainerProvider getContainerProvider()
        throws CoreException
    {
        return new GenericAntProvider(
            CactusPreferences.getContextURLPort(),
            CactusPreferences.getTempDir(),
            CactusPreferences.getContainerHomes());
    }

    /**
     * Displays an error dialog.
     * @param theTitle title of the dialog
     * @param theMessage message to display in the dialog
     * @param theStatus status of the error
     */
    public static void displayErrorMessage(
        final String theTitle,
        final String theMessage,
        IStatus theStatus)
    {
        if (theStatus == null)
        {
            log(
                new Status(
                    IStatus.ERROR,
                    getPluginId(),
                    IStatus.OK,
                    theMessage,
                    null));
        }
        else
        {
            log(theStatus);
        }
        Display.getDefault().asyncExec(new Runnable()
        {
            public void run()
            {
                MessageDialog.openError(
                    getActiveWorkbenchShell(),
                    theTitle,
                    theMessage);
            }
        });
    }
    /**
     * Helper method for other classes. Returns a CoreException with a message
     * corresponding to the given message key.
     * @param theMessageKey the key of the message to be thrown
     * @param theException a low-level exception, or null if not applicable 
     * @return the constructed CoreException
     */
    public static CoreException createCoreException(
        String theMessageKey,
        Throwable theException)
    {
        return createCoreException(theMessageKey, "", theException);
    }

    /**
     * Helper method for other classes. Returns a CoreException with a message
     * corresponding to the given message key and the additional String.
     * @param theMessageKey the key of the message to be thrown
     * @param theString String to be concatenated with the message
     * @param theException a low-level exception, or null if not applicable 
     * @return the constructed CoreException
     */
    public static CoreException createCoreException(
        String theMessageKey,
        String theString,
        Throwable theException)
    {
        String message = CactusMessages.getString(theMessageKey);
        message += theString;
        return new CoreException(
            new Status(
                IStatus.ERROR,
                CactusPlugin.getPluginId(),
                IStatus.OK,
                message,
                theException));
    }

    /**
     * @return the current CactusLaunchShortcut instance.
     */
    public CactusLaunchShortcut getCactusLaunchShortcut()
    {
        return launchShortcut;
    }

    /**
     * Sets the current CactusLaunchShortcut
     * @param theCactusLaunchShortcut the instance to set
     */
    public void addCactusLaunchShortcut(
        CactusLaunchShortcut theCactusLaunchShortcut)
    {
        this.launchShortcut = theCactusLaunchShortcut;
    }
}