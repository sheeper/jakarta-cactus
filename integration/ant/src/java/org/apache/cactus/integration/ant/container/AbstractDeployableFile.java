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

import org.apache.cactus.integration.ant.deployment.WarArchive;
import org.apache.tools.ant.BuildException;
import org.xml.sax.SAXException;

/**
 * Logic common to all deployable implementations (WAR and EAR
 * deployments).  
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public abstract class AbstractDeployableFile implements DeployableFile
{
    /**
     * The WAR or EAR file to deploy.
     */
    protected File deployableFile;

    /**
     * WAR deployment descriptor as a java object. In case of an EAR,
     * it is the first WAR containing the Cactus Servlet redirector.
     */
    protected WarArchive warArchive;

    /**
     * Webapp context path containing the Cactus tests
     */
    protected String contextPath;
        
    /**
     * Servlet mapping of the Cactus Servlet redirector found
     * in the warArchive WAR.
     */
    protected String servletRedirectorMapping;

    /**
     * Filter mapping of the Cactus Filter redirector found
     * in the warArchive WAR.
     */
    protected String filterRedirectorMapping;

    /**
     * JSP mapping of the Cactus JSP redirector found
     * in the warArchive WAR.
     */
    protected String jspRedirectorMapping;

    /**
     * @param theDeployableFile the WAR/EAR file to deploy in a container
     * @exception BuildException on parsing error
     */
    public AbstractDeployableFile(File theDeployableFile) throws BuildException
    {
        this.deployableFile = theDeployableFile;        

        try
        {
            parse();        
            parseServletRedirectorMapping();
            parseFilterRedirectorMapping();
            parseJspRedirectorMapping();
        }
        catch (SAXException e)
        {
            throw new BuildException(
                "Parsing of deployment descriptor failed", e);
        }
        catch (IOException e)
        {
            throw new BuildException("Failed to open archive", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new BuildException("XML parser configuration error", e);
        }
    }

    /**
     * @see DeployableFile#getFile()
     */
    public final File getFile()
    {
        return this.deployableFile;
    }

    /**
     * @see DeployableFile#getTestContext()
     */
    public final String getTestContext()
    {
        return this.contextPath;
    }

    /**
     * @see DeployableFile#getServletRedirectorMapping()
     */
    public final String getServletRedirectorMapping()
    {
        return this.servletRedirectorMapping;
    }

    /**
     * @see DeployableFile#getFilterRedirectorMapping()
     */
    public final String getFilterRedirectorMapping()
    {
        return this.filterRedirectorMapping;
    }

    /**
     * @see DeployableFile#getJspRedirectorMapping()
     */
    public final String getJspRedirectorMapping()
    {
        return this.jspRedirectorMapping;
    }

    /**
     * @see DeployableFile#getWarArchive()
     */
    public final WarArchive getWarArchive()
    {
        return this.warArchive;
    }
    
    /**
     * Parse the WAR/EAR deployment descriptor to extract needed information.
     * 
     * @throws IOException If there was a problem reading the deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    protected abstract void parse() 
        throws IOException, SAXException, ParserConfigurationException;

    /**
     * @see #parseServletRedirectorMapping(WarArchive)
     */
    protected void parseServletRedirectorMapping()
        throws SAXException, IOException, ParserConfigurationException
    {
        this.servletRedirectorMapping =
            parseServletRedirectorMapping(this.warArchive);
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
    protected String parseServletRedirectorMapping(WarArchive theWar)
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
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    private void parseFilterRedirectorMapping()
        throws IOException, SAXException, ParserConfigurationException
    {
        Iterator filterNames = 
            this.warArchive.getWebXml().getFilterNamesForClass(
            "org.apache.cactus.server.FilterTestRedirector");
        if (filterNames.hasNext())
        {
            // we only care about the first definition and the first mapping
            String name = (String) filterNames.next(); 
            Iterator mappings = 
                this.warArchive.getWebXml().getFilterMappings(name);
            if (mappings.hasNext())
            {
                this.filterRedirectorMapping = (String) mappings.next();
                return;
            }
        }
        this.filterRedirectorMapping = null;
    }

    /**
     * Find the first URL-pattern to which the Cactus JSP redirector is 
     * mapped in the deployment descriptor.
     * 
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    private void parseJspRedirectorMapping()
        throws IOException, SAXException, ParserConfigurationException
    {
        // To get the JSP redirector mapping, we must first get the full path to
        // the corresponding JSP file in the WAR
        String jspRedirectorPath = 
            this.warArchive.findResource("jspRedirector.jsp");
        if (jspRedirectorPath != null)
        {
            jspRedirectorPath = "/" + jspRedirectorPath;
            Iterator jspNames = 
                this.warArchive.getWebXml().getServletNamesForJspFile(
                jspRedirectorPath);
            if (jspNames.hasNext())
            {
                // we only care about the first definition and the first
                // mapping
                String name = (String) jspNames.next(); 
                Iterator mappings = 
                    this.warArchive.getWebXml().getServletMappings(name);
                if (mappings.hasNext())
                {
                    this.jspRedirectorMapping = (String) mappings.next();
                    return;
                }
            }
        }
        this.jspRedirectorMapping = null;
    }
}
