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
package org.apache.cactus.integration.api.cactify;

import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlVersion;
import org.codehaus.cargo.util.log.Logger;

/**
 * Implementation of <code>Redirector</code> for filter test redirectors. 
 */
public final class FilterRedirector extends Redirector
{

    /**
     * The name of the Cactus filter redirector class.
     */
    private static final String FILTER_REDIRECTOR_CLASS =
        "org.apache.cactus.server.FilterTestRedirector";
    
    /**
     * The default mapping of the Cactus filter redirector.
     */
    private static final String DEFAULT_FILTER_REDIRECTOR_MAPPING =
        "/FilterRedirector";
    
    /**
     * Default constructor.
     */
    public FilterRedirector()
    {
        this.name = "FilterRedirector";
        this.mapping = DEFAULT_FILTER_REDIRECTOR_MAPPING;
    }
    /**
     * Constructor with owner task to set.
     * @param theOwnerTask object
     */
    public FilterRedirector(Logger logger)
    {
        this();
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     * @see CactifyWarTask.Redirector#mergeInto
     */
    public void mergeInto(WebXml theWebXml)
    {
        if (WebXmlVersion.V2_3.compareTo(theWebXml.getVersion()) <= 0)
        {
            if (theWebXml.getFilterNamesForClass
                (FILTER_REDIRECTOR_CLASS).hasNext() && logger != null) 
            {
                logger.warn("WARNING: Your web.xml already includes " 
                + this.name + " mapping. Cactus is adding another one " 
                + "which may prevent your container from starting.", "WARNING");
            }
            theWebXml.addFilter(this.name, FILTER_REDIRECTOR_CLASS);
            theWebXml.addFilterMapping(this.name, this.mapping);
            if (this.roles != null)
            {
                addSecurity(theWebXml);
            }
        }
    }
    
}
