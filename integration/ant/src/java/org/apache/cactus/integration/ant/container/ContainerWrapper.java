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
package org.apache.cactus.integration.ant.container;

import java.io.File;

import org.apache.cactus.integration.ant.util.AntTaskFactory;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * Class that wraps around an implementation of the <code>Container</code>
 * interface and delegates all calls to the wrapped instance.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
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
     * @see Container#getName
     */
    public String getName()
    {
        return container.getName();
    }

    /**
     * @see Container#getStartUpWait()
     */
    public long getStartUpWait()
    {
        return container.getStartUpWait();
    }
    
    /**
     * @see Container#getPort
     */
    public int getPort()
    {
        return this.container.getPort();
    }

    /**
     * @see Container#getToDir
     */
    public File getToDir()
    {
        return this.container.getToDir();
    }

    /**
     * @see Container#getSystemProperties
     */
    public Variable[] getSystemProperties()
    {
        return this.container.getSystemProperties();
    }
    
    /**
     * @see Container#init
     */
    public void init()
    {
        this.container.init();
    }

    /**
     * @see Container#isEnabled
     */
    public boolean isEnabled()
    {
        return this.container.isEnabled();
    }

    /**
     * @see Container#isExcluded
     */
    public boolean isExcluded(String theTestName)
    {
        return this.container.isExcluded(theTestName);
    }

    /**
     * @see Container#startUp
     */
    public void startUp()
    {
        this.container.startUp();
    }

    /**
     * @see Container#shutDown
     */
    public void shutDown()
    {
        this.container.shutDown();
    }
    
    /**
     * @see Container#setAntTaskFactory
     */
    public void setAntTaskFactory(AntTaskFactory theFactory)
    {
        this.container.setAntTaskFactory(theFactory);
    }

    /**
     * @see Container#setLog
     */
    public void setLog(Log theLog)
    {
        this.container.setLog(theLog);
    }

    /**
     * @see Container#setDeployableFile
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
}
