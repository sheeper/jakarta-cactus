/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */
package org.apache.commons.cactus.util;

/**
 * Contains cookie information for cookies returned from the server to the
 * client.
 *
 * @version @version@
 */
public class ClientCookie
{
    /**
     * The cookie's name.
     */
    private String m_Name;

    /**
     * The cookie's value.
     */
    private String m_Value;

    /**
     * The cookie's comment.
     */
    private String m_Comment;

    /**
     * The cookie's domain.
     */
    private String m_Domain;

    /**
     * The cookie's max age.
     */
    private long m_MaxAge;

    /**
     * The cookie's path.
     */
    private String m_Path;

    /**
     * Specify if the cookie is a secure cookie
     */
    private boolean m_IsSecure;

    /**
     * The cookie's spec. version
     */
    private float m_Version;

    /**
     * Construct a client cookie.
     *
     * @param theName the cookie's name
     * @param theValue the cookie's value
     * @param theComment the cookie's comment
     * @param theDomain the cookie's domain
     * @param theMaxAge the cookie's max age
     * @param thePath the cookie's path
     * @param isSecure true is the cookies is a secure cookie
     * @param theVersion the cookie's version
     */
    public ClientCookie(String theName, String theValue, String theComment,
        String theDomain, long theMaxAge, String thePath, boolean isSecure, float theVersion)
    {
        m_Comment = theComment;
        m_Domain = theDomain;
        m_IsSecure = isSecure;
        m_MaxAge = theMaxAge;
        m_Name = theName;
        m_Path = thePath;
        m_Value = theValue;
        m_Version = theVersion;
    }

    /**
     * @return the cookie's name
     */
    public String getName()
    {
        return m_Name;
    }

    /**
     * @return the cookie's value
     */
    public String getValue()
    {
        return m_Value;
    }

    /**
     * @return the cookie's comment
     */
    public String getComment()
    {
        return m_Comment;
    }

    /**
     * @return the cookie's domain
     */
    public String getDomain()
    {
        return m_Domain;
    }

    /**
     * @return the cookie's max age
     */
    public long getMaxAge()
    {
        return m_MaxAge;
    }

    /**
     * @return the cookie's path
     */
    public String getPath()
    {
        return m_Path;
    }

    /**
     * @return true if the cookie is a secure cookie
     */
    public boolean isSecure()
    {
        return m_IsSecure;
    }

    /**
     * @return the cookie's spec. version
     */
    public float getVersion()
    {
        return m_Version;
    }

}