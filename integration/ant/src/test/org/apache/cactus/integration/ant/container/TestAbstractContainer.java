/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Cactus" and "Apache Software
 *    Foundation" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
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
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
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

        this.container.setDeployableFile(
            new EarDeployableFile(earFile));

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