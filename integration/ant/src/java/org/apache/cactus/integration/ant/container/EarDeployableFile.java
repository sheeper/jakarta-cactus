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
package org.apache.cactus.integration.ant.container;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.deployment.ApplicationXml;
import org.apache.cactus.integration.ant.deployment.EarArchive;
import org.apache.cactus.integration.ant.deployment.WarArchive;
import org.apache.tools.ant.BuildException;
import org.xml.sax.SAXException;

/**
 * Represents an EAR file to deploy in a container. 
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class EarDeployableFile extends AbstractDeployableFile
{
    /**
     * EAR deployment descriptor as a java object
     */
    private EarArchive earArchive;

    /**
     * @see AbstractDeployableFile#AbstractDeployableFile(File) 
     */
    public EarDeployableFile(File theDeployableFile) throws BuildException
    {
        super(theDeployableFile);
    }

    /**
     * @see DeployableFile#isWar()
     */
    public final boolean isWar()
    {
        return false;
    }
    
    /**
     * @see DeployableFile#isEar()
     */
    public final boolean isEar()
    {
        return true;
    }

    /**
     * @see AbstractDeployableFile#parse()
     */
    protected void parse() 
        throws SAXException, IOException, ParserConfigurationException
    {
        this.earArchive = new EarArchive(getFile());
        String webUri = getUriOfCactifiedWebModule();
        if (webUri == null)
        {
            throw new BuildException("Could not find cactified web "
                + "module in the EAR");
        }
        this.warArchive = this.earArchive.getWebModule(webUri);
        if (this.warArchive == null)
        {
            throw new BuildException("Could not find the WAR [" + webUri
                + "] in the EAR");
        }
        String context = this.earArchive.getApplicationXml()
            .getWebModuleContextRoot(webUri);
        if (context == null)
        {
            throw new BuildException("Could not find the Cactus context "
                + "path in the EAR");            
        }
        this.contextPath = context;
    }   

    /**
     * Finds the web module in the EAR that contains the servlet test 
     * redirector, and returns the web-uri of the module found.
     * 
     * <em>A web-app is considered cactified when it contains at least a 
     * mapping for the Cactus servlet test redirector</em>
     * 
     * @return The URI of the cactified web-module, or <code>null</code> if no
     *         cactified web-app could be found
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    private String getUriOfCactifiedWebModule()
        throws SAXException, IOException, ParserConfigurationException
    {
        ApplicationXml applicationXml = this.earArchive.getApplicationXml();
        for (Iterator i = applicationXml.getWebModuleUris(); i.hasNext();)
        {
            String webUri = (String) i.next();
            WarArchive war = this.earArchive.getWebModule(webUri);
            if ((war != null) && (parseServletRedirectorMapping(war) != null))
            {
                return webUri;
            }
        }
        return null;
    }
}
