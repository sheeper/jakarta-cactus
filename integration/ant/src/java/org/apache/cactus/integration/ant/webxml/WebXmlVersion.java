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
package org.apache.cactus.integration.ant.webxml;

import org.w3c.dom.DocumentType;

/**
 * Enumerated type that represents the version of the web deployment descriptor.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public class WebXmlVersion implements Comparable
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
    public final boolean equals(Object theOther)
    {
        return super.equals(theOther);
    }
    
    /**
     * @see java.lang.Object#hashCode
     */
    public final int hashCode()
    {
        return super.hashCode();
    }
    
    /**
     * Returns the tag name.
     * 
     * @return The tag name
     */
    public final String getVersion()
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
    public final String toString()
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
