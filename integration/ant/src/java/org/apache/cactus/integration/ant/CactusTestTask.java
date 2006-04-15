/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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

import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import org.apache.cactus.integration.ant.util.HttpProbe;
import org.apache.cactus.integration.ant.deployment.DeployableFile;
import org.apache.cactus.integration.ant.deployment.EarParser;
import org.apache.cactus.integration.ant.deployment.WarParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * An Ant task that extends the optional JUnit task to provide support for
 * in-container testing. 
 * This class is a refactor of CactusTask v.133 and
 * RunContainerTask v.133 to use cargo
 * 
 * @version $Id$
 * @since 1.8
 */
public class CactusTestTask extends JUnitTask
{
    /**
     * The servlet port element.
     */
    private String serverName;

    /**
     * The servlet port element.
     */
    private String servletPort;

    /**
     * The context URL element.
     */
    private String contextURL;

    /**
     * The testreportDir element.
     */
    private File toDir;

    /**
     * The log element.
     */
    private File logs;

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
     * The runLocal element.
     */
    private boolean runLocal;


    /**
     * The deployable file.
     */
    private DeployableFile deployableFile;

    /**
     * Additional classpath entries for the classpath that will be used to start
     * the containers.
     */
    private Path additionalClasspath;

    /**
     * Timeout after trying to connect(in ms).
     */
    private long timeout = 180000;

    /**
     * The time interval in milliseconds to sleep between polling the container.
     */
    private long checkInterval = 500;


    // Constructors -----------------------------------------------------------

    /**
     * Constructor.
     *
     * @throws Exception
     *             If the constructor of JUnitTask throws an exception
     */
    public CactusTestTask() throws Exception
    {
        super();
    }

    // Public Methods -------------------------------------------------------

    /**
     * @see org.apache.tools.ant.Task#init()
     */
    public void init()
    {
        super.init();

        addClasspathEntry("/org/apache/cactus/ServletTestCase.class");
        addClasspathEntry("/org/apache/commons/logging/Log.class");
        addClasspathEntry("/org/apache/commons/httpclient/HttpClient.class");
    }

    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException
    {
        if ((this.warFile != null) && (this.earFile != null))
        {
             throw new BuildException(
                    "You must specify either the [warfile] or "
                          + "the [earfile] attribute but not both");
        }
        if ((this.warFile == null) && (this.earFile == null))
        {
            throw new BuildException("You must specify either the [warfile] or "
                + "the [earfile] attribute");
        }
        // Parse deployment descriptors for WAR or EAR files
        if (this.warFile != null)
        {
            deployableFile = WarParser.parse(this.warFile);
        }
        else
        {
            deployableFile = EarParser.parse(this.earFile);
        }

        addRedirectorNameProperties();

        if (runLocal)
        {
           log("Tests run locally", Project.MSG_VERBOSE);
           super.execute();
           return;
        }

        if (this.contextURL == null)
        {
            if (this.servletPort == null)
            {
                 log("Using default servletport=8080", Project.MSG_INFO);
                 servletPort = "8080";

            }
            if (this.serverName == null)
            {
                log("Using default servername=localhost", Project.MSG_INFO);
                serverName = "localhost";
            }
        }

        if (this.toDir == null)
        {
            throw new BuildException(
                    "You must specify the test report directory");

        }


        Variable contextUrlVar = new Variable();
        contextUrlVar.setKey("cactus.contextURL");
        if (this.contextURL != null)
        {
           contextUrlVar.setValue(this.contextURL);
        }
        else
        {
            contextUrlVar.setValue("http://" + this.serverName + ":"
                    + this.servletPort
                    + "/" + deployableFile.getTestContext());
        }

        addSysproperty(contextUrlVar);

        //Setup logs
        setupLogs();
        //Run the test cases
        testInContainer();
    }

    /**
     * Extracts the redirector mappings from the deployment descriptor and sets
     * the corresponding system properties.
     */
    private void addRedirectorNameProperties()
    {
        String filterRedirectorMapping = deployableFile
                .getFilterRedirectorMapping();
        if (filterRedirectorMapping != null)
        {
            Variable filterRedirectorVar = new Variable();
            filterRedirectorVar.setKey("cactus.filterRedirectorName");
            filterRedirectorVar.setValue(filterRedirectorMapping
                    .substring(1));
            addSysproperty(filterRedirectorVar);
        }
        else
        {
            log("No mapping of the filter redirector found",
                    Project.MSG_VERBOSE);
        }

        String jspRedirectorMapping = deployableFile.getJspRedirectorMapping();
        if (jspRedirectorMapping != null)
        {
            Variable jspRedirectorVar = new Variable();
            jspRedirectorVar.setKey("cactus.jspRedirectorName");
            jspRedirectorVar.setValue(jspRedirectorMapping.substring(1));
            addSysproperty(jspRedirectorVar);
        }
        else
        {
            log("No mapping of the JSP redirector found",
                    Project.MSG_VERBOSE);
        }

        String servletRedirectorMapping = deployableFile
                .getServletRedirectorMapping();
        if (servletRedirectorMapping != null)
        {
            Variable servletRedirectorVar = new Variable();
            servletRedirectorVar.setKey("cactus.servletRedirectorName");
            servletRedirectorVar.setValue(servletRedirectorMapping
                    .substring(1));
            addSysproperty(servletRedirectorVar);
        }
        else
        {
            throw new BuildException("The WAR has not been cactified");
        }

    }


