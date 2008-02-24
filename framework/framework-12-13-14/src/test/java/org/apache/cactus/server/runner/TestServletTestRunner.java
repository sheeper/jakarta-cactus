/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.cactus.server.runner;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;

import junit.framework.TestCase;

/**
 * Unit tests for {@link ServletTestRunner}.
 *
 * @version $Id: TestServletTestRunner.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public final class TestServletTestRunner extends TestCase
{   
    /**
     * Control mock for {@link ServletConfig}.
     */
    private Mock mockServletConfig;

    /**
     * Mock for {@link ServletConfig}.
     */
    private ServletConfig servletConfig;

    /**
     * Control mock for {@link ServletContext}.
     */
    private Mock mockServletContext;

    /**
     * Mock for {@link ServletContext}.
     */
    private ServletContext servletContext;
    
    /**
     * Object to unit test.
     */
    private ServletTestRunner runner;

    /**
     * @see TestCase#setUp()
     */
    protected void setUp()
    {
        mockServletConfig = new Mock(ServletConfig.class);
        servletConfig = (ServletConfig) mockServletConfig.proxy();

        mockServletContext = new Mock(ServletContext.class);
        servletContext = (ServletContext) mockServletContext.proxy();

        mockServletConfig.matchAndReturn("getServletContext", servletContext);
        mockServletConfig.matchAndReturn("getServletName", "TestServlet");
        mockServletContext.expect("log", C.ANY_ARGS);
        
        runner = new ServletTestRunner();
    }

    /**
     * Verify that the {@link ServletTestRunner#init()} method works when
     * there are no user stylesheet defined.
     * 
     * @throws ServletException in case of error
     */
    public void testInitWhenNoXslStylesheet() throws ServletException
    {
        mockServletConfig.expectAndReturn("getInitParameter", 
            "xsl-stylesheet", null);

        runner.init(servletConfig);        
    }

    /**
     * Verify that the {@link ServletTestRunner#init()} method works when
     * there is a user stylesheet defined which points to an invalid
     * file.
     * 
     * @throws ServletException in case of error
     */
    public void testInitWhenXslStylesheetNotFound() throws ServletException
    {
        mockServletConfig.expectAndReturn("getInitParameter", 
            "xsl-stylesheet", "some-stylesheet.xsl");
        mockServletContext.expectAndReturn("getResourceAsStream", C.ANY_ARGS, 
            null);
        
        try
        {
            runner.init(servletConfig);
            fail("Should have thrown an UnavailableException exception");
        }
        catch (UnavailableException expected)
        {
            assertEquals("The initialization parameter 'xsl-stylesheet' does "
                + "not refer to an existing resource", expected.getMessage());
        }
    }

    /**
     * Verify that the {@link ServletTestRunner#init()} method works when
     * there is a valid user stylesheet defined.
     * 
     * @throws ServletException in case of error
     */
    public void testInitWithXslStylesheet() throws ServletException
    {
        mockServletConfig.expectAndReturn("getInitParameter", 
            "xsl-stylesheet", "some-stylesheet.xsl");

        InputStream mockInputStream = new InputStream()
        {
            private int counter = 0;
            
            private static final String CONTENT = ""
                + "<xsl:stylesheet xmlns:xsl=\""
                + "http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                + "</xsl:stylesheet>";
            
            public int read() throws IOException
            {
                while (counter < CONTENT.length())
                {
                    return CONTENT.charAt(counter++);
                }
                return -1;
            }
        };

        mockServletContext.expectAndReturn("getResourceAsStream", C.ANY_ARGS, 
            mockInputStream);

        // Note: There should be no call to log. If there is it means there 
        // has been an error...

        runner.init(servletConfig);        
    }

}
