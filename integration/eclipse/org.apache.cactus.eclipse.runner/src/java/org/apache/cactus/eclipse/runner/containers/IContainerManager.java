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
package org.apache.cactus.eclipse.runner.containers;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Interface for container providers management.
 * 
 * @version $Id$
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
