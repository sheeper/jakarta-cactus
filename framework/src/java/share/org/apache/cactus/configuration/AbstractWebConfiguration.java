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

import org.apache.cactus.WebRequest;

/**
 * Common implementation for all <code>WebConfiguration</code> 
 * implementations.
 *
 * @version $Id$
 */
public abstract class AbstractWebConfiguration extends BaseConfiguration 
    implements WebConfiguration
{
    /**
     * @see WebConfiguration#getDefaultRedirectorURL()
     */
    public String getDefaultRedirectorURL()
    {
        return getContextURL() + "/" + getDefaultRedirectorName();
    }

    /**
     * @see WebConfiguration#getRedirectorURL(WebRequest)
     */
    public String getRedirectorURL(WebRequest theRequest)
    {
        return getContextURL() + "/" + getRedirectorName(theRequest);
    }

    /**
     * @see WebConfiguration#getRedirectorName(WebRequest)
     */
    public String getRedirectorName(WebRequest theRequest)
    {
        String redirectorName;
        
        if (theRequest.getRedirectorName() != null)
        {
            redirectorName = theRequest.getRedirectorName();
        }
        else
        {
            redirectorName = getDefaultRedirectorName();
        }

        return redirectorName;
    }

}
