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
package org.apache.cactus.util;

import org.apache.cactus.client.HttpClientConnectionHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Enumeration;

/**
 * Provides access to the Cactus configuration parameters that are independent
 * of any redirector. All Cactus configuration are defined as Java System
 * Properties. However, a Cactus configuration can also be used, in which case
 * all properties defined withint it will be exported as Java System Properties.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class Configuration
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
     * Name of Cactus property that specify the URL up to the webapp context.
     * This is the base URL to call for the redirectors. It is made up of :
     * "http://" + serverName + port + "/" + contextName.
     */
    public static final String CACTUS_CONTEXT_URL_PROPERTY =
        "cactus.contextURL";

    /**
     * Name of the Cactus property for overriding the default
     * {@link org.apache.cactus.client.ConnectionHelper}. Defaults to
     * {@link org.apache.cactus.client.HttpClientConnectionHelper}
     */
    private static final String CACTUS_CONNECTION_HELPER_CLASSNAME_PROPERTY =
        "cactus.connectionHelper.classname";

    /**
     * Default {@link org.apache.cactus.client.ConnectionHelper} to use.
     */
    public static final String DEFAULT_CACTUS_CONNECTION_HELPER_CLASSNAME =
        HttpClientConnectionHelper.class.getName();

    /**
     * True if the Cactus configuration file has already been read.
     * @see #initialize()
     */
    private static boolean isInitialized;

    /**
     * Read the cactus configuration file from the java property defined
     * on the command line (named CACTUS_CONFIG_PROPERTY) and if none has been
     * defined tries to read the DEFAULT_CONFIG_NAME file from the classpath.
     * All properties found are exported as java system properties.
     */
    public static final void initialize()
    {
        if (!isInitialized) {

            ResourceBundle config;

            // Has the user passed the location of the cactus configuration
            // file as a java property
            String configOverride = System.getProperty(CACTUS_CONFIG_PROPERTY);
            if (configOverride == null) {
                // Try to read the default cactus configuration file from the
                // classpath
                config = ClassLoaderUtils.loadPropertyResourceBundle(
                    DEFAULT_CONFIG_NAME, Configuration.class);
            } else {
                // Try to read from specified properties file
                try {
                    config = new PropertyResourceBundle(
                            new FileInputStream(configOverride));
                } catch (IOException e) {
                    throw new ChainedRuntimeException(
                            "Cannot read cactus configuration file ["
                            + configOverride + "]", e);
                }
            }

            Enumeration keys = config.getKeys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();

                // Only set the system property if it does not already exist.
                // This allows to have a cactus properties file and override
                // some values on the command line.
                if (System.getProperty(key) == null) {
                    System.setProperty(key, config.getString(key));
                }
            }

            isInitialized = true;
        }
    }

    /**
     * @return the context URL under which our application to test runs.
     */
    public static String getContextURL()
    {
        initialize();

        // Try to read it from a System property first and then if it fails
        // from the Cactus configuration file.
        String contextURL = System.getProperty(CACTUS_CONTEXT_URL_PROPERTY);
        if (contextURL == null) {
            throw new ChainedRuntimeException("Missing Cactus property ["
                + CACTUS_CONTEXT_URL_PROPERTY + "]");
        }
        return contextURL;
    }

    /**
     * @return the {@link org.apache.cactus.client.ConnectionHelper} classname
     *         to use for opening the HTTP connection
     */
    public static String getConnectionHelper()
    {
        // Try to read it from a System property first and then if not defined
        // use the default.
        String connectionHelperClassname =
            System.getProperty(CACTUS_CONNECTION_HELPER_CLASSNAME_PROPERTY);
        if (connectionHelperClassname == null) {
            connectionHelperClassname =
                DEFAULT_CACTUS_CONNECTION_HELPER_CLASSNAME;
        }

        return connectionHelperClassname;
    }
}
