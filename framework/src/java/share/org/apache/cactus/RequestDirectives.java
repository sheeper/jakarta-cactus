/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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
 * Encapsulates the Cactus-specific parameters added to a request.
 *
 * <p>
 *   <strong>WARNING</strong><br/>
 *   This interface is not intended for use by API clients. It may be altered in
 *   backwards-incompatible ways and even moved or removed at any time without
 *   further notice.
 * </p>
 * 
 * @author <a href="mailto:ndlesiecki@apache.org>Nicholas Lesiecki</a>
 * @author <a href="mailto:cmlenz@apache.org>Christopher Lenz</a>
 *
 * @version $Id$
 */
public class RequestDirectives
{

    /**
     * The WebRequest that the directives modifies.
     */
    private WebRequest underlyingRequest;

    /**
     * @param theRequest The WebRequest to read directives from or
     *                   apply directives to.
     */
    public RequestDirectives(WebRequest theRequest)
    {
        this.underlyingRequest = theRequest;
    }

    /**
     * @param theName name of the test class.
     */
    public void setClassName(String theName)
    {
        addDirective(HttpServiceDefinition.CLASS_NAME_PARAM, theName);
    }

    /**
     * @param theName The name of the wrapped test.
     */
    public void setWrappedTestName(String theName)
    {
        addDirective(HttpServiceDefinition.WRAPPED_CLASS_NAME_PARAM, theName);
    }

    /**
     * @param theName name of the test method to execute.
     */
    public void setMethodName(String theName)
    {
        addDirective(HttpServiceDefinition.METHOD_NAME_PARAM, theName);
    }

    /**
     * @param theService The service to request of the redirector.
     */
    public void setService(ServiceEnumeration theService)
    {
        addDirective(HttpServiceDefinition.SERVICE_NAME_PARAM,
            theService.toString());
    }

    /**
     * @param isAutoSession A "boolean string" indicating
     *                       whether or not to use the 
     *                       autoSession option.
     */
    public void setAutoSession(String isAutoSession)
    {
        addDirective(HttpServiceDefinition.AUTOSESSION_NAME_PARAM,
            isAutoSession);
    }

    /**
     * Adds a cactus-specific command to the URL of the WebRequest
     * The URL is used to allow the user to send whatever he wants
     * in the request body. For example a file, ...
     * 
     * @param theName The name of the directive to add
     * @param theValue The directive value
     * @throws IllegalArgumentException If the directive name is invalid
     */
    private void addDirective(String theName, String theValue)
        throws IllegalArgumentException
    {
        if (!theName.startsWith(HttpServiceDefinition.COMMAND_PREFIX))
        {
            throw new IllegalArgumentException("Cactus directives must begin"
                + " with [" + HttpServiceDefinition.COMMAND_PREFIX
                + "]. The offending directive was [" + theName + "]");
        }
        underlyingRequest.addParameter(theName, theValue,
            WebRequest.GET_METHOD);
    }

}
