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

import java.util.ArrayList;
import java.util.List;

import org.apache.cactus.integration.ant.container.Container;
import org.apache.cactus.integration.ant.container.ContainerFactory;
import org.apache.cactus.integration.ant.container.ContainerWrapper;
import org.apache.cactus.integration.ant.container.GenericContainer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfigurator;
import org.apache.tools.ant.types.DataType;

/**
 * Ant data type that represents a set of J2EE containers.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class ContainerSet extends DataType implements DynamicConfigurator
{

    // Instance Variables ------------------------------------------------------

    /**
     * The list of nested container elements.
     */
    private ContainerFactory factory = new ContainerFactory();

    /**
     * The list of nested container and containerset elements.
     */
    private List containers = new ArrayList();

    /**
     * The timeout in milliseconds. 
     */
    private long timeout = -1;

    /**
     * The proxy port. 
     */
    private int proxyPort = -1;

    // DynamicConfigurator Implementation --------------------------------------

    /**
     * @see org.apache.tools.ant.DynamicConfigurator#createDynamicElement
     */
    public final Object createDynamicElement(String theName)
        throws BuildException
    {
        if (isReference())
        {
            throw noChildrenAllowed();
        }
        Container container = this.factory.createContainer(theName);
        this.containers.add(container);
        return container;
    }

    /**
     * @see org.apache.tools.ant.DynamicConfigurator#setDynamicAttribute
     */
    public final void setDynamicAttribute(String theName, String theValue)
        throws BuildException
    {
        if (isReference())
        {
            throw tooManyAttributes();
        }
        throw new BuildException("Attribute [" + theName
            + "] not supported");
    }

    // Public Methods ----------------------------------------------------------

    /**
     * Adds a nested generic container to the set of containers.
     * 
     * @param theContainer The generic container element to add
     */
    public final void addGeneric(GenericContainer theContainer)
    {
        this.containers.add(theContainer);
    }

    /**
     * Returns an iterator over the nested container elements, in the order
     * they appear in the build file.
     * 
     * @return An iterator over the nested container elements
     */
    public final Container[] getContainers()
    {
        Container[] containers = (Container[])
            this.containers.toArray(new Container[this.containers.size()]);
        if (this.proxyPort > 0)
        {
            for (int i = 0; i < containers.length; i++)
            {
                containers[i] = new ContainerWrapper(containers[i])
                {
                    public int getPort()
                    {
                        return proxyPort;
                    }
                };
            }
        }
        return containers;
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
