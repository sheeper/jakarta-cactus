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
package org.apache.cactus;

import org.apache.cactus.internal.AbstractCactusTestCase;
import org.apache.cactus.internal.HttpServiceDefinition;
import org.apache.cactus.internal.client.AbstractEJBClient;
import org.apache.cactus.internal.util.JUnitVersionHelper;

/**
 * Prototype of EJBRedirector for Cactus.
 * @author Siddhartha P. Chandurkar (siddhartha@visioncodified.com)
 */
public abstract class AbstractEJBTestCase extends AbstractCactusTestCase 
{
    /**
     * Constructor with the test-case name as a parameter.
     * @param theName
     */
    public AbstractEJBTestCase(String theName) 
    {
        super(theName);
    }

    /**
     * The client method to execute the tests.
     * @param theClient
     * @throws Throwable
     */
    protected void runGenericTest(AbstractEJBClient theClient)
        throws Throwable 
    {
        EJBRequest request = new EJBRequest();
        request.setClassName(
            HttpServiceDefinition.CLASS_NAME_PARAM,
            this.getClass().getName());
        request.setMethodName(
            HttpServiceDefinition.METHOD_NAME_PARAM,
            JUnitVersionHelper.getTestCaseName(this));
       theClient.doTest(request);
    }
}
