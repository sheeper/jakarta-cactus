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
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @since Cactus 1.5
 * 
 * @version $Id$
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
     * XSLT output method for XML content.
     */
    private static final String XML_OUTPUT_METHOD = "xml";

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
