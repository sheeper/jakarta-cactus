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

import org.apache.cactus.internal.server.MessageDrivenBeanTestController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.naming.InitialContext;

/**
 * Generic Message Driven Bean redirector that calls a test method on the
 * server side.
 *
 * @since 1.4
 *
 * @version $Id$
 */
public class MessageDrivenBeanRedirector 
    implements MessageDrivenBean, MessageListener
{
    /**
     * The Message context.
     */
    private MessageDrivenContext context;
    
    /**
     * Connection for the MDB.
     */
    QueueConnection connection;
    
    /**
     * Session of the MDB.
     */
    QueueSession session; 
    
    /**
     * The logger.
     */
    private static final Log LOGGER = 
        LogFactory.getLog(MessageDrivenBeanRedirector.class);

    /**
     * Sets the Message context (automatically called by the container).
     *
     * @param theContext the Message context
     */
    public void setMessageDrivenContext(MessageDrivenContext theContext)
    {
        this.context = theContext;
    }

    /**
     * Called by the container to create an instance of this Message Driven
     * bean.
     *
     * @exception CreateException see the EJB specification
     */
    public void ejbCreate() throws CreateException
    {
        LOGGER.debug("------------- MDB redirector service created");
        
        try 
        {
            InitialContext initContext = new InitialContext(); 
            QueueConnectionFactory qcf = (QueueConnectionFactory) 
            initContext.lookup("java:comp/env/jms/QCF1"); 
            connection = qcf.createQueueConnection(); 
            session = connection.createQueueSession(false, 
                    QueueSession.AUTO_ACKNOWLEDGE); 
            connection.start(); 
        } 
        catch (Exception e) 
        {
            throw new EJBException("Failed to initialize MyMDB", e); 
        } 
    }

    /**
     * The container invokes this method when the instance is about to be
     * discarded. This might happens if the container needs to reduce the size
     * of the pool.
     */
    public void ejbRemove()
    {
        LOGGER.debug("------------- MDB redirector service removed");
    }

    /**
     * Receives a message from a JMS Queue and make it available to the
     * test case.
     *
     * @param theMessage the JMS Message
     */
    public void onMessage(Message theMessage)
    {
        // Mark beginning of test on server side
        LOGGER.debug("------------- Start MDB service");
        
        // Gather MDB implicit objects
        MessageDrivenBeanImplicitObjects implicitObjects =
            new MessageDrivenBeanImplicitObjects();
        implicitObjects.setMessage(theMessage);
        implicitObjects.setMessageDrivenBeanContext(this.context);

        // Call the controller to handle the message
        MessageDrivenBeanTestController controller = new MessageDrivenBeanTestController();

        try 
        {
            controller.handleRequest(implicitObjects);
        } 
        catch (JMSException e) 
        {
            e.printStackTrace();
        }
        
    }

}
