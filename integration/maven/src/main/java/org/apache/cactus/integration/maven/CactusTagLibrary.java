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
package org.apache.cactus.integration.maven;

import org.apache.commons.jelly.TagLibrary;

/**
 * Maven tag library for use in Jelly scripts.
 *
 * @version $Id: CactusTagLibrary.java 238815 2004-02-29 16:34:44Z vmassol $
 */
public class CactusTagLibrary extends TagLibrary
{
    /**
     * Register Cactus jelly tags.
     */
    public CactusTagLibrary()
    {
        registerTag("scanner", CactusScannerTag.class);
    }
}
