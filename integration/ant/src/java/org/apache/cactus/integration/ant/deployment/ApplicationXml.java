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
package org.apache.cactus.integration.ant.deployment;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates the DOM representation of an EAR descriptor 
 * (<code>application.xml</code>) to provide convenience methods for easy 
 * access and manipulation.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public interface ApplicationXml
{
    /**
     * Returns the DOM document representing the deployment descriptor. The 
     * document will contain any modifications made through this instance.
     * 
     * @return The document representing the deploy descriptor
     */
    Document getDocument();

    /**
     * Returns the J2EE API version.
     * 
     * @return The version
     */
    ApplicationXmlVersion getVersion();

    /**
     * Returns the element that contains the definition of a specific web
     * module, or <code>null</code> if a web module with the specified web-uri
     * is not defined.
     * 
     * @param theWebUri The uri of the web module
     * @return The DOM element representing the web module definition
     */
    Element getWebModule(String theWebUri);
    
    /**
     * Returns the context root of the the specified web module.
     * 
     * @param theWebUri The uri of the web module
     * @return The context root of the web module
     */
    String getWebModuleContextRoot(String theWebUri);
    
    /**
     * Returns an iterator over the URIs of the web modules defined in the 
     * descriptor.
     * 
     * @return An iterator over the URIs of the web modules
     */
    Iterator getWebModuleUris();
    
    /**
     * Returns an iterator over the elements that match the specified tag.
     * 
     * @param theTag The descriptor tag of which the elements should be
     *        returned
     * @return An iterator over the elements matching the tag, in the order 
     *         they occur in the descriptor
     */
    Iterator getElements(ApplicationXmlTag theTag);
}
