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
package org.apache.cactus.client.authentication;

import org.apache.cactus.WebRequest;
import org.apache.cactus.configuration.Configuration;

/**
 * Interface for different authentication modules. An authentication class is
 * invoked on the client side to perform the actual authentication, for example
 * by modifying the request to includes credentials.
 * 
 * @since 1.5
 *
 * @version $Id$
 */
public interface Authentication
{

    /**
     * Modifies the request so that it will carry authentication information.
     * 
     * @param theRequest The request object that will be sent to the Cactus
     *        Redirector over HTTP
     * @param theConfiguration The Cactus configuration so that 
     *        authentication methods can get access to Cactus configuration 
     *        properties
     */
    void configure(WebRequest theRequest, Configuration theConfiguration);

}
