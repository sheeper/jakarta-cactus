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

import org.apache.cactus.integration.ant.container.Container;

/**
 * Unit tests for {@link ContainerSet}.
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
