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

import org.apache.cactus.EJBRequest;
import org.apache.cactus.spi.server.ImplicitObjects;

import javax.ejb.SessionContext;

/**
 * Prototype of EJBRedirector for Cactus.
 * 
 * @version $Id$
 */
public interface EJBImplicitObjects extends ImplicitObjects 
{

    /**
     * Gettermethod for the ejb context.
     * 
     * @return the context of the bean
     */
    public SessionContext getEJBContext();

    /**
     * Setter method for the session context.
     * 
     * @param theContext the context of the bean.
     */
    public void setEJBContext(SessionContext theContext);

    /**
     * Setter method for the ejb request.
     * 
     * @param theRequest for the bean
     */
    public void setEJBRequest(EJBRequest theRequest);

    /**
     * Getter method for the ejb request.
     * 
     * @return the request for the bean
     */
    public EJBRequest getEJBRequest();

}
