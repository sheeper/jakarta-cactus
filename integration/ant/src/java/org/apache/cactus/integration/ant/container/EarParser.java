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
import org.apache.cactus.integration.ant.deployment.DefaultEarArchive;
import org.apache.cactus.integration.ant.deployment.EarArchive;
import org.apache.cactus.integration.ant.deployment.WarArchive;
import org.apache.tools.ant.BuildException;
import org.xml.sax.SAXException;

/**
 * Parse an EAR descriptor to extract meaninful information for Cactus,
 * the results being stored in a {@link EarDeployableFile} object. 
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public class EarParser
{
    /**
     * Parse an EAR descriptor to extract meaninful information for Cactus.
     * 
     * @param theDeployableFile the file to parse and deploy
     * @return the parse results as a {@link EarDeployableFile} object
     */
    public static final EarDeployableFile parse(File theDeployableFile)
    {
        EarDeployableFile deployable = new EarDeployableFile();

        try
        {
            deployable.setFile(theDeployableFile);

            EarArchive earArchive = new DefaultEarArchive(theDeployableFile);
            String webUri = getUriOfCactifiedWebModule(earArchive);
            if (webUri == null)
            {
                throw new BuildException("Could not find cactified web "
                    + "module in the [" + theDeployableFile + "] EAR.");
            }

            WarArchive warArchive = earArchive.getWebModule(webUri);
            if (warArchive == null)
            {
                throw new BuildException("Could not find the WAR [" + webUri
                    + "] in the [" + theDeployableFile + "] EAR.");
            }
            
            deployable.setWarArchive(warArchive);
            deployable.setTestContext(parseTestContext(earArchive, webUri));
            deployable.setServletRedirectorMapping(
                WarParser.parseServletRedirectorMapping(
                    deployable.getWarArchive()));
            deployable.setFilterRedirectorMapping(
                WarParser.parseFilterRedirectorMapping(
                    deployable.getWarArchive()));
            deployable.setJspRedirectorMapping(
                WarParser.parseJspRedirectorMapping(
                    deployable.getWarArchive()));
        }
        catch (IOException e)
        {
            throw new BuildException("Failed to parse deployment descriptor "
                + "for EAR file [" + theDeployableFile + "].", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new BuildException("Failed to parse deployment descriptor "
                + "for EAR file [" + theDeployableFile + "].", e);
        }
        catch (SAXException e)
        {
            throw new BuildException("Failed to parse deployment descriptor "
                + "for EAR file [" + theDeployableFile + "].", e);
        }
        
        return deployable;
    }   

    /**
     * Find the test context from the EAR archive. 
     * 
     * @return the test context
     * @param theEar the EAR archive from which to extract the test context
     * @param theWebUri the WAR URI of the WAR file in the EAR from which to
     *        extract the test context
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    protected static final String parseTestContext(EarArchive theEar, 
        String theWebUri) 
        throws ParserConfigurationException, IOException, SAXException
    {
        String context = theEar.getApplicationXml()
            .getWebModuleContextRoot(theWebUri);
        if (context == null)
        {
            // The application.xml does not define a <context-root> element.
            // We will thus assume that the root context is the webUri
            // retrieved above minus the extension.
            context = theWebUri.substring(0, theWebUri.length() - 4);
        }

        // Remove leading "/" if there is one.
        if (context.startsWith("/"))
        {
            context = context.substring(1);
        }

        return context;
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
     * @param theEar the EAR archive from which to extract the web URI 
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    protected static final String getUriOfCactifiedWebModule(EarArchive theEar)
        throws SAXException, IOException, ParserConfigurationException
    {
        ApplicationXml applicationXml = theEar.getApplicationXml();
        for (Iterator i = applicationXml.getWebModuleUris(); i.hasNext();)
        {
            String webUri = (String) i.next();
            WarArchive war = theEar.getWebModule(webUri);
            if ((war != null) 
                && (WarParser.parseServletRedirectorMapping(war) != null))
            {
                return webUri;
            }
        }
        return null;
    }
}