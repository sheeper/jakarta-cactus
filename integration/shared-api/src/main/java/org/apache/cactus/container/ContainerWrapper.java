/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cactus.container;

import java.io.File;
import java.util.Map;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.util.log.Logger;

/**
 * Class that wraps around an implementation of the <code>Container</code>
 * interface and delegates all calls to the wrapped instance.
 * 
 * @version $Id: ContainerWrapper.java 239130 2005-01-29 15:49:18Z vmassol $
 */
public class ContainerWrapper
{
   /**
    * The default constructor.
    */
    public ContainerWrapper()
    { }

    // Instance Variables ------------------------------------------------------

    /**
     * The nested container.
     */
    private org.codehaus.cargo.container.Container container;

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param theContainer The container to wrap
     */
    public ContainerWrapper(org.codehaus.cargo.container.Container theContainer)
    {
        if (theContainer == null)
        {
            throw new NullPointerException("'theContainer' must not be null");
        }
        this.container = theContainer;
    }
    
    // AbstractContainer Implementation ----------------------------------------

    /**
     * {@inheritDoc}
     * @see Container#getName()
     */
    public String getName()
    {
        return container.getName();
    }
    
    /**
     * {@inheritDoc}
     * @see Container#getTestContext()
     */
    public String getTestContext()
    {
        return ((InstalledLocalContainer) container).getConfiguration()
            .getPropertyValue("testContext");
    }    
    
    /**
     * {@inheritDoc}
     * @see Container#getStartUpWait()
     */
    public long getStartUpWait()
    {
        //We don't need startup wait, as we integrate with cargo, 
        //and it will do the work for us :-)
       return 0L;
    }
    
    /**
     * {@inheritDoc}
     * @see Container#getPort()
     */
    public int getPort()
    {
        String port = ((InstalledLocalContainer) container).getConfiguration()
            .getPropertyValue("cargo.servlet.port");
        if (port != null)
        {
            return Integer.parseInt(port);
        }
        else
        {
            return 8080;
        }
    }

    /**
     * {@inheritDoc}
     * @see Container#getToDir()
     */
    public File getToDir()
    {
        String location = ((InstalledLocalContainer) container)
            .getConfiguration().getPropertyValue("cactus.toDir");
        File toDir;
        if (location != null) 
        {
            toDir = new File(location);
        }
        else
        {
            toDir = new File("./target/" + container.getId() + "/");
            toDir.mkdirs();
        }
        return toDir;
    }

    /**
     * {@inheritDoc}
     * @see Container#init()
     */
    public void init()
    {
        //this.container.init();
    }

    /**
     * {@inheritDoc}
     * @see Container#isEnabled()
     */
    public boolean isEnabled()
    {
        //For now containers are always enabled. Think of a way to implement.
        return true;
    }

    /**
     * {@inheritDoc}
     * @see Container#isExcluded(String)
     */
    public boolean isExcluded(String theTestName)
    {
        //No tests are excluded per container for now. Think of 
        //a way to implement.
        return false;
    }

    /**
     * {@inheritDoc}
     * @see Container#startUp()
     */
    public void startUp()
    {
        ((InstalledLocalContainer) container).start();
    }

    /**
     * {@inheritDoc}
     * @see Container#shutDown()
     */
    public void shutDown()
    {
        ((InstalledLocalContainer) container).stop();
    }
    
    /**
     * {@inheritDoc}
     * @see Container#setSystemProperties
     */
    public void setSystemProperties(Map theProperties)
    {
        if (theProperties != null) 
        {
            ((InstalledLocalContainer) container).setSystemProperties(theProperties);
        }
    }

    /**
     * {@inheritDoc}
     * @see Container#setContainerClasspath(Path)
     * @since Cactus 1.6
     */
    public void setContainerClasspath(String[] theClasspath)
    {
        if (theClasspath != null)
        {
            ((InstalledLocalContainer) container)
                .setExtraClasspath(theClasspath);
        }
    }

    /**
     * {@inheritDoc}
     * @see Container#getContainerClasspath()
     * @since Cactus 1.6
     */
    public String[] getContainerClasspath()
    {
        return ((InstalledLocalContainer) container).getExtraClasspath();
    }

    /**
     * {@inheritDoc}
     * @see Container#getServer()
     */
    public String getServer()
    {
        return ((InstalledLocalContainer) container).getConfiguration()
            .getPropertyValue(GeneralPropertySet.HOSTNAME);
    }

    /**
     * {@inheritDoc}
     * @see Container#getProtocol()
     */
    public String getProtocol()
    {
        return ((InstalledLocalContainer) container).getConfiguration()
             .getPropertyValue(GeneralPropertySet.PROTOCOL);
    }

    /**
     * {@inheritDoc}
     * @see Container#getBaseURL()
     */
    public String getBaseURL()
    {
        return getProtocol() + "://" + getServer() + ":" + getPort();
    }
    /**
     * @param theLogger sets  the logger
     */
    public void setLogger(Logger theLogger)
    {
        this.container.setLogger(theLogger);
    }
    /**
     *  @param theContainer - the container to set
     */
    public void setContainer(Container theContainer)
    {
        this.container = theContainer;
    }
}
