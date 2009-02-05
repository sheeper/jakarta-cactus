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

package org.apache.cactus.internal.server;

import org.apache.cactus.internal.AbstractCactusTestCase;

/**
 * Prototype of EJBRedirector for Cactus.
 * 
 * @version $Id$
 */
public class EJBTestCaller extends AbstractEJBTestCaller 
{
    /**
     * A constructor that takes the implicitObjects as a parameter.
     * 
     * @param theObjects for the bean
     */
    public EJBTestCaller(EJBImplicitObjects theObjects) 
    {
        super(theObjects);
    }

    /**
     * A constructor that takes the instance of the test-case as a parameter.
     * 
     * @param theTestInstance to test
     * @throws Exception if an error occurs
     */
    protected void setTestCaseFields(AbstractCactusTestCase theTestInstance) throws Exception 
    {
        //Does nothing
    }
}
