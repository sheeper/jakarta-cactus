/*
 * ====================================================================
 *
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
package org.apache.cactus.extension.jetty;

import java.net.URL;

import org.apache.cactus.server.ServletTestRedirector;
import org.apache.cactus.util.ClassLoaderUtils;
import org.apache.cactus.util.Configuration;
import org.apache.cactus.util.Initializable;
import org.apache.cactus.util.ServletConfiguration;

/**
 * Cactus initializer to start an embedded Jetty server (it will be stopped
 * automatically upon JVM shutdown).
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id: $
 */
public class JettyInitializer implements Initializable
{
    /**
     * Name of optional system property that points to a Jetty XML
     * configuration file.
     */
    private static final String CACTUS_JETTY_CONFIG_PROPERTY = 
        "cactus.jetty.config";
    
    /**
     * Start an embedded Jetty server. It is allowed to pass a Jetty XML as
     * a system property (<code>cactus.jetty.config</code>) to further 
     * configure Jetty. Example: 
     * <code>-Dcactus.jetty.config=./jetty.xml</code>.
     *
     * @exception Exception if an error happens during initialization
     */
    public void initialize() throws Exception
    {
        // Note: We are currently using reflection in order not to need Jetty
        // to compile Cactus. If the code becomes more complex or we need to 
        // add other initializer, it will be worth considering moving them
        // to a separate "extension" subproject which will need additional jars
        // in its classpath (using the same mechanism as the Ant project is
        // using to conditionally compile tasks).
        
        // Create Jetty Server object
        Class serverClass = ClassLoaderUtils.loadClass(
            "org.mortbay.jetty.Server", this.getClass());
        Object server = serverClass.newInstance();        
              
        URL contextURL = new URL(Configuration.getContextURL());
        
        // Add a listener on the port defined in the Cactus configuration
        serverClass.getMethod("addListener", new Class[] { String.class })
            .invoke(server, new Object[] { "" + contextURL.getPort() });

        // Create a Jetty Servlet HTTP context to handle calls to the Cactus
        // Servlet redirector
        Object context = serverClass.getMethod(
            "getContext", new Class[] { String.class })
            .invoke(server, new Object[] { contextURL.getPath() });

        // Add the context under which calls will be served by that HTTP 
        // context
        context.getClass().getMethod(
            "addServlet", new Class[] { String.class, String.class })
            .invoke(context, new Object[] { "/" 
            + ServletConfiguration.getServletRedirectorName(), 
            ServletTestRedirector.class.getName() });

        // Configure Jetty with an XML file if one has been specified on the
        // command line.
        if (System.getProperty(CACTUS_JETTY_CONFIG_PROPERTY) != null)
        {
            serverClass.getMethod("configure", new Class[] { String.class })
                .invoke(server, new Object[] { 
                System.getProperty(CACTUS_JETTY_CONFIG_PROPERTY) });
        }

        // Start the Jetty server
        serverClass.getMethod("start", null).invoke(server, null);
    }

}