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
package org.apache.cactus.integration.ant.deployment;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.deployment.webapp.DefaultWarArchive;
import org.apache.cactus.integration.ant.deployment.webapp.WarArchive;
import org.apache.tools.ant.BuildException;
import org.xml.sax.SAXException;

/**
 * Parse an WAR descriptor to extract meaninful information for Cactus,
 * the results being stored in a {@link WarDeployableFile} object. 
 * 
 * @since Cactus 1.5
 * @version $Id$
 */
public class WarParser
{
    /**
     * Parse an WAR descriptor to extract meaninful information for Cactus.
     * 
     * @param theDeployableFile the file to parse and deploy
     * @return the parse results as a {@link WarDeployableFile} object
     */
    public static final WarDeployableFile parse(File theDeployableFile)
    {
        WarDeployableFile deployable = new WarDeployableFile();

        try
        {
            deployable.setFile(theDeployableFile);
            deployable.setWarArchive(new DefaultWarArchive(theDeployableFile));
            deployable.setTestContext(parseWebContext(theDeployableFile));
            deployable.setServletRedirectorMapping(
                parseServletRedirectorMapping(deployable.getWarArchive()));
            deployable.setFilterRedirectorMapping(
                parseFilterRedirectorMapping(deployable.getWarArchive()));
            deployable.setJspRedirectorMapping(
                parseJspRedirectorMapping(deployable.getWarArchive()));
        }
        catch (IOException e)
        {
            throw new BuildException("Failed to parse deployment descriptor "
                + "for WAR file [" + theDeployableFile + "].", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new BuildException("Failed to parse deployment descriptor "
                + "for WAR file [" + theDeployableFile + "].", e);
        }
        catch (SAXException e)
        {
            throw new BuildException("Failed to parse deployment descriptor "
                + "for WAR file [" + theDeployableFile + "].", e);
        }
        
        return deployable;
    }   

    /**
     * @param theDeployableFile the file to parse and deploy
     * @return the test context that will be used to verify if the container
     *         is started or not
     */
    protected static String parseWebContext(File theDeployableFile)
    {
        String context = theDeployableFile.getName();
        int warIndex = context.toLowerCase().lastIndexOf(".war");
        if (warIndex >= 0)
        {
            context = context.substring(0, warIndex);
        }        
        return context;
    }
    
    /**
     * Find the first URL-pattern to which the Cactus servlet redirector is 
     * mapped in the deployment descriptor.
     *
     * @return the servlet redirector mapping if found or <code>null</code>
     *         if not found
     * @param theWar the WAR descriptor that is parsed when looking for
     *        a Cactus servlet redirector mapping  
     * @throws IOException If there was a problem reading the deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    static String parseServletRedirectorMapping(WarArchive theWar)
        throws SAXException, IOException, ParserConfigurationException
    {
        Iterator servletNames = theWar.getWebXml().getServletNamesForClass(
            "org.apache.cactus.server.ServletTestRedirector");
        if (servletNames.hasNext())
        {
            // we only care about the first definition and the first mapping
            String name = (String) servletNames.next(); 
            Iterator mappings = theWar.getWebXml().getServletMappings(name);
            if (mappings.hasNext())
            {
                return (String) mappings.next();
            }
        }        
        return null;
    }

    /**
     * Find the first URL-pattern to which the Cactus filter redirector is 
     * mapped in the deployment descriptor.
     * 
     * @return the filter redirector mapping if found or <code>null</code>
     *         if not found
     * @param theWar the WAR descriptor that is parsed when looking for
     *        a Cactus filter redirector mapping  
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    static String parseFilterRedirectorMapping(WarArchive theWar)
        throws IOException, SAXException, ParserConfigurationException
    {
        Iterator filterNames = theWar.getWebXml().getFilterNamesForClass(
            "org.apache.cactus.server.FilterTestRedirector");
        if (filterNames.hasNext())
        {
            // we only care about the first definition and the first mapping
            String name = (String) filterNames.next(); 
            Iterator mappings = theWar.getWebXml().getFilterMappings(name);
            if (mappings.hasNext())
            {
                return (String) mappings.next();
            }
        }
        return null;
    }

    /**
     * Find the first URL-pattern to which the Cactus JSP redirector is 
     * mapped in the deployment descriptor.
     * 
     * @return the JSP redirector mapping if found or <code>null</code>
     *         if not found
     * @param theWar the WAR descriptor that is parsed when looking for
     *        a Cactus JSP redirector mapping  
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    static String parseJspRedirectorMapping(WarArchive theWar)
        throws IOException, SAXException, ParserConfigurationException
    {
        // To get the JSP redirector mapping, we must first get the full path to
        // the corresponding JSP file in the WAR
        String jspRedirectorPath = theWar.findResource("jspRedirector.jsp");
        if (jspRedirectorPath != null)
        {
            jspRedirectorPath = "/" + jspRedirectorPath;
            Iterator jspNames = theWar.getWebXml().getServletNamesForJspFile(
                jspRedirectorPath);
            if (jspNames.hasNext())
            {
                // we only care about the first definition and the first
                // mapping
                String name = (String) jspNames.next(); 
                Iterator mappings = 
                    theWar.getWebXml().getServletMappings(name);
                if (mappings.hasNext())
                {
                    return (String) mappings.next();
                }
            }
        }
        return null;
    }
}
