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
package org.apache.cactus.extension.jetty;

import java.io.File;
import java.net.URL;

import junit.extensions.TestSetup;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;

import org.apache.cactus.internal.configuration.BaseConfiguration;
import org.apache.cactus.internal.configuration.Configuration;
import org.apache.cactus.internal.configuration.FilterConfiguration;
import org.apache.cactus.internal.configuration.ServletConfiguration;
import org.apache.cactus.internal.util.ClassLoaderUtils;
import org.apache.cactus.server.FilterTestRedirector;
import org.apache.cactus.server.ServletTestRedirector;

/**
 * Custom JUnit test setup to use to automatically start Jetty. Example:<br/>
 * <code><pre>
 * public static Test suite()
 * {
 *     TestSuite suite = new TestSuite(Myclass.class);
 *     return new JettyTestSetup(suite);
 * }
 * </pre></code>
 * 
 * @version $Id$
 */
public class JettyTestSetup extends TestSetup
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
     * The configuration file to be used for initializing Jetty.
     */
    private File configFile;

    /**
     * The directory containing the resources of the web-application.
     */
    private File resourceDir;

    /**
     * The Jetty server object representing the running instance. It is
     * used to stop Jetty in {@link #tearDown()}.
     */
    private Object server; 

    /**
     * @param theTest the test we are decorating (usually a test suite)
     */
    public JettyTestSetup(Test theTest)
    {
        super(theTest);
    }

    /**
     * Make sure that {@link #tearDown} is called if {@link #setUp} fails
     * to start the container properly. The default 
     * {@link TestSetup#run(TestResult)} method does not provide this feature
     * unfortunately.
     *  
     * @see TestSetup#run(TestResult)
     */
    public void run(final TestResult theResult)
    {
        Protectable p = new Protectable()
        {
            public void protect() throws Exception
            {
                try
                {
                    setUp();
                    basicRun(theResult);
                }
                finally
                {
                    tearDown();
                }
            }
        };
        theResult.runProtected(this, p);
    }  
    
    /**
     * Start an embedded Jetty server. It is allowed to pass a Jetty XML as
     * a system property (<code>cactus.jetty.config</code>) to further 
     * configure Jetty. Example: 
     * <code>-Dcactus.jetty.config=./jetty.xml</code>.
     *
     * @exception Exception if an error happens during initialization
     */
    protected void setUp() throws Exception
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
        this.server = createServer(baseConfig);

        // Create a Jetty context.
        Object context = createContext(this.server, baseConfig);
        
        // Add the Cactus Servlet redirector
        addServletRedirector(context, servletConfig);

        // Add the Cactus Jsp redirector
        addJspRedirector(context);

        // Add the Cactus Filter redirector
        addFilterRedirector(context, filterConfig);

        // Configure Jetty with an XML file if one has been specified on the
        // command line.
        if (getConfigFile() != null)
        {
            this.server.getClass().getMethod("configure", 
                new Class[] {String.class}).invoke(
                    this.server, new Object[] {getConfigFile().toString()});
        }

        // Start the Jetty server
        this.server.getClass().getMethod("start", null).invoke(
            this.server, null);
    }

    /**
     * Stop the running Jetty server.
     * 
     * @exception Exception if an error happens during the shutdown
     */
    protected void tearDown() throws Exception
    { 
        if (this.server != null)
        { 
            // First, verify if the server is running
            boolean started = ((Boolean) this.server.getClass().getMethod(
                "isStarted", null).invoke(this.server, null)).booleanValue(); 

            // Stop the Jetty server, if started
            if (started)
            {
                this.server.getClass().getMethod("stop", null).invoke(
                    this.server, null);
            }
        } 
    }

    /**
     * Sets the configuration file to use for initializing Jetty.
     * 
     * @param theConfigFile The configuration file to set
     */
    public final void setConfigFile(File theConfigFile)
    {
        this.configFile = theConfigFile;
    }

    /**
     * Sets the directory in which Jetty will look for the web-application
     * resources.
     * 
     * @param theResourceDir The resource directory to set
     */
    public final void setResourceDir(File theResourceDir)
    {
        this.resourceDir = theResourceDir;
    }

    /**
     * @return The resource directory, or <code>null</code> if it has not been
     *         set
     */
    protected final File getConfigFile()
    {
        if (this.configFile == null)
        {
            String configFileProperty = System.getProperty(
                CACTUS_JETTY_CONFIG_PROPERTY);
            if (configFileProperty != null)
            {
                this.configFile = new File(configFileProperty);
            }
        }
        return this.configFile;
    }

    /**
     * @return The resource directory, or <code>null</code> if it has not been
     *         set
     */
    protected final File getResourceDir()
    {
        if (this.resourceDir == null)
        {
            String resourceDirProperty = System.getProperty(
                CACTUS_JETTY_RESOURCE_DIR_PROPERTY);
            if (resourceDirProperty != null)
            {
                this.resourceDir = new File(resourceDirProperty);
            }
        }
        return this.resourceDir;
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
    private Object createServer(Configuration theConfiguration) 
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
    private Object createContext(Object theServer,
        Configuration theConfiguration) throws Exception
    {
        // Add a web application. This creates a WebApplicationContext.
        // Note: We do not put any WEB-INF/, lib/ nor classes/ directory
        // in the webapp.
        URL contextURL = new URL(theConfiguration.getContextURL());

        if (getResourceDir() != null)
        {
            theServer.getClass().getMethod("addWebApplication", 
                new Class[] {String.class, String.class})
                .invoke(theServer, new Object[] {contextURL.getPath(), 
                    getResourceDir().toString()});
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
    private void addServletRedirector(Object theContext,
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
     * CACTUS_JETTY_RESOURCE_DIR_PROPERTY has been provided by the user. This
     * is because JSPs need to be attached to a WebApplicationHandler in Jetty.
     * 
     * @param theContext the Jetty context under which to add the configuration
     * 
     * @exception Exception if an error happens during initialization
     */
    private void addJspRedirector(Object theContext) throws Exception
    {
        if (getResourceDir() != null)
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
                new Object[] {
                    "JspRedirector",
                    "/JspRedirector",
                    "org.apache.jasper.servlet.JspServlet",
                    "/jspRedirector.jsp"});
        }
    }

    /**
     * Adds the Cactus Filter redirector configuration. We only add it if the
     * CACTUS_JETTY_RESOURCE_DIR_PROPERTY has been provided by the user. This
     * is because Filters need to be attached to a WebApplicationHandler in 
     * Jetty.
     * 
     * @param theContext the Jetty context under which to add the configuration
     * @param theConfiguration the Cactus Filter configuration
     * 
     * @exception Exception if an error happens during initialization
     */
    private void addFilterRedirector(Object theContext,
        FilterConfiguration theConfiguration) throws Exception
    {
        if (getResourceDir() != null)
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
    
            filterHolder.getClass().getMethod("addAppliesTo",
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
