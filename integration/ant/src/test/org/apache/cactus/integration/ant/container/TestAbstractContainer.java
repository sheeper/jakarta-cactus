/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant.container;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.types.FilterChain;

/**
 * Unit tests for {@link AbstractContainer}.
 *  
 * @version $Id$
 */
public class TestAbstractContainer extends TestCase
{
    /**
     * The instance to test
     */
    private AbstractContainer container;

    /**
     * @see TestCase#setUp()
     */    
    protected void setUp()
    {
        this.container = new AbstractContainer()
        {
            public String getName()
            {
                return "test container";
            }

            public int getPort()
            {
                return 8080;
            }

            public void startUp()
            {
            }
            
            public void shutDown()
            {                
            }
        };
    }

    /**
     * Verify that the Ant filter chain is correctly configured to 
     * replace the "cactus.port" and "cactus.context" tokens.
     *  
     * @throws Exception if an error happens during the test
     */
    public void testCreateFilterChainOk() throws Exception
    {
        String testInputDirProperty = System.getProperty("testinput.dir");
        assertTrue("The system property 'testinput.dir' must be set",
            testInputDirProperty != null);
        File testInputDir = new File(testInputDirProperty);
        assertTrue("The system property 'testinput.dir' must point to an "
            + "existing directory", testInputDir.isDirectory());
        String fileName = 
            "org/apache/cactus/integration/ant/cactified.ear";
        File earFile = new File(testInputDir, fileName);
        assertTrue("The test input " + fileName + " does not exist",
            earFile.exists());

        this.container.setDeployableFile(EarParser.parse(earFile));

        // Note that we needed to add a last character to the string
        // after the @cactus.context@ token as otherwise the Ant code 
        // fails! It looks like an Ant bug to me...       
        String buffer = "@cactus.port@:@cactus.context@:";
                        
        FilterChain chain = this.container.createFilterChain();        

        ChainReaderHelper helper = new ChainReaderHelper();
        Vector chains = new Vector();
        chains.addElement(chain);
        helper.setFilterChains(chains);
        helper.setPrimaryReader(new StringReader(buffer));
        Reader reader = helper.getAssembledReader();       
        assertEquals("8080:empty:", helper.readFully(reader)); 
    }
}
