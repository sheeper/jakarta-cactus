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
package org.apache.commons.cactus.sample;

import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;
import java.io.*;

import junit.framework.*;

import org.apache.commons.cactus.*;
import org.apache.commons.cactus.util.*;

/**
 * Tests of the <code>SampleServlet</code> servlet class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestSampleServlet extends ServletTestCase
{
    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestSampleServlet(String theName)
    {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs)
    {
        junit.ui.TestRunner.main(new String[] {
            TestSampleServlet.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite()
    {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestSampleServlet.class);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can assert the servlet output stream.
     */
    public void testReadServletOutputStream() throws IOException
    {
        SampleServlet servlet = new SampleServlet();
        servlet.doGet(request, response);
    }

    /**
     * Verify that we can assert the servlet output stream.
     *
     * @param theConnection the HTTP connection that was used to call the
     *                      server redirector. It contains the returned HTTP
     *                      response.
     */
    public void endReadServletOutputStream(HttpURLConnection theConnection)
        throws IOException
    {
        String expected = "<html><head/><body>A GET request</body></html>";
        String result = AssertUtils.getResponseAsString(theConnection);
        assertEquals(expected, result);            
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate a POST request to a servlet. Note that
     * the POST method is the default method so we don't need to initialize
     * any parameter in <code>beginPostMethod()</code>.
     */
    public void testPostMethod()
    {
        SampleServlet servlet = new SampleServlet();
        assertEquals("POST", servlet.checkMethod(request));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate a GET request to a servlet.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginGetMethod(WebRequest theRequest)
    {
        theRequest.setMethod(WebRequest.GET_METHOD);
    }

    /**
     * Verify that we can simulate a GET request to a servlet
     */
    public void testGetMethod()
    {
        SampleServlet servlet = new SampleServlet();
        assertEquals("GET", servlet.checkMethod(request));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that by default the session implicit object is available and can
     * be used.
     */
    public void testSetAttribute()
    {
        SampleServlet servlet = new SampleServlet();
        servlet.setSessionVariable(request);

        assert(session != null);
        assertEquals("value_setSessionVariable",
            session.getAttribute("name_setSessionVariable"));
    }

    /**
     * Verify that we can set an attribute in the request.
     */
    public void testSetRequestAttribute()
    {
        SampleServlet servlet = new SampleServlet();
        servlet.setRequestAttribute(request);

        assertEquals("value_setRequestAttribute",
            request.getAttribute("name_setRequestAttribute"));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate HTTP parameters in the HTTP request.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSendParams(WebRequest theRequest)
    {
        theRequest.addParameter("param1", "value1");
        theRequest.addParameter("param2", "value2");
    }

    /**
     * Verify that we can send several parameters in the HTTP request.
     */
    public void testSendParams()
    {
        SampleServlet servlet = new SampleServlet();
        Hashtable params = servlet.getRequestParameters(request);

        assert(params.get("param1") != null);
        assert(params.get("param2") != null);
        assertEquals("value1", params.get("param1"));
        assertEquals("value2", params.get("param2"));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate HTTP headers in the HTTP request.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSendHeader(WebRequest theRequest)
    {
        theRequest.addHeader("testheader", "this is a header test");
    }

    /**
     * Verify that we can simulate HTTP headers in the HTTP request.
     */
    public void testSendHeader()
    {
        SampleServlet servlet = new SampleServlet();
        String headerValue = servlet.getRequestHeader(request);

        assertEquals("this is a header test", headerValue);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate a single cookie sent in the HTTP request.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSendCookie(WebRequest theRequest)
    {
        // Note: The cookie value that was chosen is a string without spaces
        // because there is a problem with Resin 1.2.1 which does not support
        // quoted cookies. It has been fixed since the 15/12/2000 release of
        // Resin.
        theRequest.addCookie("testcookie", "thisisacookie");
    }

    /**
     * Verify that we can simulate a single cookie sent in the HTTP request.
     */
    public void testSendCookie()
    {
        SampleServlet servlet = new SampleServlet();
        Hashtable cookies = servlet.getRequestCookies(request);

        assert("Cannot find [testcookie] cookie in request",
                cookies.get("testcookie") != null);
        assertEquals("thisisacookie", cookies.get("testcookie"));
    }

    /**
     * Verify that we can simulate multiple cookies sent in the HTTP request.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSendMultipleCookies(WebRequest theRequest)
    {
        theRequest.addCookie("testcookie1", "cookie1");
        theRequest.addCookie("testcookie2", "cookie2");
    }

    /**
     * Verify that we can simulate multiple cookies sent in the HTTP request.
     */
    public void testSendMultipleCookies()
    {
        SampleServlet servlet = new SampleServlet();
        Hashtable cookies = servlet.getRequestCookies(request);

        assert(cookies.get("testcookie1") != null);
        assertEquals("cookie1", cookies.get("testcookie1"));

        assert(cookies.get("testcookie2") != null);
        assertEquals("cookie2", cookies.get("testcookie2"));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that it is possible to send back a header and verify it on the
     * client side.
     */
    public void testReceiveHeader()
    {
        SampleServlet servlet = new SampleServlet();
        servlet.setResponseHeader(response);
    }

    /**
     * Verify that it is possible to send back a header and verify it on the
     * client side.
     *
     * @param theConnection the HTTP connection that was used to call the
     *                      server redirector. It contains the returned HTTP
     *                      response.
     */
    public void endReceiveHeader(HttpURLConnection theConnection)
    {
        assertEquals("this is a response header",
            theConnection.getHeaderField("responseheader"));
    }

    //-------------------------------------------------------------------------

    /**
     * Test that it is possible to send back a Cookie and verify it on the
     * client side.
     */
    public void testReceiveCookie()
    {
        SampleServlet servlet = new SampleServlet();
        servlet.setResponseCookie(response);
    }

    /**
     * Test that it is possible to send back a Cookie and verify it on the
     * client side.
     *
     * @param theConnection the HTTP connection that was used to call the
     *                      server redirector. It contains the returned HTTP
     *                      response.
     */
    public void endReceiveCookie(HttpURLConnection theConnection)
    {
        Hashtable cookies = AssertUtils.getCookies(theConnection);

        Vector list = (Vector)cookies.get("responsecookie");
        assert(list.size() == 1);

        ClientCookie cookie = (ClientCookie)list.elementAt(0);
        assertEquals("responsecookie", cookie.getName());
        assertEquals("this is a response cookie", cookie.getValue());
        assertEquals("jakarta.apache.org", cookie.getDomain());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can use a <code>RequestDispatcher</code> in the class to
     * test and compare the result sent to the output stream on the client side.
     */
    public void testRequestDispatcher() throws Exception
    {
        SampleServlet servlet = new SampleServlet();
        servlet.doForward(request, response, config);
    }

    /**
     * Verify that we can use a <code>RequestDispatcher</code> in the class to
     * test and compare the result sent to the output stream on the client side.
     *
     * @param theConnection the HTTP connection that was used to call the
     *                      server redirector. It contains the returned HTTP
     *                      response.
     */
    public void endRequestDispatcher(HttpURLConnection theConnection)
        throws IOException
    {
        StringBuffer sb = new StringBuffer();
        BufferedReader input = new BufferedReader(
            new InputStreamReader(theConnection.getInputStream()));
        String str;
        while (null != ((str = input.readLine()))) {
            sb.append(str);
        }
        input.close ();

        // We cannot test what is exactly returned by the called JSP between
        // different Servlet engine return different text ! For example some
        // return the JSP comment, other do not, ...
        // Thus, we only test for a match of "Hello !"
        assert(sb.toString().indexOf("Hello !") > 0);
    }

}