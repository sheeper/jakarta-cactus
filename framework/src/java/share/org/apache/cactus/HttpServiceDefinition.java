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
package org.apache.cactus;

/**
 * Constants that define HTTP parameters required for defining a service that
 * is performed by the <code>ServletTestRedirector</code> servlet.
 *
 * <p>
 *   <strong>WARNING</strong><br/>
 *   This interface is not intended for use by API clients. It may be altered in
 *   backwards-incompatible ways and even moved or removed at any time without
 *   further notice.
 * </p>
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public interface HttpServiceDefinition
{
    /**
     * Prefix indicating that a String is an official Cactus command.
     */
    String COMMAND_PREFIX = "Cactus_";

    /**
     * Name of the parameter in the HTTP request that represents the unique id
     * of the test case (to ensure that the client-side test gets the correct
     * results).
     */
    String TEST_ID_PARAM = COMMAND_PREFIX + "UniqueId";

    /**
     * Name of the parameter in the HTTP request that represents the name of the
     * Test class to call. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    String CLASS_NAME_PARAM = COMMAND_PREFIX + "TestClass";

    /**
     * Name of the parameter in the HTTP request that represents an optional
     * Test being wrapped by the class represented by CLASS_NAME_PARAM.
     */
    String WRAPPED_CLASS_NAME_PARAM = COMMAND_PREFIX + "WrappedTestClass";

    /**
     * Name of the parameter in the HTTP request that represents the name of the
     * Test method to call. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    String METHOD_NAME_PARAM = COMMAND_PREFIX + "TestMethod";

    /**
     * Name of the parameter in the HTTP request that specify if a session
     * should be automatically created for the user or not.
     */
    String AUTOSESSION_NAME_PARAM = COMMAND_PREFIX + "AutomaticSession";

    /**
     * Name of the parameter in the HTTP request that specify the service asked
     * to the Redirector Servlet. It can be either to ask the Redirector Servlet
     * to call the test method or to ask the Redirector Servlet to return the
     * result of the last test.
     *
     * @see ServiceEnumeration
     */
    String SERVICE_NAME_PARAM = COMMAND_PREFIX + "Service";
}
