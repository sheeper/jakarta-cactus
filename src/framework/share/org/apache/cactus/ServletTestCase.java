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
package org.apache.commons.cactus;

import java.lang.reflect.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;

import junit.framework.*;

import org.apache.commons.cactus.client.*;
import org.apache.commons.cactus.server.*;

/**
 * Test classes that need access to valid Servlet implicit objects (such as the
 * the HTTP request, the HTTP response, the servlet config, ...) must subclass
 * this class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ServletTestCase extends AbstractTestCase
{
    /**
     * Valid <code>HttpServletRequest</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public org.apache.commons.cactus.server.HttpServletRequestWrapper request;

    /**
     * Valid <code>HttpServletResponse</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public HttpServletResponse response;

    /**
     * Valid <code>HttpSession</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public HttpSession session;

    /**
     * Valid <code>ServletConfig</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public ServletConfigWrapper config;

    /**
     * Constructs a JUnit test case with the given name.
     *
     * @param theName the name of the test case
     */
    public ServletTestCase(String theName)
    {
        super(theName);
    }

    /**
     * Runs a test case. This method is overriden from the JUnit
     * <code>TestCase</code> class in order to seamlessly call the
     * Cactus redirection servlet.
     */
    protected void runTest() throws Throwable
    {
        runGenericTest(new ServletHttpClient());
    }

    /**
     * Execute the test case begin method, then connect to the server proxy
     * redirector (where the test case test method is executed) and then
     * executes the test case end method.
     *
     * @param theHttpClient the HTTP client class to use to connect to the
     *                      proxy redirector.
     */
    protected void runGenericTest(AbstractHttpClient theHttpClient)
        throws Throwable
    {
        this.logger.entry("runGenericTest(...)");

        // Log the test name
        this.logger.debug("Test case = " + currentTestMethod);

        // Call the begin method to fill the request object
        ServletTestRequest request = new ServletTestRequest();
        callBeginMethod(request);

        // Add the class name, the method name, the URL to simulate and
        // automatic session creation flag to the request
        request.addParameter(ServiceDefinition.CLASS_NAME_PARAM,
            this.getClass().getName());
        request.addParameter(ServiceDefinition.METHOD_NAME_PARAM, name());
        request.addParameter(ServiceDefinition.AUTOSESSION_NAME_PARAM,
            new Boolean(request.getAutomaticSession()).toString());

        // Add the simulated URL (if one has been defined)
        if (request.getURL() != null) {
            request.getURL().saveToRequest(request);
        }

        // Open the HTTP connection to the servlet redirector
        // and manage errors that could be returned in the
        // HTTP response.
        HttpURLConnection connection = theHttpClient.doTest(request);

        // Call the end method
        callEndMethod(connection);

        // Close the intput stream (just in the case the user has not done it
        // in it's endXXX method (or if he has no endXXX method) ....
        connection.getInputStream().close();
        //connection.disconnect();

        this.logger.exit("runGenericTest");
     }
	
}
