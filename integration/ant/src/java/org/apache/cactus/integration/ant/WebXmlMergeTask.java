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
package org.apache.cactus.integration.ant;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.util.AntLog;
import org.apache.cactus.integration.ant.webxml.WebXml;
import org.apache.cactus.integration.ant.webxml.WebXmlIo;
import org.apache.cactus.integration.ant.webxml.WebXmlMerger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.XMLCatalog;
import org.xml.sax.SAXException;

/**
 * Ant task that can merge the definitions from two web deployment descriptors
 * into one descriptor.
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public class WebXmlMergeTask extends Task
{
    
    // Instance Variables ------------------------------------------------------
    
    /**
     * Location of the original <code>web.xml</code>
     */
    private File srcFile;  

    /**
     * Location of the overriding <code>web.xml</code>
     */
    private File mergeFile;  

    /**
     * Location of the resulting <code>web.xml</code>
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
                    WebXml srcWebXml =
                        WebXmlIo.parseWebXmlFromFile(this.srcFile, this.xmlCatalog);
                    WebXml mergeWebXml =
                        WebXmlIo.parseWebXmlFromFile(this.mergeFile, this.xmlCatalog);
                    WebXmlMerger merger = new WebXmlMerger(srcWebXml);
                    merger.setLog(new AntLog(this));
                    merger.merge(mergeWebXml);
                    WebXmlIo.writeWebXml(srcWebXml, this.destFile);
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
