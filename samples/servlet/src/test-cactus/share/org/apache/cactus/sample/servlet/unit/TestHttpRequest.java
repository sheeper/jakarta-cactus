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
package org.apache.cactus.sample.servlet.unit;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;

/**
 * Tests that exercise the HTTP request.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestHttpRequest extends ServletTestCase
{
    /**
     * Verify that <code>HttpServletRequestWrapper.getPathTranslated()</code>
     * takes into account the simulated URL (if any).
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginGetPathTranslated(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/mywebapp", "/myservlet", 
            "/test1/test2", "PARAM1=value1");
    }

    /**
     * Verify that <code>HttpServletRequestWrapper.getPathTranslated()</code>
     * takes into account the simulated URL (if any) or null in situations
     * where the servlet container cannot determine a valid file path for
     * these methods, such as when the web application is executed from an
     * archive, on a remote file system not accessible locally, or in a
     * database (see section SRV.4.5 of the Servlet 2.3 spec).
     */
    public void testGetPathTranslated()
    {
        String nativePathInfo = File.separator + "test1" + File.separator
            + "test2";

        String pathTranslated = request.getPathTranslated();

        // Should be null if getRealPath("/") is null
        if (request.getRealPath("/") == null)
        {
            assertNull("Should have been null", pathTranslated);
        }
        else
        {
            assertNotNull("Should not be null", pathTranslated);
            assertTrue("Should end with [" + nativePathInfo + "] but got ["
                + pathTranslated + "] instead", 
                pathTranslated.endsWith(nativePathInfo));
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can send arbitrary data in the request body.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSendUserData(WebRequest theRequest)
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(
            "<data>some data to send in the body</data>".getBytes());

        theRequest.setUserData(bais);
        theRequest.setContentType("text/xml");
    }

    /**
     * Verify that we can send arbitrary data in the request body.
     * 
     * @exception Exception on test failure
     */
    public void testSendUserData() throws Exception
    {
        String buffer;
        StringBuffer body = new StringBuffer();

        BufferedReader reader = request.getReader();

        while ((buffer = reader.readLine()) != null)
        {
            body.append(buffer);
        }

        assertEquals("<data>some data to send in the body</data>", 
            body.toString());
        assertEquals("text/xml", request.getContentType());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate the client remote IP address and the client
     * remote host name.
     */
    public void testRemoteClientCheck()
    {
        request.setRemoteIPAddress("192.168.0.1");
        request.setRemoteHostName("atlantis");
        request.setRemoteUser("george");

        assertEquals("192.168.0.1", request.getRemoteAddr());
        assertEquals("atlantis", request.getRemoteHost());
        assertEquals("george", request.getRemoteUser());
    }

}
