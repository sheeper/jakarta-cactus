/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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

/**
 * Represents the various top-level tags in a enterprise application deployment
 * descriptor as a typesafe enumeration.
 * 
 * @since Cactus 1.5
 * @version $Id$
 */
public final class ApplicationXmlTag
{
    
    // Public Constants --------------------------------------------------------
    
    /**
     * Element name 'icon'.
     */
    public static final ApplicationXmlTag ICON =
        new ApplicationXmlTag("icon");
    
    /**
     * Element name 'display-name'.
     */
    public static final ApplicationXmlTag DISPLAY_NAME =
        new ApplicationXmlTag("display-name");
    
    /**
     * Element name 'description'.
     */
    public static final ApplicationXmlTag DESCRIPTION =
        new ApplicationXmlTag("description");
    
    /**
     * Element name 'module'.
     */
    public static final ApplicationXmlTag MODULE =
        new ApplicationXmlTag("module");
    
    /**
     * Element name 'web',
     */
    public static final ApplicationXmlTag WEB =
        new ApplicationXmlTag("web");
    
    /**
     * Element name 'web-uri',
     */
    public static final ApplicationXmlTag WEB_URI =
        new ApplicationXmlTag("web-uri");
    
    /**
     * Element name 'context-root',
     */
    public static final ApplicationXmlTag CONTEXT_ROOT =
        new ApplicationXmlTag("context-root");
    
    // Instance Variables ------------------------------------------------------
    
    /**
     * The tag name,
     */
    private String tagName;
    
    // Constructors ------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @param theTagName The tag name of the element
     */
    private ApplicationXmlTag(String theTagName)
    {
        this.tagName = theTagName;
    }

    // Public Methods ----------------------------------------------------------
    
    /**
     * @see java.lang.Object#toString
     */
    public boolean equals(Object theOther)
    {
        return super.equals(theOther);
    }
    
    /**
     * @see java.lang.Object#hashCode
     */
    public int hashCode()
    {
        return super.hashCode();
    }
    
    /**
     * Returns the tag name.
     * 
     * @return The tag name
     */
    public String getTagName()
    {
        return this.tagName;
    }
    
    /**
     * @see java.lang.Object#toString
     */
    public String toString()
    {
        return getTagName();
    }
    
}
