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
package org.apache.cactus.sample.unit;

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletContext;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.server.ServletContextWrapper;

/**
 * Tests that exercise the Servlet Config.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestServletConfig extends ServletTestCase
{
    /**
     * Verify that we can add parameters to the config list of parameters
     * programatically, without having to define them in <code>web.xml</code>.
     */
    public void testSetConfigParameter()
    {
        config.setInitParameter("testparam", "test value");

        assertEquals("test value", config.getInitParameter("testparam"));

        boolean found = false;
        Enumeration enum = config.getInitParameterNames();

        while (enum.hasMoreElements())
        {
            String name = (String) enum.nextElement();

            if (name.equals("testparam"))
            {
                found = true;

                break;
            }
        }

        assertTrue("[testparam] not found in parameter names", found);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that calling <code>setInitParameter()</code> with a parameter
     * already defined in <code>web.xml</code> will override it.
     */
    public void testSetConfigParameterOverrideWebXmlParameter()
    {
        // Note: "param1" is a parameter that must be defined on the Servlet
        // redirector, with a value different than "testoverrideparam1".
        assertTrue(
            config.getOriginalConfig().getInitParameter("param1") != null);
        assertTrue(
            !config.getOriginalConfig().getInitParameter("param1").equals(
            "testoverrideparam1"));

        config.setInitParameter("param1", "testoverrideparam1");

        Enumeration enum = config.getInitParameterNames();
        int count = 0;
        
        while (enum.hasMoreElements())
        {
            String name = (String) enum.nextElement();

            if (name.equals("param1"))
            {
                assertEquals("testoverrideparam1",
                    config.getInitParameter(name));
                count++;
            }
        }

        assertTrue("[param1] was found " + count + " times. Should have "
            + "been found once.", count == 1);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can override the
     * <code>ServletConfig.getServletName()</code> method.
     */
    public void testGetServletNameOverriden()
    {
        config.setServletName("MyServlet");
        assertEquals("MyServlet", config.getServletName());
        assertTrue(!config.getOriginalConfig().getServletName().equals(
            config.getServletName()));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that if we don't override the servlet name we get the original
     * name (i.e. Cactus is effectively transparent).
     */
    public void testGetServletNameNoOverride()
    {
        assertEquals(config.getOriginalConfig().getServletName(),
            config.getServletName());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that calls to <code>ServletContext.log()</code> methods can
     * be retrieved and asserted.
     */
    public void testGetLogs()
    {
        String message = "some test log";
        ServletContext context = config.getServletContext();

        context.log(message);

        Vector logs = ((ServletContextWrapper) context).getLogs();

        assertEquals("Found more than one log message", logs.size(), 1);
        assertTrue("Cannot find expected log message : [" + message + "]", 
            logs.contains("some test log"));
    }

}