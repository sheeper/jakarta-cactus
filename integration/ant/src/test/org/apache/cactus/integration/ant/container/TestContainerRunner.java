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


import java.net.URL;

import org.apache.tools.ant.BuildException;

import junit.framework.TestCase;

/**
 * Unit tests for {@link ContainerRunner}.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
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
            runner.setUrl(new URL("ftp://ftp.example.com/test/"));
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
        runner.setUrl(
            new URL("http", "localhost", this.server.getPort(), "/test"));
        runner.setTimeout(0);
        runner.setCheckInterval(250);
        runner.setStartUpWait(0);
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
        runner.setUrl(
            new URL("http", "localhost", this.server.getPort(), "/test"));
        runner.setTimeout(0);
        runner.setCheckInterval(250);
        runner.setStartUpWait(0);
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
        runner.setUrl(
            new URL("http", "localhost", this.server.getPort(), "/test"));
        runner.setTimeout(5000);
        runner.setCheckInterval(250);
        runner.setStartUpWait(0);
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
        runner.setUrl(
            new URL("http", "localhost", this.server.getPort(), "/test"));
        runner.setTimeout(0);
        runner.setCheckInterval(250);
        runner.setStartUpWait(0);
        runner.setShutDownWait(0);
        runner.startUpContainer();
        runner.shutDownContainer();
        
        container.verify();

        server.stop();
        server.verify();
    }

}
