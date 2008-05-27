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

import javax.jms.QueueSession;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;

import org.apache.cactus.util.ChainedRuntimeException;

/**
 * Contains all JMS request data for a test case. It is the data that
 * will be sent to the server redirector and that will be available to the test
 * methods.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @since 1.4
 *
 * @version $Id$
 */
public class JmsRequest implements Request
{
    /**
     * The JMS Queue Session that will be used to send JMS messages to the
     * server side.
     */
    private QueueSession queueSession;

    /**
     * The JNDI queue name  of the queue to use to send the JMS message.
     */
    private String queueName;

    /**
     * The Message to send.
     */
    private Message message;

    /**
     * @param theQueueSession the JMS Queue Session that we will use to send
     *        JMS messages to the server side
     */
    public JmsRequest(QueueSession theQueueSession)
    {
        this.queueSession = theQueueSession;
    }

    /**
     * @return the Queue Session that is used to send messages to the server
     *         side
     */
    private QueueSession getQueueSession()
    {
        return this.queueSession;
    }

    /**
     * Creates a text message with a text.
     *
     * @param theText the text message
     * @return the created text message
     */
    public TextMessage createTextMessage(String theText)
    {
        try 
        {
            this.message = getQueueSession().createTextMessage(theText);
        } 
        catch (JMSException e) 
        {
            throw new ChainedRuntimeException(
                "Failed to create text message", e);
        }
        return (TextMessage) this.message;
    }

    /**
     * Creates an empty text message.
     *
     * @return the created text message
     */
    public TextMessage createTextMessage()
    {
        try 
        {
            this.message = getQueueSession().createTextMessage();
        } 
        catch (JMSException e) 
        {
            throw new ChainedRuntimeException(
                "Failed to create text message", e);
        }
        return (TextMessage) this.message;
    }

    /**
     * @return the JMS Message to send
     */
    public Message getMessage()
    {
        return this.message;
    }

    /**
     * Sets the Queue name to use to send the JMS message.
     *
     * @param theQueueName the JNDI queue name
     */
    public void setQueueName(String theQueueName)
    {
        this.queueName = theQueueName;
    }

    /**
     * @return the JNDI queue name to use to send the JMS message
     */
    public String getQueueName()
    {
        return this.queueName;
    }
}
