package org.apache.cactus.internal.server;
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
import javax.ejb.SessionContext;
import org.apache.cactus.EJBRequest;

/**
 * Prototype of EJBRedirector for Cactus.
 * @author Siddhartha P. Chandurkar (siddhartha@visioncodified.com)
 */
public class BeanImplicitObjects implements EJBImplicitObjects 
{
    /**
     * The session context to use.
     */
    private SessionContext context;
    
    /**
     * The ejb request to use.
     */
    private EJBRequest request;

    /**
     * Getter method for the ejb context.
     */
    public SessionContext getEJBContext() 
    {
        return context;
    }

    /**
     * Setter method for the ejb context.
     * @param theContext
     */
    public void setEJBContext(SessionContext theContext) 
    {
        context = theContext;
    }

    /**
     * Getter method for the ejb request.
     * @return ejb request object
     */
    public EJBRequest getEJBRequest() 
    {
        return request;
    }
    
    /**
     * Setter method for the ejb request.
     * @param theRequest
     */
    public void setEJBRequest(EJBRequest theRequest) 
    {
        request = theRequest;
    }
}
