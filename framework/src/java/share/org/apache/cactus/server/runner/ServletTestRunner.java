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
package org.apache.cactus.server.runner;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestResult;

import org.apache.cactus.util.BaseConfiguration;

/**
 * Helper servlet to start a JUnit Test Runner in a webapp.
 * 
 * <p>
 *   This class currently does a couple of reflection tricks to avoid a direct 
 *   dependancy on the TraX API (<code>javax.xml.transform.*</code>),
 *   encapsulated in the {@link XMLTransformer} class.
 * </p>
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class ServletTestRunner extends HttpServlet
{
    /**
     * HTTP parameter containing name of test suite to execute
     */
    private static final String HTTP_SUITE_PARAM = "suite";

    /**
     * HTTP parameter that determines whether the XML test results should be 
     * transformed using the XSLT stylesheet specified as initialization 
     * parameter.
     */
    private static final String HTTP_TRANSFORM_PARAM = "transform";

    /**
     * HTTP parameter containing name of the XSL stylesheet to put in the
     * returned XML test result. It will only work if the browser supports
     * this feature (IE does, I don't know about others).
     */
    private static final String HTTP_XSL_PARAM = "xsl";

    /**
     * Name of the servlet initialization parameter that contains the path to
     * the XSLT stylesheet for transforming the XML report into HTML.
     */
    private static final String XSL_STYLESHEET_PARAM = "xsl-stylesheet";

    /**
     * The XML transformer. Avoid direct dependancy by using reflection.
     */
    private Object transformer = null;

    /**
     * Called by the container when the servlet is initialized.
     * 
     * @throws ServletException If an initialization parameter contains an
     *         illegal value
     */
    public void init() throws ServletException
    {
        String xslStylesheetParam = getInitParameter(XSL_STYLESHEET_PARAM);
        if (xslStylesheetParam != null)
        {
            InputStream xslStylesheet =
                getServletContext().getResourceAsStream(xslStylesheetParam);
            if (xslStylesheet != null)
            {
                try
                {
                    Class transformerClass =
                        Class.forName(
                            "org.apache.cactus.server.runner.XMLTransformer");
                    Constructor transformerCtor =
                        transformerClass.getConstructor(
                            new Class[] {InputStream.class});
                    transformer =
                        transformerCtor.newInstance(
                            new Object[] {xslStylesheet});
                }
                catch (Throwable t)
                {
                    log("Could not instantiate XMLTransformer - will not "
                        + "perform server-side XSLT transformations", t);
                }
            }
            else
            {
                throw new UnavailableException(
                    "The initialization parameter 'xsl-stylesheet' does not "
                    + "refer to an existing resource");
            }
        }
    }

    /**
     * Starts the test suite passed as a HTTP parameter
     *
     * @param theRequest the incoming HTTP client request
     * @param theResponse the outgoing HTTP client request to send back.
     *
     * @exception ServletException if an error occurs when servicing the
     *            request
     * @exception IOException if an error occurs when servicing the request
     */
    public void doGet(HttpServletRequest theRequest, 
        HttpServletResponse theResponse) throws ServletException, 
        IOException
    {
        // Verify if a suite parameter exists
        String suiteClassName = theRequest.getParameter(HTTP_SUITE_PARAM);

        if (suiteClassName == null)
        {
            throw new ServletException("Missing HTTP parameter ["
                + HTTP_SUITE_PARAM + "] in request");
        }

        // Set up default Cactus System properties so that there is no need
        // to have a cactus.properties file in WEB-INF/classes
        System.setProperty(BaseConfiguration.CACTUS_CONTEXT_URL_PROPERTY, 
            "http://" + theRequest.getServerName() + ":"
            + theRequest.getServerPort()
            + theRequest.getContextPath());

        // Get the XSL stylesheet parameter if any
        String xslParam = theRequest.getParameter(HTTP_XSL_PARAM);

        // Get the transform parameter if any
        String transformParam = theRequest.getParameter(HTTP_TRANSFORM_PARAM);

        // Run the tests
        String xml = run(suiteClassName, xslParam);

        // Check if we should do the transformation server side
        if ((transformParam != null) && (transformer != null))
        {
            // Transform server side
            try
            {
                Method getContentTypeMethod =
                    transformer.getClass().getMethod(
                        "getContentType", new Class[0]);
                theResponse.setContentType((String)
                    getContentTypeMethod.invoke(transformer, new Object[0]));
                PrintWriter out = theResponse.getWriter();
                Method transformMethod =
                    transformer.getClass().getMethod(
                        "transform", new Class[] {Reader.class,Writer.class});
                transformMethod.invoke(transformer,
                    new Object[] {new StringReader(xml), out});
            }
            catch (Exception e)
            {
                throw new ServletException(
                    "Problem applying the XSLT transformation", e);
            }
        }
        else
        {
            // Transform client side (or not at all)
            theResponse.setContentType("text/xml");
            PrintWriter pw = theResponse.getWriter();
            pw.println(xml);
        }
    }

    /**
     * Run the suite tests and return the result.
     *
     * @param theSuiteClassName the suite containing the tests to run
     * @param theXslFileName the name of the XSL stylesheet or null if we don't
     *        want to apply a stylesheet to the returned XML data
     * @return the result object
     * @exception ServletException if the suite failed to be loaded
     */
    protected String run(String theSuiteClassName, String theXslFileName)
        throws ServletException
    {
        TestResult result = new TestResult();

        XMLFormatter formatter = new XMLFormatter();

        formatter.setXslFileName(theXslFileName);

        formatter.setSuiteClassName(theSuiteClassName);

        result.addListener(formatter);

        long startTime = System.currentTimeMillis();

        WebappTestRunner testRunner = new WebappTestRunner();

        Test suite = testRunner.getTest(theSuiteClassName);

        if (suite == null)
        {
            throw new ServletException("Failed to load test suite ["
                + theSuiteClassName + "], Reason is ["
                + testRunner.getErrorMessage() + "]");
        }


        // Run the tests
        suite.run(result);

        long endTime = System.currentTimeMillis();

        formatter.setTotalDuration(endTime - startTime);

        return formatter.toXML(result);
    }
}
