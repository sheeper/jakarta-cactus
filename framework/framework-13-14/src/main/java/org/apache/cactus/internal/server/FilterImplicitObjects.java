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

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;

/**
 * Holder class that contains the instances of the implicit objects that will
 * be accessible in the test classes (ie subclasses of
 * <code>FilterTestCase</code>).
 *
 * @version $Id: FilterImplicitObjects.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class FilterImplicitObjects extends AbstractWebImplicitObjects
{
    /**
     * The Filter configuration object.
     */
    protected FilterConfig config;

    /**
     * The Filter chain object.
     */
    protected FilterChain filterChain;

    /**
     * @return the <code>FilterConfig</code> implicit object
     */
    public FilterConfig getFilterConfig()
    {
        return this.config;
    }

    /**
     * @param theConfig the <code>FilterConfig</code> implicit object
     */
    public void setFilterConfig(FilterConfig theConfig)
    {
        this.config = theConfig;
    }

    /**
     * @return the <code>FilterChain</code> implicit object
     */
    public FilterChain getFilterChain()
    {
        return this.filterChain;
    }

    /**
     * @param theFilterChain the <code>FilterChain</code> implicit object
     */
    public void setFilterChain(FilterChain theFilterChain)
    {
        this.filterChain = theFilterChain;
    }
}
