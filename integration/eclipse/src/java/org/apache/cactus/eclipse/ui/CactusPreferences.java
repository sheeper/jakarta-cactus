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

import java.util.Vector;

import org.apache.cactus.eclipse.containers.ant.ContainerHome;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Central class for managing the Cactus preferences.
 * 
 * @version $Id$
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 */
public class CactusPreferences
{
    /**
     * The protocol scheme component of the context URL (either 'http' or 
     * 'https') preference.
     */
    public static final String CONTEXT_URL_SCHEME = "contextURL_Scheme";

    /**
     * The host component of the context URL preference. 
     */
    public static final String CONTEXT_URL_HOST = "contextURL_Host";

    /**
     * The port component of the context URL preference.
     */
    public static final String CONTEXT_URL_PORT = "contextURL_Port";

    /**
     * The path component of the context URL preference.
     */
    public static final String CONTEXT_URL_PATH = "contextURL_Path";

    /**
     * The directory of the jars needed by Cactus.
     */
    public static final String JARS_DIR = "jars_Dir";

    /**
     * The temp directory used by the plugin to set up containers.
     */
    public static final String TEMP_DIR = "temp_Dir";

    /**
     * The directory where the plugin can find the web application.
     */
    public static final String WEBAPP_DIR = "webapp_Dir";

    /**
     * Returns the context URL that should be used by the client, as 
     * configured in the plug-in preferences.
     * 
     * @return the context URL
     */
    public static String getContextURL()
    {
        IPreferenceStore store = CactusPlugin.getDefault().getPreferenceStore();
        StringBuffer buf =
            new StringBuffer()
                .append(store.getString(CONTEXT_URL_SCHEME))
                .append("://")
                .append(store.getString(CONTEXT_URL_HOST))
                .append(":")
                .append(store.getInt(CONTEXT_URL_PORT))
                .append("/")
                .append(store.getString(CONTEXT_URL_PATH));
        String result = buf.toString();
        CactusPlugin.log("Cactus preference : ContextURL = [" + result + "]");
        return result;
    }

    /**
     * Returns the context URL port that should be used by the client, as 
     * configured in the plug-in preferences.
     * 
     * @return the context port
     */
    public static int getContextURLPort()
    {
        IPreferenceStore store = CactusPlugin.getDefault().getPreferenceStore();
        int result = store.getInt(CONTEXT_URL_PORT);
        CactusPlugin.log(
            "Cactus preference : ContextURLPort = [" + result + "]");
        return result;
    }

    /**
     * Returns the context URL path that should be used by the client, as 
     * configured in the plug-in preferences.
     * 
     * @return the context path
     */
    public static String getContextURLPath()
    {
        IPreferenceStore store = CactusPlugin.getDefault().getPreferenceStore();
        String result = store.getString(CONTEXT_URL_PATH);
        CactusPlugin.log(
            "Cactus preference : ContextURLPath = [" + result + "]");
        return result;
    }

    /**
     * Returns the directory containing the jars needed by cactus, as 
     * configured in the plug-in preferences.
     * 
     * @return the context path
     */
    public static String getJarsDir()
    {
        IPreferenceStore store = CactusPlugin.getDefault().getPreferenceStore();
        String result = store.getString(JARS_DIR);
        CactusPlugin.log("Cactus preference : JarsDir = [" + result + "]");
        return result;
    }

    /**
     * Returns the temp directory used by cactus, as 
     * configured in the plug-in preferences.
     * 
     * @return the context path
     */
    public static String getTempDir()
    {
        IPreferenceStore store = CactusPlugin.getDefault().getPreferenceStore();
        String result = store.getString(TEMP_DIR);
        CactusPlugin.log("Cactus preference : TempDir = [" + result + "]");
        return result;
    }

    /**
     * Returns the Webapp directory used by cactus, as 
     * configured in the plug-in preferences.
     * 
     * @return the context path
     */
    public static String getWebappDir()
    {
        IPreferenceStore store = CactusPlugin.getDefault().getPreferenceStore();
        String result = store.getString(WEBAPP_DIR);
        CactusPlugin.log("Cactus preference : WebappDir = [" + result + "]");
        return result;
    }

    /**
     * Returns the home directory for the given container id.
     * @param theContainerId container id
     * @return String
     */
    public static String getContainerDir(String theContainerId)
    {
        IPreferenceStore store = CactusPlugin.getDefault().getPreferenceStore();
        String result = store.getString(theContainerId);
        CactusPlugin.log(
            "Cactus preference : " + theContainerId + "= [" + result + "]");
        return result;
    }

    /**
     * Returns the ContainerHome array.
     * @return String
     */
    public static ContainerHome[] getContainerHomes()
    {
        Vector containerHomes = new Vector();
            String[] containerIds =
                CactusPlugin.getContainers();

            for (int i = 0; i < containerIds.length; i++)
            {
                String currentId = containerIds[i];
                String currentContainerDir = getContainerDir(currentId);
                if (!currentContainerDir.equals(""))
                {
                    containerHomes.addElement(
                        new ContainerHome(currentId, currentContainerDir));
                }

            }

        ContainerHome[] result =
            (ContainerHome[]) containerHomes.toArray(new ContainerHome[0]);
        CactusPlugin.log(
            "Cactus preference : ContainerHomes = [" + result + "]");
        return result;
    }
}
