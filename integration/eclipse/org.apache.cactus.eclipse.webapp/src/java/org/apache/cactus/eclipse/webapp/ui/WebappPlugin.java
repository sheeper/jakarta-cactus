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
package org.apache.cactus.eclipse.webapp.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @version $Id$
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
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
    public WebappPlugin(IPluginDescriptor theDescriptor)
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
    public static String getResourceString(String theKey)
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
    public ResourceBundle getResourceBundle()
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
}
