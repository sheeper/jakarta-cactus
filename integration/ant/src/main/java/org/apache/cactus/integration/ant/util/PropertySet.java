/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.tools.ant.BuildException;

/**
 * Ant element used to tell the Cactus task to load a properties file
 * and passed its properties to the client side or server side JVMs. 
 * 
 * @version $Id: PropertySet.java 238812 2004-02-29 10:21:34Z vmassol $
 */
public class PropertySet
{
    /**
     * Properties file to load.
     */
    private File propertiesFile;

    /**
     * Are the properties for the Cactus server side JVM?
     */
    private boolean isServer;
    
    /**
     * @param thePropertiesFile the properties file to load
     */
    public void setPropertiesFile(File thePropertiesFile)
    {
        this.propertiesFile = thePropertiesFile;
    }

    /**
     * @param isServer if true the properties will be passed to the
     *        Cactus server side JVM
     */
    public void setServer(boolean isServer)
    {
        this.isServer = isServer;
    }

    /**
     * @return true if the properties are to be passed to the Cactus
     *         server side JVM, false otherwise
     */
    public boolean isServer()
    {
        return this.isServer;
    }

    /**
     * @return the properties loaded from the proeprties file
     */
    public ResourceBundle readProperties()
    {
        if (this.propertiesFile == null)
        {
            throw new BuildException("Missing 'propertiesFiles' attribute");
        }
        
        ResourceBundle bundle;
        try
        {
            bundle = new PropertyResourceBundle(
                new FileInputStream(this.propertiesFile));
        } 
        catch (IOException e)
        {
            throw new BuildException("Failed to load properties "
                + "file [" + this.propertiesFile + "]");
        }
        return bundle;
    }
}
