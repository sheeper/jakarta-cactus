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


import java.net.URL;

import org.apache.tools.ant.BuildException;

import junit.framework.TestCase;

/**
 * Unit tests for {@link ContainerRunner}.
 *
 * @version $Id$
 */
public final class TestContainerRunner extends TestCase
{

    // Instance Variables ------------------------------------------------------

    /**
     * The dummy HTTP server used to test the container runner.
     */
    private MockHttpServer server;

    // TestCase Implementation -------------------------------------------------

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        int unusedPort =
            MockHttpServer.findUnusedLocalPort("localhost", 8000, 8099);
        if (unusedPort > 0)
        {
            this.server = new MockHttpServer(unusedPort);
            this.server.expectMethod("GET");
            this.server.expectUri("/test");
        }
    }

    // Test Methods ------------------------------------------------------------

    /**
     * Verifies that an exception is thrown when setting the URL property to a 
     * non-HTTP URL.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testSetInvalidUrl() throws Exception
    {
        ContainerRunner runner =
            new ContainerRunner(new MockContainer(this.server));
        try
        {
            runner.setURL(new URL("ftp://ftp.example.com/test/"));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
            // expected
        }
    }

    /**
     * Verifies that an exception is thrown if startUpContainer() is invoked
     * before a URL has been set. 
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testStartUpWithoutUrl() throws Exception
    {
        ContainerRunner runner =
            new ContainerRunner(new MockContainer(this.server));
        try
        {
            runner.startUpContainer();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException expected)
        {
            // expected
        }
    }

    /**
     * Verifies that an exception is thrown if shutDownContainer() is invoked
     * before a URL has been set. 
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testShutDownWithoutUrl() throws Exception
    {
        ContainerRunner runner =
            new ContainerRunner(new MockContainer(this.server));
        try
        {
            runner.shutDownContainer();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException expected)
        {
            // expected
        }
    }

    /**
     * Verifies that the runner correctly starts and stops a well-behaved
     * container.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testStartUpShutDown() throws Exception
    {
        this.server.setResponse("HTTP/1.1 200 Ok\n\n");
        this.server.expectRequestCount(3);

        ContainerRunner runner =
            new ContainerRunner(new MockContainer(this.server));
        runner.setURL(
            new URL("http", "localhost", this.server.getPort(), "/test"));
        runner.setTimeout(0);
        runner.setCheckInterval(250);
        runner.setShutDownWait(0);
        runner.startUpContainer();
        runner.shutDownContainer();

        server.verify();
    }

    /**
     * Verifies that the runner throws an exception when a container doesn't
     * return a success status.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testStartUpFailure() throws Exception
    {
        this.server.setResponse("HTTP/1.1 500 Internal Server Error\n\n");
        this.server.expectRequestCount(1);

        ContainerRunner runner =
            new ContainerRunner(new MockContainer(this.server));
        runner.setURL(
            new URL("http", "localhost", this.server.getPort(), "/test"));
        runner.setTimeout(0);
        runner.setCheckInterval(250);
        runner.setShutDownWait(0);
        try
        {
            runner.startUpContainer();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            // expected
        }

        server.stop();
        server.verify();
    }

    /**
     * Verifies that the runner tries for a time approximately close to the
     * timeout period to start up the container, and then fails if the container
     * isn't responding.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testStartUpTimeout() throws Exception
    {
        this.server.setResponse("HTTP/1.1 500 Internal Server Error\n\n");
        this.server.expectRequestCount(-1); // can't tell exactly

        ContainerRunner runner =
            new ContainerRunner(new MockContainer(this.server));
        runner.setURL(
            new URL("http", "localhost", this.server.getPort(), "/test"));
        runner.setTimeout(5000);
        runner.setCheckInterval(250);
        runner.setShutDownWait(0);
        long startTime = System.currentTimeMillis();
        try
        {
            runner.startUpContainer();
            fail("Expected BuildException");
        }
        catch (BuildException expected)
        {
            // expected
        }

        long time = System.currentTimeMillis() - startTime;
        assertTrue("Process finished before the timeout was reached",
            time > 5000);
        assertTrue("Process took " + (time - 5000) + "ms longer than the "
            + "timeout period", time < 10000);

        server.stop();
        server.verify();
    }

    /**
     * Verifies that the runner correctly recognizes an already running
     * container, and does not attempt to shut it down.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testIgnoreAlreadyRunning() throws Exception
    {
        this.server.setResponse("HTTP/1.1 200 Ok\n\n");
        this.server.expectRequestCount(1);
        Thread thread = new Thread(this.server);
        thread.start();

        MockContainer container = new MockContainer(this.server);
        container.expectStartUpCalled(false);
        container.expectShutDownCalled(false);

        ContainerRunner runner = new ContainerRunner(container);
        runner.setURL(
            new URL("http", "localhost", this.server.getPort(), "/test"));
        runner.setTimeout(0);
        runner.setCheckInterval(250);
        runner.setShutDownWait(0);
        runner.startUpContainer();
        runner.shutDownContainer();
        
        container.verify();

        server.stop();
        server.verify();
    }

}
