/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.cactus.test;

import java.util.Map;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;

public class TestProperties extends ServletTestCase
{

    /**
     * Verify that the properties were set correctly.
     */
    public void testProperties()
    {
      String prop1 = System.getProperty("prop1");
      String prop2 = System.getProperty("prop2");
      assertNotNull( prop1 );
      assertNotNull( prop2 );
      assertEquals( "value1", prop1 );
      assertEquals( "value2", prop2 );
    }

}
