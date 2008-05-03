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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlUtils;
import org.codehaus.cargo.util.log.Logger;

/**
 * Abstract base class for nested redirector elements.
 * @version 
 */
public abstract class Redirector
{
	/**
	 * Cargo logger - when used by Ant, AntLogger
	 * must instantiated, when used by Maven
	 * MavenLogger should be instantiated;
	 */
	protected Logger logger;
    /**
     * Default constructor.
     */
    public Redirector()
    { }
    
    /**
     * Constructor with ownerTask.
     * 
     * @param theOwnerTask to be instantiated
     */
    public Redirector(Logger logger)
    {
        this.logger = logger;
    }
    
    // Instance Variables --------------------------------------------------

    /**
     * The name of the redirector.
     */
    protected String name;

    /**
     * The URL pattern that the redirector will be mapped to. 
     */
    protected String mapping;
    
    /**
     * Comma-separated list of role names that should be granted access to
     * the redirector.
     */
    protected String roles;
    
    // Abstract Methods ----------------------------------------------------

    /**
     * Merges the definition of the redirector into the provided deployment
     * descriptor.
     * 
     * @param theWebXml The deployment descriptor into which the redirector
     *        definition should be merged
     */
    public abstract void mergeInto(WebXml theWebXml);

    // Public Methods ------------------------------------------------------

    /**
     * Sets the name of the redirector.
     * 
     * @param theName The name to set
     */
    public final void setName(String theName)
    {
        this.name = theName;
    }

    /**
     * Sets the URL pattern that the redirector should be mapped to.
     * 
     * @param theMapping The URL pattern to set
     */
    public final void setMapping(String theMapping)
    {
        this.mapping = theMapping;
    }

    /**
     * Sets the comma-separated list of role names that should be granted
     * access to the redirector.
     * 
     * @param theRoles The roles to set
     */
    public final void setRoles(String theRoles)
    {
        this.roles = theRoles;
    }

    // Protected Methods ---------------------------------------------------

    /**
     * Adds the comma-separated list of security roles to a deployment
     * descriptor.
     * 
     * @param theWebXml The deployment descriptor
     */
    protected final void addSecurity(WebXml theWebXml)
    {
        StringTokenizer tokenizer = new StringTokenizer(this.roles, ",");
        List roles = new ArrayList();
        while (tokenizer.hasMoreTokens())
        {
            String role = tokenizer.nextToken().trim();
            if (!WebXmlUtils.hasSecurityRole(theWebXml,role))
            {
                WebXmlUtils.addSecurityRole(theWebXml,role);
            }
            roles.add(role);
        }
        if (!roles.isEmpty())
        {
            if (!WebXmlUtils.hasLoginConfig(theWebXml))
            {
                WebXmlUtils.setLoginConfig(theWebXml, "BASIC", "myrealm");
            }
            if (!WebXmlUtils.hasSecurityConstraint(theWebXml,this.mapping))
            {
                WebXmlUtils.addSecurityConstraint(theWebXml,"Cactus Test Redirector",
                    this.mapping, roles);
            }
        }
    }

}
