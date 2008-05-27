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
package org.apache.cactus.maven2.mojos;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
/**
 * A maven2 mojo that injects elements necessary to run Cactus tests into an
 * existing EAR file.
 * 
 * @version $Id: CactifyEarMojo.java 394252 2008-04-29 04:20:17Z ptahchiev $
 * @goal cactifyear
 * @requiresDependencyResolution compile
 */
public class CactifyEarMojo extends AbstractMojo
{

	public void execute() throws MojoExecutionException, MojoFailureException 
	{
	}

}