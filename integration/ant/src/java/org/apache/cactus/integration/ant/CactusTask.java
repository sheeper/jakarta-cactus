/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
package org.apache.cactus.integration.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.applicationxml.ApplicationXml;
import org.apache.cactus.integration.ant.applicationxml.ApplicationXmlIo;
import org.apache.cactus.integration.ant.container.Container;
import org.apache.cactus.integration.ant.container.ContainerFactory;
import org.apache.cactus.integration.ant.container.ContainerRunner;
import org.apache.cactus.integration.ant.container.ContainerWrapper;
import org.apache.cactus.integration.ant.util.AntLog;
import org.apache.cactus.integration.ant.util.AntTaskFactory;
import org.apache.cactus.integration.ant.util.ResourceUtils;
import org.apache.cactus.integration.ant.webxml.WebXml;
import org.apache.cactus.integration.ant.webxml.WebXmlIo;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfigurator;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.types.Environment.Variable;
import org.xml.sax.SAXException;

/**
 * An Ant task that extends the optional JUnit task to provide support for
 * in-container testing.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class CactusTask extends JUnitTask
{

    /**
     * Nested element that can contain a list of containers against which the
     * tests should be run.
     */
    public static class ContainerSet extends ProjectComponent
        implements DynamicConfigurator
    {

        // Instance Variables --------------------------------------------------

        /**
         * The list of nested container elements.
         */
        private ContainerFactory factory = new ContainerFactory();

        /**
         * The list of nested container elements.
         */
        private List containers = new ArrayList();

        /**
         * The timeout in milliseconds. 
         */
        private long timeout = -1;

        /**
         * The proxy port. 
         */
        private int proxyPort = 0;

        // DynamicConfigurator Implementation ----------------------------------

        /**
         * @see org.apache.tools.ant.DynamicConfigurator#createDynamicElement
         */
        public Object createDynamicElement(String theName) throws BuildException
        {
            Container container = this.factory.createContainer(theName);
            this.containers.add(container);
            return container;
        }

        /**
         * @see org.apache.tools.ant.DynamicConfigurator#setDynamicAttribute
         */
        public void setDynamicAttribute(String theName, String theValue)
            throws BuildException
        {
            throw new BuildException("Attribute [" + theName
                + "] not supported");
        }

        // Public Methods ------------------------------------------------------

        /**
         * Returns an iterator over the nested container elements, in the order
         * they appear in the build file.
         * 
         * @return An iterator over the nested container elements
         */
        public Container[] getContainers()
        {
            Container[] containers = (Container[])
                this.containers.toArray(new Container[this.containers.size()]);
            if (this.proxyPort > 0)
            {
                for (int i = 0; i < containers.length; i++)
                {
                    containers[i] = new ContainerWrapper(containers[i])
                    {
                        public int getPort()
                        {
                            return proxyPort;
                        }
                    };
                }
            }
            return containers;
        }

        /**
         * Returns the timeout after which connecting to a container will be
         * given up.
         * 
         * @return The timeout in milliseconds
         */
        public long getTimeout()
        {
            return this.timeout;
        }

        /**
         * Sets the timeout after which connecting to a container will be given
         * up.
         * 
         * @param theTimeout The timeout in milliseconds
         */
        public void setTimeout(long theTimeout)
        {
            this.timeout = theTimeout;
        }

        /**
         * Sets the proxy port which will be used by the test caller instead 
         * of the real container port. This can be used to insert protocol 
         * tracers between the test caller and the container.
         * 
         * @param theProxyPort The proxy port to set
         */
        public void setProxyPort(int theProxyPort)
        {
            this.proxyPort = theProxyPort;
        }

    }

    // Instance Variables ------------------------------------------------------

    /**
     * The nested containerset element.
     */
    private ContainerSet containerSet;

    /**
     * The archive that contains the enterprise application that should be
     * tested.
     */
    private File earFile;

    /**
     * The archive that contains the web-app that is ready to be tested.
     */
    private File warFile;

    /**
     * The factory for creating ant tasks that is passed to the containers.
     */
    private transient AntTaskFactory antTaskFactory = new AntTaskFactory()
    {
        public Task createTask(String theName)
        {
            Task retVal = getProject().createTask(theName);
            if (retVal != null)
            {
                retVal.setTaskName(getTaskName());
                retVal.setLocation(getLocation());
                retVal.setOwningTarget(getOwningTarget());
            }
            return retVal;
        }
    };

    // Constructors ------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @throws Exception If the constructor of JUnitTask throws an exception
     */
    public CactusTask() throws Exception
    {
    }

    // Public Methods ----------------------------------------------------------

    /**
     * @see org.apache.tools.ant.Task#init()
     */
    public void init()
    {
        super.init();
        
        addClasspathEntry("/org/aspectj/lang/JoinPoint.class");
        addClasspathEntry("/org/apache/cactus/ServletTestCase.class");
        addClasspathEntry(
            "/org/apache/cactus/integration/ant/CactusTask.class");
        addClasspathEntry("/org/apache/commons/logging/Log.class");
        addClasspathEntry("/org/apache/commons/httpclient/HttpClient.class");
    }

    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException
    {
        if ((this.warFile == null) && (this.earFile == null))
        {
            throw new BuildException("You must specify either the [warfile] or "
                + "the [earfile] attribute");
        }

        // Open the archive as JAR file and extract the deployment descriptor
        JarInputStream war = null;
        WebXml webXml = null;
        try
        {
            if (this.warFile != null)
            {
                war = new JarInputStream(new FileInputStream(this.warFile));
            }
            else
            {
                war = getTestWar(this.earFile);
                if (war == null)
                {
                    throw new BuildException("Could not find cactified web "
                        + "module in EAR " + this.earFile);
                }
            }
            webXml = WebXmlIo.parseWebXmlFromWar(war, null);
            addRedirectorNameProperties(war, webXml);
        }
        catch (SAXException e)
        {
            throw new BuildException(
                "Parsing of deployment descriptor failed", e);
        }
        catch (IOException e)
        {
            throw new BuildException("Failed to open WAR", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new BuildException("XML parser configuration error", e);
        }
        finally
        {
            if (war != null)
            {
                try
                {
                    war.close();
                }
                catch (IOException ioe)
                {
                    // pass the original exception
                }
            }
        }

        Container[] containers = this.containerSet.getContainers();
        if (containers.length == 0)
        {
            log("No containers specified, tests will run locally",
                Project.MSG_VERBOSE);
            super.execute();
        }
        else
        {
            Variable contextUrl = new Variable();
            contextUrl.setKey("cactus.contextURL");
            addSysproperty(contextUrl);
            for (int i = 0; i < containers.length; i++)
            {
                containers[i].setAntTaskFactory(this.antTaskFactory);
                containers[i].setLog(new AntLog(this));
                containers[i].setDeployableFile(
                    this.earFile != null ? this.earFile : this.warFile);
                if (containers[i].isEnabled())
                {
                    containers[i].init();
                    log("--------------------------------------------------"
                        + "---------------",
                        Project.MSG_INFO);
                    log("Running tests against " + containers[i].getName(),
                        Project.MSG_INFO);
                    log("--------------------------------------------------"
                        + "---------------",
                        Project.MSG_INFO);
                    contextUrl.setValue(
                        "http://localhost:" + containers[i].getPort() + "/"
                        + containers[i].getContextPath());
                    executeInContainer(containers[i], war, webXml);
                }
            }
        }
    }

    /**
     * Adds the nested containers element (only one is permitted).
     * 
     * @param theContainerSet The nested element to add
     */
    public final void addContainerSet(ContainerSet theContainerSet)
    {
        if (this.containerSet != null)
        {
            throw new BuildException(
                "Only one nested containerset element supported");
        }
        this.containerSet = theContainerSet;
    }

    /**
     * Sets the enterprise application archive that will be tested. It must
     * already contain the test-cases and the required libraries as a web
     * module.
     * 
     * @param theEarFile The EAR file to set  
     */
    public final void setEarFile(File theEarFile)
    {
        if (this.warFile != null)
        {
            throw new BuildException(
                "You may only specify one of [earfile] and [warfile]");
        }
        this.earFile = theEarFile;
    }

    /**
     * Sets the web application archive that will be tested. It must already 
     * contain the test-cases and the required libraries.
     * 
     * @param theWarFile The WAR file to set  
     */
    public final void setWarFile(File theWarFile)
    {
        if (this.earFile != null)
        {
            throw new BuildException(
                "You may only specify one of [earfile] and [warfile]");
        }
        this.warFile = theWarFile;
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Adds a Cactus system property.
     * 
     * @param theKey The property name (not including the 'cactus.' prefix)
     * @param theValue The property value
     */
    private void addCactusProperty(String theKey, String theValue)
    {
        log("Adding Cactus system property 'cactus." + theKey + "' with value '"
            + theValue + "'", Project.MSG_VERBOSE);
        Variable sysProperty = new Variable();
        sysProperty.setKey("cactus." + theKey);
        sysProperty.setValue(theValue);
        addSysproperty(sysProperty);
    }

    /**
     * Extracts the redirector mappings from the deployment descriptor and sets 
     * the corresponding system properties.
     * 
     * @param theWar The web-app archive
     * @param theWebXml The parsed deployment descriptor
     * @throws IOException If an I/O error occurred reading the WAR file
     */
    private void addRedirectorNameProperties(JarInputStream theWar,
        WebXml theWebXml)
        throws IOException
    {
        String filterRedirectorMapping =
            getFilterRedirectorMapping(theWar, theWebXml);
        if (filterRedirectorMapping != null)
        {
            addCactusProperty("filterRedirectorName",
                filterRedirectorMapping.substring(1));
        }
        else
        {
            log("No mapping of the filter redirector found",
                Project.MSG_VERBOSE);
        }
        String jspRedirectorMapping =
            getJspRedirectorMapping(theWar, theWebXml);
        if (jspRedirectorMapping != null)
        {
            addCactusProperty("jspRedirectorName",
                jspRedirectorMapping.substring(1));
        }
        else
        {
            log("No mapping of the JSP redirector found",
                Project.MSG_VERBOSE);
        }
        String servletRedirectorMapping =
            getServletRedirectorMapping(theWar, theWebXml);
        if (servletRedirectorMapping != null)
        {
            addCactusProperty("servletRedirectorName",
                servletRedirectorMapping.substring(1));
        }
        else
        {
            throw new BuildException("Mapping of the servlet redirector not "
                + "found in " + this.warFile);
        }
    }

    /**
     * Executes the unit tests in the given container.
     * 
     * @param theContainer The container to run the tests against
     * @param theWar The web-app archive
     * @param theWebXml The parsed deployment descriptor
     */
    private void executeInContainer(Container theContainer,
        JarInputStream theWar, WebXml theWebXml)
    {
        log("Starting up container", Project.MSG_VERBOSE);
        ContainerRunner runner = new ContainerRunner(theContainer);
        runner.setLog(new AntLog(this));
        try
        {
            URL url =
                new URL("http", "localhost", theContainer.getPort(), "/"
                + theContainer.getContextPath()
                + getServletRedirectorMapping(theWar, theWebXml)
                + "?Cactus_Service=RUN_TEST");
            runner.setUrl(url);
            if (this.containerSet.getTimeout() > 0)
            {
                runner.setTimeout(this.containerSet.getTimeout());
            }
            runner.startUpContainer();
            log("Container responding to HTTP requests as "
                + runner.getServerName(), Project.MSG_VERBOSE);
            Enumeration tests = getIndividualTests();
            while (tests.hasMoreElements())
            {
                JUnitTest test = (JUnitTest) tests.nextElement();
                if (test.shouldRun(getProject())
                 && !theContainer.isExcluded(test.getName()))
                {
                    if (theContainer.getToDir() != null)
                    {
                        test.setTodir(theContainer.getToDir());
                    }
                    execute(test);
                }
            }
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
        finally
        {
            log("Shutting down container", Project.MSG_VERBOSE);
            runner.shutDownContainer();
            log("Container shut down", Project.MSG_VERBOSE);
        }
    }

    /**
     * Returns the first URL-pattern to which the Cactus filter redirector is 
     * mapped in the deployment descriptor.
     * 
     * @param theWar The web-application archive
     * @param theWebXml The deployment descriptor
     * @return The mapping, or <code>null</code> if the filter redirector is not
     *         defined or mapped in the descriptor
     */
    private String getFilterRedirectorMapping(JarInputStream theWar,
        WebXml theWebXml)
    {
        Iterator filterNames = theWebXml.getFilterNamesForClass(
            "org.apache.cactus.server.FilterTestRedirector");
        if (filterNames.hasNext())
        {
            // we only care about the first definition and the first mapping
            String name = (String) filterNames.next(); 
            Iterator mappings = theWebXml.getFilterMappings(name);
            if (mappings.hasNext())
            {
                return (String) mappings.next();
            }
        }
        return null;
    }

    /**
     * Returns the first URL-pattern to which the Cactus JSP redirector is 
     * mapped in the deployment descriptor.
     * 
     * @param theWar The web-application archive
     * @param theWebXml The deployment descriptor
     * @return The mapping, or <code>null</code> if the JSP redirector is not
     *         defined or mapped in the descriptor
     * @throws IOException If an I/O error occurred reading the WAR file
     */
    private String getJspRedirectorMapping(JarInputStream theWar,
        WebXml theWebXml)
        throws IOException
    {
        // To get the JSP redirector mapping, we must first get the full path to
        // the corresponding JSP file in the WAR
        String jspRedirectorPath =
            ResourceUtils.getResourcePath(theWar, "jspRedirector.jsp");
        if (jspRedirectorPath != null)
        {
            jspRedirectorPath = "/" + jspRedirectorPath;
            Iterator jspNames = theWebXml.getServletNamesForJspFile(
                jspRedirectorPath);
            if (jspNames.hasNext())
            {
                // we only care about the first definition and the first
                // mapping
                String name = (String) jspNames.next(); 
                Iterator mappings = theWebXml.getServletMappings(name);
                if (mappings.hasNext())
                {
                    return (String) mappings.next();
                }
            }
        }
        return null;
    }

    /**
     * Returns the first URL-pattern to which the Cactus servlet redirector is 
     * mapped in the deployment descriptor.
     * 
     * @param theWar The web-application archive
     * @param theWebXml The deployment descriptor
     * @return The mapping, or <code>null</code> if the servlet redirector is
     *         not defined or mapped in the descriptor
     */
    private String getServletRedirectorMapping(JarInputStream theWar,
        WebXml theWebXml)
    {
        Iterator servletNames = theWebXml.getServletNamesForClass(
            "org.apache.cactus.server.ServletTestRedirector");
        if (servletNames.hasNext())
        {
            // we only care about the first definition and the first mapping
            String name = (String) servletNames.next(); 
            Iterator mappings = theWebXml.getServletMappings(name);
            if (mappings.hasNext())
            {
                return (String) mappings.next();
            }
        }
        return null;
    }

    /**
     * Finds the web module in the enterprise application archive that contains
     * the servlet test redirector.
     * 
     * @param theEarFile The enterprise application archive
     * @return The input stream for the WAR containing the tests
     */
    private JarInputStream getTestWar(File theEarFile)
    {
        JarInputStream ear = null;
        try {
            ear = new JarInputStream(new FileInputStream(theEarFile));
            ApplicationXml applicationXml =
                ApplicationXmlIo.parseApplicationXmlFromEar(ear, null);
            for (Iterator i = applicationXml.getWebModuleUris(); i.hasNext();)
            {
                JarInputStream jar = new JarInputStream(
                    ResourceUtils.getResource(ear, (String) i.next()));
                WebXml webXml = WebXmlIo.parseWebXml(jar, null);
                if (getServletRedirectorMapping(jar, webXml) != null)
                {
                    return jar;
                }
            }
        }
        catch (SAXException e)
        {
            throw new BuildException(
                "Parsing of deployment descriptor failed", e);
        }
        catch (IOException e)
        {
            throw new BuildException("Failed to open WAR", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new BuildException("XML parser configuration error", e);
        }
        finally
        {
            if (ear != null)
            {
                try
                {
                    ear.close();
                }
                catch (IOException ioe)
                {
                    // pass the original exception, if any
                }
            }
        }
        return null;
    }

}
