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

import org.apache.cactus.framework.Listener;
import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * Intercepts client side JUnit tests and sets up the test listener socket
 * if not set.
 */
public class ListenerAspect extends Aspect
{
    /**
     * @Execution * *..TestCase+.test*()
     */
    private Pointcut interceptClientTest;

    private Listener listener = new Listener();
    
    /**
     * @Around interceptClientTest
     */
    public Object setupListener(JoinPoint joinPoint) throws Throwable
    {
        this.listener.setupListener();
        return joinPoint.proceed();
    }
}
