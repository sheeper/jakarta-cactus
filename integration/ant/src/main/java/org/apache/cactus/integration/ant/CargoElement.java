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

import org.apache.tools.ant.BuildException;
import org.codehaus.cargo.ant.CargoTask;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
/**
 * This is the element in the cactus task that holds 
 * the cargo properties, and in which the configuration for 
 * the cargo container is hold.
 * @version $Id: CactusWar.java 239162 2005-04-26 09:57:59Z paranoiabla $
 */
public class CargoElement extends CargoTask
{
    /**
     * The container to create.
     */
    private Container container = null;
    
    
   /**
    * Creates, verifies and initializes the container.
    *  
    * @return Container
    */
    protected Container getCargoContainer()
    {
        container = makeContainer();
        
        // Verify that the task is correctly set up.
        verify();

        // Setup all attributes and nested elements

        setupLogger();

        if (getContainer().getType().isLocal())
        {
            setupOutput();
            setupTimeout();

            if (getContainer().getType() == ContainerType.INSTALLED)
            {
                setupHome();
                setupExtraClasspath();
                setupSystemProperties();
            }
        }

        // Save the reference id if specified
        if (getId() != null)
        {
            getProject().addReference(getId(), getContainer());
        }
        return container;
    }

    
   /**
    * Checks if the task is correctly initialized and that the container
    * is ready to be used.
    */
    private void verify()
    {
        if ((getId() != null) && (getRefid() != null))
        {
            throw new BuildException("You must use either [id]"
                + " or [refid] but not both");
        }

        if ((getContainerId() == null) && (getRefid() == null))
        {
            throw new BuildException("You must specify a [containerId]"
                + " attribute or use a [refid] "
                + "attribute");
        }

        if ((getHome() == null) && (getZipURLInstaller() == null))
        {
            boolean doFail = false;

            if (getRefid() == null)
            {
                doFail = true;
            }
            else if ((getContainer().getType() == ContainerType.INSTALLED)
                && (((InstalledLocalContainer) getContainer())
                    .getHome() == null))
            {
                doFail = true;
            }

            if (doFail)
            {
                throw new BuildException("You must specify either"
                    + " a [home] attribute pointing "
                    + "to the location where the " + getContainer().getName()
                    + " is installed, or a nested [zipurlinstaller] element");
            }
        }
    }
    
    /**
     * Override the method so that we use our container.
     * @return Container - the current container being used
     */
    public Container getContainer() 
    {
    	return this.container;
    }
}
