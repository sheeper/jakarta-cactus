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
package org.apache.cactus.server;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * Wrapper around <code>ServletConfig</code> which overrides the
 * <code>getServletContext()</code> method to return our own wrapper around
 * <code>ServletContext</code>.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 * @see ServletContext
 */
public class ServletConfigWrapper implements ServletConfig
{
    /**
     * The original servlet config object
     */
    private ServletConfig originalConfig;

    /**
     * List of parameters set using the <code>setInitParameter()</code> method.
     */
    private Hashtable initParameters;

    /**
     * Simulated name of the servlet
     */
    private String servletName;

    /**
     * @param theOriginalConfig the original servlet config object
     */
    public ServletConfigWrapper(ServletConfig theOriginalConfig)
    {
        this.originalConfig = theOriginalConfig;
        this.initParameters = new Hashtable();
    }

    /**
     * Sets a parameter as if it were set in the <code>web.xml</code> file.
     *
     * @param theName the parameter's name
     * @param theValue the parameter's value
     */
    public void setInitParameter(String theName, String theValue)
    {
        this.initParameters.put(theName, theValue);
    }

    /**
     * Sets the servlet name. That will be the value returned by the
     * <code>getServletName()</code> method.
     *
     * @param theServletName the servlet's name
     */
    public void setServletName(String theServletName)
    {
        this.servletName = theServletName;
    }

    //--Overridden methods ----------------------------------------------------

    /**
     * @return our own wrapped servlet context object
     */
    public ServletContext getServletContext()
    {
        return new ServletContextWrapper(
            this.originalConfig.getServletContext());
    }

    /**
     * @param theName the name of the parameter's value to return
     * @return the value of the parameter, looking for it first in the list of
     *         parameters set using the <code>setInitParameter()</code> method
     *         and then in those set in <code>web.xml</code>.
     */
    public String getInitParameter(String theName)
    {
        // Look first in the list of parameters set using the
        // setInitParameter() method.
        String value = (String) this.initParameters.get(theName);

        if (value == null)
        {
            value = this.originalConfig.getInitParameter(theName);
        }

        return value;
    }

    /**
     * @return the union of the parameters defined in the Redirector
     *         <code>web.xml</code> file and the one set using the
     *         <code>setInitParameter()</code> method.
     */
    public Enumeration getInitParameterNames()
    {
        Vector names = new Vector();

        Enumeration enum = this.initParameters.keys();

        while (enum.hasMoreElements())
        {
            String value = (String) enum.nextElement();

            names.add(value);
        }

        enum = this.originalConfig.getInitParameterNames();

        while (enum.hasMoreElements())
        {
            String value = (String) enum.nextElement();

            names.add(value);
        }

        return names.elements();
    }

    /**
     * @return the simulated servlet's name if defined or the redirector
     *         servlet's name
     */
    public String getServletName()
    {
        if (this.servletName != null)
        {
            return this.servletName;
        }

        return this.originalConfig.getServletName();
    }
}