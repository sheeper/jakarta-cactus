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
package org.apache.cactus.integration.ant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.cactus.integration.ant.container.Container;
import org.apache.cactus.integration.ant.container.ContainerRunner;
import org.apache.cactus.integration.ant.container.DeployableFile;
import org.apache.cactus.integration.ant.container.EarParser;
import org.apache.cactus.integration.ant.container.WarParser;
import org.apache.cactus.integration.ant.util.AntLog;
import org.apache.cactus.integration.ant.util.AntTaskFactory;
import org.apache.cactus.integration.ant.util.PropertySet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * An Ant task that extends the optional JUnit task to provide support for
 * in-container testing.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
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
     * System properties that will be set in the container JVM.
     */
    private List systemProperties = new ArrayList();
    
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
            deployableFile = WarParser.parse(this.warFile);
        }
        else 
        {
            deployableFile = EarParser.parse(this.earFile);
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

                // Clone the DeployableFile instance as each container can
                // override default deployment properties (e.g. port, context
                // root, etc).
                DeployableFile thisDeployable = null;
                try
                {
                    thisDeployable = (DeployableFile) deployableFile.clone();
                }
                catch (CloneNotSupportedException e)
                {
                    throw new BuildException(e);
                }
                containers[i].setDeployableFile(thisDeployable);

                // Allow the container to override the default test context. 
                // This is to support container extensions to the web.xml file.
                // Most containers allow defining the root context in these
                // extensions.
                if (containers[i].getTestContext() != null)
                {
                    thisDeployable.setTestContext(
                        containers[i].getTestContext());
                }               
                
                containers[i].setSystemProperties(
                    (Variable[]) this.systemProperties.toArray(
                        new Variable[0]));
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
                        + thisDeployable.getTestContext());
                    executeInContainer(containers[i], thisDeployable); 
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

    /**
     * Adds a system property. Note that we can't reuse the JUnitTask
     * sysproperty element as there is no getter to get them.
     * 
     * @see JUnitTask#addSysproperty(Environment.Variable) 
     */
    public void addSysproperty(Environment.Variable theProperty)
    {
        addCactusServerProperty(theProperty);
        super.addSysproperty(theProperty);
    }

    /**
     * Called by Ant when the Variable object has been properly initialized.
     * 
     * @param theProperty the system property to set 
     */
    public void addConfiguredSysproperty(Environment.Variable theProperty)
    {
        addSysproperty(theProperty);
    }

    /**
     * Adds a set of properties that will be used as system properties
     * either on the client side or on the server side.
     *
     * @param thePropertySet the set of properties to be added
     */
    public void addConfiguredCactusproperty(PropertySet thePropertySet)
    {
        // Add all properties from the properties file
        ResourceBundle bundle = thePropertySet.readProperties();
        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            Variable var = new Variable();
            var.setKey(key);
            var.setValue(bundle.getString(key));
            if (thePropertySet.isServer())
            {
                addCactusServerProperty(var);
            }
            else
            {
                super.addSysproperty(var);
            }
        }
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Adds a Cactus system property for the client side JVM.
     * 
     * @param theKey The property name
     * @param theValue The property value
     */
    private void addCactusClientProperty(String theKey, String theValue)
    {
        log("Adding Cactus client system property [" + theKey 
            + "] with value [" + theValue + "]", Project.MSG_VERBOSE);
        Variable sysProperty = new Variable();
        sysProperty.setKey(theKey);
        sysProperty.setValue(theValue);
        super.addSysproperty(sysProperty);
    }

    /**
     * Adds a Cactus system property for the server side JVM.
     * 
     * @param theProperty The system property to set in the container JVM
     */
    private void addCactusServerProperty(Variable theProperty)
    {
        log("Adding Cactus server system property [" 
            + theProperty.getKey() + "] with value [" 
            + theProperty.getValue() + "]", Project.MSG_VERBOSE);
        this.systemProperties.add(theProperty);
    }

    /**
     * Adds a Cactus system property for the server side JVM.
     * 
     * @param theKey The property name
     * @param theValue The property value
     */
    private void addCactusServerProperty(String theKey, String theValue)
    {
        Variable property = new Variable();
        property.setKey(theKey);
        property.setValue(theValue);
        addCactusServerProperty(property);
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
            addCactusClientProperty("cactus.filterRedirectorName",
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
            addCactusClientProperty("cactus.jspRedirectorName",
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
            addCactusClientProperty("cactus.servletRedirectorName",
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
