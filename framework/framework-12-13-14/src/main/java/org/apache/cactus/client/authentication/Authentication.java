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
package org.apache.cactus.client.authentication;

import org.apache.cactus.WebRequest;
import org.apache.cactus.internal.configuration.Configuration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;

/**
 * Interface for different authentication modules. An authentication class is
 * invoked on the client side to perform the actual authentication, for example
 * by modifying the request to includes credentials.
 * 
 * @since 1.5
 *
 * @version $Id: Authentication.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public interface Authentication
{

    /**
     * Modifies the request so that it will carry authentication information.
     *
     * @param theState The HttpClient state object that can be used to ask
     *        HttpClient to set up authentication
     * @param theMethod the HttpClient HTTP method that will be used to connect 
     *        to the server side 
     * @param theRequest The request object that will be sent to the Cactus
     *        Redirector over HTTP
     * @param theConfiguration The Cactus configuration so that 
     *        authentication methods can get access to Cactus configuration 
     *        properties
     */
    void configure(HttpState theState, HttpMethod theMethod, 
        WebRequest theRequest, Configuration theConfiguration);

}
