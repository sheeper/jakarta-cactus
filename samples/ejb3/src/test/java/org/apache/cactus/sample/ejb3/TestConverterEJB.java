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
package org.apache.cactus.sample.ejb3;

import org.apache.cactus.ServletTestCase;

import javax.naming.InitialContext;
import java.util.Properties;

/**
 * Sample Cactus test for a session bean.
 *
 * @version $Id: TestConverterEJB.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestConverterEJB extends ServletTestCase
{
    /**
     * Class under test.
     */
    //private Converter converter;
    private IConvertLocal converter;

    /**
     * @see TestCase#setUp()
     */ 
    public void setUp() throws Exception
    {
        Properties properties = new Properties();
        properties.put("java.naming.factory.initial",
            "org.jnp.interfaces.NamingContextFactory");
        properties.put("java.naming.factory.url.pkgs",
            "org.jboss.naming rg.jnp.interfaces");
        //properties.put("java.naming.provider.url", "jnp://localhost:1099");
        InitialContext ctx = new InitialContext(properties);
        
        
        converter = (IConvertLocal) ctx.lookup("cactus.samples.ejb3-cactified/"
                + ConverterBean.class.getSimpleName() + "/local");
        
        //ConverterHome home = (ConverterHome) PortableRemoteObject.narrow(
        //    ctx.lookup("Converter"), ConverterHome.class);
        //this.converter = home.create();
    }

    /**
     * Verify yen to dollars conversion works.
     * @throws Exception on error
     */
    public void testConvert() throws Exception
    {
        double dollar = this.converter.convert(100.0);
        assertEquals("dollar", 1.0, dollar, 0.01);
    }
}
