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
package org.apache.cactus.integration.ant.deployment;

import org.w3c.dom.DocumentType;

/**
 * Enumerated type that represents the version of the web deployment descriptor.
 * 
 * @since Cactus 1.5
 * @version $Id$
 */
public final class WebXmlVersion implements Comparable
{

    // Public Constants --------------------------------------------------------

    /**
     * Instance for version 2.2.
     */
    public static final WebXmlVersion V2_2 = new WebXmlVersion("2.2",
        "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
        "http://java.sun.com/j2ee/dtds/web-app_2_2.dtd");

    /**
     * Instance for version 2.3.
     */
    public static final WebXmlVersion V2_3 = new WebXmlVersion("2.3",
        "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN",
        "http://java.sun.com/dtd/web-app_2_3.dtd");

    // Instance Variables ------------------------------------------------------

    /**
     * The version as strnig,
     */
    private String version;

    /**
     * The public ID of the corresponding document type.
     */
    private String publicId;

    /**
     * The system ID of the corresponding document type.
     */
    public String systemId;

    // Constructors ------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @param theVersion The version as string
     * @param thePublicId The public ID of the correspondig document type
     * @param theSystemId The system ID of the correspondig document type
     */
    private WebXmlVersion(String theVersion, String thePublicId,
        String theSystemId)
    {
        this.version = theVersion;
        this.publicId = thePublicId;
        this.systemId = theSystemId;
    }

    // Public Methods ----------------------------------------------------------
    
    /**
     * @see java.lang.Comparable#compareTo
     */
    public int compareTo(Object theOther)
    {
        if (theOther == this)
        {
            return 0;
        }
        WebXmlVersion otherVersion = (WebXmlVersion) theOther;
        if (otherVersion == V2_3)
        {
            return -1;
        }
        return 1;
    }
    
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
    public String getVersion()
    {
        return this.version;
    }
    
    /**
     * Returns the public ID of the document type corresponding to the 
     * descriptor version.
     * 
     * @return The public ID
     */
    public String getPublicId()
    {
        return publicId;
    }

    /**
     * Returns the system ID of the document type corresponding to the 
     * descriptor version.
     * 
     * @return The system ID
     */
    public String getSystemId()
    {
        return systemId;
    }

    /**
     * @see java.lang.Object#toString
     */
    public String toString()
    {
        return getVersion();
    }

    /**
     * Returns the version corresponding to the given document type.
     * 
     * @param theDocType The document type
     * @return The version that matches the document type, or <code>null</code>
     *         if the doctype is not recognized
     * @throws NullPointerException If the document type is <code>null</code>
     */
    public static WebXmlVersion valueOf(DocumentType theDocType)
        throws NullPointerException
    {
        return valueOf(theDocType.getPublicId());
    }

    /**
     * Returns the version corresponding to the given public ID.
     * 
     * @param thePublicId The public ID
     * @return The version that matches the public ID, or <code>null</code>
     *         if the ID is not recognized
     */
    public static WebXmlVersion valueOf(String thePublicId)
    {
        if (V2_2.getPublicId().equals(thePublicId))
        {
            return WebXmlVersion.V2_2;
        }
        else if (V2_3.getPublicId().equals(thePublicId))
        {
            return WebXmlVersion.V2_3;
        }
        return null;
    }

}
