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
package org.apache.cactus.internal.client.jms;

import org.apache.cactus.JmsRequest;

import javax.jms.QueueSender;

/**
 * JMS class for performing the steps necessary to run a test. It involves
 * sending a first JMS message to a queue on which the Cactus MDB Redirector
 * is listening.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @since 1.4
 *
 * @version $Id$
 */
public class JmsClient
{
    /**
     * Calls the test method indirectly by calling the Redirector MDB (by
     * sending a JMS Message on a queue it is listening to).
     *
     * @param theRequest the request containing data to be sent using JMS to
     *        the server side
     * @exception Throwable if an error occured in the test method or in the
     *                      redirector servlet.
     */
    public void doTest(JmsRequest theRequest) throws Throwable
    {
        // 1 - Create a sender
        QueueSender sender =
            JmsClientHelper.createQueueSender(theRequest.getQueueName());

        this.getClass().getName();
        
        
        // 2 - Send the JMS Message
        sender.send(theRequest.getMessage());
    }
}
