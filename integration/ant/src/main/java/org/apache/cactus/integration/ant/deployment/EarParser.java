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
package org.apache.cactus.integration.ant.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.BuildException;
import org.codehaus.cargo.module.application.ApplicationXml;
import org.codehaus.cargo.module.application.DefaultEarArchive;
import org.codehaus.cargo.module.application.EarArchive;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.xml.sax.SAXException;

/**
 * Parse an EAR descriptor to extract meaninful information for Cactus,
 * the results being stored in a {@link EarDeployableFile} object. 
 * 
 * @since Cactus 1.5
 * @version $Id: EarParser.java 239003 2004-05-31 20:05:27Z vmassol $
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

            EarArchive earArchive = new DefaultEarArchive(
                new FileInputStream(theDeployableFile));
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
            // This is wrong!
            throw new BuildException("Your application.xml must define a "
                + "<context-root> element in the <web> module definition.");
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
