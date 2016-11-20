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
package org.apache.cactus.internal.server;

import org.apache.cactus.internal.HttpServiceDefinition;
import org.apache.cactus.internal.ServiceEnumeration;
import org.apache.cactus.spi.server.ImplicitObjects;
import org.apache.cactus.spi.server.TestController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Controller that extracts the requested service from the HTTP request and
 * executes the request. Examples of requests are: executing a given test, 
 * returning the test result, verifying that the controller is correctly 
 * configured, etc.
 *
 * @version $Id: AbstractWebTestController.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public abstract class AbstractWebTestController implements TestController
{
    /**
     * The logger.
     */
    private static final Log LOGGER = 
        LogFactory.getLog(AbstractWebTestController.class);

    /**
     * @param theObjects the implicit objects coming from the redirector
     * @return the test caller that will be used to execute the test
     */
    protected abstract AbstractWebTestCaller getTestCaller(
        WebImplicitObjects theObjects);

    /**
     * Handles the incoming request by extracting the requested service and
     * calling the correct method on a <code>WebTestCaller</code>.
     *
     * @param theObjects the implicit objects (they are different for the
     *                   different redirectors)
     * @exception ServletException if an error occurs when servicing the
     *            request
     */
    public void handleRequest(ImplicitObjects theObjects)
        throws ServletException
    {
        WebImplicitObjects webImplicitObjects = (WebImplicitObjects) theObjects;

        // If the Cactus user has forgotten to put a needed jar on the server
        // classpath (i.e. in WEB-INF/lib), then the servlet engine Webapp
        // class loader will throw a NoClassDefFoundError exception. As this
        // method is the entry point of the webapp, we'll catch all
        // NoClassDefFoundError exceptions and report a nice error message
        // for the user so that he knows he has forgotten to put a jar in the
        // classpath. If we don't do this, the error will be trapped by the
        // container and may not result in an ... err ... understandable error
        // message (like in Tomcat) ...
        try
        {
            String serviceName = 
                getServiceName(webImplicitObjects.getHttpServletRequest());

            AbstractWebTestCaller caller = getTestCaller(webImplicitObjects);

            // TODO: will need a factory here real soon...
            
            ServiceEnumeration service =
                ServiceEnumeration.valueOf(serviceName);
            
            // Is it the call test method service ?
            if (service == ServiceEnumeration.CALL_TEST_SERVICE)
            {
                caller.doTest();
            }
            // Is it the get test results service ?
            else if (service == ServiceEnumeration.GET_RESULTS_SERVICE)
            {
                caller.doGetResults();
            }
            // Is it the test connection service ?
            // This service is only used to verify that connection between
            // client and server is working fine
            else if (service == ServiceEnumeration.RUN_TEST_SERVICE)
            {
                caller.doRunTest();
            }
            // Is it the service to create an HTTP session?
            else if (service == ServiceEnumeration.CREATE_SESSION_SERVICE)
            {
                caller.doCreateSession();                
            }
            else if (service == ServiceEnumeration.GET_VERSION_SERVICE)
            {
                caller.doGetVersion();
            }
            else
            {
                String message = "Unknown service [" + serviceName
                    + "] in HTTP request.";

                LOGGER.error(message);
                throw new ServletException(message);
            }
        }
        catch (NoClassDefFoundError e)
        {
            // try to display messages as descriptive as possible !
            if (e.getMessage().startsWith("junit/framework"))
            {
                String message = "You must put the JUnit jar in "
                    + "your server classpath (in WEB-INF/lib for example)";

                LOGGER.error(message, e);
                throw new ServletException(message, e);
            }
            else
            {
                String message = "You are missing a jar in your "
                    + "classpath (class [" + e.getMessage()
                    + "] could not " + "be found";

                LOGGER.error(message, e);
                throw new ServletException(message, e);
            }
        }
    }

    /**
     * @param theRequest the HTTP request
     * @return the service name of the service to call (there are 2 services
     *         "do test" and "get results"), extracted from the HTTP request
     * @exception ServletException if the service to execute is missing from
     *            the HTTP request
     */
    private String getServiceName(HttpServletRequest theRequest)
        throws ServletException
    {
        // Call the correct Service method
        String queryString = theRequest.getQueryString();
        String serviceName = ServletUtil.getQueryStringParameter(queryString, 
            HttpServiceDefinition.SERVICE_NAME_PARAM);

        if (serviceName == null)
        {
            String message = "Missing service name parameter ["
                + HttpServiceDefinition.SERVICE_NAME_PARAM
                + "] in HTTP request. Received query string is ["
                + queryString + "].";

            LOGGER.debug(message);
            throw new ServletException(message);
        }

        LOGGER.debug("Service to call = " + serviceName);

        return serviceName;
    }
}
