/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
 */
package org.apache.cactus;

import junit.framework.TestCase;

/**
 * Unit tests of the <code>ServletURL</code> class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestServletURL extends TestCase
{
    /**
     * Verify that if the context path is not empty or null it must start with
     * a "/" character.
     */
    public void testSetContextPathFirstCharacterNotForwardSlash()
    {
        try 
        {
            new ServletURL(null, "invalidcontextpath", null, null, null);
            fail("The context path must start with a \"/\" character");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals("The Context Path must start with a \"/\" character.", 
                expected.getMessage());
        }       
    }

    /**
     * Verify that if the context path is not empty or null it must not end 
     * with the "/" character. 
     */
    public void testSetContextPathLastCharacterNotForwardSlash()
    {
        try 
        {
            new ServletURL(null, "/invalidcontextpath/", null, null, null);
            fail("The context path must not end with a \"/\" character");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals("The Context Path must not end with a \"/\""
                + " character.", expected.getMessage());
        }       
    }

    /**
     * Verify that the context path can be an empty string.
     */
    public void testSetContextPathEmptyString()
    {
        ServletURL servletURL = new ServletURL(null, "", null, null, null);
        assertEquals("", servletURL.getContextPath());
    }

    /**
     * Verify that if the servlet path is not empty or null it must start with
     * a "/" character.
     */
    public void testSetServletPathFirstCharacterNotForwardSlash()
    {
        try 
        {
            new ServletURL(null, null, "invalidservletpath", null, null);
            fail("The servlet path must start with a \"/\" character");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals("The Servlet Path must start with a \"/\" character.", 
                expected.getMessage());
        }       
    }

    /**
     * Verify that the servlet path can be an empty string.
     */
    public void testSetServletPathEmptyString()
    {
        ServletURL servletURL = new ServletURL(null, null, "", null, null);
        assertEquals("", servletURL.getServletPath());
    }

    /**
     * Verify that if the path info is not null it must start with
     * a "/" character.
     */
    public void testSetPathInfoFirstCharacterNotForwardSlash()
    {
        try 
        {
            new ServletURL(null, null, null, "invalidpathinfo", null);
            fail("The path info must start with a \"/\" character");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals("The Path Info must start with a \"/\" character.", 
                expected.getMessage());
        }       
    }

    /**
     * Verify that the path info cannot be an empty string.
     */
    public void testSetPathInfoEmptyNotAllowed()
    {
        try 
        {
            new ServletURL(null, null, null, "", null);
            fail("The path info must not be an empty string");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals("The Path Info must not be an empty string. Use "
                + "null if you don't want to have a path info.", 
                expected.getMessage());
        }               
    }

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

        try
        {
            servletURL.setProtocol("invalid protocol");
            fail("Should have raised an invalid protocol error");
        }
        catch (RuntimeException e)
        {
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
