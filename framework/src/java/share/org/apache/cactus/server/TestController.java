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
package org.apache.cactus.server;

/**
 * Controller interface that simlulates a bit the controller from the MVC
 * model in that this controller implementation classes are in charge of
 * extracting data from the request and calling the test method to execute.
 * Note that this is independent of the protocol (HTTP, JMS, etc).
 *
 * @version $Id$
 */
public interface TestController
{
    /**
     * Handles the incoming request by extracting the requested service and
     * calling the correct test method.
     *
     * @param theObjects the implicit objects (they are different for the
     *                   different redirectors)
     * @exception Exception if an error occurs when servicing the request
     */
    void handleRequest(ImplicitObjects theObjects) throws Exception;
}
