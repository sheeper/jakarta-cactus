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
package org.apache.cactus.sample.unit;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;

/**
 * Test the {@link WebRequest#setURL} method.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestSetURL extends ServletTestCase
{
    /**
     * Verify that we can simulate the basic parts of the URL : server name,
     * default server port of 80, root servlet context, URI.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURLBasics(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "", "/test/test.jsp", null, 
            null);
    }

    /**
     * Verify that we can simulate the basic parts of the URL : server name,
     * default server port of 80, no servlet context, servlet path.
     */
    public void testSimulatedURLBasics()
    {
        // Verify URI
        assertEquals("/test/test.jsp", request.getRequestURI());

        // Verify server name
        assertEquals("jakarta.apache.org", request.getServerName());

        // Returns 80 when no port is specified
        assertEquals(80, request.getServerPort());

        // Return "" when no context path is defined
        assertEquals("", request.getContextPath());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate different parts of the URL.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURL1(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/mywebapp", "/test/test.jsp", 
            null, null);
    }

    /**
     * Verify that we can simulate different parts of the URL.
     */
    public void testSimulatedURL1()
    {
        assertEquals("/mywebapp/test/test.jsp", request.getRequestURI());
        assertEquals("jakarta.apache.org", request.getServerName());
        assertEquals(80, request.getServerPort());
        assertEquals("/mywebapp", request.getContextPath());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate different parts of the URL.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURL2(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/catalog", "/lawn", 
            "/index.html", null);
    }

    /**
     * Verify that we can simulate different parts of the URL.
     */
    public void testSimulatedURL2()
    {
        assertEquals("jakarta.apache.org", request.getServerName());
        assertEquals("/catalog/lawn/index.html", request.getRequestURI());
        assertEquals(80, request.getServerPort());
        assertEquals("/catalog", request.getContextPath());
        assertEquals("/lawn", request.getServletPath());
        assertEquals("/index.html", request.getPathInfo());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate different parts of the URL.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURL3(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/catalog", "/garden", 
            "/implements/", null);
    }

    /**
     * Verify that we can simulate different parts of the URL.
     */
    public void testSimulatedURL3()
    {
        assertEquals("jakarta.apache.org", request.getServerName());
        assertEquals("/catalog/garden/implements/", request.getRequestURI());
        assertEquals(80, request.getServerPort());
        assertEquals("/catalog", request.getContextPath());
        assertEquals("/garden", request.getServletPath());
        assertEquals("/implements/", request.getPathInfo());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate different parts of the URL.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURL4(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/catalog", 
            "/help/feedback.jsp", null, null);
    }

    /**
     * Verify that we can simulate different parts of the URL.
     */
    public void testSimulatedURL4()
    {
        assertEquals("jakarta.apache.org", request.getServerName());
        assertEquals("/catalog/help/feedback.jsp", request.getRequestURI());
        assertEquals(80, request.getServerPort());
        assertEquals("/catalog", request.getContextPath());
        assertEquals("/help/feedback.jsp", request.getServletPath());
        assertNull(request.getPathInfo());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate different parts of the URL. Also verify
     * that HTTP parameters put in the simulation URL will be
     * available on the server side as real HTTP parameters.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURL5(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/catalog", 
            "/help/feedback.jsp", null, "PARAM1=param1&PARAM2=&PARAM3=param3");
    }

    /**
     * Verify that we can simulate different parts of the URL. Also verify
     * that HTTP parameters put in the simulation URL will be
     * available on the server side as real HTTP parameters.
     */
    public void testSimulatedURL5()
    {
        assertEquals("jakarta.apache.org", request.getServerName());
        assertEquals("/catalog/help/feedback.jsp", request.getRequestURI());
        assertEquals(80, request.getServerPort());
        assertEquals("/catalog", request.getContextPath());
        assertEquals("/help/feedback.jsp", request.getServletPath());
        assertNull(request.getPathInfo());
        assertEquals("PARAM1=param1&PARAM2=&PARAM3=param3", 
            request.getQueryString());
        assertEquals(request.getParameter("PARAM1"), "param1");
        assertEquals(request.getParameter("PARAM2"), "");
        assertEquals(request.getParameter("PARAM3"), "param3");
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that if the context path is null in <code>setURL()</code> the
     * real context path will be used.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURLNullContextPath(WebRequest theRequest)
    {
        theRequest.setURL(null, null, null, null, null);
    }

    /**
     * Verify that if the context path is null in <code>setURL()</code> the
     * real context path will be used.
     */
    public void testSimulatedURLNullContextPath()
    {
        assertNotNull(request.getContextPath());       
    }
}
