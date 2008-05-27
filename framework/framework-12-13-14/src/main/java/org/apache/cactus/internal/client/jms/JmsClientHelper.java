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

import java.util.Hashtable;
import javax.jms.QueueConnection;
import javax.jms.JMSException;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.QueueConnectionFactory;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.cactus.util.JmsConfiguration;
import org.apache.cactus.util.ChainedRuntimeException;

/**
 * Helper class to send a JMS message.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @since 1.4
 *
 * @version $Id$
 */
public class JmsClientHelper
{
    /**
     * The JMS queue session used to send messages to the server side.
     */
    private static QueueSession queueSession;

    /**
     * Create a JMS Queue Connection to be able to send messages later on.
     *
     * @return the created queue connection
     * @exception JMSException if an error occurred
     */
    protected static QueueConnection createQueueConnection()
        throws JMSException
    {
        QueueConnection queueConnection =
            getQueueConnnectionFactory().createQueueConnection();
        return queueConnection;
    }

    /**
     * Return the Queue session that we will use to send all JMS messages
     * (the Session is created if it is the first time this method is
     * called).
     *
     * @return the created Queue Session
     */
    public static synchronized QueueSession getQueueSession()
    {
        if (queueSession == null) {
            try 
            {
                queueSession =
                    createQueueConnection().createQueueSession(false,
                        Session.AUTO_ACKNOWLEDGE);
            } 
            catch (JMSException e) 
            {
                throw new ChainedRuntimeException(
                    "Failed to create JMS Queue Session", e);
            }
        }
        return queueSession;
    }

    /**
     * @return the JNDI Initial Context as defined in the Cactus configuration
     *         file (used to retrieve the JMS Queue Connection Factory)
     */
    public static InitialContext getInitialContext()
    {
        InitialContext context = null;
        try 
        {
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                JmsConfiguration.getJndiInitialContextFactory());
            env.put(Context.PROVIDER_URL,
                JmsConfiguration.getJndiProviderURL());
            env.put(Context.URL_PKG_PREFIXES,
                    JmsConfiguration.getJndiUrlPkgPrefixes());
            env.put("j2ee.clientName", "JmsClientHelper");
            
            context = new InitialContext(env);
            System.out.println(context);
        } 
        catch (NamingException e) 
        {
            throw new ChainedRuntimeException(
                "Failed to create JNDI initial context", e);
        }

        return context;
    }

    /**
     * @return the JMS Queue Connection Factory from which to retrieve Queues
     */
    public static QueueConnectionFactory getQueueConnnectionFactory()
    {
        QueueConnectionFactory queueConnectionFactory = null;
        try 
        {
            queueConnectionFactory =
                (QueueConnectionFactory) (getInitialContext().
                    lookup(JmsConfiguration.getJmsConnectionFactoryJndiName()));
        } 
        catch (NamingException e) 
        {
            throw new ChainedRuntimeException(
                "Failed to lookup [" +
                JmsConfiguration.getJmsConnectionFactoryJndiName() +
                "] Connection Factory in JNDI", e);
        }

        return queueConnectionFactory;
    }

    /**
     * Lookup a queue in JNDI.
     *
     * @param theQueueName the JNDI name of the queue to look up
     * @return the queue object
     */
    public static Queue getQueue(String theQueueName)
    {
        Queue queue = null;
        try 
        {
            queue = (Queue) (getInitialContext().lookup(theQueueName));
        } 
        catch (NamingException e) 
        {
            throw new ChainedRuntimeException(
                "Failed to lookup [" + theQueueName + "] Queue in JNDI", e);
        }
        return queue;
    }

    /**
     * Creates a Queue Sender to send JMS messages.
     *
     * @param theQueueName the JNDI name of the queue to use to send messages
     * @return the queue sender object
     */
    public static QueueSender createQueueSender(String theQueueName)
    {
        Queue queue = getQueue(theQueueName);
        QueueSession queueSession = getQueueSession();

        QueueSender queueSender = null;
        try 
        {
            queueSender = queueSession.createSender(queue);
        } 
        catch (JMSException e) 
        {
            throw new ChainedRuntimeException("Failed to create queue sender" +
                "for queue [" + queue + "]", e);
        }

        return queueSender;
    }
}
