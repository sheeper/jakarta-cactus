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

import junit.framework.Assert;

import org.apache.cactus.integration.ant.util.AntTaskFactory;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * Mock implementation of the {@link Container} interface.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public final class MockContainer implements Container
{
    // Instance Variables ------------------------------------------------------

    /**
     * The dummy server.
     */
    private MockHttpServer server;

    /**
     * Whether it is expected that the startUp() method is called.
     * <code>null</code> for "Don't care".
     */
    private Boolean expectedStartUpCalled;

    /**
     * Whether the startUp() method was called.
     */
    private boolean startUpCalled;

    /**
     * Whether it is expected that the shutDown() method is called.
     * <code>null</code> for "Don't care".
     */
    private Boolean expectedShutDownCalled;

    /**
     * Whether the shutDown() method was called.
     */
    private boolean shutDownCalled;

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param theServer The dummy HTTP server
     */
    public MockContainer(MockHttpServer theServer)
    {
        this.server = theServer;
    }

    /**
     * Constructor.
     */
    public MockContainer()
    {
    }

    // Container Implementation ------------------------------------------------

    /**
     * @see Container#getName()
     */
    public String getName()
    {
        return null;
    }

    /**
     * @see Container#getStartUpWait()
     */
    public long getStartUpWait()
    {
        return 0;
    }
    
    /**
     * @see Container#getPort()
     */
    public int getPort()
    {
        return 0;
    }

    /**
     * @see Container#getToDir()
     */
    public File getToDir()
    {
        return null;
    }

    /**
     * @see Container#init()
     */
    public void init()
    {
    }

    /**
     * @see Container#isEnabled()
     */
    public boolean isEnabled()
    {
        return false;
    }

    /**
     * @see Container#isExcluded(java.lang.String)
     */
    public boolean isExcluded(String theTestName)
    {
        return false;
    }

    /**
     * @see Container#setAntTaskFactory(AntTaskFactory)
     */
    public void setAntTaskFactory(AntTaskFactory theFactory)
    {
    }

    /**
     * @see Container#setLog(Log)
     */
    public void setLog(Log theLog)
    {
    }

    /**
     * @see Container#setDeployableFile
     */
    public void setDeployableFile(DeployableFile theWarFile)
    {
    }

    /**
     * @see Container#startUp()
     */
    public void startUp()
    {
        this.startUpCalled = true;
        if (this.server != null)
        {
            Thread thread = new Thread(this.server);
            thread.start();
        }
    }

    /**
     * @see Container#shutDown()
     */
    public void shutDown()
    {
        this.shutDownCalled = true;
        if (this.server != null)
        {
            this.server.stop();
        }
    }

    // Public Methods ----------------------------------------------------------

    /**
     * Sets whether to expect a call to the startUp() method.
     *
     * @param isExpected Whether a call is expected or not 
     */
    public void expectStartUpCalled(boolean isExpected)
    {
        this.expectedStartUpCalled = isExpected ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Sets whether to expect a call to the shutDown() method.
     *
     * @param isExpected Whether a call is expected or not 
     */
    public void expectShutDownCalled(boolean isExpected)
    {
        this.expectedShutDownCalled = isExpected ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Verify the mock object's state.
     */
    public void verify()
    {
        if (this.expectedStartUpCalled != null)
        {
            Assert.assertTrue("The startUp() method should "
                + (this.expectedStartUpCalled.booleanValue() ? "" : "not ")
                + "have been called",
                this.expectedStartUpCalled.booleanValue() == startUpCalled);
        }
        if (this.expectedShutDownCalled != null)
        {
            Assert.assertTrue("The shutDown() method should "
                + (this.expectedShutDownCalled.booleanValue() ? "" : "not ")
                + "have been called",
                this.expectedShutDownCalled.booleanValue() == shutDownCalled);
        }
    }

    /** 
     * @see Container#setSystemProperties(Variable[])
     */
    public void setSystemProperties(Variable[] theProperties)
    {
        // do nothing
    }

    /**
     * @see Container#getSystemProperties()
     */
    public Variable[] getSystemProperties()
    {
        throw new RuntimeException("not implemented");
    }
}
