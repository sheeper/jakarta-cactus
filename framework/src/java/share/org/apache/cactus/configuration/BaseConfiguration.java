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
package org.apache.cactus.configuration;

import org.apache.cactus.client.connector.http.HttpClientConnectionHelper;
import org.apache.cactus.util.ChainedRuntimeException;

/**
 * Provides access to the Cactus configuration parameters that are independent
 * of any redirector. All Cactus configuration are defined as Java System
 * Properties.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class BaseConfiguration implements Configuration
{
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
     * Default {@link org.apache.cactus.client.connector.http.ConnectionHelper}
     * to use.
     */
    public static final String DEFAULT_CACTUS_CONNECTION_HELPER_CLASSNAME = 
        HttpClientConnectionHelper.class.getName();

    /**
     * Name of the Cactus property for defining an initializer (i.e. a class
     * that is executed before the Cactus tests start on the client side).
     */
    private static final String CACTUS_INITIALIZER_PROPERTY = 
        "cactus.initializer";

    /**
     * @return the context URL under which our application to test runs.
     */
    public String getContextURL()
    {
        // Try to read it from a System property first and then if it fails
        // from the Cactus configuration file.
        String contextURL = System.getProperty(CACTUS_CONTEXT_URL_PROPERTY);

        if (contextURL == null)
        {
            throw new ChainedRuntimeException("Missing Cactus property ["
                + CACTUS_CONTEXT_URL_PROPERTY + "]");
        }

        return contextURL;
    }

    /**
     * @return the 
     * {@link org.apache.cactus.client.connector.http.ConnectionHelper} 
     * classname to use for opening the HTTP connection
     */
    public String getConnectionHelper()
    {
        // Try to read it from a System property first and then if not defined
        // use the default.
        String connectionHelperClassname = 
            System.getProperty(CACTUS_CONNECTION_HELPER_CLASSNAME_PROPERTY);

        if (connectionHelperClassname == null)
        {
            connectionHelperClassname = 
                DEFAULT_CACTUS_CONNECTION_HELPER_CLASSNAME;
        }

        return connectionHelperClassname;
    }

    /**
     * @return the initializer class (i.e. a class that is executed before the
     *         Cactus tests start on the client side) or null if none has been
     *         defined
     */
    public String getInitializer()
    {
        return System.getProperty(CACTUS_INITIALIZER_PROPERTY);
    }
}
