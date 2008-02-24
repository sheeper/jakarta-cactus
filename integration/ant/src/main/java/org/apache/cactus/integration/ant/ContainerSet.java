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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.ant.CargoTask;

/**
 * Ant data type that represents a set of J2EE containers.
 * 
 * @version $Id: ContainerSet.java 238810 2004-02-29 10:05:26Z vmassol $
 */
public class ContainerSet 
{

    // Instance Variables ------------------------------------------------------

    /**
     * The list of nested container and containerset elements.
     */
    private List cargos = new ArrayList();

    /**
     * The timeout in milliseconds. 
     */
    private long timeout = -1;

    /**
     * The proxy port. 
     */
    private int proxyPort = -1;

    // Public Methods ----------------------------------------------------------

    /**
     * Adds a nested generic container to the set of containers.
     * 
     * @param theElement The generic cargo element to add
     */
    public final void addCargo(CargoElement theElement)
    {
        this.cargos.add(theElement);
    }
    /**
     * @return CargoTask - the created cargo task.
     */
    public CargoTask createCargo()
    {
        CargoTask task = new CargoTask();
        cargos.add(task);
        return task;
    }

    /**
     * Returns an iterator over the nested container elements, in the order
     * they appear in the build file.
     * 
     * @return An iterator over the nested container elements
     */
    public final CargoElement[] getCargos()
    {
        CargoElement[] result = new CargoElement[cargos.size()];
        for (int i = 0; i < cargos.size(); i++)
        {
            result[i] = (CargoElement) cargos.get(i);
        }
        return result;
    }

    /**
     * Returns the timeout after which connecting to a container will be
     * given up, or <code>-1</code> if no timeout has been set.
     * 
     * @return The timeout in milliseconds
     */
    public final long getTimeout()
    {
        return this.timeout;
    }

    /**
     * Sets the timeout after which connecting to a container will be given
     * up.
     * 
     * @param theTimeout The timeout in milliseconds
     */
    public final void setTimeout(long theTimeout)
    {
        this.timeout = theTimeout;
    }

    /**
     * Returns the proxy port, or <code>-1</code> if no proxy port has been set.
     * 
     * @return The proxy port
     */
    public final int getProxyPort()
    {
        return this.proxyPort;
    }

    /**
     * Sets the proxy port which will be used by the test caller instead 
     * of the real container port. This can be used to insert protocol 
     * tracers between the test caller and the container.
     * 
     * @param theProxyPort The proxy port to set
     */
    public final void setProxyPort(int theProxyPort)
    {
        this.proxyPort = theProxyPort;
    }

}
