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

import org.apache.cactus.eclipse.containers.IContainerProvider;
import org.apache.cactus.eclipse.containers.ant.GenericAntProvider;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The plug-in runtime class for the Cactus plug-in.
 * 
 * @version $Id:$
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
        theStore.setDefault(CactusPreferences.CONTEXT_URL_SCHEME, "http");
        theStore.setDefault(CactusPreferences.CONTEXT_URL_HOST, "localhost");
        theStore.setDefault(CactusPreferences.CONTEXT_URL_PORT, 8080);
        theStore.setDefault(CactusPreferences.CONTEXT_URL_PATH, "test");
        theStore.setDefault(
            CactusPreferences.JARS_DIR,
            "D:/dev/cactus/jakarta-cactus-13-1.4b1/lib");
        theStore.setDefault(CactusPreferences.TEMP_DIR, "C:/temp");
        theStore.setDefault(CactusPreferences.TOMCAT40_DIR, "");
        theStore.setDefault(CactusPreferences.RESIN20_DIR, "");
    }

    /**
     * Returns a container provider.
     * @return IContainerProvider a container provider to use for Cactus tests.
     */
    public static IContainerProvider getContainerProvider()
    {
        return new GenericAntProvider(
            CactusPreferences.getContextURLPort(),
            CactusPreferences.getTempDir(),
            CactusPreferences.getContainerHomes());
    }
}
