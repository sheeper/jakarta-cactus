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

import junit.framework.Assert;

import org.apache.cactus.integration.ant.deployment.DeployableFile;
import org.apache.cactus.integration.ant.util.AntTaskFactory;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * Mock implementation of the {@link Container} interface.
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
     * @see Container#getTestContext()
     */
    public String getTestContext()
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

    /**
     * @see Container#setContainerClasspath(Path)
     */
    public void setContainerClasspath(Path theClasspath)
    {
        // do nothing
    }

    /**
     * @see Container#getContainerClasspath()
     */
    public Path getContainerClasspath()
    {
        throw new RuntimeException("not implemented");
    }
}
