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
package org.apache.cactus;

import java.util.Enumeration;

import junit.framework.TestCase;

import org.apache.cactus.util.ChainedRuntimeException;
import org.apache.cactus.util.ServletConfiguration;

/**
 * Unit tests of the <code>WebRequest</code> class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestWebRequest extends TestCase
{
    // Make sure logging is disabled
    static
    {
        System.setProperty("org.apache.commons.logging.Log", 
            "org.apache.commons.logging.impl.NoOpLog");
    }

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestWebRequest(String theName)
    {
        super(theName);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that an exception is thrown when an invalid HTTP METHOD is used
     * when adding an HTTP parameter.
     */
    public void testAddParameterInvalidMethod()
    {
        WebRequest request = new WebRequest(new ServletConfiguration());

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
        WebRequest request = new WebRequest(new ServletConfiguration());

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
        WebRequest request = new WebRequest(new ServletConfiguration());

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
        WebRequest request = new WebRequest(new ServletConfiguration());

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
        WebRequest request = new WebRequest(new ServletConfiguration());

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
        WebRequest request = new WebRequest(new ServletConfiguration());

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
        WebRequest request = new WebRequest(new ServletConfiguration());

        String result = request.getHeader("header1");

        assertNull(result);
    }

    /**
     * Verify that <Code>toString()</code> returns a nice string representation
     * of the <code>WebRequest</code>.
     */
    public void testToString()
    {
        WebRequest request = new WebRequest(new ServletConfiguration());

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
        WebRequest request = new WebRequest(new ServletConfiguration());

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
        WebRequest request = new WebRequest(new ServletConfiguration());

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