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
package org.apache.cactus.internal.server;

import javax.servlet.ServletConfig;

/**
 * Holder class that contains the instances of the implicit objects that will
 * be accessible in the test classes (ie subclasses of
 * <code>ServletTestCase</code>).
 *
 * @version $Id: ServletImplicitObjects.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class ServletImplicitObjects extends AbstractWebImplicitObjects
{
    /**
     * The Servlet configuration object.
     */
    protected ServletConfig config;

    /**
     * @return the <code>ServletConfig</code> implicit object
     */
    public ServletConfig getServletConfig()
    {
        return this.config;
    }

    /**
     * @param theConfig the <code>ServletConfig</code> implicit object
     */
    public void setServletConfig(ServletConfig theConfig)
    {
        this.config = theConfig;
    }
}
