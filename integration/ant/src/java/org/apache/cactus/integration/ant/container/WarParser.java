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

import org.apache.cactus.integration.ant.deployment.DefaultWarArchive;
import org.apache.cactus.integration.ant.deployment.WarArchive;
import org.apache.tools.ant.BuildException;
import org.xml.sax.SAXException;

/**
 * Parse an WAR descriptor to extract meaninful information for Cactus,
 * the results being stored in a {@link WarDeployableFile} object. 
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
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
