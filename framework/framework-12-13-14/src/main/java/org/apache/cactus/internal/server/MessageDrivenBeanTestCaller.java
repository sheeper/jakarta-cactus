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

import junit.framework.TestCase;
import org.apache.cactus.JmsRequest;
import org.apache.cactus.JmsTestCase;
import org.apache.cactus.spi.server.MessageDrivenBeanImplicitObjects;

import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Responsible for instanciating the <code>TestCase</code> class on the server
 * side, set up the implicit objects and call the test method.
 *
 * @version $Id: MessageDrivenBeanTestCaller.java 292559 2005-09-29 21:36:43Z ptahchiev $
 */
public class MessageDrivenBeanTestCaller extends AbstractJMSTestCaller 
{
    /**
     * The implicit objects (which will be used to set the test case fields
     * in the <code>setTesCaseFields</code> method.
     */
    protected MessageDrivenBeanImplicitObjects mdbImplicitObjects;
    

    /**
     * @param theObjects the implicit objects coming from the redirector
     */
    public MessageDrivenBeanTestCaller(MessageDrivenBeanImplicitObjects theObjects)
    {
        super(theObjects);
    }
    
    /**
     * {@inheritDoc}
     * @see AbstractWebTestCaller#setTestCaseFields(TestCase)
     */
    protected void setTestCaseFields(TestCase theTestInstance)
        throws Exception
    {
        if (!(theTestInstance instanceof JmsTestCase))
        {
            return; 
        }
        
        JmsTestCase jmsInstance = (JmsTestCase) theTestInstance;

        // Sets the request field of the test case class
        // ---------------------------------------------
        // Extract from the JMS request the queue to simulate (if any)
        JmsRequest request = 
            mdbImplicitObjects.getRequest();

        String nameOfQueue = request.getQueueName();
        
        QueueConnectionFactory qcf = ((QueueConnectionFactory) 
        ((InitialContext) mdbImplicitObjects.getMessageDrivenBeanContext()).lookup(nameOfQueue)); 
        
        Method respondMethod = jmsInstance.getClass().getMethod("getJMSReplyTo", new Class[]{});
        
        Field requestField = jmsInstance.getClass().getField("request");  //TODO find the appropriate field names here.

        //requestField.set(jmsInstance, 
        //    AbstractHttpServletRequestWrapper.newInstance(request, url));

        // Set the response queue field of the test case class
        // ---------------------------------------------
        //TODO find the appropriate field names here.
        Field responseQueueField = jmsInstance.getClass().getField("jMSReplyTo"); 

        //responseQueueField.set(jmsInstance, 
        //    mdbImplicitObjects.getHttpServletResponse());

        // Set the config field of the test case class
        // -------------------------------------------
        Field configField = jmsInstance.getClass().getField("config");

        //configField.set(jmsInstance, AbstractServletConfigWrapper.
        //    newInstance(mdbImplicitObjects.getServletConfig()));

        // Set the session field of the test case class
        // --------------------------------------------
        // Create a Session object if the auto session flag is on
//        if (isAutoSession())
//        {
//            HttpSession session = mdbImplicitObjects.getRequest()
//                .getSession(true);
//
//            Field sessionField = jmsInstance.getClass().getField("session");
//
//            sessionField.set(jmsInstance, session);
//        }
    }
    /**
     * Empty implementation.
     */
    public void doTest()
    {
        
    }
    
    /**
     * Empty implementation.
     */
    public void doGetResults()
    {
        
    }
    
    /**
     * Empty implementation.
     */
    public void doRunTest()
    {
        
    }
    
    /**
     * Empty implementation.
     */
    public void doCreateSession()
    {
        
    }
    /**
     * Empty implementation.
     */
    public void doGetVersion()
    {
        
    }

}
