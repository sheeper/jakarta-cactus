/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.container.jboss;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.container.AbstractJavaContainer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Special container support for the JBoss application server.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * 
 * @version $Id$
 */
public class JBoss3xContainer extends AbstractJavaContainer
{
    // Instance Variables ------------------------------------------------------

    /**
     * The JBoss 3.x installation directory.
     */
    private File dir;

    /**
     * The name of the server configuration to use for running the tests.
     */
    private String config = "default";

    /**
     * The port to which the container should be bound.
     */
    private int port = 8080;

    /**
     * The server JNDI Port (used during shutdown)
     */
    private int jndiPort = 1099;
    
    /**
     * The JBoss version detected by reading the Manifest file in the
     * installation directory.
     */
    private String version;

    /**
     * The context root of the tested application.
     */
    private String testContextRoot;
    
    // Public Methods ----------------------------------------------------------

    /**
     * Sets the JBoss installation directory.
     * 
     * @param theDir The directory to set
     * @throws BuildException If the specified directory doesn't contain a valid
     *         JBoss 3.x installation
     */
    public final void setDir(File theDir) throws BuildException
    {
        this.dir = theDir;
    }

    /**
     * Sets the name of the server configuration to use for running the tests.
     * 
     * @param theConfig The configuration name
     */
    public final void setConfig(String theConfig)
    {
        this.config = theConfig;
    }

    /**
     * Sets the port that will be used to poll the server to verify if
     * it is started. This is needed for the use case where the user
     * has defined his own JBoss configuration by using the
     * {@link #setConfig(String)} call and has defined a port other
     * than the default one. 
     * 
     * Note: This value is not yet used to set the port
     * to which the container will listen to. The reason is that this is
     * hard to implement with JBoss and nobody had the courage to implement
     * it yet...
     * 
     * @param thePort The port to set
     */
    public final void setPort(int thePort)
    {
        this.port = thePort;
    }

    /**
     * Specify the JNDI port to use.
     *
     * @param theJndiPort The JNDI port
     */
    public final void setJndiPort(int theJndiPort)
    {
        this.jndiPort = theJndiPort;
    }
    
    // Container Implementation ------------------------------------------------

    /**
     * @see AbstractJavaContainer#getTestContext()
     */
    public String getTestContext()
    {
        return this.testContextRoot;
    }
    
    /**
     * @see AbstractJavaContainer#getName()
     */
    public final String getName()
    {
        return "JBoss " + this.version;
    }

    /**
     * @see AbstractJavaContainer#getPort() 
     */
    public final int getPort()
    {
        return this.port;
    }

    /**
     * Returns the server JNDI port.
     *
     * @return The JNDI port
     */
    public final int getJndiPort()
    {
        return this.jndiPort;
    }
    
    /**
     * @see AbstractJavaContainer#init()
     */
    public final void init()
    {
        // Verify the installation directory
        this.version = getVersion(this.dir);
        if (this.version == null)
        {
            throw new BuildException(this.dir
                + " not recognized as a JBoss 3.x installation");
        }
        if (!this.version.startsWith("3"))
        {
            throw new BuildException(
                "This element doesn't support version " + this.version
                + " of JBoss");
        }

        // Try to infer the test root context from the JBoss specific
        // <code>jboss-web.xml</code> file.
        this.testContextRoot = getTestContextFromJBossWebXml();
        
        // TODO: as long as we don't have a way to set the port on the JBoss 
        // instance, we'll at least need to extract the port from a config file
        // in the installation directory
    }

    /**
     * @see AbstractJavaContainer#startUp()
     */
    public final void startUp()
    {
        try
        {
            prepare("cactus/jboss3x");
            
            File binDir = new File(this.dir, "bin");
            File configDir = new File(this.dir, "server");
            
            Java java = createJavaForStartUp();
            java.setDir(binDir);
            
            java.addSysproperty(
                createSysProperty("program.name",
                    new File(binDir, "run.bat")));
            java.addSysproperty(
                createSysProperty("jboss.server.home.dir",
                    new File(configDir, this.config)));
            java.addSysproperty(
                createSysProperty("jboss.server.home.url",
                    new File(configDir, this.config).toURL().toString()));

            Path classpath = java.createClasspath();
            classpath.createPathElement().setLocation(
                new File(binDir, "run.jar"));
            addToolsJarToClasspath(classpath);
            java.setClassname("org.jboss.Main");
            java.createArg().setValue("-c");
            java.createArg().setValue(this.config);
            java.execute();
        }
        catch (IOException ioe)
        {
            getLog().error("Failed to startup the container", ioe);
            throw new BuildException(ioe);
        }
    }

