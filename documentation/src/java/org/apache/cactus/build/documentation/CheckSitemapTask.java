/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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
package org.apache.cactus.build.documentation;

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
     * The output directory where files are generated.
     */
    private File outputDir;

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
     * @param theOutputDir the location of the output directory where files are
     * generated
     */
    public void setOutputDir(File theOutputDir)
    {
        this.outputDir = theOutputDir;
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
        if (this.outputDir == null)
        {
            throw new BuildException("The [outputDir] attribute must be set");
        }

        if (!this.sitemap.exists())
        {
            throw new BuildException(
                "The [sitemap] attribute must point to an existing file");
        }
        
        log("Checking sitemap at [" + sitemap + "]", Project.MSG_INFO);
        log("Generated doc output directory is [" + outputDir + "]", 
            Project.MSG_VERBOSE);
               
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
        boolean isResourcePresent = true;       
        String id = theElement.getAttribute("id");

        if (isResourceToBeChecked(theElement))
        {
            String target = theElement.getAttribute("target");
                             
            if ((target == null) || (target.length() == 0))
            {
                log("Skipping remote resource [" + id + "]", 
                    Project.MSG_VERBOSE);
            }
            else
            {
                isResourcePresent = checkLocalSitemapResource(id, target);
            }
        }
        else
        {
            log("This resource should not be checked: [" + id + "]", 
                Project.MSG_VERBOSE);
        }
        return isResourcePresent;
    }

    /**
     * @param theElement the resource for which to verify if it is to be checked
     * for existence
     * @return true if the resource must be checked or false otherwise
     */
    private boolean isResourceToBeChecked(Element theElement)
    {
        // Checks are enabled by default 
        boolean isToBeChecked = true;

        if ((theElement.getAttribute("check") != null) 
            && (theElement.getAttribute("check").length() > 0))
        {
            isToBeChecked = Boolean.valueOf(
                theElement.getAttribute("check")).booleanValue();
        }
        return isToBeChecked;            
    }
    
    /**
     * Checks whether a specified local sitemap resource points to an existing
     * file.
     * 
     * @param theId The <code>id</code> attribute of the resource element
     * @param theTarget The <code>target</code> attribute of the resource
     * element, the relative path from the directory containing the sitemap file
     * to the generated file
     * @return Whether the file exists
     */
    private boolean checkLocalSitemapResource(String theId, String theTarget)
    {
        File targetFile = new File(this.outputDir, theTarget);
        log("Checking resource [" + theId + "] at [" + theTarget + "]",
            Project.MSG_DEBUG);
        if (!targetFile.exists())
        {
            log("Sitemap resource [" + theId + "] not found under [" 
                + targetFile + "]", Project.MSG_ERR);
            return false;
        }
        return true;
    }
    
}
