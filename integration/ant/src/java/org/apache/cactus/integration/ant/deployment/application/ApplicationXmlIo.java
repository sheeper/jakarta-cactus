/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.deployment.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides convenience methods for reading and writing enterprise application
 * deployment descriptors (application.xml).
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public class ApplicationXmlIo
{
    
    // Inner Classes -----------------------------------------------------------

    /**
     * Implementation of the SAX EntityResolver interface that looks up the
     * application DTDs from the JAR.
     */
    private static class ApplicationXmlEntityResolver implements EntityResolver
    {

        /**
         * @see org.xml.sax.EntityResolver#resolveEntity
         */
        public InputSource resolveEntity(String thePublicId, String theSystemId)
            throws SAXException, IOException
        {
            ApplicationXmlVersion version =
                ApplicationXmlVersion.valueOf(thePublicId);
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
            return null;
        }

    }

    // Public Methods ----------------------------------------------------------

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
    public static ApplicationXml parseApplicationXmlFromFile(File theFile,
        EntityResolver theEntityResolver)
        throws SAXException, ParserConfigurationException, IOException
    {
        InputStream in = null;
        try
        {
            in = new FileInputStream(theFile);
            return parseApplicationXml(in, theEntityResolver);
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
    public static ApplicationXml parseApplicationXml(InputStream theInput,
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
            builder.setEntityResolver(new ApplicationXmlEntityResolver());
        }
        return new DefaultApplicationXml(builder.parse(theInput));
    }

}
