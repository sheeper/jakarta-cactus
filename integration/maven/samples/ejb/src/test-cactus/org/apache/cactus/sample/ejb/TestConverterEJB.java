/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.cactus.ServletTestCase;

/**
 * Sample Cactus test for a session bean.
 *
 * @version $Id$
 */
public class TestConverterEJB extends ServletTestCase
{
    /**
     * Class under test
     */
    private Converter converter;

    /**
     * @see TestCase#setUp()
     */ 
    public void setUp() throws Exception
    {
        Context ctx = new InitialContext();
        ConverterHome home = (ConverterHome) PortableRemoteObject.narrow(
            ctx.lookup("Converter"), ConverterHome.class);
        this.converter = home.create();
    }

    /**
     * Verify yen to dollars conversion works.
     * @throws Exception on error
     */
    public void testConvert() throws Exception
    {
        double dollar = this.converter.convertYenToDollar(100.0);
        assertEquals("dollar", 1.0, dollar, 0.01);
    }
}
