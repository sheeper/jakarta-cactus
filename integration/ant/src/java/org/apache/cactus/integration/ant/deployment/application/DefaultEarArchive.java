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
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.deployment.DefaultJarArchive;
import org.apache.cactus.integration.ant.deployment.webapp.DefaultWarArchive;
import org.apache.cactus.integration.ant.deployment.webapp.WarArchive;
import org.xml.sax.SAXException;

/**
 * Encapsulates access to an EAR.
 * 
 * @since Cactus 1.5
 * @version $Id$
 */
public class DefaultEarArchive extends DefaultJarArchive implements EarArchive
{
    // Instance Variables ------------------------------------------------------

    /**
     * The parsed deployment descriptor.
     */
    private ApplicationXml applicationXml;

    // Constructors ------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @param theFile The enterprise application archive
     * @throws IOException If there was a problem reading the EAR
     */
    public DefaultEarArchive(File theFile)
        throws IOException
    {
        super(theFile);
    }

    /**
     * Constructor.
     * 
     * @param theInputStream The input stream for the enterprise application
     *        archive
     * @throws IOException If there was a problem reading the EAR
     */
    public DefaultEarArchive(InputStream theInputStream)
        throws IOException
    {
        super(theInputStream);
    }

    // Public Methods ----------------------------------------------------------

    /**
     * @see EarArchive#getApplicationXml()
     */
    public final ApplicationXml getApplicationXml()
        throws IOException, SAXException, ParserConfigurationException
    {
        if (this.applicationXml == null)
        {
            InputStream in = null;
            try
            {
                in = getResource("META-INF/application.xml");
                this.applicationXml =
                    ApplicationXmlIo.parseApplicationXml(in, null);
            }
            finally
            {
                if (in != null)
                {
                    in.close();
                }
            }
        }
        return this.applicationXml;
    }

    /**
     * @see EarArchive#getWebModule(String)
     */
    public final WarArchive getWebModule(String theUri)
        throws IOException
    {
        InputStream war = null;
        try
        {
            war = getResource(theUri);
            if (war != null)
            {
                return new DefaultWarArchive(war);
            }
        }
        finally
        {
            if (war != null)
            {
                war.close();
            }
        }
        return null;
    }

}
