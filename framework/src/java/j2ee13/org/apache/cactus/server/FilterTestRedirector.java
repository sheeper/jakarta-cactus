/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Cactus" and "Apache Software
 *    Foundation" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
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

import org.apache.cactus.configuration.ConfigurationInitializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generic Filter redirector that calls a test method on the server side.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 * @see FilterTestCaller
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
     * The logger
     */
    private static final Log LOGGER = 
        LogFactory.getLog(FilterTestRedirector.class);

    /**
     * The filter configuration object passed by the container when it calls
     * <code>init(FilterConfig)</code>
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
     * latest Filter spec yet (ex: Orion 1.5.2)
     *
     * @param theConfig the Filter Config
     */
    public void setFilterConfig(FilterConfig theConfig)
    {
        this.config = theConfig;
    }

    /**
     * Provided so that it works with containers that do not support the
     * latest Filter spec yet (ex: Orion 1.5.2)
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
