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
package org.apache.cactus.client.connector.http;

import org.apache.cactus.ServletURL;
import org.apache.cactus.WebRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods to manipulate cookies and transform Cactus cookie objects 
 * to HttpClient cookie objects.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @version $Id$
 * @since 1.5
 */
public class CookieUtil
{
    /**
     * The logger
     */
    private static final Log LOGGER = LogFactory.getLog(CookieUtil.class);

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

        LOGGER.debug("Cookie validation path = [" + path + "]");

        return path;
    }
}
