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
package org.apache.cactus.util.log;

import java.net.URL;
import java.util.Hashtable;
import java.util.MissingResourceException;

import org.apache.log4j.PropertyConfigurator;
import org.apache.cactus.util.Configuration;

/**
 * Logging service acting as a wrapper around the Jakarta Log4j logging
 * framework.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class LogService
{
    /**
     * Name of property that enables Cactus logging if passed as a System
     * property.
     */
    private static final String ENABLE_LOGGING_PROPERTY =
        "cactus.enableLogging";

    /**
     * List of <code>Log</code> instances (i.e. <code>Category</code>
     * objects for Log4j) indexed on the category's name.
     */
    private Hashtable logCategories = new Hashtable();

    /**
     * Has initialization been performed yet ?
     */
    private boolean isInitialized = false;

    /**
     * The singleton's unique instance
     */
    private static LogService instance;

    /**
     * @return the unique singleton instance
     */
    public static synchronized LogService getInstance()
    {
        if (instance == null) {
            instance = new LogService();
        }
        return instance;
    }

    /**
     * Initialize the logging system. Need to be called once before calling
     * <code>getLog()</code>.
     *
     * @param theFileName the file name (Ex: "/log_client.properties") or null
     *        to initialize a dummy logging system, meaning that all log calls
     *        will have no effect. This is useful for unit testing for
     *        instance where the goal is not to verify that logs are printed.
     */
    public void init(String theFileName)
    {
        // If logging system already initialized, do nothing
        if (isInitialized()) {
            return;
        }

        if (theFileName != null) {

            if (isLoggingEnabled()) {

                URL url = this.getClass().getResource(theFileName);
                if (url != null) {
                    // Initialize Log4j
                    PropertyConfigurator.configure(url);
                } else {
                    // Failed to configure logging system, simply print
                    // a warning on stderr
                    System.err.println("[warning] Failed to configure " +
                        "logging system : Could not find file [" +
                        theFileName + "]");
                }

            }

        }

        this.isInitialized = true;
    }

    /**
     * @return true if logging is enabled or false otherwise.
     */
    private boolean isLoggingEnabled()
    {
        boolean isLoggingEnabled = false;

        // 1 - Checks if a ENABLE_LOGGING_PROPERTY System property exist and
        //     if it is set to "true"
        // 2 - Checks if logging has been enabled in Cactus configuration
        //     file

        String value = System.getProperty(ENABLE_LOGGING_PROPERTY);
        if ((value != null) && (value.equalsIgnoreCase("true"))) {
            isLoggingEnabled = true;
        } else {
            try {
                isLoggingEnabled = Configuration.isLoggingEnabled();
            } catch (MissingResourceException e) {
                // ignored. It simply means that the cactus configuration file
                // has not been defined or is not in the classpath.
            }
        }

        // 3 - If logging is turned on but log4j is not on the classpath, do
        //     not do anything.
        return isLoggingEnabled && isLog4jInClasspath();
    }

    /**
     * @return true if Log4J is in the classpath, false otherwise.
     */
    private boolean isLog4jInClasspath()
    {
        boolean isLog4jInClasspath;
        try {
            Class.forName("org.apache.log4j.PropertyConfigurator");
            isLog4jInClasspath = true;
        } catch (ClassNotFoundException e) {
            isLog4jInClasspath = false;
        }
        return isLog4jInClasspath;
    }

    /**
     * @param theCategoryName the category's name. Usually, it is the full
     *        name of the class being logged, including the package name
     * @return the <code>Log</code> instance associated with the specified
     *         category name
     */
    public synchronized Log getLog(String theCategoryName)
    {
        // Check first if initialization has been performed
        if (!isInitialized()) {
            throw new RuntimeException("Must call init() first");
        }

        Log log = (Log) this.logCategories.get(theCategoryName);

        if (log == null) {

            if (isLoggingEnabled()) {
                log = new BaseLog(theCategoryName);
            } else {
                log = new BaseLogDummy();
            }

            this.logCategories.put(theCategoryName, log);
        }

        return log;
    }

    /**
     * @return true if the logging system has already been initialized.
     */
    public boolean isInitialized()
    {
        return this.isInitialized;
    }

}
