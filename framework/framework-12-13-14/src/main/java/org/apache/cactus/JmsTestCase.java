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

import junit.framework.Test;
import org.apache.cactus.internal.AbstractCactusTestCase;
import org.apache.cactus.internal.client.connector.http.JmsProtocolHandler;
import org.apache.cactus.spi.client.connector.ProtocolHandler;
import org.apache.cactus.util.JmsConfiguration;

import javax.ejb.MessageDrivenContext;
import javax.jms.Message;

/**
 * TestCase class to test Message Driven Beans (or any JMS listener for that
 * matter).
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @since 1.4
 *
 * @version $Id$
 */
public class JmsTestCase extends AbstractCactusTestCase
{
    /**
     * Valid <code>Message</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public Message message;

    /**
     * Valid <code>MessageDrivenContext</code> object that you can access from
     * the <code>testXXX()</code>, <code>setUp</code> and
     * <code>tearDown()</code> methods. If you try to access it from either the
     * <code>beginXXX()</code> or <code>endXXX()</code> methods it will
     * have the <code>null</code> value.
     */
    public MessageDrivenContext context;
    
    /**
     * @see AbstractCactusTestCase#AbstractCactusTestCase()
     */
    public JmsTestCase()
    {
        super();
    }

    /**
     * Constructs a JUnit test case with the given name.
     *
     * @param theName the name of the test case
     */
    public JmsTestCase(String theName)
    {
        super(theName);
    }
    
    /**
     * Constructs a JUnit test case with the given name.
     *
     * @param theName the name of the test case
     */
    public JmsTestCase(String theName, Test theTest)
    {
        super(theName, theTest);
    }

    /**
     * {@inheritDoc}
     * @see AbstractCactusTestCase#createProtocolHandler()
     */
    protected ProtocolHandler createProtocolHandler()
    {
        return new JmsProtocolHandler(new JmsConfiguration());
    }
}
