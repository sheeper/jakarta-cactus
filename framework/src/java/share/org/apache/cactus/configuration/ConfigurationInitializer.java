/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
package org.apache.cactus.configuration;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.cactus.util.ChainedRuntimeException;
import org.apache.cactus.util.ClassLoaderUtils;

/**
 * Read Cactus configuration files and set the properties found as
 * System properties.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
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
     */
    public static synchronized void initialize()
    {
        if (!isInitialized)
        {    
            initializeConfig();
            initializeLoggingConfig();
            isInitialized = true;
        }
    }
    
    /**
     * Initialize general cactus configuration. Read the cactus configuration 
     * file from the java property defined on the command line 
     * (named CACTUS_CONFIG_PROPERTY) and if none has been defined tries to 
     * read the DEFAULT_CONFIG_NAME file from the classpath. All properties 
     * found are exported as java system properties.
     */
    private static void initializeConfig()
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

        addSystemProperties(config);
    }

    /**
     * Initialize logging configuration.
     */
    private static void initializeLoggingConfig()
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
            addSystemProperties(bundle);
        }
    }

    /**
     * Add all properties found in the resource bundle as system
     * properties.
     *
     * @param theBundle the resource bundle containing the properties to
     *        set as system properties
     */
    private static void addSystemProperties(ResourceBundle theBundle)
    {
        Enumeration keys = theBundle.getKeys();

        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            // Only set the system property if it does not already exist.
            // This allows to have a cactus properties file and override
            // some values on the command line.
            if (System.getProperty(key) == null)
            {
                System.setProperty(key, theBundle.getString(key));
            }
        }
    }
}
