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
package org.apache.cactus;

import java.io.Serializable;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Client cookie. Used for manipulating client cookies either in
 * <code>beginXXX()</code> (to send cookies) or in
 * <code>endXXX()</code> methods (to assert returned cookies).
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class Cookie implements Serializable
{
    /**
     * The logger
     */
    private static final Log LOGGER = LogFactory.getLog(Cookie.class);

    /**
     * The cookie name
     */
    private String name;

    /**
     * The cookie value
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
     * Sets the cookie name
     *
     * @param theName the cookie name
     */
    public void setName(String theName)
    {
        this.name = theName;
    }

    /**
     * @return the cookie name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the cookie value
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
     * Returns the domain that will be used to send the cookies. If a host
     * was specified using <code>setURL()</code> then the domain will be
     * this host. Otherwise it will be the real redirector host.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theRealHost the real host to which we are connecting to. We will
     *        use it if no simulation host has been specified.
     * @return the cookie domain to use
     */
    public static String getCookieDomain(WebRequest theRequest, 
        String theRealHost)
    {
        String domain;
        ServletURL url = theRequest.getURL();

        if ((url != null) && (url.getHost() != null))
        {
            domain = url.getHost();
        }
        else
        {
            domain = theRealHost;
        }

        LOGGER.debug("Cookie validation domain = [" + domain + "]");

        return domain;
    }

    /**
     * Returns the port that will be used to send the cookies. If a port
     * was specified using <code>setURL()</code> then the port sent will be
     * this port. Otherwise it will be the real redirector port.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theRealPort the real port to which we are connecting to. We will
     *        use it if no simulation port has been specified.
     * @return the cookie domain to use
     */
    public static int getCookiePort(WebRequest theRequest, int theRealPort)
    {
        int port;
        ServletURL url = theRequest.getURL();

        if ((url != null) && (url.getHost() != null))
        {
            port = url.getPort();
        }
        else
        {
            port = theRealPort;
        }

        LOGGER.debug("Cookie validation port = [" + port + "]");

        return port;
    }

    /**
     * Returns the path that will be used to validate if a cookie will be
     * sent or not. The algorithm is as follows : if the cookie path is not
     * set (i.e. null) then the cookie is always sent (provided the domain
     * is right). If the cookie path is set, the cookie is sent only if
     * the request path starts with the same string as the cookie path. If
     * <code>setURL()</code> has been called, return the path it has been
     * set to (context + servletPath + pathInfo). Otherwise return the
     * real redirector path.
     *
     * @param theRequest the request containing all data to pass to the server
     *        redirector.
     * @param theRealPath the real path to which we are connecting to. We will
     *        use it if no simulation path has been specified.
     * @return the path to use to decide if a cookie will get sent
     */
    public static String getCookiePath(WebRequest theRequest, 
        String theRealPath)
    {
        String path;
        ServletURL url = theRequest.getURL();

        if ((url != null) && (url.getPath() != null))
        {
            path = url.getPath();
        }
        else
        {
            String file = theRealPath;

            if (file != null)
            {
                int q = file.lastIndexOf('?');

                if (q != -1)
                {
                    path = file.substring(0, q);
                }
                else
                {
                    path = file;
                }
            }
            else
            {
                path = null;
            }
        }

        LOGGER.debug("Cookie validation pah = [" + path + "]");

        return path;
    }
}