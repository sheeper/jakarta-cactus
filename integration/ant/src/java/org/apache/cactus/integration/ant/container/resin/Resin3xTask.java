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
package org.apache.cactus.integration.ant.container.resin;

/**
 * Start/stop a Resin 3.x instance.
 * 
 * @version $Id$
 */
public class Resin3xTask extends AbstractResinTask
{
    /**
     * @see AbstractResinTask#getResinContainer()
     */
    protected AbstractResinContainer getResinContainer()
    {
        return new Resin3xContainer();
    }
}
