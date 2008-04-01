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

/**
 * Provides access to the Cactus configuration parameters related to the
 * Servlet Redirector.
 *
 * @version $Id: DefaultServletConfiguration.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class DefaultServletConfiguration 
    extends AbstractWebConfiguration implements ServletConfiguration 
{
    /**
     * Name of the cactus property that specifies the name of the Servlet
     * redirector.
     */
    public static final String CACTUS_SERVLET_REDIRECTOR_NAME_PROPERTY = 
        "cactus.servletRedirectorName";

    /**
     * {@inheritDoc}
     * @see WebConfiguration#getDefaultRedirectorName()
     */
    public String getDefaultRedirectorName()
    {
        String redirectorName = 
            System.getProperty(CACTUS_SERVLET_REDIRECTOR_NAME_PROPERTY);

        if (redirectorName == null)
        {
            redirectorName = "ServletRedirector";
        }

        return redirectorName;
    }
}
