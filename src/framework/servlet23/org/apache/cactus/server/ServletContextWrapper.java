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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
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
package org.apache.cactus.server;

import java.util.*;
import java.io.*;
import java.security.*;
import java.net.*;
import java.lang.reflect.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Wrapper around Servlet 2.3 <code>ServletContext</code>. This wrapper
 * provides additional behaviour (see
 * <code>AbstractServletContextWrapper</code>).
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 * @see RequestDispatcherWrapper
 */
public class ServletContextWrapper extends AbstractServletContextWrapper
{
    /**
     * @param theOriginalContext the original servlet context object
     */
    public ServletContextWrapper(ServletContext theOriginalContext)
    {
        super(theOriginalContext);
    }

    public String getServletContextName()
    {
        return this.originalContext.getServletContextName();
    }

    /**
     * @see #getResourcePaths(String)
     */
    public Set getResourcePaths()
    {
        Set returnSet;

        // Use reflection because newest Servlet API 2.3 changes removed this
        // method
        try {
            Method method = this.originalContext.getClass().
                getMethod("getResourcePaths", null);

            if (method != null) {
                returnSet = (Set)method.invoke(this.originalContext, null);
            } else {
                throw new RuntimeException("Method ServletContext." +
                    "getResourcePaths() no longer supported by your servlet " +
                    "engine !");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting/calling method " +
                "getResourcePaths()");
        }

        return returnSet;
    }

    /**
     * Added to support the changes of the Jakarta Servlet API 2.3 of the
     * 17/03/2001 (in anticipation of the upcoming draft of Servlet 2.3). Kept
     * the method without parameters for servlet engines that do not have
     * upgraded yet to the new signature.
     */
    public Set getResourcePaths(String thePath)
    {
        Set returnSet;

        // Check if the method exist (for servlet engines that do not have
        // upgraded yet)
        try {
            Method method = this.originalContext.getClass().
                getMethod("getResourcePaths", new Class[] { String.class });

            if (method != null) {
                returnSet = (Set)method.invoke(this.originalContext,
                    new Object[] { thePath });
            } else {
                throw new RuntimeException("Method ServletContext." +
                    "getResourcePaths(String path) not supported yet by your " +
                    "servlet engine !");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting/calling method " +
                "getResourcePaths(String path)");
        }

        return returnSet;
    }

}
