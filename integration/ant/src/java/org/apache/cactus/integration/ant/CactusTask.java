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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import org.apache.cactus.integration.ant.container.Container;
import org.apache.cactus.integration.ant.container.ContainerRunner;
import org.apache.cactus.integration.ant.container.DeployableFile;
import org.apache.cactus.integration.ant.container.EarDeployableFile;
import org.apache.cactus.integration.ant.container.WarDeployableFile;
import org.apache.cactus.integration.ant.util.AntLog;
import org.apache.cactus.integration.ant.util.AntTaskFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.types.Environment.Variable;

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
        // TODO: Fix comment for this constructor as it doesn't seem quite 
        // right. Explain why we don't call the super constructor?
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

        if ((this.warFile != null) && (this.earFile != null))
        {
            throw new BuildException("You must specify either the [warfile] or "
                + "the [earfile] attribute but not both");
        }

        // Parse deployment descriptors for WAR or EAR files
        DeployableFile deployableFile;
        if (this.warFile != null)
        {
            deployableFile = new WarDeployableFile(this.warFile);
        }
        else 
        {
            deployableFile = new EarDeployableFile(this.earFile);
        } 

        addRedirectorNameProperties(deployableFile);

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
                containers[i].setDeployableFile(deployableFile);
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
                        + deployableFile.getTestContext());
                    executeInContainer(containers[i], deployableFile); 
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
     * @param theFile The file to deploy in the container
     */
    private void addRedirectorNameProperties(DeployableFile theFile)
    {
        String filterRedirectorMapping = 
            theFile.getFilterRedirectorMapping();
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
            theFile.getJspRedirectorMapping();
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
            theFile.getServletRedirectorMapping();
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
     * @param theFile the file to deploy in the container
     */
    private void executeInContainer(Container theContainer, 
        DeployableFile theFile)
    {
        log("Starting up container", Project.MSG_VERBOSE);
        ContainerRunner runner = new ContainerRunner(theContainer);
        runner.setLog(new AntLog(this));
        try
        {
            URL url =
                new URL("http", "localhost", theContainer.getPort(), "/"
                + theFile.getTestContext() 
                + theFile.getServletRedirectorMapping()
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
    }
}
