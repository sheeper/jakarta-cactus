/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
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
package org.apache.commons.cactus.util.log;

import java.util.*;
import java.net.*;

import org.apache.log4j.PropertyConfigurator;

/**
 * Logging service acting as a wrapper around the Jakarta Log4j logging
 * framework.
 */
public class LogService
{
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
     * Is Log4j in the classpath ?
     */
    private boolean isLog4jInClasspath = false;

    /**
     * The singleton's unique instance
     */
    private static LogService instance;

    /**
     * Initialization
     */
    private LogService()
    {
        // Check if Log4j is in the classpath. If not, use a dummy
        // implementation that does nothing. This is to make it easy on user
        // who do not want to have to download log4j and put it in their
        // classpath !
        isLog4jInClasspath = true;
        try {
            Class aClass = Class.forName("org.apache.log4j.PropertyConfigurator");
        } catch (ClassNotFoundException e) {
            isLog4jInClasspath = false;
        }
    }

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

            if (isLog4jInClasspath) {

                URL url = this.getClass().getResource(theFileName);
                if (url != null) {
                    // Initialize Log4j
                    PropertyConfigurator.configure(url);
                } else {
                    // Failed to configure logging system, simply print
                    // a warning on stderr
                    System.err.println("Failed to configure logging " +
                        "system : Could not find file [" + theFileName + "]");
                }

            }

        }

        isInitialized = true;
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
        if (!isInitialized) {
            throw new RuntimeException("Must call init() first");
        }

        Log log = (Log)logCategories.get(theCategoryName);

        if (log == null) {

            if (isLog4jInClasspath) {
                log = new BaseLog(theCategoryName);
            } else {
                log = new BaseLogDummy(theCategoryName);
            }

            logCategories.put(theCategoryName, log);

        }

        return log;
    }

    /**
     * @return true if the logging system has already been initialized.
     */
    public boolean isInitialized()
    {
        return isInitialized;
    }

}
