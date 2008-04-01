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
package org.apache.cactus.internal.configuration;

import org.apache.cactus.WebRequest;

/**
 * Common implementation for all <code>WebConfiguration</code> 
 * implementations.
 *
 * @version $Id: AbstractWebConfiguration.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public abstract class AbstractWebConfiguration extends BaseConfiguration 
    implements WebConfiguration
{
    /**
     * {@inheritDoc}
     * @see WebConfiguration#getDefaultRedirectorURL()
     */
    public String getDefaultRedirectorURL()
    {
        return getContextURL() + "/" + getDefaultRedirectorName();
    }

    /**
     * {@inheritDoc}
     * @see WebConfiguration#getRedirectorURL(WebRequest)
     */
    public String getRedirectorURL(WebRequest theRequest)
    {
        return getContextURL() + "/" + getRedirectorName(theRequest);
    }

    /**
     * {@inheritDoc}
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
