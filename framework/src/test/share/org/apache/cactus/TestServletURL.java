/*
 * ====================================================================
 *
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
package org.apache.cactus;

import junit.framework.TestCase;

import org.apache.cactus.util.log.LogService;

/**
 * Unit tests of the <code>ServletURL</code> class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestServletURL extends TestCase
{
    // Initialize logging system first
    static {
        LogService.getInstance().init(null);
    }

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestServletURL(String theName)
    {
        super(theName);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that the <code>getHost()</code> method is returning the correct
     * host when a port is specified and that the <code>getPort()</code>
     * method returns the specified port.
     */
    public void testGetHostWithPort()
    {
        ServletURL servletURL = new ServletURL("jakarta.apache.org:8080",
            null, null, null, null);

        assertEquals("jakarta.apache.org", servletURL.getHost());
        assertEquals(8080, servletURL.getPort());
    }

    /**
     * Verify that <code>getPort()</code> returns -1 when the port is invalid.
     */
    public void testGetPortInvalidPortNumber()
    {
        ServletURL servletURL = new ServletURL();
        servletURL.setServerName("jakarta.apache.org:invalidPort80");

        int port = servletURL.getPort();

        assertEquals(-1, port);
    }

    /**
     * Verify that an invalid protocol raises an exception.
     */
    public void testSetProtocolInvalidProtocol()
    {
        ServletURL servletURL = new ServletURL();

        try {
            servletURL.setProtocol("invalid protocol");
            fail("Should have raised an invalid protocol error");
        } catch (RuntimeException e) {
            assertEquals("Invalid protocol [invalid protocol]. Currently "
                + "supported protocols are [http] and [https].",
                e.getMessage());
        }
    }

    /**
     * Verify that a valid protocol works.
     */
    public void testSetProtocolOk()
    {
        ServletURL servletURL = new ServletURL();

        servletURL.setProtocol(ServletURL.PROTOCOL_HTTP);
        assertEquals(ServletURL.PROTOCOL_HTTP, servletURL.getProtocol());

        servletURL.setProtocol(ServletURL.PROTOCOL_HTTPS);
        assertEquals(ServletURL.PROTOCOL_HTTPS, servletURL.getProtocol());
    }

    /**
     * Verify <code>getPath()</code> is ok when all parts are filled.
     */
    public void testGetPath()
    {
        ServletURL servletURL = new ServletURL();
        servletURL.setQueryString("param1=value1");
            servletURL.setContextPath("/context");
        servletURL.setServletPath("/servletPath");
        servletURL.setPathInfo("/pathInfo");

        String path = servletURL.getPath();

        assertEquals("/context/servletPath/pathInfo", path);
    }

    /**
     * Verify <code>getPath()</code> returns null when no parts are filled.
     */
    public void testGetPathNull()
    {
        ServletURL servletURL = new ServletURL();

        String path = servletURL.getPath();

        assertNull(path);
    }

}