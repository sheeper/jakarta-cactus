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
package org.apache.commons.cactus.sample.unit;

import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;
import java.io.*;

import junit.framework.*;

import org.apache.commons.cactus.*;

/**
 * Some Cactus unit tests for testing <code>ServletTestCase</code>.
 *
 * These tests should not really be part of the sample application functional
 * tests as they are unit tests for Cactus. However, they are unit tests that
 * need a servlet environment running for their execution, so they have been
 * package here for convenience. They can also be read by end-users to
 * understand how Cactus work.
 *
 * @version @version@
 */
public class TestServletTestCase2 extends ServletTestCase
{
    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestServletTestCase2(String theName)
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
        junit.ui.TestRunner.main(new String[] {TestServletTestCase2.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite()
    {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestServletTestCase2.class);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that it is possible to ask for no automatic session creation in
     * the <code>beginXXX()</code> method.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginNoAutomaticSessionCreation(ServletTestRequest theRequest)
    {
        theRequest.setAutomaticSession(false);
    }

    /**
     * Verify that it is possible to ask for no automatic session creation in
     * the <code>beginXXX()</code> method.
     */
    public void testNoAutomaticSessionCreation()
    {
        assert("A valid session has been found when no session should exist", session == null);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that multi value parameters can be sent in the
     * <code>beingXXX()</code> method to the server redirector.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginMultiValueParameters(ServletTestRequest theRequest)
    {
        theRequest.addParameter("multivalue", "value 1");
        theRequest.addParameter("multivalue", "value 2");
    }

    /**
     * Verify that multi value parameters can be sent in the
     * <code>beingXXX()</code> method to the server redirector.
     */
    public void testMultiValueParameters()
    {
        String[] values = request.getParameterValues("multivalue");
        if (values[0].equals("value 1")) {
            assertEquals("value 2", values[1]);
        } else if (values[0].equals("value 2")) {
            assertEquals("value 1", values[1]);
        } else {
            fail("Shoud have returned a vector with the values \"value 1\" and \"value 2\"");
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that it is possible to write to the servlet output stream.
     */
    public void testWriteOutputStream() throws IOException
    {
        PrintWriter pw = response.getWriter();
        pw.println("should not result in an error");
    }

    /**
     * Verify that it is possible to write to the servlet output stream.
     *
     * @param theConnection the HTTP connection that was used to call the
     *                      server redirector. It contains the returned HTTP
     *                      response.
     */
    public void endWriteOutputStream(HttpURLConnection theConnection) throws IOException
    {
        DataInputStream dis = new DataInputStream(theConnection.getInputStream());
        assertEquals("should not result in an error", dis.readLine());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can add parameters to the config list of parameters
     * programatically, without having to define them in <code>web.xml</code>.
     */
    public void testSetConfigParameter()
    {
        config.setInitParameter("testparam", "test value");

        assertEquals("test value", config.getInitParameter("testparam"));

        boolean found = false;
        Enumeration enum = config.getInitParameterNames();
        while(enum.hasMoreElements()) {
            String name = (String)enum.nextElement();
            if (name.equals("testparam")) {
                found = true;
                break;
            }
        }

        assert("[testparam] not found in parameter names", found);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can override the
     * <code>ServletConfig.getServletName()</code> method.
     */
    public void testGetServletName()
    {
        config.setServletName("MyServlet");
        assertEquals("MyServlet", config.getServletName());
    }

}