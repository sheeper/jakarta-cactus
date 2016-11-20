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
package org.apache.cactus.eclipse.webapp.internal.ui;

import org.apache.cactus.eclipse.webapp.internal.Webapp;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @version $Id: WebappPlugin.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class WebappPlugin extends AbstractUIPlugin
{
    /**
     * The shared instance.
     */
    private static WebappPlugin plugin;
    /**
     * Resource bundle.
     */
    private ResourceBundle resourceBundle;

    /**
     * The constructor.
     * @param theDescriptor the descriptor for this plugin
     */
    public WebappPlugin(final IPluginDescriptor theDescriptor)
    {
        super(theDescriptor);
        plugin = this;
        try
        {
            resourceBundle =
                ResourceBundle.getBundle("webapp.webappPluginResources");
        }
        catch (MissingResourceException x)
        {
            resourceBundle = null;
        }
    }

    /**
     * Returns the shared instance.
     * @return the instance of this plugin
     */
    public static WebappPlugin getDefault()
    {
        return plugin;
    }

    /**
     * Returns the workspace instance.
     * @return the instance of the current workspace
     */
    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     * @param theKey the key of the resource to return
     * @return the string
     */
    public static String getResourceString(final String theKey)
    {
        ResourceBundle bundle = WebappPlugin.getDefault().getResourceBundle();
        try
        {
            return bundle.getString(theKey);
        }
        catch (MissingResourceException e)
        {
            return theKey;
        }
    }

    /**
     * Returns the plugin's resource bundle
     * @return the resource bundle
     */
    public final ResourceBundle getResourceBundle()
    {
        return resourceBundle;
    }

    /**
     * @return the plugin identifier
     */
    public static String getPluginId()
    {
        return getDefault().getDescriptor().getUniqueIdentifier();
    }

    /**
     * @param theJavaProject the Java project to get the webapp from
     * @return the webapp associated to the given Java project
     */
    public static Webapp getWebapp(final IJavaProject theJavaProject)
    {
        return new Webapp(theJavaProject);
    }
}
