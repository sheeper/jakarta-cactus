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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cactus.internal.configuration.ConfigurationInitializer;
import org.apache.cactus.internal.server.FilterImplicitObjects;
import org.apache.cactus.internal.server.FilterTestController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generic Filter redirector that calls a test method on the server side.
 *
 * @version $Id: FilterTestRedirector.java 238991 2004-05-22 11:34:50Z vmassol $
 * @see org.apache.cactus.internal.server.FilterTestCaller
 */
public class FilterTestRedirector implements Filter
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
        LogFactory.getLog(FilterTestRedirector.class);

    /**
     * The filter configuration object passed by the container when it calls
     * <code>init(FilterConfig)</code>.
     */
    private FilterConfig config;

    /**
     * Handle the request. Extract from the HTTP request paramete the
     * Service to perform : call test method or return tests results.
     *
     * @param theRequest the incoming HTTP request which contains all needed
     *                   information on the test case and method to call
     * @param theResponse the response to send back to the client side
     * @param theFilterChain contains the chain of filters.
     * @exception IOException if an error occurred during test on server side
     * @exception ServletException if an error occurred during test on server
     *            side
     */
    public void doFilter(ServletRequest theRequest, 
        ServletResponse theResponse, FilterChain theFilterChain) 
        throws IOException, ServletException
    {
        // Mark beginning of test on server side
        LOGGER.debug("------------- Start Filter service");

        // Create implicit object holder
        FilterImplicitObjects objects = new FilterImplicitObjects();

        objects.setHttpServletRequest((HttpServletRequest) theRequest);
        objects.setHttpServletResponse((HttpServletResponse) theResponse);
        objects.setFilterConfig(this.config);
        objects.setServletContext(this.config.getServletContext());
        objects.setFilterChain(theFilterChain);

        FilterTestController controller = new FilterTestController();

        controller.handleRequest(objects);
    }

    /**
     * Initialise this filter redirector. Called by the container.
     *
     * @param theConfig the filter config containing initialisation
     *                  parameters from web.xml
     */
    public void init(FilterConfig theConfig)
    {
        // Save the config to pass it to the test case later on
        this.config = theConfig;
    }

    /**
     * Provided so that it works with containers that do not support the
     * latest Filter spec yet (ex: Orion 1.5.2).
     *
     * @param theConfig the Filter Config
     */
    public void setFilterConfig(FilterConfig theConfig)
    {
        this.config = theConfig;
    }

    /**
     * Provided so that it works with containers that do not support the
     * latest Filter spec yet (ex: Orion 1.5.2).
     *
     * @return the Filter Config
     */
    public FilterConfig getFilterConfig()
    {
        return this.config;
    }

    /**
     * Destroy the filter. Called by the container.
     */
    public void destroy()
    {
    }
}
