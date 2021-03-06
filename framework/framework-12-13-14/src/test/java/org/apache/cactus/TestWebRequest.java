/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.apache.cactus.internal.WebRequestImpl;
import org.apache.cactus.internal.configuration.DefaultServletConfiguration;
import org.apache.cactus.util.ChainedRuntimeException;

import java.util.Enumeration;

/**
 * Unit tests of the <code>WebRequest</code> class.
 *
 * @version $Id: TestWebRequest.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class TestWebRequest extends TestCase
{
    /**
     * Object to unit test.
     */
    private WebRequest request;

    /**
     * @see TestCase#setUp()
     */
    protected void setUp()
    {
        request = new WebRequestImpl(new DefaultServletConfiguration());
    }
    
    /**
     * Verify that an exception is thrown when an invalid HTTP METHOD is used
     * when adding an HTTP parameter.
     */
    public void testAddParameterInvalidMethod()
    {
        try
        {
            request.addParameter("param1", "value1", "INVALIDMETHOD");
            fail("Should have thrown an exception");
        }
        catch (ChainedRuntimeException e)
        {
            assertEquals("The method need to be either \"POST\" or \"GET\"", 
                e.getMessage());
        }
    }

    /**
     * Verify that <code>getParameterGet</code> returns the first parameter
     * that was added to the request.
     */
    public void testGetParametersGetOk()
    {
        request.addParameter("param1", "value1", WebRequest.GET_METHOD);
        request.addParameter("param1", "value2", WebRequest.GET_METHOD);

        String result = request.getParameterGet("param1");

        assertEquals("value1", result);
    }

    /**
     * Verify that <code>getParameterGet</code> returns null if no parameter
     * of a given name was added to the request.
     */
    public void testGetParameterGetNull()
    {
        request.addParameter("param1", "value1", WebRequest.POST_METHOD);

        String result = request.getParameterGet("param1");

        assertNull(result);
    }

    /**
     * Verify that <code>getParameterPost</code> returns the first parameter
     * that was added to the request.
     */
    public void testGetParametersPostOk()
    {
        request.addParameter("param1", "value1", WebRequest.POST_METHOD);
        request.addParameter("param1", "value2", WebRequest.POST_METHOD);

        String result = request.getParameterPost("param1");

        assertEquals("value1", result);
    }

    /**
     * Verify that <code>getParameterPost</code> returns null if no parameter
     * of a given name was added to the request.
     */
    public void testGetParameterPostNull()
    {
        request.addParameter("param1", "value1", WebRequest.GET_METHOD);

        String result = request.getParameterPost("param1");

        assertNull(result);
    }

    /**
     * Verify that <code>getHeader</code> returns the first header
     * that was added to the request.
     */
    public void testGetHeaderOk()
    {
        request.addHeader("header1", "value1");
        request.addHeader("header2", "value2");

        String result = request.getHeader("header1");

        assertEquals("value1", result);
    }

    /**
     * Verify that <code>getHeader</code> returns null if no header
     * of a given name was added to the request.
     */
    public void testGetHeaderNull()
    {
        String result = request.getHeader("header1");

        assertNull(result);
    }

    /**
     * Verify that <Code>toString()</code> returns a nice string representation
     * of the <code>WebRequest</code>.
     */
    public void testToString()
    {
        request.addHeader("header1", "value1");
        request.addHeader("header1", "value2");
        request.addParameter("param1", "value1", WebRequest.GET_METHOD);
        request.addParameter("param1", "value1", WebRequest.POST_METHOD);
        request.addCookie("cookie1", "value1");
        request.setAutomaticSession(false);
        request.setURL("jakarta.apache.org:80", "/catalog", "/garden", 
            "/implements/", "param1=value1&param2=&param3=value3");

        String result = request.toString();

        assertEquals("simulation URL = [protocol = [http], host name = "
            + "[jakarta.apache.org], port = [80], context path = [/catalog], "
            + "servlet path = [/garden], path info = [/implements/], query "
            + "string = [param1=value1&param2=&param3=value3]], automatic "
            + "session = [false], cookies = [[name = [cookie1], value = "
            + "[value1], domain = [localhost], path = [null], isSecure = "
            + "[false], comment = [null], expiryDate = [null]]], headers = "
            + "[[[header1] = [[value1], [value2]]]], GET parameters = "
            + "[[[param3] = [[value3]]][[param2] = [[]]][[param1] = [[value1], "
            + "[value1]]]], POST parameters = [[[param1] = [[value1]]]]", 
            result);
    }

    /**
     * Verify that an error in the query string of <code>setURL()</code>
     * raises an exception.
     */
    public void testSetURLBadQueryString()
    {
        try
        {
            request.setURL("jakarta.apache.org:80", "/catalog", "/garden", 
                "/implements/", "badquerystring");
            fail("Failed to recognize invalid query string");
        }
        catch (RuntimeException e)
        {
            assertEquals("Bad QueryString [badquerystring] NameValue pair: "
                + "[badquerystring]", e.getMessage());
        }
    }

    /**
     * Verify that we can retrieve several POST parameters.
     */
    public void testGetPostParametersSeveral()
    {
        request.addParameter("param1", "value1", WebRequest.POST_METHOD);
        request.addParameter("param2", "value2", WebRequest.POST_METHOD);
        request.addParameter("param3", "value3", WebRequest.POST_METHOD);

        Enumeration keys = request.getParameterNamesPost();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            String[] values = request.getParameterValuesPost(key);

            assertEquals(1, values.length);
            
            if (!values[0].equals("value1") && !values[0].equals("value2")
                && !values[0].equals("value3"))
            {
                fail("Return value was [" + values[0] + "] but should have "
                    + "been one of [value1], [value2] or [value3]");
            }            
        }
    }
}
