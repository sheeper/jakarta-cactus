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
package org.apache.cactus.integration.ant.container.resin;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Path;

/**
 * Special container support for the Caucho Resin 2.x servlet container.
 * 
 * @version $Id$
 */
public class Resin2xContainer extends AbstractResinContainer
{
    // AbstractContainer Implementation ----------------------------------------

    /**
     * @see org.apache.cactus.integration.ant.container.Container#getName
     */
    public final String getName()
    {
        return "Resin 2.x";
    }
    
    // AbstractResinContainer Implementation -----------------------------------

    /**
     * @see AbstractResinContainer#getContainerDirName
     */
    protected final String getContainerDirName()
    {
        return "resin2x";
    }

    /**
     * @see AbstractResinContainer#startUpAdditions(Java, Path)
     */
    protected void startUpAdditions(Java theJavaContainer, Path theClasspath)
    {
        // Nothing additional required
    }

    /**
     * @see AbstractResinContainer#prepareAdditions(FilterChain)
     */
    protected void prepareAdditions(FilterChain theFilterChain)
    {
        // Nothing additional required
    }
}
