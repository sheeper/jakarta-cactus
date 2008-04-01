/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cactus.eclipse.runner.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.cactus.eclipse.runner.containers.IContainerManager;
import org.apache.cactus.eclipse.runner.containers.ant.AntContainerManager;
import org.apache.cactus.eclipse.runner.containers.jetty.JettyContainerManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The plug-in runtime class for the Cactus plug-in.
 * 
 * @version $Id: CactusPlugin.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class CactusPlugin extends AbstractUIPlugin
{
    /**
     * The single instance of this plug-in runtime class.
     */
    private static CactusPlugin plugin;

    /**
     * Plug-in relative path to the Ant build file.
     */
    private static final String BUILD_FILE_PATH = "./script/build.xml";

    /**
     * Plug-in relative path to the Ant container build files.
     */
    private static final String CONTAINER_BUILD_FILES_PATH = "./script";

    /**
     * Prefix of container build files.
     */
    private static final String CONTAINER_BUILD_FILES_PREFIX = "build-tests-";
    
    /* Some constants */
    /**
     * The location where Eclipse was installed;
     */
    public static final String CACTUS_VARIABLE_ECLIPSE_HOME = "ECLIPSE_HOME";
    
    /**
     * Path to the plugin's library directory
     */
    public static final String CACTUS_LIBRARY_PATH = "lib/";

    /**
     * Name of the common libraries folder
     */
    public static final String CACTUS_COMMON_LIBRARY_PATH = "common";

    /**
     * Name of the client libraries folder
     */
    public static final String CACTUS_CLIENT_LIBRARY_PATH = "client";

	public static final String CACTUS_RUN_IMAGE = "cactus.run.image";
    
    

    /**
     * Manager for the container provider.
     */
    private static IContainerManager containerManager = null;

    /**
     * @see org.eclipse.core.runtime.Plugin#Plugin(IPluginDescriptor)
     */
    public CactusPlugin(IPluginDescriptor theDescription)
    {
        super(theDescription);
        plugin = this;
    }
    /*
    public CactusPlugin() {
    	super();
    } */

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
            CactusMessages.getString("CactusPreferencePage.protocol.init"));
        theStore.setDefault(
            CactusPreferences.CONTEXT_URL_HOST,
            CactusMessages.getString("CactusPreferencePage.host.init"));
        theStore.setDefault(
            CactusPreferences.CONTEXT_URL_PORT,
            CactusMessages.getString("CactusPreferencePage.port.init"));
        theStore.setDefault(
            CactusPreferences.CONTEXT_URL_PATH,
            CactusMessages.getString("CactusPreferencePage.context.init"));
        theStore.setDefault(
            CactusPreferences.TEMP_DIR,
            System.getProperty("java.io.tmpdir"));
        theStore.setDefault(CactusPreferences.JETTY, true);
        theStore.setDefault(
            CactusPreferences.JETTY_XML,
            CactusMessages.getString("ContainersPreferencePage.jettyxml.init"));
    }

    /**
     * Returns a container manager.
     * @param theInitializeFlag true if the container manager should be
     * initialized
     * @return a container provider to use for Cactus tests
     *  or null if Jetty is selected as the container.
     * @throws CoreException if the container manager can't be contructed
     */
    public static IContainerManager getContainerManager(
        boolean theInitializeFlag)
        throws CoreException
    {
        if (containerManager != null && !theInitializeFlag)
        {
            return containerManager;
        }
        if (CactusPreferences.getJetty())
        {
            containerManager = new JettyContainerManager();
            return containerManager;
        }
        containerManager =
            new AntContainerManager(
                BUILD_FILE_PATH,
                CactusPreferences.getContextURLPort(),
                CactusPreferences.getTempDir(),
                CactusPreferences.getContainerHomes(),
                CactusPreferences.getContextURLPath());
        return containerManager;
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
                    (theTitle == null) ? "" : theTitle,
                    (theMessage == null) ? "" : theMessage);
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
     * Filter for container script files.
     * i.e. accepts files like 'build-tests-mycontainer3.1.xml'
     * 
     * @version $Id: CactusPlugin.java 238816 2004-02-29 16:36:46Z vmassol $
     */
    private static class BuildFilenameFilter implements FilenameFilter
    {
        /**
         * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        public boolean accept(File theDir, String theFilename)
        {
            return theFilename.startsWith(CONTAINER_BUILD_FILES_PREFIX);
        }
    }
    /**
     * @see IContainerManager#getContainerIds()
     */
    public static String[] getContainerIds()
    {
        Vector containers = new Vector();
        URL containerDirURL =
            CactusPlugin.getDefault().find(
                new Path(CONTAINER_BUILD_FILES_PATH));
        if (containerDirURL == null)
        {
            // No container available
            return new String[0];
        }
        Path containerDir = new Path(containerDirURL.getPath());
        File dir = containerDir.toFile();
        String[] containerFiles = dir.list(new BuildFilenameFilter());
        for (int i = 0; i < containerFiles.length; i++)
        {
            String currentFileName = containerFiles[i];
            if (currentFileName.startsWith(CONTAINER_BUILD_FILES_PREFIX))
            {
                String currentId =
                    currentFileName.substring(
                        CONTAINER_BUILD_FILES_PREFIX.length(),
                        currentFileName.lastIndexOf("."));
                containers.add(currentId);
            }
        }
        return (String[]) containers.toArray(new String[containers.size()]);
    }
    
    
    protected ImageRegistry createImageRegistry() {
	    final ImageRegistry registry = super.createImageRegistry();
	    
		URL url = null;
		try {
			url = new URL(CactusPlugin.getDefault().getDescriptor().getInstallURL(),"icons/calaunch.gif");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    //ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(CactusPlugin
	    //    .getDefault().getBundle().getResource("icons/calaunch.gif"));
	    registry.put(CACTUS_RUN_IMAGE, ImageDescriptor.createFromURL(url));
	    return registry;
	}

}
