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
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.util.ResourceUtils;
import org.xml.sax.SAXException;

/**
 * Class that encapsulates access to a enterprise application archive (EAR).
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public class EarArchive extends JarArchive
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
    public EarArchive(File theFile)
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
    public EarArchive(InputStream theInputStream)
        throws IOException
    {
        super(theInputStream);
    }

    // Public Methods ----------------------------------------------------------

    /**
     * Returns the deployment descriptor of the web application.
     * 
     * @return The parsed deployment descriptor
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the EAR
     * @throws SAXException If the deployment descriptor of the EAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    public ApplicationXml getApplicationXml()
        throws IOException, SAXException, ParserConfigurationException
    {
        if (this.applicationXml == null)
        {
            JarInputStream in = null;
            try
            {
                in = getContentAsStream();
                this.applicationXml =
                    ApplicationXmlIo.parseApplicationXmlFromEar(in, null);
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
     * Returns the web-app archive stored in the EAR with the specified URI.
     * 
     * @param theUri The URI of the web module
     * @return The web-app archive, or <code>null</code> if no WAR was found at
     *         the specified URI
     * @throws IOException If there was an errors reading from the EAR or WAR
     */
    public WarArchive getWebModule(String theUri)
        throws IOException
    {
        JarInputStream in = null;
        try
        {
            in = getContentAsStream();
            InputStream war = ResourceUtils.getResource(in, theUri);
            if (war != null)
            {
                return new WarArchive(war);
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
        return null;
    }

}
