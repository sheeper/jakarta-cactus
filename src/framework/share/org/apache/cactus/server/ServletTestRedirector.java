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
import org.apache.commons.cactus.util.log.*;

/**
 * Generic Servlet redirector that calls a test method on the server side.
 *
 * @version @version@
 * @see ServletTestCaller
 */
public class ServletTestRedirector extends HttpServlet
{
    /**
     * Initialize the logging subsystem so that it can get it's configuration
     * details from the correct properties file. Initialization is done here
     * as this servlet is the first point of entry to the server code.
     */
    static {
        LogService.getInstance().init("/log_server.properties");
    }

    /**
     * The logger
     */
    private static Log logger = LogService.getInstance().getLog(ServletTestRedirector.class.getName());

    /**
     * Some magic keyword that is prepended to the servlet output stream in
     * order to never have an empty stream returned to the client side. This is
     * needed because the client side will try to read all the returned data and
     * if there is none will block ...
     */
    static final String MAGIC_KEYWORD = "C*&()C$$";

    /**
     * Handle GET requests.
     *
     * @param theRequest the incoming HTTP client request
     * @param theResponse the outgoing HTTP client request to send back.
     *
     * @exception ServletException if an error occurred when sending back
     *                             the response to the client.
     */
    public void doGet(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException, IOException
    {
        logger.entry("doGet(...)");

        // Same handling than for a POST
        doPost(theRequest, theResponse);

        logger.exit("doGet");
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
    public void doPost(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException, IOException
    {
        logger.entry("doPost(...)");

        // If the Cactus user has forgotten to put a needed jar on the server 
        // classpath (i.e. in WEB-INF/lib), then the servlet engine Webapp 
        // class loader will throw a NoClassDefFoundError exception. As this
        // method is the entry point of the webapp, we'll catch all
        // NoClassDefFoundError exceptions and report a nice error message
        // for the user so that he knows he has forgotten to put a jar in the
        // classpath. If we don't do this, the error will be trapped by the
        // container and may not result in an ... err ... understandable error
        // message (like in Tomcat) ...
        try {

            // Call the correct Service method
            String serviceName = theRequest.getParameter(ServiceDefinition.SERVICE_NAME_PARAM);
            if (serviceName == null) {
                throw new ServletException("Missing parameter [" +
                    ServiceDefinition.SERVICE_NAME_PARAM + "] in HTTP request.");
            }
    
            logger.debug("Service called = " + serviceName);
    
            ServletTestCaller caller = new ServletTestCaller();
            ServletImplicitObjects objects = new ServletImplicitObjects();
            objects.m_Config = getServletConfig();
            objects.m_Request = theRequest;
            objects.m_Response = theResponse;
    
            // Is it the call test method service ?
            if (ServiceEnumeration.CALL_TEST_SERVICE.equals(serviceName)) {
    
                caller.doTest(objects);
    
                // Ugly hack here : The client side need to read all the data
                // returned on the servlet output stream. Otherwise the servlet
                // engine might do an io block, waiting for the client to read
                // more data. Is this happens, then we are stuck because the client
                // side is waiting for the test result to be committed but the
                // latter will only be committed when all the data has been sent
                // on the servlet output stream. So we need to read the data from
                // the client side. However, some tests do not return any data and
                // thus the read would block ... So we always send at the end of
                // the stream a magic keyword so that the returned stream is never
                // empty. This magic keyword will be ignored by the client side
                // ... ugly, no ?
    
                // This can easily be corrected with the Servlet API 2.3 (by
                // doing all the read on the server side instead of the client side
                // ). However it does not work on some 2.2 API servlet engines
                // (like Tomcat 3.2 ...).
    
                // Send magic keyword ... well at least try ... Yes, you read it
                // correctly ! I said 'try' because if the test has done a forward
                // for example, then the Writer or OutputStream will have already
                // been used and thus it would lead to an error to try to write to
                // them ... In that case, we won't write anything back but that
                // should be fine as a forward is supposed to return something ...
        
                // Note: There might still be the case where I get a Writer in my
                // test case but don't use it ... hum ... To verify ... later ...
    
                // Try sending the magic keyword ...
                logger.debug("Sending magic keyword ...");
                try {
                    theResponse.getOutputStream().print(MAGIC_KEYWORD);
                } catch (Exception e) {
                    logger.debug("Failed to to send magic keyword (this is normal)", e);
                    // It failed ... Do nothing
                }
    
            // Is it the get test results service ?
            } else if (ServiceEnumeration.GET_RESULTS_SERVICE.equals(serviceName)) {
    
                caller.doGetResults(objects);
    
            } else {
                throw new ServletException("Unknown service [" + serviceName +
                    "] in HTTP request.");
            }

        } catch (NoClassDefFoundError e) {

            // try to display messages as descriptive as possible !

            if (e.getMessage().startsWith("junit/framework")) {
                throw new ServletException("You must put the JUnit jar in " +
                    "your server classpath (in WEB-INF/lib for example)", e);
            } else {
                throw new ServletException("You are missing a jar in your " +
                    "classpath (class [" + e.getMessage() + "] could not " +
                    "be found", e);
            }
        }

        logger.exit("doPost");

    }

}