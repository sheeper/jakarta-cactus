/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cactus.internal.configuration.ConfigurationInitializer;
import org.apache.cactus.internal.server.ServletImplicitObjects;
import org.apache.cactus.internal.server.ServletTestController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generic Servlet redirector that calls a test method on the server side.
 *
 * @version $Id: ServletTestRedirector.java 238991 2004-05-22 11:34:50Z vmassol $
 * @see org.apache.cactus.internal.server.ServletTestCaller
 */
public class ServletTestRedirector extends HttpServlet
{
    /**
     * As this class is the first one loaded on the server side, we ensure
     * that the Cactus configuration has been initialized. A better 
     * implementation might be to perform this initialization in the 
     * init() method. However, that requires removing the static LOGGER
     * object.
     */
    static
    {
        ConfigurationInitializer.initialize();
    }
    
    /**
     * The logger.
     */
    private static final Log LOGGER = 
        LogFactory.getLog(ServletTestRedirector.class);

    /**
     * Handle GET requests.
     *
     * @param theRequest the incoming HTTP client request
     * @param theResponse the outgoing HTTP client request to send back.
     *
     * @exception ServletException if an error occurs when servicing the
     *            request
     */
    public void doGet(HttpServletRequest theRequest, 
        HttpServletResponse theResponse) throws ServletException
    {
        // Same handling than for a POST
        doPost(theRequest, theResponse);
    }

    /**
     * Handle POST request. Extract from the HTTP request parameter, the
     * Service to perform : call test method or return tests results.
     *
     * @param theRequest the incoming HTTP request.
     * @param theResponse the outgoing HTTP response.
     *
     * @exception ServletException if an error occurs when servicing the
     *            request
     */
    public void doPost(HttpServletRequest theRequest, 
        HttpServletResponse theResponse) throws ServletException
    {
        // Mark beginning of test on server side
        LOGGER.debug("------------- Start Servlet service");

        // Create implicit object holder
        ServletImplicitObjects objects = new ServletImplicitObjects();

        objects.setHttpServletRequest(theRequest);
        objects.setHttpServletResponse(theResponse);
        objects.setServletContext(getServletContext());
        objects.setServletConfig(getServletConfig());

        ServletTestController controller = new ServletTestController();

        controller.handleRequest(objects);
    }
}
