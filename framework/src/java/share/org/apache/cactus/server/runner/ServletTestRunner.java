/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.cactus.configuration.BaseConfiguration;

/**
 * Helper servlet to start a JUnit Test Runner in a webapp.
 * 
 * <p>
 *   This class currently does a couple of reflection tricks to avoid a direct 
 *   dependancy on the TraX API (<code>javax.xml.transform.*</code>),
 *   encapsulated in the {@link XMLTransformer} class.
 * </p>
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
     * Encoding to use for the returned XML.
     */
    private static final String ENCODING_PARAM = "encoding";
    
    /**
     * The XML transformer. Avoid direct dependancy by using reflection.
     */
    private Object transformer = null;

    /**
     * Indicates whether the servlet has sufficient permissions to set a
     * system property, to be able to set the cactus.contentURL property. This
     * is set to false if the first attempt to set the property throws a
     * SecurityException.
     */
    private boolean canSetSystemProperty = true;

    /**
     * Called by the container when the servlet is initialized.
     * 
     * @throws ServletException If an initialization parameter contains an
     *         illegal value
     */
    public void init() throws ServletException
    {
        // Check whether XSLT transformations should be done server-side and
        // build the templates if an XSLT processor is available
        String xslStylesheetParam = getInitParameter(XSL_STYLESHEET_PARAM);
        if (xslStylesheetParam != null)
        {
            InputStream xslStylesheet =
                getServletContext().getResourceAsStream(xslStylesheetParam);
            if (xslStylesheet != null)
            {
                try
                {
                    Class transformerClass = Class.forName(
                        "org.apache.cactus.server.runner.XMLTransformer");
                    Constructor transformerCtor = 
                        transformerClass.getConstructor(
                        new Class[] {InputStream.class});
                    transformer = transformerCtor.newInstance(
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

        // Set up default Cactus System properties so that there is no need
        // to have a cactus.properties file in WEB-INF/classes
        setSystemProperties(theRequest);

        if (suiteClassName == null)
        {
            throw new ServletException("Missing HTTP parameter ["
                + HTTP_SUITE_PARAM + "] in request");
        }

        // Get the XSL stylesheet parameter if any
        String xslParam = theRequest.getParameter(HTTP_XSL_PARAM);

        // Get the transform parameter if any
        String transformParam = theRequest.getParameter(HTTP_TRANSFORM_PARAM);

        // Get the enconding parameter, if any
        String encoding = theRequest.getParameter(ENCODING_PARAM);
        
        // Run the tests
        String xml = run(suiteClassName, xslParam, encoding);

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
                        "transform", new Class[] {Reader.class, Writer.class});
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
     * Set up default Cactus System properties so that there is no need
     * to have a cactus.properties file in WEB-INF/classes.
     * 
     * Note: If the JVM security policy prevents setting System properties
     * you will still need to provide a cactus.properties file.
     * 
     * @param theRequest the HTTP request coming from the browser (used
     *        to extract information about the server name, port, etc) 
     */
    private void setSystemProperties(HttpServletRequest theRequest)
    {
        if (this.canSetSystemProperty)
        {
            try
            {
                System.setProperty(
                    BaseConfiguration.CACTUS_CONTEXT_URL_PROPERTY,
                    "http://" + theRequest.getServerName() + ":"
                    + theRequest.getServerPort()
                    + theRequest.getContextPath());
            }
            catch (SecurityException se)
            {
                log("Could not set the Cactus context URL as system property, "
                    + "you will have to include a Cactus properties file in "
                    + "the class path of the web application", se);
                this.canSetSystemProperty = false;
            }
        }
    }
    
    /**
     * Run the suite tests and return the result.
     *
     * @param theSuiteClassName the suite containing the tests to run
     * @param theXslFileName the name of the XSL stylesheet or null if we don't
     *        want to apply a stylesheet to the returned XML data
     * @param theEncoding the encoding to use for the returned XML or null if
     *        default encoding is to be used
     * @return the result object
     * @exception ServletException if the suite failed to be loaded
     */
    protected String run(String theSuiteClassName, String theXslFileName,
        String theEncoding) throws ServletException
    {
        TestResult result = new TestResult();

        XMLFormatter formatter = new XMLFormatter();
        formatter.setXslFileName(theXslFileName);
        formatter.setSuiteClassName(theSuiteClassName);

        if (theEncoding != null)
        {
            formatter.setEncoding(theEncoding);
        }
        
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
