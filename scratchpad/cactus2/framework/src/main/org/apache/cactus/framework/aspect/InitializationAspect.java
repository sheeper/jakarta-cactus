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
package org.apache.cactus.framework.aspect;

import org.apache.cactus.framework.internal.orchestrator.Orchestrator;
import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * Intercepts client side JUnit tests and initialize the Cactus system: 
 * <ul>
 *   <li>sets up PicoContainer,</li>
 *   <li>sets up the test listener socket if not set</li>
 * </ul>
 */
public class InitializationAspect extends Aspect
{
    /**
     * @Execution * *..TestCase+.test*()
     */
    private Pointcut interceptClientTest;

    /**
     * Has the Cactus system been already initialized? 
     */
    private boolean isInitialized = false;
    
    /**
     * @Around interceptClientTest
     */
    public synchronized Object initialize(JoinPoint joinPoint) 
        throws Throwable
    {
        if (!this.isInitialized)
        {
            // TODO: Create a Configuration component to externalize 
            // configuration data
            Orchestrator orchestrator = new Orchestrator(7777);
            orchestrator.start();
            
            this.isInitialized = true;
        }
        return joinPoint.proceed();
    }
}
