/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
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
package org.apache.cactus.unit;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;
import org.apache.cactus.client.ServletHttpClient;

/**
 * Some Cactus unit tests for testing <code>AbstractWebTestCase</code> that
 * verifies that the client <code>clientSetUp()</code> and
 * <code>clientTearDown()</code> work correctly.
 *
 * These tests should not really be part of the sample application functional
 * tests as they are unit tests for Cactus. However, they are unit tests that
 * need a servlet environment running for their execution, so they have been
 * package here for convenience. They can also be read by end-users to
 * understand how Cactus work.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestAbstractWebTestCase extends ServletTestCase
{
    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestAbstractWebTestCase(String theName)
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
        junit.swingui.TestRunner.main(new String[]{
            TestAbstractWebTestCase.class.getName()});
    }

    //-------------------------------------------------------------------------

    /**
     * true if <code>clientTearDown()</code> has been called.
     */
    private boolean isClientTearDownCalled;

    /**
     * Verifies that <code>clientTearDown()</code> has been called correctly.
     */
    protected void runTest() throws Throwable
    {
        runGenericTest(new ServletHttpClient());

        if (!this.isClientTearDownCalled) {
            fail("clientTearDown has not been called");
        }
    }

    /**
     * Verify that it is possible to modify the <code>WebRequest</code> in
     * the common <code>clientSetUp()</code> method. It also verifies that
     * <code>clientSetUp()</code> is called at all.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void clientSetUp(WebRequest theRequest)
    {
        theRequest.addParameter("param1", "value1");
    }

    /**
     * Verify that it is possible to modify the <code>WebRequest</code> in
     * the common <code>clientSetUp()</code> method. It also verifies that
     * <code>clientSetUp()</code> is called at all.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSetUpTearDown(WebRequest theRequest)
    {
        assertEquals("value1", theRequest.getParameterGet("param1"));
    }

    /**
     * Verify that it is possible to modify the <code>WebRequest</code> in
     * the common <code>clientSetUp()</code> method. It also verifies that
     * <code>clientSetUp()</code> is called at all.
     */
    public void testSetUpTearDown() throws Exception
    {
        assertEquals("value1", request.getParameter("param1"));
        response.getWriter().print("Hello there!");
    }

    /**
     * Verify that it is possible to read the connection object once in
     * endXXX() and then again in <code>clientTearDown()</code>. It also
     * verifies that <code>clientTearDown()</code> is called at all.
     *
     * @param theResponse the response from the server side.
     */
    public void endSetUpTearDown(WebResponse theResponse)
    {
        assertEquals("Hello there!", theResponse.getText());
    }

    /**
     * Verify that it is possible to read the connection object once in
     * endXXX() and then again in <code>clientTearDown()</code>. It also
     * verifies that <code>clientTearDown()</code> is called at all.
     *
     * @param theResponse the response from the server side.
     */
    public void clientTearDown(WebResponse theResponse)
    {
        assertEquals("Hello there!", theResponse.getText());
        this.isClientTearDownCalled = true;
    }

}