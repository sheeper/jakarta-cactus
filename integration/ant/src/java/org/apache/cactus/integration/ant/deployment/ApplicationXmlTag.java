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

/**
 * Represents the various top-level tags in a enterprise application deployment
 * descriptor as a typesafe enumeration.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
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
    public final String getTagName()
    {
        return this.tagName;
    }
    
    /**
     * @see java.lang.Object#toString
     */
    public final String toString()
    {
        return getTagName();
    }
    
}
