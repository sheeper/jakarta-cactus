/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.container.resin;

import java.io.File;
import java.net.URL;

import org.apache.cactus.integration.ant.container.ContainerRunner;
import org.apache.cactus.integration.ant.deployment.EarParser;
import org.apache.cactus.integration.ant.deployment.WarParser;
import org.apache.cactus.integration.ant.util.DefaultAntTaskFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

/**
 * Task to start/stop a Resin instance.
 * 
 * @since Cactus 1.7
 * @version $Id$
 */
public abstract class AbstractResinTask extends Task
{
    /**
     * The mandatory Resin installation directory.
     */
    private File dir;

    /**
     * The action that will be executed by this task
     * @see #setAction(String)
     */
    private String action;

    /**
     * The archive that contains the enterprise application that will be
     * deployed
     */
    private File earFile;

    /**
     * The archive that contains the web-app that will be deployed.
     */
    private File warFile;

    /**
     * URL used to verify if the container is started.
     */
    private URL testURL;

    /**
     * A user-specific resin.conf configuration file. If this variable is not
     * set, the default configuration file from the JAR resources will be used.
     */
    private File resinConf;

    /**
     * The port to which the container should be bound.
     */
    private int port = 8080;

    /**
     * The temporary directory from which the container will be started.
     */
    private File tmpDir;

    /**
     * The file to which output of the container should be written.
     */
    private File output;

    /**
     * Whether output of the container should be appended to an existing file, 
     * or the existing file should be truncated.
     */
    private boolean append;

    /**
     * Additional classpath entries for the classpath that will be used to 
     * start the containers.
     */
    private Path containerClasspath;

    /**
     * Sets the Resin installation directory.
     * 
     * @param theDir The directory to set
     */
    public final void setDir(File theDir)
    {
        this.dir = theDir;
    }
    
    /**
     * Sets the action to execute (either "start" or "stop").
     * 
     * @param theAction the action that will be executed by this task
     */
    public void setAction(String theAction)
    {
        this.action = theAction;
    }
    
    /**
     * Sets a web application archive to deploy in the container.
     * 
     * @param theWarFile The WAR file to deploy  
     */
    public final void setWarFile(File theWarFile)
    {
        if (getEarFile() != null)
        {
            throw new BuildException(
                "You may only specify one of [earfile] and [warfile]");
        }
        this.warFile = theWarFile;
    }

    /**
     * Sets an enterprise application aarchive to deploy.
     * 
     * @param theEarFile The EAR file to deploy  
     */
    public final void setEarFile(File theEarFile)
    {
        if (getWarFile() != null)
        {
            throw new BuildException(
                "You may only specify one of [earfile] and [warfile]");
        }
        this.earFile = theEarFile;
    }

    /**
     * Sets the URL to call for testing if the server is running.
     *
     * @param theTestURL the test URL to ping
     */
    public void setTestURL(URL theTestURL)
    {
        this.testURL = theTestURL;
    }

    /**
     * Sets the temporary directory from which the container is run.
     * 
     * @param theTmpDir The temporary directory to set
     */
    public final void setTmpDir(File theTmpDir)
    {
        this.tmpDir = theTmpDir;
    }
    
    /**
     * Sets the configuration file to use for the test installation of Resin
     * 
     * @param theResinConf The resin.conf file
     */
    public final void setResinConf(File theResinConf)
    {
        this.resinConf = theResinConf;
    }

    /**
     * Sets the port to which the container should listen.
     * 
     * @param thePort The port to set
     */
    public final void setPort(int thePort)
    {
        this.port = thePort;
    }

    /**
     * Sets the file to which output of the container should be written.
     * 
     * @param theOutput The output file to set
     */
    public final void setOutput(File theOutput)
    {
        this.output = theOutput;
    }

    /**
     * Sets whether output of the container should be appended to an existing
     * file, or the existing file should be truncated.
     * 
     * @param isAppend Whether output should be appended
     */
    public final void setAppend(boolean isAppend)
    {
        this.append = isAppend;
    }
    
