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

import org.apache.cactus.integration.ant.container.Container;

/**
 * Unit tests for {@link ContainerSet}.
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public final class TestContainerSet extends AntTestCase
{

    // Constructors ------------------------------------------------------------

    /**
     * @see AntTestCase#AntTestCase
     */
    public TestContainerSet()
    {
        super("org/apache/cactus/integration/ant/test-containerset.xml");
    }

    // TestCase Implementation -------------------------------------------------

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        getProject().addDataTypeDefinition("containerset", ContainerSet.class);
    }

    // Test Methods ------------------------------------------------------------

    /**
     * Verifies that a completely empty container set definition is constructed
     * as expected.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testEmpty() throws Exception
    {
        executeTestTarget();
        ContainerSet set = (ContainerSet) getProject().getReference("test");
        assertEquals(-1, set.getProxyPort());
        assertEquals(-1, set.getTimeout());
        Container[] containers = set.getContainers();
        assertEquals(0, containers.length);
    }

    /**
     * Verifies that the <code>proxyport</code> attribute of a container set
     * results in the proxy port being set as expected.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testEmptyWithProxyPort() throws Exception
    {
        executeTestTarget();
        ContainerSet set = (ContainerSet) getProject().getReference("test");
        assertEquals(8088, set.getProxyPort());
        assertEquals(-1, set.getTimeout());
        Container[] containers = set.getContainers();
        assertEquals(0, containers.length);
    }

    /**
     * Verifies that the <code>timeout</code> attribute of a container set
     * results in the timeout being set as expected.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testEmptyWithTimeout() throws Exception
    {
        executeTestTarget();
        ContainerSet set = (ContainerSet) getProject().getReference("test");
        assertEquals(-1, set.getProxyPort());
        assertEquals(60000, set.getTimeout());
        Container[] containers = set.getContainers();
        assertEquals(0, containers.length);
    }

    /**
     * Verifies that a <em>generic</em> container can be added to an otherwise
     * empty container, and be retrieved as expected.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGenericContainer() throws Exception
    {
        executeTestTarget();
        ContainerSet set = (ContainerSet) getProject().getReference("test");
        Container[] containers = set.getContainers();
        assertEquals(1, containers.length);
        Container container = (Container) containers[0];
        assertEquals(8080, container.getPort());
    }

    /**
     * Verifies that setting the proxy port on the container set results in the
     * port of the nested containers being set to the proxy port.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGenericContainerWithProxyPort() throws Exception
    {
        executeTestTarget();
        ContainerSet set = (ContainerSet) getProject().getReference("test");
        Container[] containers = set.getContainers();
        assertEquals(1, containers.length);
        Container container = containers[0];
        assertEquals(8088, container.getPort());
    }

    /**
     * Verifies that the startup and shutdown hooks of a generic container
     * nested in a container set definition are not invoked when defined, but
     * when explicitly calling the container lifecycle methods.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGenericContainerWithTasks() throws Exception
    {
        executeTestTarget();
        ContainerSet set = (ContainerSet) getProject().getReference("test");
        Container[] containers = set.getContainers();

        // Make sure that neither the startup nor the shutdown hook have been
        // called yet
        assertNull("The startup hook should not have been executed",
            getProject().getProperty("startup.executed"));
        assertNull("The shutdown hook should not have been executed",
            getProject().getProperty("shutdown.executed"));

        // Call the startup and shutdown hooks and assert that they have been
        // executed
        Container container = containers[0];
        container.startUp();
        assertEquals("The startup hook should have been executed, ",
            "true", getProject().getProperty("startup.executed"));
        container.shutDown();
        assertEquals("The shutdown hook should have been executed, ",
            "true", getProject().getProperty("shutdown.executed"));
    }

}
