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
package org.apache.commons.cactus.server;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import org.apache.commons.cactus.*;
import org.apache.commons.cactus.util.log.*;

/**
 * Call the test method on the server side after assigning the JSP implicit
 * objects using reflection.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class JspTestCaller extends ServletTestCaller
{
    /**
     * The logger
     */
    protected static Log logger =
        LogService.getInstance().getLog(JspTestCaller.class.getName());

    /**
     * @param theObjects the implicit objects coming from the redirector
     */
    public JspTestCaller(JspImplicitObjects theObjects)
    {
        super(theObjects);
        this.servletImplicitObjects = theObjects;
    }

    /**
     * Sets the test case fields using the implicit objects (using reflection).
     * @param theTestInstance the test class instance
     */
    protected void setTestCaseFields(ServletTestCase theTestInstance)
        throws Exception
    {
        logger.entry("setTestCaseFields([" + theTestInstance + "])");

        JspImplicitObjects jspImplicitObjects =
            (JspImplicitObjects)this.servletImplicitObjects;

        // Sets the Servlet-related implicit objects
        // -----------------------------------------

        super.setTestCaseFields(theTestInstance);

        // Set the page context field of the test case class
        // -------------------------------------------------

        Field pageContextField = theTestInstance.getClass().
            getField("pageContext");
        pageContextField.set(theTestInstance,
            jspImplicitObjects.getPageContext());

        // Set the JSP writer field of the test case class
        // -----------------------------------------------

        Field outField = theTestInstance.getClass().getField("out");
        outField.set(theTestInstance, jspImplicitObjects.getJspWriter());

        logger.exit("setTestCaseFields");
    }

}