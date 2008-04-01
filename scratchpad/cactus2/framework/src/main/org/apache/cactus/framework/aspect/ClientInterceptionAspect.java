/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import junit.framework.TestCase;

import org.apache.cactus.framework.internal.ClientManager;
import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * Intercepts client side JUnit tests.
 */
public class ClientInterceptionAspect extends Aspect
{
    /**
     * @Execution * *..TestCase+.test*()
     */
    private Pointcut interceptClientTest;

    /**
     * Has the Cactus system been already initialized? 
     */
    private boolean isInitialized = false;
    
    private ClientManager manager = new ClientManager();
    
    /**
     * @Around interceptClientTest
     */
    public synchronized Object intercept(JoinPoint joinPoint) 
        throws Throwable
    {
        if (!this.isInitialized)
        {
            manager.initialize();
            this.isInitialized = true;
        }

        manager.prepareTest((TestCase) joinPoint.getTargetInstance());
        
        return joinPoint.proceed();
    }
}
