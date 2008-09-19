package org.apache.cactus.sample.ejb;
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
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;


public class SampleMDB implements MessageDrivenBean, MessageListener {
    
      MessageDrivenContext context = null; 
      QueueConnection connection; 
      QueueSession session; 
    
      public SampleMDB() {
          System.out.println("Constructing MyMDB"); 
      } 
    
      public void setMessageDrivenContext(MessageDrivenContext context) {
          this.context = context; 
          System.out.println("setMessageDrivenContext"); 
      } 
    
      public void ejbCreate() throws EJBException {
          System.out.println("ejbCreate"); 
          try {
              InitialContext initContext = new InitialContext(); 
              QueueConnectionFactory qcf = (QueueConnectionFactory) 
              initContext.lookup("java:comp/env/jms/QCF"); 
              connection = qcf.createQueueConnection(); 
              session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE); 
              connection.start(); 
          } 
          catch(Exception e) {
              throw new EJBException("Failed to initialize MyMDB", e); 
          } 
      } 
    
      public void ejbRemove() {
          System.out.println("ejbRemove"); 
          context = null; 
          try {
              if( session != null ) 
                  session.close(); 
              if( connection != null ) 
                  connection.close(); 
          } 
          catch(JMSException e) {
              e.printStackTrace(); 
          } 
      } 
    
      public void onMessage(Message msg) {
          System.out.println("onMessage"); 
          try {
              TextMessage message = (TextMessage) msg; 
              Queue queue = (Queue) msg.getJMSReplyTo(); 
              QueueSender sender = session.createSender(queue); 
              TextMessage message2 = session.createTextMessage(message.getText()); 
              sender.send(message2); 
              sender.close(); 
          } 
          catch(Exception e) {
              e.printStackTrace(); 
          } 
      } 
}
