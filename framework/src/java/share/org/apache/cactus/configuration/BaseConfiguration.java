/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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
package org.apache.cactus.configuration;

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
     * Default HTTP connection class to use.
     * 
     * Note: We are using a string to point to the class to use. The reason is
     * that this class is only needed on the client side. Using an explicit
     * <code>HttpClientConnectionHelper.class.getName()</code> would require 
     * the implementation library (e.g commons-httpclient) to be also present 
     * on the server side classpath.
     */
    public static final String DEFAULT_CACTUS_CONNECTION_HELPER_CLASSNAME = 
        "org.apache.cactus.client.connector.http.HttpClientConnectionHelper";

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
