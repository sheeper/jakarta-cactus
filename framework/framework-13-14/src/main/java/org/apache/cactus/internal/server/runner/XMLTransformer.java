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
package org.apache.cactus.internal.server.runner;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Helper class that handles the transformation of the XML test results to
 * some output format determined by a stylesheet.
 * 
 * @since Cactus 1.5
 * 
 * @version $Id: XMLTransformer.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class XMLTransformer
{
    // Constants ---------------------------------------------------------------

    /**
     * Mime type of HTML content.
     */
    private static final String HTML_MIME_TYPE = "text/html";

    /**
     * XSLT output method for HTML.
     */
    private static final String HTML_OUTPUT_METHOD = "html";

    /**
     * Mime type of plain text content.
     */
    private static final String TEXT_MIME_TYPE = "text/plain";

    /**
     * XSLT output method for plain text.
     */
    private static final String TEXT_OUTPUT_METHOD = "text";

    /**
     * Mime type of XML content.
     */
    private static final String XML_MIME_TYPE = "text/xml";

    /**
     * Name of the XSLT output method property.
     */
    private static final String XSL_OUTPUT_PROPERTY_METHOD = "method";

    // Instance Variables ------------------------------------------------------

    /**
     * The XSLT templates to use for transforming the XML report into HTML.
     */
    private Templates templates = null;

    /**
     * The MIME type of the content we'll be sending to the client. This
     * defaults to "text/xml", but depends on the provided XSLT stylesheet.
     */
    private String contentType = XML_MIME_TYPE;

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param theStylesheet The input stream for the stylesheet to use for the 
     *        transformations
     * @exception TransformerConfigurationException if an error occurs when
     *            creating the XSL templates 
     */
    public XMLTransformer(InputStream theStylesheet)
        throws TransformerConfigurationException
    {
        // Setup the transformation templates
        // NOTE: Because this is done at initialization time for 
        // better performance and simplicity, changes to the
        // stylesheet will only go live after the web-app is
        // restarted
        TransformerFactory transformerFactory =
            TransformerFactory.newInstance();
        Source source = new StreamSource(theStylesheet);
        this.templates = transformerFactory.newTemplates(source);
        
        // Find out which content type is produced by the
        // stylesheet (valid values per XSLT 1.0 are 'xml', 'html'
        // and 'text')
        String outputMethod = this.templates.getOutputProperties().getProperty(
            XSL_OUTPUT_PROPERTY_METHOD);

        this.contentType = getContentType(outputMethod);
    }

    // Public Methods ----------------------------------------------------------

    /**
     * Returns the content type that will be produced by the XSLT stylesheet 
     * after transformation.
     * 
     * @return The content type
     */
    public String getContentType()
    {
        return this.contentType;
    }

    /**
     * Performs the actual transformation.
     * 
     * @param theXml The XML source to transform
     * @param theWriter The writer to which the transformation result should be 
     *        written.
     * @exception TransformerException if an error occurs when applying the
     *            XSL template to the XML source
     */
    public void transform(Reader theXml, Writer theWriter)
        throws TransformerException
    {
        Transformer transformer = this.templates.newTransformer();
        transformer.transform(new StreamSource(theXml), 
            new StreamResult(theWriter));
    }

    // Private Methods --------------------------------------------------------

    /**
     * @param theOutputMethod the output method type (Ex: "xml", "html",
     *        "text", etc)
     * @return the MIME type of the content we'll be sending to the client
     */
    private String getContentType(String theOutputMethod)
    {
        String contentType;

        if (HTML_OUTPUT_METHOD.equals(theOutputMethod))
        {
            contentType = HTML_MIME_TYPE;
        }
        else if (TEXT_OUTPUT_METHOD.equals(theOutputMethod))
        {
            contentType = TEXT_MIME_TYPE;
        }
        else
        {
            contentType = XML_MIME_TYPE;
        }
        return contentType;
    }

}
