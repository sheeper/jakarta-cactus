/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
package org.apache.cactus.documentation;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.XMLCatalog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Checks the sitemap for invalid resource links (currently only local).
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class CheckSitemapTask extends Task
{
    /**
     * Location of the sitemap.xml file.
     */
    private File sitemap;

    /**
     * Whether the task should fail when an error occurred.
     */
    private boolean failOnError = true;

    /**
     * The directory in which the sitemap is located.
     */
    private File sitemapDir;

    /**
     * For resolving entities such as DTDs.
     */
    private XMLCatalog xmlCatalog = new XMLCatalog();

    /**
     * Sets the location of the sitemap file.
     * 
     * @param theSitemap The sitemap file
     */
    public void setSitemap(File theSitemap)
    {
        this.sitemap = theSitemap;
    }

    /**
     * Sets whether the task should fail when an error occurs.
     *
     * @param theFailOnError Whether to fail on errors
     */
    public void setFailOnError(boolean theFailOnError)
    {
        this.failOnError = theFailOnError;
    }

    /**
     * Add the catalog to our internal catalog
     *
     * @param theXmlCatalog the XMLCatalog instance to use to look up DTDs
     */
    public void addConfiguredXMLCatalog(XMLCatalog theXmlCatalog)
    {
        this.xmlCatalog.addConfiguredXMLCatalog(theXmlCatalog);
    }

    /**
     * @see Task#init()
     */
    public void init() throws BuildException
    {
        super.init();
        // Initialize internal instance of XMLCatalog
        this.xmlCatalog.setProject(getProject());
    }

    /**
     * Execute task.
     *
     * @see Task#execute()
     */
    public void execute() throws BuildException
    {
        if (this.sitemap == null)
        {
            throw new BuildException("The [sitemap] attribute must be set");
        }
        if (!this.sitemap.exists())
        {
            throw new BuildException(
                "The [sitemap] attribute must point to an existing file");
        }
        
        log("Checking sitemap at " + sitemap + "", Project.MSG_INFO);
        
        this.sitemapDir = sitemap.getParentFile();
        
        try
        {
            DocumentBuilder builder = getDocumentBuilder();
            builder.setEntityResolver(this.xmlCatalog);
            Document document = builder.parse(sitemap);
            if (!checkSitemap(document) && (this.failOnError))
            {
                throw new BuildException("Found errors in sitemap.");
            }
        }
        catch (SAXException e)
        {
            throw new BuildException(e);
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
        catch (ParserConfigurationException e)
        {
            throw new BuildException(e);
        }
    }
    
    /**
     * Instantiates and returns a JAXP DocumentBuilder.
     * 
     * @return The DocumentBuilder instance 
     * @throws ParserConfigurationException If instantiating the builder threw
     *         a configuration exception 
     */
    private DocumentBuilder getDocumentBuilder()
        throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        return factory.newDocumentBuilder();
    }
    
    /**
     * Checks the sitemap for valid links.
     * 
     * @param theDocument The DOM representation of the Sitemap.
     * @return Whether the check was successful
     */
    private boolean checkSitemap(Document theDocument)
    {
        Element sitemap =
            (Element) theDocument.getElementsByTagName("sitemap").item(0);
        NodeList resources = sitemap.getElementsByTagName("resource");
        boolean success = true;
        for (int i = 0; i < resources.getLength(); i++)
        {
            Element resource = (Element) resources.item(i);
            if (!checkSitemapResource(resource))
            {
                success = false;
            }
        }
        return success;
    }
    
    /**
     * Checks a single resource in a sitemap.
     * 
     * @param theElement The resource element
     * @return Whether the check was successful
     */
    private boolean checkSitemapResource(Element theElement)
    {
        String id = theElement.getAttribute("id");
        String source = theElement.getAttribute("source");
        if ((source == null) || (source.length() == 0))
        {
            log("Skipping remote resource [" + id + "]", Project.MSG_VERBOSE);
        }
        else
        {
            checkLocalSitemapResource(id, source);
        }
        return true;
    }
    
    /**
     * Checks whether a specified local sitemap resource points to an existing
     * file.
     * 
     * @param theId The <code>id</code> attribute of the resource element
     * @param theSource The <code>source</code> attribute of the resource
     * element, the relative path from the directory containing the sitemap file
     * to the resource file
     * @return Whether the file exists
     */
    private boolean checkLocalSitemapResource(String theId, String theSource)
    {
        File sourceFile = new File(sitemapDir, theSource);
        log("Checking resource [" + theId + "] at [" + theSource + "]",
            Project.MSG_DEBUG);
        if (!sourceFile.exists())
        {
            log("Sitemap resource [" + theId + "] not found under [" 
                + theSource + "]", Project.MSG_ERR);
            return false;
        }
        return true;
    }
    
}
