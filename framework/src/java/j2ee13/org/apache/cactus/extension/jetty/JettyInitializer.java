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
package org.apache.cactus.extension.jetty;

import java.net.URL;

import org.apache.cactus.client.initialization.Initializable;
import org.apache.cactus.configuration.BaseConfiguration;
import org.apache.cactus.configuration.Configuration;
import org.apache.cactus.configuration.FilterConfiguration;
import org.apache.cactus.configuration.ServletConfiguration;
import org.apache.cactus.server.FilterTestRedirector;
import org.apache.cactus.server.ServletTestRedirector;
import org.apache.cactus.util.ClassLoaderUtils;

/**
 * Cactus initializer to start an embedded Jetty server. It will be stopped
 * automatically upon JVM shutdown.
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
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
     * Name of optional system property that gives the directory
     * where JSPs and other resources are located. 
     */
    private static final String CACTUS_JETTY_RESOURCE_DIR_PROPERTY = 
        "cactus.jetty.resourceDir";

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

        // Create configuration objects
        BaseConfiguration baseConfig = new BaseConfiguration();
        ServletConfiguration servletConfig = new ServletConfiguration();
        FilterConfiguration filterConfig = new FilterConfiguration();

        // Create a Jetty Server object and configure a listener
        Object server = createServer(baseConfig);

        // Create a Jetty context.
        Object context = createContext(server, baseConfig);
        
        // Add the Cactus Servlet redirector
        addServletRedirector(context, servletConfig);

        // Add the Cactus Jsp redirector
        addJspRedirector(context);

        // Add the Cactus Filter redirector
        addFilterRedirector(context, filterConfig);

        // Configure Jetty with an XML file if one has been specified on the
        // command line.
        if (System.getProperty(CACTUS_JETTY_CONFIG_PROPERTY) != null)
        {
            server.getClass().getMethod("configure", 
                new Class[] {String.class})
                .invoke(server, new Object[] {System.getProperty(
                    CACTUS_JETTY_CONFIG_PROPERTY)});
        }

        // Start the Jetty server
        server.getClass().getMethod("start", null).invoke(server, null);
    }

    /**
     * Create a Jetty server object and configures a listener on the
     * port defined in the Cactus context URL property.
     * 
     * @param theConfiguration the base Cactus configuration
     * @return the Jetty <code>Server</code> object
     * 
     * @exception Exception if an error happens during initialization
     */
    public Object createServer(Configuration theConfiguration) 
        throws Exception
    {
        // Create Jetty Server object
        Class serverClass = ClassLoaderUtils.loadClass(
            "org.mortbay.jetty.Server", this.getClass());
        Object server = serverClass.newInstance();

        URL contextURL = new URL(theConfiguration.getContextURL());

        // Add a listener on the port defined in the Cactus configuration
        server.getClass().getMethod("addListener", 
            new Class[] {String.class})
            .invoke(server, new Object[] {"" + contextURL.getPort()});

        return server;
    }

    /**
     * Create a Jetty Context. We use a <code>WebApplicationContext</code>
     * because we need to use Servlet Filters.
     * 
     * @param theServer the Jetty Server object
     * @param theConfiguration the base Cactus configuration
     * @return Object the <code>WebApplicationContext</code> object
     * 
     * @exception Exception if an error happens during initialization
     */
    public Object createContext(Object theServer,
        Configuration theConfiguration) throws Exception
    {
        // Add a web application. This creates a WebApplicationContext.
        // Note: We do not put any WEB-INF/, lib/ nor classes/ directory
        // in the webapp.
        URL contextURL = new URL(theConfiguration.getContextURL());

        if (System.getProperty(CACTUS_JETTY_RESOURCE_DIR_PROPERTY) != null)
        {
            theServer.getClass().getMethod("addWebApplication", 
                new Class[] {String.class, String.class})
                .invoke(theServer, new Object[] {contextURL.getPath(), 
                System.getProperty(CACTUS_JETTY_RESOURCE_DIR_PROPERTY)});
        }
        
        // Retrieves the WebApplication context created by the
        // "addWebApplication". We need it to be able to manually configure
        // other items in the context.
        Object context = theServer.getClass().getMethod(
            "getContext", new Class[] {String.class})
            .invoke(theServer, new Object[] {contextURL.getPath()});

        return context;
    }
    
    /**
     * Adds the Cactus Servlet redirector configuration
     * 
     * @param theContext the Jetty context under which to add the configuration
     * @param theConfiguration the Cactus Servlet configuration
     * 
     * @exception Exception if an error happens during initialization
     */
    public void addServletRedirector(Object theContext,
        ServletConfiguration theConfiguration) throws Exception
    {
        theContext.getClass().getMethod("addServlet", 
            new Class[] {String.class, String.class, String.class})
            .invoke(theContext, 
            new Object[] {theConfiguration.getDefaultRedirectorName(),
            "/" + theConfiguration.getDefaultRedirectorName(), 
            ServletTestRedirector.class.getName()});
    }
    
    /**
     * Adds the Cactus Jsp redirector configuration. We only add it if the
     * CACTUS_JETTY_RESOURCE_DIR_PROPERTY has been provided by the user.
     * 
     * @param theContext the Jetty context under which to add the configuration
     * 
     * @exception Exception if an error happens during initialization
     */
    public void addJspRedirector(Object theContext) throws Exception
    {
        if (System.getProperty(CACTUS_JETTY_RESOURCE_DIR_PROPERTY) != null)
        {
            theContext.getClass().getMethod("addServlet", 
                new Class[] {String.class, String.class})
                .invoke(theContext, 
                new Object[] {"*.jsp", 
                "org.apache.jasper.servlet.JspServlet"});

            // Get the WebApplicationHandler object in order to be able to 
            // call the addServlet() method that accpets a forced path.
            Object handler = theContext.getClass().getMethod(
                "getWebApplicationHandler", 
                new Class[] {}).invoke(theContext, new Object[] {});

            handler.getClass().getMethod("addServlet", 
                new Class[] {String.class, String.class, String.class, 
                    String.class})
                .invoke(handler, 
                new Object[] {"JspRedirector",
                "/JspRedirector",
                "org.apache.jasper.servlet.JspServlet",
                "/jspRedirector.jsp"});
        }
    }

    /**
     * Adds the Cactus Filter redirector configuration.
     * 
     * @param theContext the Jetty context under which to add the configuration
     * @param theConfiguration the Cactus Filter configuration
     * 
     * @exception Exception if an error happens during initialization
     */
    public void addFilterRedirector(Object theContext,
        FilterConfiguration theConfiguration) throws Exception
    {
        if (System.getProperty(CACTUS_JETTY_RESOURCE_DIR_PROPERTY) != null)
        {
            // Get the WebApplicationHandler object in order to be able to add
            // the Cactus Filter redirector
            Object handler = theContext.getClass().getMethod(
                "getWebApplicationHandler", 
                new Class[] {}).invoke(theContext, new Object[] {});
    
            Object filterHolder = handler.getClass().getMethod("defineFilter",
                new Class[] {String.class, String.class})
                .invoke(handler, 
                new Object[] {theConfiguration.getDefaultRedirectorName(),
                FilterTestRedirector.class.getName()});        
    
            filterHolder.getClass().getMethod("applyTo",
                new Class[] {String.class})
                .invoke(filterHolder, new Object[] {"REQUEST"});        
    
            // Map the Cactus Filter redirector to a path
            handler.getClass().getMethod("mapPathToFilter", 
                new Class[] {String.class, String.class})
                .invoke(handler, 
                new Object[] {"/" 
                + theConfiguration.getDefaultRedirectorName(),
                theConfiguration.getDefaultRedirectorName()});
        }
    }

}