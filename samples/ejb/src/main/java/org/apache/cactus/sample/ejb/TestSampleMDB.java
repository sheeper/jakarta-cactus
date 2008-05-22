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
package org.apache.cactus.sample.ejb;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.cactus.JmsTestCase;

/**
 * Tests of the <code>SampleTag</code> class.
 *
 * @version $Id: TestSampleTag.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestSampleMDB extends JmsTestCase
{
    private static String QUEUE_NAME         = "CactusQueue";
    private static String PROVIDER_URL       = "t3://localhost:7001";
    private static String CONNECTION_FACTORY = "javax.jms.QueueConnectionFactory";

	private QueueSender  sender;
	private QueueSession session;
	
	public void setUp()
	{
        try {
        	
        	Context con =  getInitialContext();
        	
        	// Lookup a JMS connection factory
			QueueConnectionFactory conFactory =
				(QueueConnectionFactory) con.lookup(CONNECTION_FACTORY);

			// Create a JMS connection
			QueueConnection connection = conFactory.createQueueConnection();

			// Create a JMS session object
			session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

			// Lookup a JMS queue
			Queue chatQueue = (Queue) con.lookup(QUEUE_NAME);

			// Create a JMS sender
			sender = session.createSender(chatQueue);
        } catch(Exception e) {
            e.printStackTrace();
        }
		
	}

	public TestSampleMDB(String msg)
	{
		super(msg);
	}
	
	public void testSayHello()
	{
		
		
		// send the mapmessage to the Queue
        try 
        {
			sender.send(message);
			this.message.setStringProperty("aaaa", "bbbb");
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Method: getInitialContext()
	 *
	 * Login to JNDI
	 *
	 * @return Context The initial context
	 */
    private Context getInitialContext() throws NamingException {
        Properties env = new Properties();
        env.put("cactus.jndi.initialContextFactory", "weblogic.jndi.WLInitialContextFactory");
        env.put("cactus.jndi.providerUrl", PROVIDER_URL);
        env.put("cactus.jndi.securityPrincipal", "system");
		env.put("cactus.jndi.securityCredentials", "weblogic");

        return new InitialContext(env);
    }

}
