/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation.
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

/**
 * Represents a WAR file to deploy in a container. 
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public class WarDeployableFile extends AbstractDeployableFile 
    implements Cloneable
{
    /**
     * @see DeployableFile#isWar()
     */
    public final boolean isWar()
    {
        return true;
    }
    
    /**
     * @see DeployableFile#isEar()
     */
    public final boolean isEar()
    {
        return false;
    }
}
