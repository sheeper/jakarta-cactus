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
package org.apache.cactus.integration.ant;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.XMLCatalog;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlMerger;
import org.codehaus.cargo.util.log.AntLogger;
import org.xml.sax.SAXException;

/**
 * Ant task that can merge the definitions from two web deployment descriptors
 * into one descriptor.
 * 
 * @since Cactus 1.5
 * @version $Id: WebXmlMergeTask.java 394252 2006-04-15 04:20:17Z felipeal $
 */
public class WebXmlMergeTask extends Task
{
    
    // Instance Variables ------------------------------------------------------
    
    /**
     * Location of the original <code>web.xml</code>.
     */
    private File srcFile;  

    /**
     * Location of the overriding <code>web.xml</code>.
     */
    private File mergeFile;  

    /**
     * Location of the resulting <code>web.xml</code>.
     */
    private File destFile;

    /**
     * Whether the merge should be performed even when the destination file is
     * up to date.
     */
    private boolean force = false;
    
    /**
     * Whether the resulting XML file should be indented.
     */
    private boolean indent = false;
    
    /**
     * The encoding of the resulting XML file.
     */
    private String encoding;

    /**
     * For resolving entities such as DTDs.
     */
    private XMLCatalog xmlCatalog = null;

    // Public Methods ----------------------------------------------------------
    
    /**
     * {@inheritDoc}
     * @see Task#execute()
     */
    public void execute() throws BuildException
    {
        if ((this.srcFile == null) || !this.srcFile.isFile())
        {
            throw new BuildException("The [srcfile] attribute is required");
        }
        if (this.destFile == null)
        {
            throw new BuildException("The [destfile] attribute is required");
        }
        
        try
        {
            if (this.mergeFile != null)
            {
                if (!this.mergeFile.isFile())
                {
                    throw new BuildException("The merge file doesn't exist");
                }
                if (force
                 || (srcFile.lastModified() > destFile.lastModified())
                 || (mergeFile.lastModified() > destFile.lastModified()))
                {
                    WebXml srcWebXml = WebXmlIo.parseWebXmlFromFile(
                        this.srcFile, this.xmlCatalog);
                    WebXml mergeWebXml = WebXmlIo.parseWebXmlFromFile(
                        this.mergeFile, this.xmlCatalog);
                    WebXmlMerger merger = new WebXmlMerger(srcWebXml);
                    merger.setLogger(new AntLogger(this));
                    merger.merge(mergeWebXml);
                    WebXmlIo.writeDescriptor(srcWebXml, this.destFile,
                        this.encoding, this.indent);
                }
                else
                {
                    log("The destination file is up to date",
                        Project.MSG_VERBOSE);
                }
            }
            else
            {
                throw new BuildException("The [mergefile] attribute is "
                    + "required");
            }
        }
        catch (ParserConfigurationException pce)
        {
            throw new BuildException("XML parser configuration problem: "
                + pce.getMessage(), pce);
        }
        catch (SAXException saxe)
        {
            throw new BuildException("Failed to parse descriptor: "
                + saxe.getMessage(), saxe);
        }
        catch (IOException ioe)
        {
            throw new BuildException("An I/O error occurred: "
                + ioe.getMessage(), ioe);
        }
    }

    /**
     * Adds an XML catalog to the internal catalog.
     *
     * @param theXmlCatalog the XMLCatalog instance to use to look up DTDs
     */
    public final void addConfiguredXMLCatalog(XMLCatalog theXmlCatalog)
    {
        if (this.xmlCatalog == null)
        {
            this.xmlCatalog = new XMLCatalog();
            this.xmlCatalog.setProject(getProject());
        }
        this.xmlCatalog.addConfiguredXMLCatalog(theXmlCatalog);
    }

    /**
     * The original web deployment descriptor into which the new elements will
     * be merged.
     * 
     * @param theSrcFile the original <code>web.xml</code>
     */
    public final void setSrcFile(File theSrcFile)
    {
        this.srcFile = theSrcFile;
    }

    /**
     * The descriptor to merge into the original file.
     * 
     * @param theMergeFile the <code>web.xml</code> to merge
     */
    public final void setMergeFile(File theMergeFile)
    {
        this.mergeFile = theMergeFile;
    }

    /**
     * The destination file where the result of the merge are stored.
     * 
     * @param theDestFile the resulting <code>web.xml</code>
     */
    public final void setDestFile(File theDestFile)
    {
        this.destFile = theDestFile;
    }
    
    /**
     * Sets whether the merge should be performed even when the destination 
     * file is up to date.
     * 
     * @param isForce Whether the merge should be forced
     */
    public final void setForce(boolean isForce)
    {
        this.force = isForce;
    }

    /**
     * Sets the encoding of the resulting XML file. Default is 'UTF-8'.
     * 
     * @param theEncoding The encoding to set
     */
    public final void setEncoding(String theEncoding)
    {
        this.encoding = theEncoding;
    }

    /**
     * Whether the result XML file should be indented for better readability.
     * Default is 'false'.
     *  
     * @param isIndent Whether the result should be indented
     */
    public final void setIndent(boolean isIndent)
    {
        this.indent = isIndent;
    }

}
