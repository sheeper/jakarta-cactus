/* 
 * ========================================================================
 * 
 * Copyright 2003-2005 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.container;

import java.io.File;

import org.apache.cactus.integration.ant.deployment.DeployableFile;
import org.apache.cactus.integration.ant.util.AntTaskFactory;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * Interface for classes that can be used as nested elements in the
 * <code>&lt;containers&gt;</code> element of the <code>&lt;cactus&gt;</code>
 * task.
 * 
 * @version $Id$
 */
public interface Container
{
    /**
     * Returns a displayable name of the container for logging purposes.
     * 
     * @return The container name
     */
    String getName();

    /**
     * Returns the port to which the container should listen.
     * 
     * @return The port
     */
    int getPort();

    /**
     * @return the context that the webapp will load on, null if the client
     * should determine on it's own
     */
    String getTestContext();
    
    /**
     * @return the time to wait after the container has been started up. 
     */
    long getStartUpWait();
    
    /**
     * Returns the value of the 'todir' attribute.
     * 
     * @return The output directory
     */
    File getToDir();

    /**
     * @return the list of system properties that will be set in the container 
     *         JVM
     */
    Variable[] getSystemProperties();
    
    /**
     * Subclasses should implement this method to perform any initialization
     * that may be necessary. This method is called before any of the accessors
     * or the methods {@link AbstractContainer#startUp} and
     * {@link AbstractContainer#shutDown} are called, but after all attributes
     * have been set.
     */
    void init();

    /**
     * Returns whether the container element is enabled, which is determined by
     * the evaluation of the if- and unless conditions
     * 
     * @return <code>true</code> if the container is enabled
     */
    boolean isEnabled();

    /**
     * Returns whether a specific test case is to be excluded from being run in
     * the container.
     * 
     * @param theTestName The fully qualified name of the test fixture class
     * @return <code>true</code> if the test should be excluded, otherwise
     *         <code>false</code>
     */
    boolean isExcluded(String theTestName);

    /**
     * Sets the factory to use for creating Ant tasks.
     * 
     * @param theFactory The factory to use for creating Ant tasks
     */
    void setAntTaskFactory(AntTaskFactory theFactory);

    /**
     * Sets the log which the implementation should use.
     *  
     * @param theLog The log to set
     */
    void setLog(Log theLog);

    /**
     * Sets the file that should be deployed to the container. This can be
     * either a WAR or an EAR file, depending on the capabilities of the
     * container.
     * 
     * @param theDeployableFile The file to deploy
     */
    void setDeployableFile(DeployableFile theDeployableFile);

    /**
     * Sets the system properties that will be passed to the JVM in which the
     * server will execute.
     * 
     * @param theProperties the list of system properties to set in the 
     *        container JVM
     */
    void setSystemProperties(Variable[] theProperties);

    /**
     * Sets additional classpath entries that will be added to the container
     * classpath used to start the containers.
     * 
     * @param theClasspath the container classpath entries to add
     * @since Cactus 1.6
     */
    void setContainerClasspath(Path theClasspath);

    /**
     * @return additional container classpath entries
     * @since Cactus 1.6
     */
    Path getContainerClasspath();
    
    /**
     * Subclasses must implement this method to perform the actual task of 
     * starting up the container.
     */
    void startUp();

    /**
     * Subclasses must implement this method to perform the actual task of 
     * shutting down the container.
     */
    void shutDown();

    /**
     * Returns the server (name or IP) to where the container is living.
     * 
     * @return The server
     */
    String getServer();

    /**
     * Returns the protocol, that should be used for communication to the 
     * container.
     * 
     * @return The protocol
     */
    String getProtocol();

    /**
     * @return the base URL of the container, including protocol, name and port
     */
    String getBaseURL();
}
