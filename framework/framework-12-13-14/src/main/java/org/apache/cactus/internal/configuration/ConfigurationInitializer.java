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
package org.apache.cactus.internal.configuration;

import org.apache.cactus.internal.util.ClassLoaderUtils;
import org.apache.cactus.util.ChainedRuntimeException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Read Cactus configuration files and set the properties found as
 * System properties.
 *
 * @version $Id: ConfigurationInitializer.java 239016 2004-06-27 15:23:30Z vmassol $
 */
public class ConfigurationInitializer
{
    /**
     * Name of the Cactus configuration file if cactus is to look for it in
     * the classpath.
     */
    private static final String DEFAULT_CONFIG_NAME = "cactus";

    /**
     * Name of the java property for specifying the location of the cactus
     * configuration file. This overrides any cactus configuration file that is
     * put in the classpath.
     */
    private static final String CACTUS_CONFIG_PROPERTY = "cactus.config";

    /**
     * Name of the Cactus property that points to a properties file
     * containing logging configuration.
     */
    private static final String CACTUS_LOGGING_CONFIG_PROPERTY = 
        "cactus.logging.config";

    /**
     * Have the Cactus configuration files been initialized?
     */
    private static boolean isInitialized;
    
    /**
     * Read Cactus configuration files.
     * 
     * @param isReinitialization if true then force a re-read of the Cactus 
     *        configuration files
     */
    public static synchronized void initialize(boolean isReinitialization)
    {
        if (!isInitialized)
        {    
            initializeConfig(isReinitialization);
            initializeLoggingConfig(isReinitialization);
            isInitialized = true;
        }
    }

    /**
     * Read Cactus configuration files.
     */
    public static synchronized void initialize()
    {
        initialize(false);
    }
    
    /**
     * Initialize general cactus configuration. Read the cactus configuration 
     * file from the java property defined on the command line 
     * (named CACTUS_CONFIG_PROPERTY) and if none has been defined tries to 
     * read the DEFAULT_CONFIG_NAME file from the classpath. All properties 
     * found are exported as java system properties.
     * 
     * @param isReinitialization if true then force a re-read of the Cactus 
     *        configuration files
     */
    private static void initializeConfig(boolean isReinitialization)
    {
        ResourceBundle config;

        // Has the user passed the location of the cactus configuration
        // file as a java property
        String configOverride = System.getProperty(CACTUS_CONFIG_PROPERTY);

        if (configOverride == null)
        {
            // Try to read the default cactus configuration file from the
            // classpath
            try
            {
                config = ClassLoaderUtils.loadPropertyResourceBundle(
                    DEFAULT_CONFIG_NAME, ConfigurationInitializer.class);
            }
            catch (MissingResourceException e)
            {
                // Cannot find cactus properties file. Do nothing.
                return;
            }
        }
        else
        {
            // Try to read from specified properties file
            try
            {
                config = new PropertyResourceBundle(
                    new FileInputStream(configOverride));
            }
            catch (IOException e)
            {
                throw new ChainedRuntimeException(
                    "Cannot read cactus configuration file ["
                    + configOverride + "]", e);
            }
        }

        addSystemProperties(config, isReinitialization);
    }

    /**
     * Initialize logging configuration.
     * 
     * @param isReinitialization if true then force a re-read of the Cactus 
     *        configuration files
     */
    private static void initializeLoggingConfig(boolean isReinitialization)
    {
        String logConfig = System.getProperty(CACTUS_LOGGING_CONFIG_PROPERTY);
        if (logConfig != null)
        {
            ResourceBundle bundle;
            try
            {
                bundle = new PropertyResourceBundle(
                    new FileInputStream(logConfig));
            } 
            catch (IOException e)
            {
                throw new ChainedRuntimeException("Failed to load logging "
                    + "configuration file [" + logConfig + "]");
            }
            addSystemProperties(bundle, isReinitialization);
        }
    }

    /**
     * Add all properties found in the resource bundle as system
     * properties.
     *
     * @param theBundle the resource bundle containing the properties to
     *        set as system properties
     * @param isReinitialization if true then force a re-read of the Cactus 
     *        configuration files
     */
    private static void addSystemProperties(ResourceBundle theBundle,
            boolean isReinitialization)
    {
        Enumeration keys = theBundle.getKeys();

        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();

            // Only set the system property if it does not already exist.
            // This allows system properties defined on the command line
            // override Cactus properties located in the Cactus configuration
            // files.
            if ((System.getProperty(key) == null) || isReinitialization)
            {
                System.setProperty(key, theBundle.getString(key));
            }
        }
    }
}
