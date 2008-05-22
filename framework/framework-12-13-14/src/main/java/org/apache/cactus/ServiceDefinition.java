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
package org.apache.cactus;

/**
 * Constants that define HTTP parameters required for defining a service that
 * is performed by the <code>ServletTestRedirector</code> servlet.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ServiceDefinition
{
    /**
     * Name of the parameter in the HTTP request that represents the name of the
     * Test class to call. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    public static final String CLASS_NAME_PARAM =
        "Cactus_TestClass";

    /**
     * Name of the parameter in the HTTP request that represents the name of the
     * Test method to call. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    public static final String METHOD_NAME_PARAM =
        "Cactus_TestMethod";

    /**
     * Name of the parameter in the HTTP request that specify if a session
     * should be automatically created for the user or not.
     */
    public static final String AUTOSESSION_NAME_PARAM =
        "Cactus_AutomaticSession";

    /**
     * Name of the parameter in the HTTP request that specify the service asked
     * to the Redirector Servlet. It can be either to ask the Redirector Servlet
     * to call the test method or to ask the Redirector Servlet to return the
     * result of the last test.
     *
     * @see ServiceEnumeration
     */
    public static final String SERVICE_NAME_PARAM =
        "Cactus_Service";

}
