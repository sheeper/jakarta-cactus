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
package org.apache.cactus.integration.api.cactify;

import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.util.log.Logger;

/**
 * Implementation of <code>Redirector</code> for JSP test redirectors. 
 */
public final class JspRedirector extends Redirector
{
    /**
     * The default mapping of the Cactus JSP redirector.
     */
    private static final String DEFAULT_JSP_REDIRECTOR_MAPPING =
        "/JspRedirector";

    /**
     * Default constructor.
     */
    public JspRedirector()
    {
        this.name = "JspRedirector";
        this.mapping = DEFAULT_JSP_REDIRECTOR_MAPPING;
    }
    
    /**
     * Constructor with owner task to set.
     * @param theOwnerTask object
     */
    public JspRedirector(Logger logger)
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
        //The iterator is never null
        if (theWebXml.getServletNamesForJspFile
                ("/jspRedirector.jsp").hasNext() && logger != null) 
        {
            logger.warn("WARNING: Your web.xml already includes " 
            + this.name + " mapping. Cactus is adding another one " 
            + "which may prevent your container from starting.", "WARNING");
        }
        theWebXml.addJspFile(this.name, "/jspRedirector.jsp");
        theWebXml.addServletMapping(this.name, this.mapping);
        if (this.roles != null)
        {
            addSecurity(theWebXml);
        }
    }
    
}
