/* 
 * ========================================================================
 * 
 * Copyright 2003-2005 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.deployment.application;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates the DOM representation of an EAR descriptor 
 * (<code>application.xml</code>) to provide convenience methods for easy 
 * access and manipulation.
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
    
    /**
     * Adds a web module to the deployment descriptor
     * 
     * @param theUri the uri of the new module
     * @param theContext the context of the new module
     */
    void addWebModule(String theUri, String theContext);
}
