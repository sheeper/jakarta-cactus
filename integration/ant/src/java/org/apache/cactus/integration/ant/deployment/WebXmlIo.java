/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
package org.apache.cactus.integration.ant.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.util.ResourceUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides convenience methods for reading and writing web deployment
 * descriptors.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public class WebXmlIo
{
    
    // Inner Classes -----------------------------------------------------------

    /**
     * Implementation of the SAX EntityResolver interface that looks up the
     * web-app DTDs from the JAR.
     */
    private static class WebXmlEntityResolver implements EntityResolver
    {

        /**
         * @see org.xml.sax.EntityResolver#resolveEntity
         */
        public InputSource resolveEntity(String thePublicId, String theSystemId)
            throws SAXException, IOException
        {
            WebXmlVersion version = WebXmlVersion.valueOf(thePublicId);
            if (version != null)
            {
                String fileName = version.getSystemId().substring(
                    version.getSystemId().lastIndexOf('/'));
                InputStream in = this.getClass().getResourceAsStream(
                    "/org/apache/cactus/integration/ant/deployment/resources"
                    + fileName);
                if (in != null)
                {
                    return new InputSource(in);
                }
            }
            System.err.println("Resource for public ID " + thePublicId
                + " not found");
            return null;
        }

    }

    // Public Methods ----------------------------------------------------------

    /**
     * Creates a new empty deployment descriptor.
     * 
     * @param theVersion The version of the descriptor to create
     * @return The new descriptor
     * @throws ParserConfigurationException If the XML parser was not correctly
     *          configured
     */
    public static WebXml newWebXml(WebXmlVersion theVersion)
        throws ParserConfigurationException
    {
        DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        DocumentType docType = null;
        if (theVersion != null)
        {
            docType =
                builder.getDOMImplementation().createDocumentType("web-app",
                    theVersion.getPublicId(), theVersion.getSystemId());
        }
        Document doc = builder.getDOMImplementation().createDocument(
            "", "web-app", docType);
        return new WebXml(doc);
    }

    /**
     * Parses the deployment descriptor of a web-application archive (WAR).
     * 
     * @param theWar The web-app archive
     * @param theEntityResolver A SAX entity resolver, or <code>null</code> to
     *        use the default
     * @return The parsed descriptor, or <code>null</code> if no descriptor was
     *         found in the WAR
     * @throws SAXException If the descriptor could not be parsed
     * @throws ParserConfigurationException If the XML parser was not correctly
     *          configured
     * @throws IOException If an I/O error occurs
     */
    public static WebXml parseWebXmlFromWar(JarInputStream theWar,
        EntityResolver theEntityResolver)
        throws SAXException, ParserConfigurationException, IOException
    {
        InputStream in = null;
        try
        {
            in = ResourceUtils.getResource(theWar, "WEB-INF/web.xml");
            if (in == null)
            {
                return null;
            }
            else
            {
                return parseWebXml(in, theEntityResolver);
            }
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ioe)
                {
                    // we'll pass on the original IO error, so ignore this one
                }
            }
        }
    }
    
    /**
     * Parses a deployment descriptor stored in a regular file.
     * 
     * @param theFile The file to parse
     * @param theEntityResolver A SAX entity resolver, or <code>null</code> to
     *        use the default
     * @return The parsed descriptor
     * @throws SAXException If the file could not be parsed
     * @throws ParserConfigurationException If the XML parser was not correctly
     *          configured
     * @throws IOException If an I/O error occurs
     */
    public static WebXml parseWebXmlFromFile(File theFile,
        EntityResolver theEntityResolver)
        throws SAXException, ParserConfigurationException, IOException
    {
        InputStream in = null;
        try
        {
            in = new FileInputStream(theFile);
            return parseWebXml(in, theEntityResolver);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ioe)
                {
                    // we'll pass on the original IO error, so ignore this one
                }
            }
        }
    }
    
    /**
     * Parses a deployment descriptor provided as input stream.
     * 
     * @param theInput The input stream
     * @param theEntityResolver A SAX entity resolver, or <code>null</code> to
     *        use the default
     * @return The parsed descriptor
     * @throws SAXException If the input could not be parsed
     * @throws ParserConfigurationException If the XML parser was not correctly
     *          configured
     * @throws IOException If an I/O error occurs
     */
    public static WebXml parseWebXml(InputStream theInput,
        EntityResolver theEntityResolver)
        throws SAXException, ParserConfigurationException, IOException
    {
        DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        if (theEntityResolver != null)
        {
            builder.setEntityResolver(theEntityResolver);
        }
        else
        {
            builder.setEntityResolver(new WebXmlEntityResolver());
        }
        return new WebXml(builder.parse(theInput));
    }

    /**
     * Writes the specified document to a file.
     * 
     * @param theWebXml The descriptor to serialize
     * @param theFile The file to write to
     * @throws IOException If an I/O error occurs
     */
    public static void writeWebXml(WebXml theWebXml, File theFile)
        throws IOException
    {
        writeWebXml(theWebXml, theFile, null, false);
    }

    /**
     * Writes the specified document to a file.
     * 
     * @param theWebXml The descriptor to serialize
     * @param theFile The file to write to
     * @param theEncoding The character encoding to use
     * @throws IOException If an I/O error occurs
     */
    public static void writeWebXml(WebXml theWebXml, File theFile,
        String theEncoding)
        throws IOException
    {
        writeWebXml(theWebXml, theFile, theEncoding, false);
    }

    /**
     * Writes the specified document to a file.
     * 
     * @param theWebXml The descriptor to serialize
     * @param theFile The file to write to
     * @param theEncoding The character encoding to use
     * @param isIndent Whether the written XML should be indented
     * @throws IOException If an I/O error occurs
     */
    public static void writeWebXml(WebXml theWebXml, File theFile,
        String theEncoding, boolean isIndent)
        throws IOException
    {
        OutputStream out = null;
        try
        {
            out = new FileOutputStream(theFile);
            writeWebXml(theWebXml, out, theEncoding, isIndent);
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException ioe)
                {
                    // we'll pass on the original IO error, so ignore this one
                }
            }
        }
    }

    /**
     * Writes the specified document to an output stream.
     * 
     * @param theWebXml The descriptor to serialize
     * @param theOutput The output stream to write to
     * @param theEncoding The character encoding to use
     * @param isIndent Whether the written XML should be indented
     * @throws IOException If an I/O error occurs
     */
    public static void writeWebXml(WebXml theWebXml, OutputStream theOutput,
        String theEncoding, boolean isIndent)
        throws IOException
    {
        OutputFormat outputFormat =
            new OutputFormat(theWebXml.getDocument());
        if (theEncoding != null)
        {
            outputFormat.setEncoding(theEncoding);
        }
        outputFormat.setIndenting(isIndent);
        outputFormat.setPreserveSpace(false);
        XMLSerializer serializer = new XMLSerializer(theOutput, outputFormat);
        serializer.serialize(theWebXml.getDocument());
    }

}
