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
package org.apache.maven.cactus.sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.FilterTestCase;
import org.apache.cactus.WebResponse;

/**
 * Tests of the <code>SampleFilter</code> filter class.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestSampleFilter extends FilterTestCase
{
    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestSampleFilter(String theName)
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
        junit.swingui.TestRunner.main(
            new String[] {TestSampleFilter.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite()
    {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestSampleFilter.class);
    }

    //-------------------------------------------------------------------------

    /**
     * Test that adding a header to the output stream is working fine when
     * a header parameter is defined.
     * 
     * @exception ServletException on test failure
     * @exception IOException on test failure
     */
    public void testAddHeaderParamOK() throws ServletException, IOException
    {
        SampleFilter filter = new SampleFilter();

        config.setInitParameter("header", "<h1>header</h1>");
        filter.init(config);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        filter.addHeader(baos);

        assertEquals("<h1>header</h1>", baos.toString());
    }

    //-------------------------------------------------------------------------

    /**
     * Test that adding a header to the output stream is working fine
     * (i.e. nothing gets written) when no header parameter is defined.
     * 
     * @exception ServletException on test failure
     * @exception IOException on test failure
     */
    public void testAddHeaderParamNotDefined() throws ServletException, 
        IOException
    {
        SampleFilter filter = new SampleFilter();

        filter.init(config);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        filter.addHeader(baos);

        assertEquals("", baos.toString());
    }

    //-------------------------------------------------------------------------

    /**
     * Test that adding a footer to the output stream is working fine when
     * a footer parameter is defined.
     * 
     * @exception ServletException on test failure
     * @exception IOException on test failure
     */
    public void testAddFooterParamOK() throws ServletException, IOException
    {
        SampleFilter filter = new SampleFilter();

        config.setInitParameter("footer", "<h1>footer</h1>");
        filter.init(config);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        filter.addFooter(baos);

        assertEquals("<h1>footer</h1>", baos.toString());
    }

    //-------------------------------------------------------------------------

    /**
     * Test that adding a footer to the output stream is working fine
     * (i.e. nothing gets written) when no footer parameter is defined.
     * 
     * @exception ServletException on test failure
     * @exception IOException on test failure
     */
    public void testAddFooterParamNotDefined() throws ServletException, 
        IOException
    {
        SampleFilter filter = new SampleFilter();

        filter.init(config);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        filter.addFooter(baos);

        assertEquals("", baos.toString());
    }

    //-------------------------------------------------------------------------

    /**
     * Test that the filter does correctly add a header and footer to
     * any requets it is serving.
     * 
     * @exception ServletException on test failure
     * @exception IOException on test failure
     */
    public void testDoFilterOK() throws ServletException, IOException
    {
        SampleFilter filter = new SampleFilter();

        config.setInitParameter("header", "<h1>header</h1>");
        config.setInitParameter("footer", "<h1>footer</h1>");
        filter.init(config);

        FilterChain mockFilterChain = new FilterChain()
        {
            public void doFilter(ServletRequest theRequest, 
                ServletResponse theResponse) throws IOException, 
                ServletException
            {
                PrintWriter writer = theResponse.getWriter();

                writer.print("<p>some content</p>");
                writer.close();
            }

            public void init(FilterConfig theConfig)
            {
            }

            public void destroy()
            {
            }
        };

        filter.doFilter(request, response, mockFilterChain);
    }

    /**
     * Test that the filter does correctly add a header and footer to
     * any requets it is serving.
     *
     * @param theResponse the response from the server side.
     */
    public void endDoFilterOK(WebResponse theResponse)
    {
        assertEquals("<h1>header</h1><p>some content</p><h1>footer</h1>", 
            theResponse.getText());
    }
}