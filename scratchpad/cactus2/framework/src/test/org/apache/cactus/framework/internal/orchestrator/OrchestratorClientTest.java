/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.cactus.framework.internal.orchestrator;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class OrchestratorClientTest extends TestCase
{
    private OrchestratorClient client;
    private Orchestrator orchestrator;
    
    protected void setUp()
    {
        orchestrator = new Orchestrator(7777);
        client = new OrchestratorClient();
        try
        {
            orchestrator.start();
        }
        catch (Throwable t)
        {
            throw new AssertionFailedError("Failed to start orchestrator ["
                + t.getMessage() + "]");
                    
        }
    }

    protected void tearDown()
    {
        try
        {
            orchestrator.stop();
        }
        catch (InterruptedException e)
        {
            throw new AssertionFailedError("Failed to stop orchestrator ["
                + e.getMessage() + "]");                    
        }
    }
    
    public void testPrepareTestWhenOrchestratorStarted() throws Throwable
    {
        client.setTest("testXXX");
        assertEquals("test", client.getTest());
    }
}
