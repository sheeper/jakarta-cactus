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
package org.apache.cactus.eclipse.runner.ui;

import java.util.Hashtable;

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
