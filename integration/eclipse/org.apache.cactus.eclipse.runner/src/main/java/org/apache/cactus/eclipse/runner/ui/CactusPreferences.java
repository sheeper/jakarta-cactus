/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.Hashtable;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Central class for managing the Cactus preferences.
 * 
 * @version $Id: CactusPreferences.java 238816 2004-02-29 16:36:46Z vmassol $
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
     * The temp directory used by the plugin to set up containers.
     */
    public static final String TEMP_DIR = "temp_Dir";

    /**
     * id for the Jetty container selection preference.
     */
    public static final String JETTY = "jetty";

    /**
     * id for the jetty.xml location preference.
     */
    public static final String JETTY_XML = "jetty_xml";

    /**
     * @return the context URL that should be used by the client, as 
     * configured in the plug-in preferences.
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
     * @return the context URL port that should be used by the client, as 
     * configured in the plug-in preferences.
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
     * @return the context URL path that should be used by the client, as 
     * configured in the plug-in preferences.
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
     * @return the temp directory used by cactus, as 
     * configured in the plug-in preferences.
     */
    public static String getTempDir()
    {
        IPreferenceStore store = CactusPlugin.getDefault().getPreferenceStore();
        String result = store.getString(TEMP_DIR);
        CactusPlugin.log("Cactus preference : TempDir = [" + result + "]");
        return result;
    }

    /**
     * @param theContainerId container id
     * @return the home directory for the given container id.
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
     * @return true if Jetty is selected
     */
    public static boolean getJetty()
    {
        IPreferenceStore store = CactusPlugin.getDefault().getPreferenceStore();
        boolean result = store.getBoolean(JETTY);
        CactusPlugin.log("Cactus preference : Jetty = [" + result + "]");
        return result;
    }

    /**
     * @return the jetty.xml location
     */
    public static String getJettyXML()
    {
        IPreferenceStore store = CactusPlugin.getDefault().getPreferenceStore();
        String result = store.getString(JETTY_XML);
        CactusPlugin.log("Cactus preference : jetty.xml = [" + result + "]");
        return result;
    }

    /**
     * @return the ContainerHome array
     */
    public static Hashtable getContainerHomes()
    {
        Hashtable containerHomes = new Hashtable();
        String[] containerIds = CactusPlugin.getContainerIds();

            for (int i = 0; i < containerIds.length; i++)
            {
                String currentId = containerIds[i];
                String currentContainerDir = getContainerDir(currentId);
                if (!currentContainerDir.equals(""))
                {
                    containerHomes.put(currentId, currentContainerDir);
                }

            }
        CactusPlugin.log(
            "Cactus preference : ContainerHomes = [" + containerHomes + "]");
        return containerHomes;
    }
}
