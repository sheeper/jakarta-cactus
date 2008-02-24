/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.cactus;

import java.io.Serializable;

import java.util.Date;

import org.apache.cactus.internal.util.CookieUtil;

/**
 * Client cookie. Used for manipulating client cookies either in
 * <code>beginXXX()</code> (to send cookies) or in
 * <code>endXXX()</code> methods (to assert returned cookies).
 *
 * @version $Id: Cookie.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class Cookie implements Serializable
{
    /**
     * The cookie name.
     */
    private String name;

    /**
     * The cookie value.
     */
    private String value;

    /**
     * The cookie description.
     * @see #setComment(String)
     */
    private String comment;

    /**
     * The cookie domain.
     * @see #setDomain(String)
     */
    private String domain;

    /**
     * The cookie expiry date.
     * @see #setExpiryDate(Date)
     */
    private Date expiryDate;

    /**
     * The cookie path.
     * @see #setPath(String)
     */
    private String path;

    /**
     * True if the cookie should only be sent over secure connections.
     * @see #setSecure(boolean)
     */
    private boolean isSecure = false;

    /**
     * Create a cookie.
     *
     * @param theDomain the cookie domain
     * @param theName the cookie name
     * @param theValue the cookie value
     */
    public Cookie(String theDomain, String theName, String theValue)
    {
        if (theDomain == null)
        {
            throw new NullPointerException("missing cookie domain");
        }

        if (theName == null)
        {
            throw new NullPointerException("missing cookie name");
        }

        if (theValue == null)
        {
            throw new NullPointerException("missing cookie value");
        }

        setDomain(theDomain);
        setName(theName);
        setValue(theValue);
    }

    /**
     * Sets the cookie name.
     *
     * @param theName the cookie name
     */
    public void setName(String theName)
    {
        this.name = theName;
    }

    /**
     * @return the cookie name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the cookie value.
     *
     * @param theValue the cookie value
     */
    public void setValue(String theValue)
    {
        this.value = theValue;
    }

    /**
     * @return the cookie value
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * Returns the comment describing the purpose of this cookie, or
     * null if no such comment has been defined.
     *
     * @return the cookie comment
     */
    public String getComment()
    {
        return this.comment;
    }

    /**
     * If a user agent (web browser) presents this cookie to a user, the
     * cookie's purpose will be described using this comment.
     *
     * @param theComment the cookie's text comment
     */
    public void setComment(String theComment)
    {
        this.comment = theComment;
    }

    /**
     * Return the expiry date.
     *
     * @return the expiry date of this cookie, or null if none set.
     */
    public Date getExpiryDate()
    {
        return this.expiryDate;
    }

    /**
     * Set the cookie expires date.
     *
     * <p>Netscape's original proposal defined an Expires header that took
     * a date value in a fixed-length variant format in place of Max-Age:
     *
     * Wdy, DD-Mon-YY HH:MM:SS GMT
     *
     * Note that the Expires date format contains embedded spaces, and that
     * "old" cookies did not have quotes around values.  Clients that
     * implement to this specification should be aware of "old" cookies and
     * Expires.
     *
     * @param theExpiryDate the expires date.
     */
    public void setExpiryDate(Date theExpiryDate)
    {
        this.expiryDate = theExpiryDate;
    }

    /**
     * @return true if the cookie should be discarded at the end of the
     *         session; false otherwise
     */
    public boolean isToBeDiscarded()
    {
        return (this.getExpiryDate() != null);
    }

    /**
     * Returns the domain of this cookie.
     *
     * @return the cookie domain
     */
    public String getDomain()
    {
        return this.domain;
    }

    /**
     * Sets the cookie domain. This cookie should be presented only to hosts
     * satisfying this domain name pattern.  Read RFC 2109 for specific
     * details of the syntax.
     *
     * Briefly, a domain name name begins with a dot (".foo.com") and means
     * that hosts in that DNS zone ("www.foo.com", but not "a.b.foo.com")
     * should see the cookie.  By default, cookies are only returned to
     * the host which saved them.
     *
     * @param theDomain the cookie domain
     */
    public void setDomain(String theDomain)
    {
        int ndx = theDomain.indexOf(":");

        if (ndx != -1)
        {
            theDomain = theDomain.substring(0, ndx);
        }

        this.domain = theDomain.toLowerCase();
    }

    /**
     * Return the path this cookie is associated with.
     *
     * @return the cookie path
     */
    public String getPath()
    {
        return this.path;
    }

    /**
     * Sets the cookie path. This cookie should be presented only with
     * requests beginning with this URL. Read RFC 2109 for a specification
     * of the default behaviour. Basically, URLs in the same "directory" as
     * the one which set the cookie, and in subdirectories, can all see the
     * cookie unless a different path is set.
     *
     * @param thePath the cookie path
     */
    public void setPath(String thePath)
    {
        this.path = thePath;
    }

    /**
     * @return true if the cookie should only be sent over secure connections.
     */
    public boolean isSecure()
    {
        return this.isSecure;
    }

    /**
     * Indicates to the user agent that the cookie should only be sent
     * using a secure protocol (https).  This should only be set when
     * the cookie's originating server used a secure protocol to set the
     * cookie's value.
     *
     * @param isSecure true if the cookie should be sent over secure
     *                 connections only
     */
    public void setSecure(boolean isSecure)
    {
        this.isSecure = isSecure;
    }

    /**
     * @return true if this cookie has expired
     */
    public boolean isExpired()
    {
        return (this.getExpiryDate() != null
            && this.getExpiryDate().getTime() <= System.currentTimeMillis());
    }

    /**
     * Hash up name, value and domain into new hash.
     *
     * @return the hashcode of this class
     */
    public int hashCode()
    {
        return (this.getName().hashCode() + this.getValue().hashCode()
            + this.getDomain().hashCode());
    }

    /**
     * Two cookies match if the name, path and domain match.
     *
     * @param theObject the cookie object to match
     * @return true of the object passed as paramater is equal to this coookie
     *         instance
     */
    public boolean equals(Object theObject)
    {
        if ((theObject != null) && (theObject instanceof Cookie))
        {
            Cookie other = (Cookie) theObject;

            return (this.getName().equals(other.getName())
                && this.getPath().equals(other.getPath())
                && this.getDomain().equals(other.getDomain()));
        }

        return false;
    }

    /**
     * @return a string representation of the cookie
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("name = [" + getName() + "], ");
        buffer.append("value = [" + getValue() + "], ");
        buffer.append("domain = [" + getDomain() + "], ");
        buffer.append("path = [" + getPath() + "], ");
        buffer.append("isSecure = [" + isSecure() + "], ");
        buffer.append("comment = [" + getComment() + "], ");
        buffer.append("expiryDate = [" + getExpiryDate() + "]");

        return buffer.toString();
    }

    /**
     * @see CookieUtil#getCookieDomain(WebRequest, String)
     * @param theRequest from which to derive the domain
     * @param theRealHost of the cookie
     * @return String
     * @deprecated use {@link CookieUtil#getCookieDomain(WebRequest, String)} 
     */
    public static String getCookieDomain(WebRequest theRequest, 
        String theRealHost)
    {
        return CookieUtil.getCookieDomain(theRequest, theRealHost);
    }

    /**
     * @see CookieUtil#getCookiePort(WebRequest, int)
     * @param theRequest from which to derive the port
     * @param theRealPort of the cookie
     * @return int
     * @deprecated use {@link CookieUtil#getCookiePort(WebRequest, int)} 
     */
    public static int getCookiePort(WebRequest theRequest, int theRealPort)
    {
        return CookieUtil.getCookiePort(theRequest, theRealPort);
    }

    /**
     * @see CookieUtil#getCookiePath(WebRequest, String)
     * @param theRequest from from which to derive the path 
     * @param theRealPath of the cookie
     * @return String
     * @deprecated use {@link CookieUtil#getCookiePath(WebRequest, String)} 
     */
    public static String getCookiePath(WebRequest theRequest, 
        String theRealPath)
    {
        return CookieUtil.getCookiePath(theRequest, theRealPath);
    }
}