    /**
     * Checks if the task is correctly initialized.
     * 
     * @param theContainer the Resin container to verify
     */
    private void verify(AbstractResinContainer theContainer)
    {
        theContainer.verify();
        
        if (getAction() == null)
        {
            throw new BuildException("You must specify an [action] attribute");
        }
        
        if (!getAction().equalsIgnoreCase("start") 
           && !getAction().equalsIgnoreCase("stop"))
        {
            throw new BuildException(
                "Valid actions are: [start] and [stop]");
        }
    }

    /**
     * @return the instance of a Resin container to start/stop
     */
    protected abstract AbstractResinContainer getResinContainer();
    
    /**
     * Start or stop the container depending on the action asked by the user.
     * When starting the container, also prepare a valid container 
     * configuration and optionally deploy a war or ear in it.
     * 
     * @see Task#execute()
     */
    public void execute()
    {
        // Resin container that prepares, starts and stops a Resin
        // instance.
        AbstractResinContainer container = getResinContainer();

        // Sets the file to be deployed in the container.
        if (getWarFile() != null)
        {
            container.setDeployableFile(
                WarParser.parse(getWarFile()));
        }
        else if (getEarFile() != null) 
        {
            container.setDeployableFile(
                EarParser.parse(getEarFile()));
        } 

        container.setDir(getDir());
        container.setAntTaskFactory(new DefaultAntTaskFactory(
            getProject(), getTaskName(), getLocation(), getOwningTarget()));
        container.setPort(getPort());
        
        // Add specific additional user-defined classpath
        container.setContainerClasspath(this.containerClasspath);
        
        if (getResinConf() != null)
        {
            container.setResinConf(getResinConf());
        }

        if (getTmpDir() != null)
        {
            container.setTmpDir(getTmpDir());
        }

        if (getOutput() != null)
        {
            container.setOutput(getOutput());
        }

        if (getAppend())
        {
            container.setAppend(getAppend());
        }
        
        // Verify that the task is correctly set up.
        verify(container);

        // If the user has provided a test URL, we use a ContainerRunner
        // that continuously polls this test URL to verify if the container
        // is started. In that case, this task will only return when the
        // container is up and running. If no test URL has been started
        // we simply start the container and give back the control to the user
        // without waiting for container to be up and running.
        
        ContainerRunner runner = null;
        if (getTestURL() != null)
        {
            runner = new ContainerRunner(container);
            runner.setURL(getTestURL());
        }
        
        // Decide whether to start or stop the container
        if (getAction().equalsIgnoreCase("start"))
        {
            if (getTestURL() != null)
            {
                runner.startUpContainer();
            }
            else
            {
                container.startUp();
            }
        }
        else if (getAction().equalsIgnoreCase("stop"))
        {
            if (getTestURL() != null)
            {
                runner.shutDownContainer();
            }
            else
            {
                container.shutDown();
            }
        }
    }

    /**
     * @return the action to execute ("start" or "stop")
     */
    protected final String getAction()
    {
        return this.action;
    }

    /**
     * @return the directory where Resin is installed
     */
    protected final File getDir()
    {
        return this.dir;
    }

    /**
     * @return the test URL to ping to verify if the container is started
     */
    protected final URL getTestURL()
    {
        return this.testURL;
    }

    /**
     * @return the port to use to start the container
     */
    protected final int getPort()
    {
        return this.port;
    }

    /**
     * @return the configuration file to use for the test installation of Resin
     */
    protected final File getResinConf()
    {
        return this.resinConf;
    }

    /**
     * @return the WAR file to deploy or null if none
     */
    protected final File getWarFile()
    {
        return this.warFile;
    }

    /**
     * @return the EAR file to deploy or null if none
     */
    protected final File getEarFile()
    {
        return this.earFile;
    }

    /**
     * @return the temporary directory from which the container is run
     */
    protected final File getTmpDir()
    {
        return this.tmpDir;
    }

    /**
     * @return the file to which output of the container should be written
     */
    protected final File getOutput()
    {
        return this.output;
    }

    /**
     * @return whether output of the container should be appended to an 
     *         existing file, or the existing file should be truncated
     */
    protected final boolean getAppend()
    {
        return this.append;
    }

    /**
     * Adds container classpath to the classpath that will be used for starting
     * the container. 
     *
     * @return reference to the classpath
     */
    public Path createContainerClasspath()
    {
        if (this.containerClasspath == null)
        {
            this.containerClasspath = new Path(this.project);            
        }
        
        return this.containerClasspath.createPath();
    }    
}
