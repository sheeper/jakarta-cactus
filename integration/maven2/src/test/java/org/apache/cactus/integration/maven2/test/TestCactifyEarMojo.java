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
package org.apache.cactus.integration.maven2.test;

import java.io.File;

import org.apache.cactus.maven2.mojos.CactifyEarMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;

/**
 * Some unit tests for the <code>CactifyEarMojo</code> class.
 * 
 * @author ptahchiev
 */
public class TestCactifyEarMojo extends AbstractMojoTestCase 
{
    /**
     * The cactifyEarMojo instance.
     */
    private CactifyEarMojo cactifyEarMojo;
    
    /**
     * Set-up method to instantiate the mojo.
     */
    public void setUp() throws Exception
    {
        super.setUp();
        this.cactifyEarMojo = new CactifyEarMojo();
    }
    

    /**
     * Tests the mojo with absolutely no parameters specified
     * @throws Exception in case some error occurs
     */
    public void testCactifyEarWithNoParametersSpecified() throws Exception
    {
        File testPom = new File(getBasedir(), "target/test-classes/unit/ear/basic-"
                + "cactify-noparameters/plugin-config.xml");
        CactifyEarMojo mojo = (CactifyEarMojo) lookupMojo("cactifyear", testPom);
        assertNotNull(mojo);
        try 
        {
        	mojo.execute();
        	fail("Exception should have been raised!");
        } 
        catch (MojoExecutionException mex)
        {
        	assertEquals("You need to specify [srcFile] for cactification!", mex.getMessage());
        }
    }
}