    /**
     * @see AbstractJavaContainer#shutDown()
     */
    public final void shutDown()
    {
        File binDir = new File(this.dir, "bin");
            
        Java java = createJavaForShutDown();
        java.setFork(true);

        Path classPath = java.createClasspath();
        classPath.createPathElement().setLocation(
            new File(binDir, "shutdown.jar"));

        java.setClassname("org.jboss.Shutdown");
        java.createArg().setValue("--server=localhost:" + this.getJndiPort());
        
        if (this.version.startsWith("3.2"))
        {
            java.createArg().setValue("--shutdown");
        }
        else
        {
            java.createArg().setValue("localhost");
            java.createArg().setValue(String.valueOf(getPort()));
        }
        java.execute();
    }

    // Private Methods ---------------------------------------------------------

    /**
     * @return the test context from JBoss's <code>jboss-web.xml</code> or null
     *         if none has been defined or if the file doesn't exist
     */
    private String getTestContextFromJBossWebXml()
    {
        String testContext = null;
        
        try
        {
            Document doc = getJBossWebXML();
            Element root = doc.getDocumentElement();
            Node context = root.getElementsByTagName("context-root").item(0);
            testContext = context.getFirstChild().getNodeValue();
        }
        catch (Exception e)
        {
            // no worries if we can't find what we are looking for (for now).
        }

        return testContext;
    }
    
    /**
     * Prepares a temporary installation of the container and deploys the 
     * web-application.
     * 
     * @param theDirName The name of the temporary container installation
     *        directory
     * @throws IOException If an I/O error occurs
     */
    private void prepare(String theDirName) throws IOException
    {
        FileUtils fileUtils = FileUtils.newFileUtils();

        // TODO: Find out how to create a valid default server configuration.
        // Copying the server directory does not seem to be enough
            
        // deploy the web-app by copying the WAR file into the webapps
        // directory
        File configDir = new File(this.dir, "server");
        File deployDir = new File(configDir, this.config + "/deploy");
        fileUtils.copyFile(getDeployableFile().getFile(),
            new File(deployDir, getDeployableFile().getFile().getName()), 
            null, true);
    }

    /**
     * Returns the version of the JBoss installation.
     * 
     * @param theDir The JBoss installation directory 
     * @return The JBoss version, or <code>null</code> if the verion number
     *         could not be retrieved
     */
    private String getVersion(File theDir)
    {
        // Extract version information from the manifest in run.jar
        String retVal = null;
        try
        {
            JarFile runJar = new JarFile(new File(theDir, "bin/run.jar"));
            Manifest mf = runJar.getManifest();
            if (mf != null)
            {
                Attributes attrs = mf.getMainAttributes();
                retVal = attrs.getValue(Attributes.Name.SPECIFICATION_VERSION);
            }
            else
            {
                getLog().warn("Couldn't find MANIFEST.MF in " + runJar);
            }
        }
        catch (IOException ioe)
        {
            getLog().warn("Couldn't retrieve JBoss version information", ioe);
        }
        return retVal;
    }

    /**
     * Get a Document object for the <code>jboss-web.xml</code> file.
     * 
     * @return The parsed XML Document object or null if not found
     * @throws IOException If there is a problem reading files
     * @throws ParserConfigurationException If there is a problem w/ parser
     * @throws SAXException If there is a problem with parsing
     */
    private Document getJBossWebXML() throws
        IOException, ParserConfigurationException, SAXException
    {
        Document doc = null;
        File configDir = new File(this.dir, "server");
        File deployDir = new File(configDir, this.config + "/deploy");
        File warFile = new File(deployDir,
            getDeployableFile().getFile().getName());
 
        JarFile war = new JarFile(warFile);
        ZipEntry entry = war.getEntry("WEB-INF/jboss-web.xml");
        if (entry != null)
        {
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new EntityResolver()
            {
                public InputSource resolveEntity(String thePublicId, 
                    String theSystemId) throws SAXException
                {
                    return new InputSource(new StringReader(""));
                }
            });
            doc = builder.parse(war.getInputStream(entry));
        }
        war.close();
        return doc;
    }
    
}
