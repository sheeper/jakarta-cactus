/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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
package org.apache.cactus.configuration;


/**
 * Provides access to the Cactus configuration parameters related to the
 * JSP Redirector.
 *
 * @version $Id$
 */
public class JspConfiguration extends AbstractWebConfiguration
{
    /**
     * Name of the cactus property that specifies the name of the JSP
     * redirector.
     */
    public static final String CACTUS_JSP_REDIRECTOR_NAME_PROPERTY = 
        "cactus.jspRedirectorName";

    /**
     * @see WebConfiguration#getDefaultRedirectorName()
     */
    public String getDefaultRedirectorName()
    {
        String redirectorName = 
            System.getProperty(CACTUS_JSP_REDIRECTOR_NAME_PROPERTY);

        if (redirectorName == null)
        {
            redirectorName = "JspRedirector";
        }

        return redirectorName;
    }

}
