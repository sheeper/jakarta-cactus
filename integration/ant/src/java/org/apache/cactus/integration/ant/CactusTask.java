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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.container.Container;
import org.apache.cactus.integration.ant.container.ContainerRunner;
import org.apache.cactus.integration.ant.util.AntLog;
import org.apache.cactus.integration.ant.util.AntTaskFactory;
import org.apache.cactus.integration.ant.deployment.ApplicationXml;
import org.apache.cactus.integration.ant.deployment.EarArchive;
import org.apache.cactus.integration.ant.deployment.WarArchive;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
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
    private AntTaskFactory antTaskFactory = new AntTaskFactory()
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
        WarArchive war = null;
        String contextPath = null;
        try
        {
            if (this.warFile != null)
            {
                war = new WarArchive(this.warFile);
                contextPath = this.warFile.getName();
                int warIndex = contextPath.toLowerCase().lastIndexOf(".war");
                if (warIndex >= 0)
                {
                    contextPath = contextPath.substring(0, warIndex);
                }
            }
            else
            {
                EarArchive ear = new EarArchive(this.earFile);
                String webUri = getUriOfCactifiedWebModule(ear);
                if (webUri == null)
                {
                    throw new BuildException("Could not find cactified web "
                        + "module in the EAR");
                }
                war = ear.getWebModule(webUri);
                if (war == null)
                {
                    throw new BuildException("Could not find the WAR " + webUri
                        + " in the EAR");
                }
                contextPath =
                    ear.getApplicationXml().getWebModuleContextRoot(webUri);
            }
            addRedirectorNameProperties(war);
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

        if (this.containerSet == null)
        {
            log("No containers specified, tests will run locally",
                Project.MSG_VERBOSE);
            super.execute();
        }
        else
        {
            Container[] containers = this.containerSet.getContainers();
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
                        + contextPath);
                    executeInContainer(containers[i], war, contextPath);
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
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    private void addRedirectorNameProperties(WarArchive theWar)
        throws SAXException, IOException, ParserConfigurationException
    {
        String filterRedirectorMapping = getFilterRedirectorMapping(theWar);
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
        String jspRedirectorMapping = getJspRedirectorMapping(theWar);
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
        String servletRedirectorMapping = getServletRedirectorMapping(theWar);
        if (servletRedirectorMapping != null)
        {
            addCactusProperty("servletRedirectorName",
                servletRedirectorMapping.substring(1));
        }
        else
        {
            throw new BuildException("The WAR has not been cactified");
        }
    }

    /**
     * Executes the unit tests in the given container.
     * 
     * @param theContainer The container to run the tests against
     * @param theWar The web-app archive
     * @param theContextPath The context path to which the test web-app will be
     *        deployed
     */
    private void executeInContainer(Container theContainer, WarArchive theWar,
        String theContextPath)
    {
        log("Starting up container", Project.MSG_VERBOSE);
        ContainerRunner runner = new ContainerRunner(theContainer);
        runner.setLog(new AntLog(this));
        try
        {
            URL url =
                new URL("http", "localhost", theContainer.getPort(), "/"
                + theContextPath + getServletRedirectorMapping(theWar)
                + "?Cactus_Service=RUN_TEST");
            runner.setUrl(url);
            if (this.containerSet.getTimeout() > 0)
            {
                runner.setTimeout(this.containerSet.getTimeout());
            }
            runner.startUpContainer();
            log("Container responding to HTTP requests as "
                + runner.getServerName(), Project.MSG_VERBOSE);
            try
            {
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
            finally
            {
                log("Shutting down container", Project.MSG_VERBOSE);
                runner.shutDownContainer();
                log("Container shut down", Project.MSG_VERBOSE);
            }
        }
        catch (MalformedURLException mue)
        {
            throw new BuildException("Malformed test URL", mue);
        }
        catch (SAXException saxe)
        {
            throw new BuildException("Error parsing a deployment descriptor",
                saxe);
        }
        catch (IOException ioe)
        {
            throw new BuildException(ioe);
        }
        catch (ParserConfigurationException pce)
        {
            throw new BuildException(pce);
        }
    }

    /**
     * Returns the first URL-pattern to which the Cactus filter redirector is 
     * mapped in the deployment descriptor.
     * 
     * @param theWar The web-application archive
     * @return The mapping, or <code>null</code> if the filter redirector is not
     *         defined or mapped in the descriptor
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    private String getFilterRedirectorMapping(WarArchive theWar)
        throws IOException, SAXException, ParserConfigurationException
    {
        Iterator filterNames = theWar.getWebXml().getFilterNamesForClass(
            "org.apache.cactus.server.FilterTestRedirector");
        if (filterNames.hasNext())
        {
            // we only care about the first definition and the first mapping
            String name = (String) filterNames.next(); 
            Iterator mappings = theWar.getWebXml().getFilterMappings(name);
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
     * @return The mapping, or <code>null</code> if the JSP redirector is not
     *         defined or mapped in the descriptor
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    private String getJspRedirectorMapping(WarArchive theWar)
        throws IOException, SAXException, ParserConfigurationException
    {
        // To get the JSP redirector mapping, we must first get the full path to
        // the corresponding JSP file in the WAR
        String jspRedirectorPath = theWar.findResource("jspRedirector.jsp");
        if (jspRedirectorPath != null)
        {
            jspRedirectorPath = "/" + jspRedirectorPath;
            Iterator jspNames = theWar.getWebXml().getServletNamesForJspFile(
                jspRedirectorPath);
            if (jspNames.hasNext())
            {
                // we only care about the first definition and the first
                // mapping
                String name = (String) jspNames.next(); 
                Iterator mappings = theWar.getWebXml().getServletMappings(name);
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
     * @return The mapping, or <code>null</code> if the servlet redirector is
     *         not defined or mapped in the descriptor
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    private String getServletRedirectorMapping(WarArchive theWar)
        throws SAXException, IOException, ParserConfigurationException
    {
        Iterator servletNames = theWar.getWebXml().getServletNamesForClass(
            "org.apache.cactus.server.ServletTestRedirector");
        if (servletNames.hasNext())
        {
            // we only care about the first definition and the first mapping
            String name = (String) servletNames.next(); 
            Iterator mappings = theWar.getWebXml().getServletMappings(name);
            if (mappings.hasNext())
            {
                return (String) mappings.next();
            }
        }
        return null;
    }

    /**
     * Finds the web module in the enterprise application archive that contains
     * the servlet test redirector, and returns the web-uri of the module found.
     * 
     * <em>A web-app is considered cactified when it contains at least a mapping
     * for the Cactus servlet test redirector</em>
     * 
     * @param theEar The enterprise application archive
     * @return The URI of the cactified web-module, or <code>null</code> if no
     *         cactified web-app could be found
     */
    private String getUriOfCactifiedWebModule(EarArchive theEar)
    {
        try
        {
            ApplicationXml applicationXml = theEar.getApplicationXml();
            for (Iterator i = applicationXml.getWebModuleUris(); i.hasNext();)
            {
                String webUri = (String) i.next();
                WarArchive war = theEar.getWebModule(webUri);
                if ((war != null) && (getServletRedirectorMapping(war) != null))
                {
                    return webUri;
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
        return null;
    }

}
