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
package org.apache.cactus.client.connector.http;

import java.lang.reflect.Constructor;

import org.apache.cactus.configuration.Configuration;
import org.apache.cactus.util.ChainedRuntimeException;

/**
 * Factory that returns the <code>ConnectionHelper</code> specified in Cactus
 * configuration or the default one if none has been specified.
 *
 * @see Configuration
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ConnectionHelperFactory
{
    /**
     * @return a <code>ConnectionHelper</code> instance of the type specified
     *         in Cactus configuration or the default one
     *         (<code>JdkConnectionHelper</code>)
     * @param theUrl the URL to connect to as a String
     * @param theConfiguration the Cactus configuration
     */
    public static ConnectionHelper getConnectionHelper(String theUrl,
        Configuration theConfiguration)
    {
        // Load the corresponding class
        ConnectionHelper connectionHelper;

        try
        {
            Class connectionHelperClass = 
                Class.forName(theConfiguration.getConnectionHelper());
            Constructor constructor = connectionHelperClass.getConstructor(
                new Class[] {String.class});

            connectionHelper = (ConnectionHelper) constructor.newInstance(
                new Object[] {theUrl});
        }
        catch (Exception e)
        {
            throw new ChainedRuntimeException("Failed to load the ["
                + theConfiguration.getConnectionHelper()
                + "] ConnectionHelper " + "class", e);
        }

        return connectionHelper;
    }
}
