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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.cactus.HttpServiceDefinition;
import org.apache.cactus.ServiceEnumeration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Controller that extracts the requested service from the HTTP request and
 * executes the request. Examples of requests are: executing a given test, 
 * returning the test result, verifying that the controller is correctly 
 * configured, etc.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public abstract class AbstractWebTestController implements TestController
{
    /**
     * The logger
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

            // FIXME: will need a factory here real soon...
            
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