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
import java.io.*;

import org.apache.cactus.*;
import org.apache.cactus.util.*;
import org.apache.cactus.util.log.*;

/**
 * Helper class that checks configuration parameters (for the client side)
 * like if the CLASSPATH contains the jar for the Servlet API, if the
 * <code>cactus.properties</code> file is in the CLASSPATH, ...
 *
 * Note: there cannot be any logging in the checking process as the
 * logging system has not been initialized yet when the methods of this class
 * are called.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ClientConfigurationChecker
{
    /**
     * Singleton instance
     */
    private static ClientConfigurationChecker instance;

    /**
     * Has the presence of httpclient jar been checked ?
     */
    private boolean isHttpClientChecked;

    /**
     * Has the presence of cactus.properties been checked ?
     */
    private boolean isCactusPropertiesChecked;

    /**
     * Has the log4j subsystem been checked ?
     */
    private boolean isLog4jChecked;

    /**
     * Private constructor (singleton)
     */
    private ClientConfigurationChecker()
    {
    }

    /**
     * @return the singleton instance
     */
    public static synchronized ClientConfigurationChecker getInstance()
    {
        if (instance == null) {
            instance = new ClientConfigurationChecker();
        }

        return instance;
    }

    /**
     * Checks if the <code>cactus.properties</code> file is in the CLASSPATH.
     *
     * @exception ChainedRuntimeException if the properties file is not in the
     *            CLASSPATH.
     */
    public synchronized void checkCactusProperties()
    {
        if (this.isCactusPropertiesChecked) {
            return;
        }

        InputStream is = ClientConfigurationChecker.class.getResourceAsStream(
            "/" + AbstractHttpClient.CONFIG_NAME + ".properties");
        
        if (is == null) {
            String msg = "The Cactus '" + AbstractHttpClient.CONFIG_NAME +
                ".properties' configuration file need to be present in the " +
                "java CLASSPATH (i.e. the directory that contains it need " +
                "to be added to the client side CLASSPATH - This is the " +
                "CLASSPATH that you used to start the JUnit test runner).";
            throw new ChainedRuntimeException(msg);
        }

        this.isCactusPropertiesChecked = true;
    }

    /**
     * Check if the httpclient jar is in the CLASSPATH
     */
    public void checkHttpClient()
    {
        if (this.isHttpClientChecked) {
            return;
        }

        try {
            Class httpclientClass =
                Class.forName("org.apache.commons.httpclient.HttpClient");
        } catch (ClassNotFoundException e) {
            String msg = "The Commons HttpClient jar file need to be " +
                "present in the client side CLASSPATH (This is the " +
                "CLASSPATH that you used to start the JUnit test runner).";
            throw new ChainedRuntimeException(msg, e);
        }

        this.isHttpClientChecked = true;
    }

    /**
     * Verify that log_client.properties is in the CLASSPATH if the log4j jar
     * is in the classpath
     *
     * TODO: This method should no longer be needed with AspectJ as we should be
     * able to ensure that logging is always initialized. Need to be tested
     * though, so I'll leave it for the moment.
     */
    public void checkLog4j()
    {
        if (this.isLog4jChecked) {
            return;
        }

        try {
            Class log4jClass =
                Class.forName("org.apache.log4j.Category");
        } catch (ClassNotFoundException e) {
            // Log4j is not in the classpath, so it is fine if
            // log_client.properties is not in the classpath as the logging
            // system will be initialized with no op log class.
            this.isLog4jChecked = true;
            return;
        }

        InputStream is = ClientConfigurationChecker.class.getResourceAsStream(
            "/" + AbstractTestCase.LOG_CLIENT_CONFIG);

        if (is == null) {
            String msg = "The '" + AbstractTestCase.LOG_CLIENT_CONFIG +
                "' logging configuration file need to be present in the " +
                "java CLASSPATH. If you do not wish to enable logging, " +
                "please remove the log4j jar from the CLASSPATH.";
            throw new ChainedRuntimeException(msg);
        }

    }

}
