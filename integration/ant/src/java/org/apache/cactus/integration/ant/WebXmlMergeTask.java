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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.webxml.WebXml;
import org.apache.cactus.integration.ant.webxml.WebXmlMerger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.XMLCatalog;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.SAXException;

/**
 * Ant task that can merge the definitions from two web deployment descriptors
 * into one descriptor.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
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
    private XMLCatalog xmlCatalog = new XMLCatalog();

    /**
     * The factory for JAXP document builders.
     */
    private DocumentBuilderFactory factory;

    // Public Methods ----------------------------------------------------------
    
    /**
     * @see org.apache.tools.ant.Task#init()
     */
    public void init() throws BuildException
    {
        this.factory = DocumentBuilderFactory.newInstance();
        this.factory.setValidating(false);
        this.factory.setNamespaceAware(false);
    
        this.xmlCatalog.setProject(project);
    }

    /**
     * @see Task#execute()
     */
    public void execute() throws BuildException
    {
        if (this.srcFile == null)
        {
            throw new BuildException("The [srcfile] attribute is required");
        }
        if (this.destFile == null)
        {
            throw new BuildException("The [destfile] attribute is required");
        }
        
        // FIXME: Skip merge if destfile newer than srcfile and mergefile
        try
        {
            WebXml srcWebXml = parseWebXml(this.srcFile);
            if (this.mergeFile != null)
            {
                WebXml mergeWebXml = parseWebXml(this.mergeFile);
                checkServletVersions(srcWebXml, mergeWebXml);
                merge(srcWebXml, mergeWebXml);
            }
            writeWebXml(srcWebXml, this.destFile);
        }
        catch (ParserConfigurationException pce)
        {
            throw new BuildException("XML parser configuration problem", pce);
        }
        catch (IOException ioe)
        {
            throw new BuildException("An I/O error occurred", ioe);
        }
    }

    /**
     * Adds an XML catalog to the internal catalog.
     *
     * @param theXmlCatalog the XMLCatalog instance to use to look up DTDs
     */
    public void addConfiguredXMLCatalog(XMLCatalog theXmlCatalog)
    {
        this.xmlCatalog.addConfiguredXMLCatalog(theXmlCatalog);
    }

    /**
     * The original web deployment descriptor into which the new elements will
     * be merged.
     * 
     * @param theSrcFile the original <code>web.xml</code>
     */
    public void setSrcFile(File theSrcFile)
    {
        this.srcFile = theSrcFile;
    }

    /**
     * The descriptor to merge into the original file.
     * 
     * @param theMergeFile the <code>web.xml</code> to merge
     */
    public void setMergeFile(File theMergeFile)
    {
        this.mergeFile = theMergeFile;
    }

    /**
     * The destination file where the result of the merge are stored.
     * 
     * @param theDestFile the resulting <code>web.xml</code>
     */
    public void setDestFile(File theDestFile)
    {
        this.destFile = theDestFile;
    }
    
    /**
     * Sets the encoding of the resulting XML file. Default is 'UTF-8'.
     * 
     * @param theEncoding The encoding to set
     */
    public void setEncoding(String theEncoding)
    {
        this.encoding = theEncoding;
    }

    /**
     * Whether the result XML file should be indented for better readability.
     * Default is 'false'.
     *  
     * @param theIndent Whether the result should be indented
     */
    public void setIndent(boolean theIndent)
    {
        this.indent = theIndent;
    }

    // Private Methods ---------------------------------------------------------
    
    /**
     * Checks the versions of the servlet API in each descriptor, and logs
     * warnings if a mismatch might result in loss of definitions.
     * 
     * @param theSrcWebXml The source descriptor
     * @param theMergeWebXml The descriptor to merge
     * @throws BuildException If the versions are incompatible
     */
    private void checkServletVersions(WebXml theSrcWebXml,
        WebXml theMergeWebXml) throws BuildException
    {
        String srcVersion = theSrcWebXml.getVersion();
        String mergeVersion = theMergeWebXml.getVersion();
        if (srcVersion != mergeVersion)
        {
            if (WebXml.SERVLET_VERSION_2_2.equals(srcVersion) &&
                WebXml.SERVLET_VERSION_2_3.equals(mergeVersion))
            {
                log("Merging elements from a version 2.3 into a version 2.2 "
                    + "descriptor, some elements may be skipped");
            }
        }
    }

    /**
     * Merges the merge descriptor with the original descriptor. 
     * 
     * @param theSrcWebXml The original descriptor
     * @param theMergeWebXml The descriptor to merge in
     * @throws BuildException If the operation fails
     */
    private void merge(WebXml theSrcWebXml, WebXml theMergeWebXml)
    {
        WebXmlMerger merger = new WebXmlMerger(theSrcWebXml);
        if (WebXml.SERVLET_VERSION_2_3.equals(theSrcWebXml.getVersion()))
        {
            int filtersMerged = merger.mergeFilters(theMergeWebXml);
            if (filtersMerged > 0)
            {
                log("Merged " + filtersMerged + " filter definitions into the "
                    + "descriptor", Project.MSG_INFO);
            }
        }
        int servletsMerged = merger.mergeServlets(theMergeWebXml);
        if (servletsMerged > 0)
        {
            log("Merged " + servletsMerged + " servlet definitions into the "
                + "descriptor", Project.MSG_INFO);
        }
    }
    
    /**
     * Parses a deployment descriptor.
     * 
     * @param theFile The file to parse
     * @return The parsed document
     * @throws BuildException If the file could not be parsed
     * @throws ParserConfigurationException If the XML parser was not correctly
     *          configured
     * @throws IOException If an I/O error occurs
     */
    private WebXml parseWebXml(File theFile)
        throws BuildException, ParserConfigurationException, IOException
    {
        FileInputStream in = null;
        try
        {
            log("Parsing file [" + theFile + "]", Project.MSG_VERBOSE);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(this.xmlCatalog);
            in = new FileInputStream(theFile);
            return new WebXml(builder.parse(in));
        }
        catch (SAXException saxe)
        {
            throw new BuildException("Error parsing file ["
                + theFile + "]: " + saxe.getMessage(), saxe);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ioe)
                {
                    // we'll pass on the original IO error, so ignore this one
                }
            }
        }
    }
    
    /**
     * Writes the specified document to an output stream.
     * 
     * @param theWebXml The descriptor to serialize
     * @param theFile The file to write to
     * @throws IOException If an I/O error occurs
     */
    protected void writeWebXml(WebXml theWebXml, File theFile)
        throws IOException
    {
        FileOutputStream out = null;
        try
        {
            log("Writing to file [" + theFile + "]", Project.MSG_VERBOSE);
            out = new FileOutputStream(theFile);
            OutputFormat outputFormat =
                new OutputFormat(theWebXml.getDocument());
            if (this.encoding != null)
            {
                outputFormat.setEncoding(this.encoding);
            }
            outputFormat.setIndenting(this.indent);
            outputFormat.setPreserveSpace(false);
            XMLSerializer serializer = new XMLSerializer(out, outputFormat);
            serializer.serialize(theWebXml.getDocument());
            System.out.println();
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException ioe)
                {
                    // we'll pass on the original IO error, so ignore this one
                }
            }
        }
    }

}
