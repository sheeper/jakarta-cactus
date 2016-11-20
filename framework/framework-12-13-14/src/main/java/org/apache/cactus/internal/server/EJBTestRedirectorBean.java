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

import org.apache.cactus.EJBRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * Prototype of EJBRedirector for Cactus.
 * @author Siddhartha P. Chandurkar (siddhartha@visioncodified.com)
 */
public class EJBTestRedirectorBean implements SessionBean 
{

    /**
     * The session context.
     */
    private SessionContext context;
    
    /**
     * The logger.
     */
    private static final Log LOGGER = 
        LogFactory.getLog(AbstractWebTestCaller.class);

    //METHODS

    /**
     * The "main" method of the redirector object.
     * @param theRequest
     */
    public void test(EJBRequest theRequest) 
    {
        try 
        {
            LOGGER.debug("Received request " + theRequest);
            BeanImplicitObjects object = new BeanImplicitObjects();
            object.setEJBContext(context);
            object.setEJBRequest(theRequest);

            EJBTestController controller = new EJBTestController();
            controller.handleRequest(object);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    /**
     * No argument constructor needed by the Container
     */
    public EJBTestRedirectorBean() 
    {
    }

    /**
     * Create method specified in EJB 1.1 section 6.10.3.
     * @throws CreateException in case an error occurs
     */
    public void ejbCreate() throws CreateException 
    {
    }

    /* Methods required by SessionBean Interface. EJB 1.1 section 6.5.1. */

    /**
     * @see javax.ejb.SessionBean#setContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(SessionContext context) 
    {
        this.context = context;
    }

    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() 
    {
    }

    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() 
    {
    }

    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() 
    {
    }
}
