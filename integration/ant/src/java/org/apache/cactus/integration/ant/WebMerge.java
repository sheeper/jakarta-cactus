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
package org.apache.cactus.integration.ant;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Merges 2 <code>web.xml</code> files.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class WebMerge
{
    /**
     * Location of the original <code>web.xml</code>
     */
    private File original;  

    /**
     * Location of the overriding <code>web.xml</code>
     */
    private File override;  

    /**
     * Location of the resulting <code>web.xml</code>
     */
    private File output;

    /**
     * Noop entity resolver so that <code>DOCTYPE</code> entries in XML files
     * do not cause any exception when we are offline and the entity to 
     * resolve is an external URI. 
     */
    class NoopEntityResolver implements EntityResolver
    {
        /**
         * @see EntityResolver#resolveEntity(String, String)
         */
        public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException
        {
            ByteArrayInputStream bais = 
                new ByteArrayInputStream("".getBytes()); 
            return new InputSource(bais);
        }        
    }

    /**
     * Perform the merge and save the resulting document
     * 
     * @exception Exception on failure
     */
    public void transform() throws Exception
    {
        Document originalDocument = 
            parse(new FileInputStream(this.original));
        Document overrideDocument =
            parse(new FileInputStream(this.override));

        Document resultDoc = merge(originalDocument, overrideDocument);
                
        FileOutputStream fos = new FileOutputStream(this.output);

        OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
        of.setIndent(1);
        of.setIndenting(true);
    
        XMLSerializer serializer = new XMLSerializer(fos, of);
        serializer.asDOMSerializer();
        serializer.serialize(resultDoc);
        fos.close();
    }

    /**
     * Perform the merge.
     * 
     * @exception Exception on failure
     */
    public Document merge(Document theOriginalDoc, Document theOverrideDoc) 
        throws Exception
    {
        // Start from the original document and add elements from the
        // override document to its DOM.
        
        Element originalRoot = theOriginalDoc.getDocumentElement();
       
        Node currentNode = originalRoot.getFirstChild();

        // If there is no child of the main <web-app> element, then
        // the merged result is equal to the override document
        if (currentNode == null)
        {
            return theOverrideDoc;
        }
        
        // Add <servlet> elements
        currentNode = insertTag("servlet", currentNode, theOriginalDoc, 
            theOverrideDoc);

        // Add <servlet-mapping> elements
        currentNode = insertTag("servlet-mapping", currentNode, 
            theOriginalDoc, theOverrideDoc);
 
        return theOriginalDoc;       
    }

    /**
     * Inserts all theTagName elements from the override document into
     * the original document, before the specified node. 
     * 
     * @param theTagName the name of the tags to insert
     * @param theCurrentNode the node before which to insert all the tags
     * @param theOriginalDocument the original document
     * @param theOverrideDocument the override document
     */
    protected Node insertTag(String theTagName, Node theCurrentNode,
        Document theOriginalDocument, Document theOverrideDocument)
    {
        Node current = theCurrentNode;
        
        NodeList overrideNl = 
            theOverrideDocument.getElementsByTagName(theTagName);
        if (overrideNl.getLength() > 0)
        {
            NodeList originalNl = 
                theOriginalDocument.getElementsByTagName(theTagName);
            if (originalNl.getLength() > 0)
            {
                insertBefore(overrideNl, originalNl.item(0));
                current = theCurrentNode.getNextSibling();
            }
            else
            {
                insertBefore(overrideNl, theCurrentNode);
            }
        }

        return current;
    }
    
    /**
     * Inserts a node list to a node.
     * 
     * @param theNodeList the node list to insert
     * @param theNode the node before which the node list will be inserted
     * @return the first sibling node that was inserted or null if the
     *         node list is empty
     */
    protected Node insertBefore(NodeList theNodeList, Node theNode)
    {
        Node insertedNode = theNode;
        for (int i = theNodeList.getLength() - 1; i >= 0; i--)
        {
            Node importedNode = theNode.getOwnerDocument().importNode(
                theNodeList.item(i), true);
            insertedNode = theNode.getParentNode().insertBefore(
                importedNode, insertedNode);
        }

        return insertedNode; 
    }

    /**
     * @param theXmlStream the XML data to read
     * @return Document the corresponding DOM Document
     * @throws Exception if error
     */
    protected Document parse(InputStream theXmlStream) throws Exception
    {
        BufferedInputStream stream = new BufferedInputStream(theXmlStream);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);

        DocumentBuilder builder = factory.newDocumentBuilder(); 
        builder.setEntityResolver(new NoopEntityResolver());
        
        return builder.parse(stream);
    }

    /**
     * @param theOriginal the original <code>web.xml</code>
     */
    public void setOriginal(File theOriginal)
    {
        this.original = theOriginal;
    }

    /**
     * @param theOverride the override <code>web.xml</code>
     */
    public void setOverride(File theOverride)
    {
        this.override = theOverride;
    }

    /**
     * @param theOutput the resulting <code>web.xml</code>
     */
    public void setOutput(File theOutput)
    {
        this.output = theOutput;
    }
}
