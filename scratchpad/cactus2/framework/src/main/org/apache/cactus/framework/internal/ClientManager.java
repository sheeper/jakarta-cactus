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
package org.apache.cactus.framework.internal;

import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.TestCase;

import org.apache.cactus.framework.internal.orchestrator.Orchestrator;
import org.apache.cactus.framework.internal.orchestrator.OrchestratorClient;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;

public class ClientManager extends Aspect
{    
    private OrchestratorClient orchestratorClient;

    public ClientManager()
    {
        this.orchestratorClient = new OrchestratorClient(); 
    }
    
    public void initialize() throws Throwable
    {
        // TODO: Create a Configuration component to externalize 
        // configuration data
        Orchestrator orchestrator = new Orchestrator(7777);
        orchestrator.start();
    }
 
    public void prepareTest(TestCase testCase) 
    	throws MalformedURLException, IOException
    {
        String name = testCase.getName();
        this.orchestratorClient.setTest(name);
    }   

}
