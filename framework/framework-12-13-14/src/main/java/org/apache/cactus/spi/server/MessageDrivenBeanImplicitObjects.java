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
package org.apache.cactus.spi.server;

import javax.jms.Message;
import javax.ejb.MessageDrivenContext;

/**
 * Implicit objects for the Message Driven Bean redirector.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class MessageDrivenBeanImplicitObjects implements ImplicitObjects
{
    /**
     * The JMS Message to process.
     */
    private Message message;

    /**
     * The Message Driven Bean context.
     */
    private MessageDrivenContext context;

    /**
     * Sets the JMS Message as retrieved by the Cactus redirector.
     *
     * @param theMessage the JMS Message
     */
    public void setMessage(Message theMessage)
    {
        this.message = theMessage;
    }

    /**
     * @return the JMS Message as retrieved by the Cactus redirector.
     */
    public Message getMessage()
    {
        return this.message;
    }

    /**
     * Sets the Message Driven Bean context as set by the J2EE container.
     *
     * @param theContext the MDB context
     */
    public void setMessageDrivenBeanContext(MessageDrivenContext theContext)
    {
        this.context = theContext;
    }

    /**
     * @return the MDB context
     */
    public MessageDrivenContext getMessageDrivenBeanContext()
    {
        return this.context;
    }
}
