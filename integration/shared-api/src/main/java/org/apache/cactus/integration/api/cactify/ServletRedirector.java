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
import org.codehaus.cargo.util.log.Logger;

/**
 * Implementation of <code>Redirector</code> for servlet test redirectors. 
 */
public final class ServletRedirector extends Redirector
{

    /**
     * The name of the Cactus servlet redirector class.
     */
    private static final String SERVLET_REDIRECTOR_CLASS =
        "org.apache.cactus.server.ServletTestRedirector";

    /**
     * The default mapping of the Cactus servlet redirector.
     */
    private static final String DEFAULT_SERVLET_REDIRECTOR_MAPPING =
        "/ServletRedirector";
    /**
     * Default constructor.
     */
    public ServletRedirector()
    {
        this.name = "ServletRedirector";
        this.mapping = DEFAULT_SERVLET_REDIRECTOR_MAPPING;
    }
    
    /**
     * Constructor with owner task to set.
     * @param theOwnerTask object
     */
    public ServletRedirector(Logger logger)
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
        if (theWebXml.getServletNamesForClass
            (SERVLET_REDIRECTOR_CLASS).hasNext() && logger != null) 
        {
            logger.warn("WARNING: Your web.xml already includes " 
            + this.name + " mapping. Cactus is adding another one " 
            + "which may prevent your container from starting.", "WARNING");
        }
        theWebXml.addServlet(this.name, SERVLET_REDIRECTOR_CLASS);
        theWebXml.addServletMapping(this.name, this.mapping);
        if (this.roles != null)
        {
            addSecurity(theWebXml);
        }
    }
    
}
