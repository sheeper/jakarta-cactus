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
package org.apache.cactus.integration.ant.container;

import java.io.File;

import org.apache.cactus.integration.ant.deployment.DeployableFile;
import org.apache.cactus.integration.ant.util.AntTaskFactory;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * Class that wraps around an implementation of the <code>Container</code>
 * interface and delegates all calls to the wrapped instance.
 * 
 * @version $Id$
 */
public class ContainerWrapper implements Container
{

    // Instance Variables ------------------------------------------------------

    /**
     * The nested container.
     */
    private Container container;

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param theContainer The container to wrap
     */
    public ContainerWrapper(Container theContainer)
    {
        if (theContainer == null)
        {
            throw new NullPointerException("'theContainer' must not be null");
        }
        this.container = theContainer;
    }

    // AbstractContainer Implementation ----------------------------------------

    /**
     * @see Container#getName()
     */
    public String getName()
    {
        return container.getName();
    }
    
    /**
     * @see Container#getTestContext()
     */
    public String getTestContext()
    {
        return this.container.getTestContext();
    }    
    
    /**
     * @see Container#getStartUpWait()
     */
    public long getStartUpWait()
    {
        return container.getStartUpWait();
    }
    
    /**
     * @see Container#getPort()
     */
    public int getPort()
    {
        return this.container.getPort();
    }

    /**
     * @see Container#getToDir()
     */
    public File getToDir()
    {
        return this.container.getToDir();
    }

    /**
     * @see Container#getSystemProperties()
     */
    public Variable[] getSystemProperties()
    {
        return this.container.getSystemProperties();
    }
    
    /**
     * @see Container#init()
     */
    public void init()
    {
        this.container.init();
    }

    /**
     * @see Container#isEnabled()
     */
    public boolean isEnabled()
    {
        return this.container.isEnabled();
    }

    /**
     * @see Container#isExcluded(String)
     */
    public boolean isExcluded(String theTestName)
    {
        return this.container.isExcluded(theTestName);
    }

    /**
     * @see Container#startUp()
     */
    public void startUp()
    {
        this.container.startUp();
    }

    /**
     * @see Container#shutDown()
     */
    public void shutDown()
    {
        this.container.shutDown();
    }
    
    /**
     * @see Container#setAntTaskFactory(AntTaskFactory)
     */
    public void setAntTaskFactory(AntTaskFactory theFactory)
    {
        this.container.setAntTaskFactory(theFactory);
    }

    /**
     * @see Container#setLog(Log)
     */
    public void setLog(Log theLog)
    {
        this.container.setLog(theLog);
    }

    /**
     * @see Container#setDeployableFile(DeployableFile)
     */
    public void setDeployableFile(DeployableFile theWarFile)
    {
        this.container.setDeployableFile(theWarFile);
    }

    /**
     * @see Container#setSystemProperties
     */
    public void setSystemProperties(Variable[] theProperties)
    {
        this.container.setSystemProperties(theProperties);
    }

    /**
     * @see Container#setContainerClasspath(Path)
     * @since Cactus 1.6
     */
    public void setContainerClasspath(Path theClasspath)
    {
        this.container.setContainerClasspath(theClasspath);
    }

    /**
     * @see Container#getContainerClasspath()
     * @since Cactus 1.6
     */
    public Path getContainerClasspath()
    {
        return this.container.getContainerClasspath();
    }
}