    /**
     * Executes the unit tests in the given container.
     *
     */
    private void testInContainer()
    {
        URL testURL = null;
        try
        {
            if (this.contextURL == null)
            {
                testURL = new URL("http://" + this.serverName + ":"
                        + this.servletPort + "/"
                        + deployableFile.getTestContext()
                        + deployableFile.getServletRedirectorMapping()
                        + "?Cactus_Service=RUN_TEST");
            }
            else
            {
                testURL = new URL(this.contextURL
                        + deployableFile.getServletRedirectorMapping()
                        + "?Cactus_Service=RUN_TEST");
            }


        }
        catch (MalformedURLException e)
        {
            throw new BuildException("Invalid URL format: " + testURL);
        }
        //Ping the container
        //Continuously try calling the test URL until it succeeds or
        // until a timeout is reached (we then throw a build exception).
        try
        {
            HttpProbe httpProbe = new HttpProbe(testURL);
            if (httpProbe.timeout(this.timeout, this.checkInterval))
            {
                throw new BuildException("Failed to start the container after "
                    + "more than [" + this.timeout + "] ms. Trying to connect "
                    + "to the [" + testURL + "] test URL yielded an "
                    + "error code. Please run in debug mode for more details  "
                    + "about the error.");
            }
        }
        catch (InterruptedException ie)
        {
            throw new BuildException("Unexpected thread interruption");
        }
        catch (IOException io)
        {
            throw new BuildException("Http reading exception");
        }

        log("Starting up tests", Project.MSG_VERBOSE);
        try
        {

            Enumeration tests = getIndividualTests();
            while (tests.hasMoreElements())
            {
                JUnitTest test = (JUnitTest) tests.nextElement();
                if (test.shouldRun(getProject()))
                {
                    test.setTodir(toDir);
                    execute(test);
                }
            }
        }
        finally
        {
            log("Finishing tests", Project.MSG_VERBOSE);

        }
    }

    /**
     * Sets the enterprise application archive that will be tested. It must
     * already contain the test-cases and the required libraries as a web
     * module.
     *
     * @param theEarFile
     *            The EAR file to set
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
     * @param theWarFile
     *            The WAR file to set
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
     * Sets the server host that will be tested.
     *
     * @param theServerName
     *            The server host
     */
    public final void setServerName(String theServerName)
    {
        this.serverName = theServerName;
    }

    /**
     * Sets the servlet port that will be tested.
     *
     * @param theServletPort
     *            The servlet port
     */
    public final void setServletPort(String theServletPort)
    {
        this.servletPort = theServletPort;
    }

    /**
     * Sets the context url that will be tested.
     *
     * @param theContextURL
     *            The context url
     */
    public final void setContextURL(String theContextURL)
    {
        this.contextURL = theContextURL;
    }

    /**
     * Sets the test report dir.
     *
     * @param theToDir
     *            The test report to set
     */
    public final void setToDir(File theToDir)
    {
        this.toDir = theToDir;
    }

    /**
    * Sets the scope of the test.
     *
     * @param theRunLocal
     *            Run Local define
     */
    public final void setRunLocal(boolean theRunLocal)
    {
        this.runLocal = theRunLocal;
    }

    /**
     * Sets the Logs.
     *
     * @param theLogs
     *            Different logs define
     */
    public final void setLogs(File theLogs)
    {
        this.logs = theLogs;
    }
    // Private Methods ---------------------------------------------------------


    /**
     * @param theTimeout the timeout after which we stop trying to call the test
     *        URL.
     */
    public void setTimeout(long theTimeout)
    {
        this.timeout = theTimeout;
    }
    /**
     * Set up the logs
     */
    public void setupLogs()
    {

        if (this.logs == null)
        {
            throw new BuildException("Missing 'logs' attribute");
        }

        ResourceBundle bundle = null;
        try
        {
            bundle = new PropertyResourceBundle(
                new FileInputStream(this.logs));
        }
        catch (IOException e)
        {
            throw new BuildException("Failed to load properties "
                + "file [" + this.logs + "]");
        }
        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            Variable var = new Variable();
            var.setKey(key);
            var.setValue(bundle.getString(key));
            super.addSysproperty(var);

        }

    }
}
