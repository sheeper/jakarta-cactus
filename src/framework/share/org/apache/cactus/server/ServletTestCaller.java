/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.cactus.*;
import org.apache.cactus.util.log.*;

/**
 * Responsible for instanciating the <code>TestCase</code> class on the server
 * side, set up the implicit objects and call the test method.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ServletTestCaller extends AbstractTestCaller
{
    /**
     * The logger
     */
    protected static Log logger =
        LogService.getInstance().getLog(ServletTestCaller.class.getName());

    /**
     * @param theObjects the implicit objects coming from the redirector
     */
    public ServletTestCaller(ServletImplicitObjects theObjects)
    {
        super(theObjects);
    }

    /**
     * Sets the test case fields using the implicit objects (using reflection).
     * @param theTestInstance the test class instance
     */
    protected void setTestCaseFields(AbstractTestCase theTestInstance)
        throws Exception
    {
        ServletTestCase servletInstance = (ServletTestCase)theTestInstance;
        ServletImplicitObjects servletImplicitObjects =
            (ServletImplicitObjects)this.webImplicitObjects;

        // Sets the request field of the test case class
        // ---------------------------------------------

        // Extract from the HTTP request the URL to simulate (if any)
        HttpServletRequest request =
            servletImplicitObjects.getHttpServletRequest();

        ServletURL url = ServletURL.loadFromRequest(request);

        Field requestField = servletInstance.getClass().getField("request");
        requestField.set(servletInstance,
            new HttpServletRequestWrapper(request, url));

        // Set the response field of the test case class
        // ---------------------------------------------

        Field responseField = servletInstance.getClass().getField("response");
        responseField.set(servletInstance,
            servletImplicitObjects.getHttpServletResponse());

        // Set the config field of the test case class
        // -------------------------------------------

        Field configField = servletInstance.getClass().getField("config");
        configField.set(servletInstance,
            new ServletConfigWrapper(
                servletImplicitObjects.getServletConfig()));

        // Set the session field of the test case class
        // --------------------------------------------

        // Create a Session object if the auto session flag is on
        if (isAutoSession()) {

            HttpSession session =
                servletImplicitObjects.getHttpServletRequest().getSession(true);

            Field sessionField = servletInstance.getClass().getField("session");
            sessionField.set(servletInstance, session);

        }
    }

}