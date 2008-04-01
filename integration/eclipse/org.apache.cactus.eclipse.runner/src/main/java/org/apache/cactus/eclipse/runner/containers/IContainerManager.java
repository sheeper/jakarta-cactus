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
package org.apache.cactus.eclipse.runner.containers;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Interface for container providers management.
 * 
 * @version $Id: IContainerManager.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public interface IContainerManager
{
    /**
     * Prepares the container providers for Cactus tests
     * @param theJavaProject the Java project to prepare providers for
     */
    void prepare(IJavaProject theJavaProject);

    /**
     * Tears down the container providers
     */
    void tearDown();
}
