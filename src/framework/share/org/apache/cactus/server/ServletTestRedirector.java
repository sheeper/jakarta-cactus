/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */
package org.apache.commons.cactus.server;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.cactus.*;

/**
 * Generic Servlet redirector that calls a test method on the server side.
 *
 * @version @version@
 * @see ServletTestCaller
 */
public class ServletTestRedirector extends HttpServlet
{
    /**
     * Handle GET requests.
     *
     * @param theRequest the incoming HTTP client request
     * @param theResponse the outgoing HTTP client request to send back.
     *
     * @exception ServletException if an error occurred when sending back
     *                             the response to the client.
     */
    public void doGet(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException
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
     * @exception ServletException if an unexpected error occurred
     */
    public void doPost(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException
    {
        // Call the correct Service method
        String serviceName = theRequest.getParameter(ServiceDefinition.SERVICE_NAME_PARAM);
        if (serviceName == null) {
            throw new ServletException("Missing parameter [" +
                ServiceDefinition.SERVICE_NAME_PARAM + "] in HTTP request.");
        }

        ServletTestCaller caller = new ServletTestCaller();
        ServletImplicitObjects objects = new ServletImplicitObjects();
        objects.m_Config = getServletConfig();
        objects.m_Request = theRequest;
        objects.m_Response = theResponse;

        // Is it the call test method service ?
        if (ServiceEnumeration.CALL_TEST_SERVICE.equals(serviceName)) {

            caller.doTest(objects);

        // Is it the get test results service ?
        } else if (ServiceEnumeration.GET_RESULTS_SERVICE.equals(serviceName)) {

            caller.doGetResults(objects);

        } else {
            throw new ServletException("Unknown service [" + serviceName +
                "] in HTTP request.");
        }

    }

}