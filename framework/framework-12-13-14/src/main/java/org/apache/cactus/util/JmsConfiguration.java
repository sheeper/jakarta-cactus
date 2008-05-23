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
package org.apache.cactus.util;

import org.apache.cactus.internal.configuration.ConfigurationInitializer;

/**
 * Provides access to the Cactus configuration parameters related to the
 * JMS Redirector.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @since 1.4
 *
 * @version $Id$
 */
public class JmsConfiguration extends ConfigurationInitializer
{
    /**
     * @return the JNDI Server Initial Context Factory class (from which the
     *         Queue connection factory will be retrieved)
     */
    public static String getJndiInitialContextFactory()
    {
        initialize();

        String property =
            System.getProperty("cactus.jndi.initialContextFactory");
        if (property == null) 
        {
            throw new ChainedRuntimeException("Missing Cactus property [" +
                "cactus.jndi.initialContextFactory" + "]");
        }
        return property;
    }

    /**
     * @return the JNDI Server URL (from which the Queue connection factory
     *         will be retrieved)
     */
    public static String getJndiProviderURL()
    {
        initialize();

        String property =
            System.getProperty("cactus.jndi.providerUrl");
        if (property == null) 
        {
            throw new ChainedRuntimeException("Missing Cactus property [" +
                "cactus.jndi.providerUrl" + "]");
        }
        return property;
    }

    /**
     * @return the JNDI Server user name (from which the Queue connection
     *         factory will be retrieved)
     */
    public static String getJndiSecurityPrincipal()
    {
        initialize();

        String property =
            System.getProperty("cactus.jndi.securityPrincipal");
        if (property == null) 
        {
            throw new ChainedRuntimeException("Missing Cactus property [" +
                "cactus.jndi.securityPrincipal" + "]");
        }
        return property;
    }

    /**
     * @return the JNDI Server user password (from which the Queue connection
     *         factory will be retrieved)
     */
    public static String getJndiSecurityCredentials()
    {
        initialize();

        String property =
            System.getProperty("cactus.jndi.securityCredentials");
        if (property == null) 
        {
            throw new ChainedRuntimeException("Missing Cactus property [" +
                "cactus.jndi.securityCredentials" + "]");
        }
        return property;
    }

    /**
     * @return the JNDI Name for the JMS Connection Factory to use (ex:
     *         "javax.jms.QueueConnectionFactory")
     */
    public static String getJmsConnectionFactoryJndiName()
    {
        initialize();

        String property =
            System.getProperty("cactus.jms.connectionFactoryJndiName");
        if (property == null) 
        {
            throw new ChainedRuntimeException("Missing Cactus property [" +
                "cactus.jms.connectionFactoryJndiName" + "]");
        }
        return property;
    }

}
