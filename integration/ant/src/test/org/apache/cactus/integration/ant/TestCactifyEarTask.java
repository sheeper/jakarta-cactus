/* 
 * ========================================================================
 * 
 * Copyright 2005 The Apache Software Foundation.
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

import java.io.File;

import org.apache.cactus.integration.ant.deployment.application.ApplicationXml;
import org.apache.cactus.integration.ant.deployment.application.DefaultEarArchive;
import org.apache.cactus.integration.ant.deployment.application.EarArchive;
import org.apache.cactus.integration.ant.deployment.webapp.WarArchive;
import org.apache.cactus.integration.ant.deployment.webapp.WebXml;

/**
 * Test class for the CactifyEar task.
 *
 * @version $Id$
 */
public class TestCactifyEarTask extends AntTestCase
{
    /**
     * @see AntTestCase#AntTestCase
     */
    public TestCactifyEarTask()
    {
        super("org/apache/cactus/integration/ant/test-cactifyear.xml");
    }
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        getProject().addTaskDefinition("cactifyear", CactifyEarTask.class);
    }

    /**
     * Tests that the basic function of the task works, 
     * that is to add a cactified war to the ear.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testCanAddCactifiedWar() throws Exception
    {   
        executeTestTarget();
        
        File destFile = getProject().resolveFile("work/cactified.ear");
        EarArchive destEar = new DefaultEarArchive(destFile);
        ApplicationXml appXml = destEar.getApplicationXml();
        assertEquals("/cactus", appXml.getWebModuleContextRoot("cactus.war"));
        WarArchive cactusWar = destEar.getWebModule("cactus.war");
        WebXml webXml = cactusWar.getWebXml();
        assertNotNull(webXml.getServlet("ServletRedirector"));
    }

    /**
     * Tests that the context of the cactus.war can be specified.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testCustomCactusWarContext() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/cactified.ear");
        EarArchive destEar = new DefaultEarArchive(destFile);
        ApplicationXml appXml = destEar.getApplicationXml();
        assertEquals("/myTestFramework", 
            appXml.getWebModuleContextRoot("cactus.war"));
    }
}
