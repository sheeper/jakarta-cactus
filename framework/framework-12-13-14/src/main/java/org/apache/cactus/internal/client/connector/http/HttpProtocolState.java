/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation.
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
package org.apache.cactus.internal.client.connector.http;

import java.net.HttpURLConnection;

import org.apache.cactus.spi.client.connector.ProtocolState;

/**
 * HTTP-specific state information to be passed to the different 
 * {@link org.apache.cactus.spi.client.connector.ProtocolHandler} lifecycle
 * methods. More specifically, we need to pass around the HTTP connection
 * object as it is created in the lifecycle method that runs the test
 * and the it is required in the lifecycle methods that create the 
 * response factory instance and that clean up the test (the HTTP connection
 * is closed if need be).
 * 
 * @version $Id: HttpProtocolState.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class HttpProtocolState implements ProtocolState
{
    /**
     * HTTP connection that was used to connect to the server side to execute
     * the test.
     */
    private HttpURLConnection connection;

    /**
     * @param theConnection the HTTP connection that was used to connect to the
     *        server side to execute the test.
     */
    public void setConnection(HttpURLConnection theConnection)
    {
        this.connection = theConnection;
    }

    /**
     * @return the HTTP connection that was used to connect to the server side
     *         to execute the test.
     */
    public HttpURLConnection getConnection()
    {
        return this.connection;
    }
}
