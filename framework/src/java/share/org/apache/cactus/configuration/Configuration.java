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

/**
 * Contains all configuration information for the Cactus framework.
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public interface Configuration
{
    /**
     * @return the context URL under which our application to test runs.
     */
    String getContextURL();

    /**
     * @return the 
     * {@link org.apache.cactus.client.connector.http.ConnectionHelper} 
     * classname to use for opening the HTTP connection
     */
    String getConnectionHelper();

    /**
     * @return the initializer class (i.e. a class that is executed before the
     *         Cactus tests start on the client side) or null if none has been
     *         defined
     */
    String getInitializer();
}
