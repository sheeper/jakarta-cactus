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

import org.apache.cactus.internal.client.EJBClient;
import org.apache.cactus.internal.client.connector.http.HttpProtocolHandler;
import org.apache.cactus.internal.configuration.DefaultServletConfiguration;
import org.apache.cactus.spi.client.connector.ProtocolHandler;

/**
 * Cactus test case to unit test EJBs. Test classes that need access to 
 * valid EJB implicit objects (such as the EJB request, the EJB response,
 * the servlet config, ...) must subclass this class.
 *
 * @version $Id: EJBTestCase.java 238991 2004-05-22 11:34:50Z$
 * @author Siddhartha P. Chandurkar (siddhartha@visioncodified.com)
 */
public class EJBTestCase extends AbstractEJBTestCase 
{
    /**
     * Constructor with the name of the test case as a parameter.
     * @param theName
     */
    public EJBTestCase(String theName) 
    {
        super(theName);
    }

    /**
     * The initial method to start the test process.
     * @throws Throwable
     */
    protected void runTest() throws Throwable 
    {
        runGenericTest(new EJBClient());
    }

    /**
     * The obligatory createProtocolHandler.
     * @return the ready-created protocol handler.
     */
    protected ProtocolHandler createProtocolHandler() 
    {
        return new HttpProtocolHandler(new DefaultServletConfiguration());
    }
}
