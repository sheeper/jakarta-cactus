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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import junit.extensions.TestSetup;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;

import org.apache.cactus.internal.configuration.BaseConfiguration;
import org.apache.cactus.internal.configuration.Configuration;
import org.apache.cactus.internal.configuration.DefaultFilterConfiguration;
import org.apache.cactus.internal.configuration.DefaultServletConfiguration;
import org.apache.cactus.internal.configuration.FilterConfiguration;
import org.apache.cactus.internal.configuration.ServletConfiguration;
import org.apache.cactus.internal.util.ClassLoaderUtils;

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
 * @version $Id: JettyTestSetup.java 239036 2004-08-17 10:35:57Z vmassol $
 */
public class Jetty6xTestSetup extends TestSetup
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
     * Whether the container had already been running before.
     */
    private boolean alreadyRunning;

    /**
     * Whether the container is running or not.
     */
    private boolean isRunning = false;

    /**
     * Whether the container should be stopped on tearDown even though
     * it was not started by us.
     */
    private boolean forceShutdown = false;
    
    /**
     * The Servlet configuration object used to configure Jetty. 
     */
    private ServletConfiguration servletConfiguration;

    /**
     * The Filter configuration object used to configure Jetty. 
     */
    private FilterConfiguration filterConfiguration;

    /**
     * The base configuration object used to configure Jetty. 
     */
    private Configuration baseConfiguration;
    
    /**
     * @param theTest the test we are decorating (usually a test suite)
     */
    public Jetty6xTestSetup(Test theTest)
    {
        super(theTest);
        this.baseConfiguration = new BaseConfiguration();
        this.servletConfiguration = new DefaultServletConfiguration();
        this.filterConfiguration = new DefaultFilterConfiguration();
    }

    /**
     * @param theTest the test we are decorating (usually a test suite)
     * @param theBaseConfiguration the base configuration object used to
     *        configure Jetty
     * @param theServletConfiguration the servlet configuration object used
     *        to configure Jetty
     * @param theFilterConfiguration the filter configuration object used
     *       to configure Jetty
     */
    public Jetty6xTestSetup(Test theTest, 
        Configuration theBaseConfiguration,
        ServletConfiguration theServletConfiguration,
        FilterConfiguration theFilterConfiguration)
    {
        this(theTest);
        this.baseConfiguration = theBaseConfiguration;
        this.servletConfiguration = theServletConfiguration;
        this.filterConfiguration = theFilterConfiguration;
    }
    
    /**
     * Make sure that {@link #tearDown} is called if {@link #setUp} fails
     * to start the container properly. The default 
     * {@link TestSetup#run(TestResult)} method does not provide this feature
     * unfortunately.
     * 
     * {@inheritDoc}
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
        // Try connecting in case the server is already running. If so, does
        // nothing
        URL contextURL = new URL(this.baseConfiguration.getContextURL()
            + "/" + this.servletConfiguration.getDefaultRedirectorName()
            + "?Cactus_Service=RUN_TEST");
        this.alreadyRunning = isAvailable(testConnectivity(contextURL));
        if (this.alreadyRunning)
        {
            // Server is already running. Record this information so that we
            // don't stop it afterwards.
            this.isRunning = true;
            return;
        }

        // Note: We are currently using reflection in order not to need Jetty
        // to compile Cactus. If the code becomes more complex or we need to 
        // add other initializer, it will be worth considering moving them
        // to a separate "extension" subproject which will need additional jars
        // in its classpath (using the same mechanism as the Ant project is
        // using to conditionally compile tasks).

        // Create a Jetty Server object and configure a listener
        this.server = createServer(this.baseConfiguration);

        // Create a Jetty context.
        Object context = createContext(this.server, this.baseConfiguration);
        
        // Add the Cactus Servlet redirector
        addServletRedirector(context, this.servletConfiguration);

        // Add the Cactus Jsp redirector
        addJspRedirector(context);

        // Add the Cactus Filter redirector
        addFilterRedirector(context, this.filterConfiguration);

        // Configure Jetty with an XML file if one has been specified on the
        // command line.
        if (getConfigFile() != null)
        {
        	Class xmlConfigClass = ClassLoaderUtils.loadClass(
                    "org.mortbay.xml.XmlConfiguration", this.getClass());
        	
        	Object xmlConfiguration = xmlConfigClass.getConstructor(new Class[]{String.class})
        		.newInstance(new Object[]{getConfigFile().toString()});
        	
        	xmlConfiguration.getClass().getMethod("configure", new Class[] {Object.class}).invoke(xmlConfiguration, new Object[] {server});

        }

        // Start the Jetty server
        
        this.server.getClass().getMethod("start", null).invoke(
            this.server, null);
        this.isRunning = true;
    }

    /**
     * Stop the running Jetty server.
     * 
     * @exception Exception if an error happens during the shutdown
     */
    protected void tearDown() throws Exception
    { 
        // Don't shut down a container that has not been started by us
        if (!this.forceShutdown && this.alreadyRunning)
        {
            return;
        }

        if (this.server != null)
        { 
            // First, verify if the server is running
            boolean started = ((Boolean) this.server.getClass().getMethod(
                "isStarted", null).invoke(this.server, null)).booleanValue(); 

            // Stop and destroy the Jetty server, if started
            if (started)
            {
                // Stop all listener and contexts
                this.server.getClass().getMethod("stop", null).invoke(
                    this.server, null);

                // Destroy a stopped server. Remove all components and send 
                // notifications to all event listeners.  
                //this.server.getClass().getMethod("destroy", null).invoke(
                //    this.server, null);
            }
        } 

        this.isRunning = false;
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
     * @param isForcedShutdown if true the container will be stopped even
     *        if it has not been started by us
     */
    public final void setForceShutdown(boolean isForcedShutdown)
    {
        this.forceShutdown = isForcedShutdown;
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
        
        Class serverConnectorClass = ClassLoaderUtils.loadClass(
        		"org.mortbay.jetty.nio.SelectChannelConnector", this.getClass());
        Object connector = serverConnectorClass.newInstance();
        //Connector connector = new SelectChannelConnector();
        connector.getClass().getMethod(
        		"setPort", new Class[] {String.class})
        		.invoke(connector, new Object[] {"" + contextURL.getPort()});
        connector.getClass().getMethod(
        		"setHost", new Class[] {String.class})
        		.invoke(connector, new Object[] {"" + contextURL.getHost().toString()});
        
        server.getClass().getMethod("addConnector", 
            new Class[] {ClassLoaderUtils.loadClass("org.mortbay.jetty.Connector", this.getClass())})
            .invoke(server, new Object[] {connector});
        
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

        URL contextURL = new URL(theConfiguration.getContextURL());
        
        Class contextClass = ClassLoaderUtils.loadClass(
                "org.mortbay.jetty.servlet.Context", this.getClass());
        
        Object context = contextClass.getConstructor(new Class[]{Class.class, String.class})
        	.newInstance(new Object[]{theServer, contextURL.getPath().toString()});
        

        context.getClass().getMethod("setClassLoader", new Class[]{ClassLoader.class})
        	.invoke(context, new Object[]{getClass().getClassLoader()});
        
        return context;
    }
    
    /**
     * Adds the Cactus Servlet redirector configuration.
     * 
     * @param theContext the Jetty context under which to add the configuration
     * @param theConfiguration the Cactus Servlet configuration
     * 
     * @exception Exception if an error happens during initialization
     */
    private void addServletRedirector(Object theContext,
        ServletConfiguration theConfiguration) throws Exception
    {
   	
    	theContext.getClass().getMethod("addServlet", new Class[]{String.class, String.class})
    		.invoke(theContext, new Object[]{"org.apache.cactus.server.ServletTestRedirector", "/" + theConfiguration.getDefaultRedirectorName().toString()});
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
                new Object[] {org.apache.jasper.servlet.JspServlet.class.getName(),"*.jsp"});
            
            Object servletHandler = theContext.getClass().getMethod("getServletHandler", new Class[]{}).invoke(theContext, new Object[]{});
            
            servletHandler.getClass().getMethod("addServletMapping", new Class[]{String.class, String.class})
            	.invoke(servletHandler, new Object[]{"org.apache.jasper.servlet.JspServlet", "/jspRedirector.jsp"});
            
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
        	
            theContext.getClass().getMethod("addFilter", 
                    new Class[] {String.class, String.class, Integer.TYPE})
                    .invoke(theContext, 
                    new Object[] {org.apache.cactus.server.FilterTestRedirector.class.getName(),theConfiguration.getDefaultRedirectorName(), new Integer(0)});
        }
    }

    /**
     * Tests whether we are able to connect to the HTTP server identified by the
     * specified URL.
     * 
     * @param theUrl The URL to check
     * @return the HTTP response code or -1 if no connection could be 
     *         established
     */
    protected int testConnectivity(URL theUrl)
    {
        int code;
        try
        {
            HttpURLConnection connection = 
                (HttpURLConnection) theUrl.openConnection();
            connection.setRequestProperty("Connection", "close");
            connection.connect();
            readFully(connection);
            connection.disconnect();
            code = connection.getResponseCode();
        }
        catch (IOException e)
        {
            code = -1;
        }
        return code;
    }

    /**
     * Tests whether an HTTP return code corresponds to a valid connection
     * to the test URL or not. Success is 200 up to but excluding 300.
     * 
     * @param theCode the HTTP response code to verify
     * @return <code>true</code> if the test URL could be called without error,
     *         <code>false</code> otherwise
     */
    protected boolean isAvailable(int theCode)
    {
        boolean result;
        if ((theCode != -1) && (theCode < 300 || theCode == 404)) 
        {
            result = true;            
        }
        else
        {
            result = false;
        }
        return result;
    }

    /**
     * Fully reads the input stream from the passed HTTP URL connection to
     * prevent (harmless) server-side exception.
     *
     * @param theConnection the HTTP URL connection to read from
     * @exception IOException if an error happens during the read
     */
    protected void readFully(HttpURLConnection theConnection)
        throws IOException
    {
        // Only read if there is data to read ... The problem is that not
        // all servers return a content-length header. If there is no header
        // getContentLength() returns -1. It seems to work and it seems
        // that all servers that return no content-length header also do
        // not block on read() operations!
        if (theConnection.getContentLength() != 0)
        {
            byte[] buf = new byte[256];
            InputStream in = null;
            try {
            	in = theConnection.getInputStream();
            } catch (FileNotFoundException fex) {
            	//JAVA BUG #4160499
            	return;
            }
            while (in.read(buf) != -1)
            {
                // Make sure we read all the data in the stream
            }
        }
    }

    /**
     * @return true if the server is running or false otherwise
     */
    protected boolean isRunning()
    {
        return this.isRunning;
    }
}
