/*
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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */
package org.apache.cactus.client;

import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.cactus.*;
import org.apache.cactus.util.log.*;
import org.apache.cactus.util.ChainedRuntimeException;

/**
 * Provides acces to the client side Cactus configuration.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ClientConfiguration
{
    /**
     * The logger
     */
    private final static Log logger =
        LogService.getInstance().getLog(ClientConfiguration.class.getName());

    /**
     * Name of the Cactus configuration file if cactus is to look for it in
     * the classpath.
     */
    private final static String CONFIG_DEFAULT_NAME = "cactus";

    /**
     * Name of the java property for specifying the location of the cactus
     * configuration file. This overrides any cactus configuration that is
     * put in the classpath.
     */
    private final static String CONFIG_PROPERTY = "cactus.config";

    /**
     * Properties file holding configuration data for Cactus.
     */
    private static ResourceBundle CONFIG = null;
     
    /**
     * Tries to read the cactus configuration from the java property defined
     * on the command line (named CONFIG_PROPERTY) and if none has been defined     
     * tries to read the CONFIG_DEFAULT_NAME file from the classpath.
     *     
     * @return the cactus configuration as a <code>ResourceBundle</code>.
     */
    private final static ResourceBundle getConfiguration()
    {
        if (CONFIG == null) {
            // Has the user passed the location of the cactus configuration file
            // as a java property
            String configOverride = System.getProperty(CONFIG_PROPERTY);
            if (configOverride == null) {
                // Try to read the default cactus configuration file from the
                // classpath
                CONFIG = PropertyResourceBundle.getBundle(CONFIG_DEFAULT_NAME);
            } else {
                try {
                    CONFIG = new PropertyResourceBundle(new FileInputStream(configOverride));
                } catch (IOException e) {
                    throw new ChainedRuntimeException("Cannot read cactus configuration file [" +
                        configOverride + "]", e);
                }
            }
        }
        return CONFIG;
    }
                                        
    /**
     * @return the Servlet redirector
     * @throw MissingResourceException if the property defining the servlet
     *        redirector is missing.
     */
    public static String getServletRedirectorURL()
    {
        return getConfiguration().getString("cactus.servletRedirectorURL");
    }

    /**
     * @return the JSP redirector
     * @throw MissingResourceException if the property defining the JSP
     *        redirector is missing.
     */
    public static String getJspRedirectorURL()
    {
        return getConfiguration().getString("cactus.jspRedirectorURL");
    }

    /**
     * @return the Filter redirector
     * @throw MissingResourceException if the property defining the Filter
     *        redirector is missing.
     */
    public static String getFilterRedirectorURL()
    {
        return getConfiguration().getString("cactus.filterRedirectorURL");
    }

}
