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
package org.apache.cactus.internal.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.cactus.EJBRequest;
import org.apache.cactus.internal.server.EJBTestRedirector;
import org.apache.cactus.internal.server.EJBTestRedirectorHome;

/**
 * Prototype of EJBRedirector for Cactus.
 * @author Siddhartha P. Chandurkar (siddhartha@visioncodified.com)
 */
public abstract class AbstractEJBClient 
{

    /**
     * Getter method for the ejb request.
     * @param theRequest
     * @return
     */
    protected abstract String getRedirectorURL(EJBRequest theRequest);

    /**
     * The test method to initiate the test process.
     * @param theRequest
     * @throws Throwable
     */
    public void doTest(EJBRequest theRequest) throws Throwable 
    {
        callRunTest(theRequest);
    }

    /**
     * This method is used to get an instance of the redirector ejb procy.
     * @param theRequest
     * @throws Throwable
     */
    private void callRunTest(EJBRequest theRequest) throws Throwable 
    {
        try 
        {
            Context ctx = new InitialContext();
            EJBTestRedirectorHome home =
                (EJBTestRedirectorHome) PortableRemoteObject.narrow(
                    ctx.lookup("CACTUS/EJBREDIRECTOR"),
                    EJBTestRedirectorHome.class);
            EJBTestRedirector redirector = home.create();
            redirector.test(theRequest);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}
