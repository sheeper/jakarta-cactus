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
 * Extends the generic <code>Configuration<code> interface with methods
 * provided configuration information related to Web redirectors.
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public interface WebConfiguration extends Configuration
{
    /**
     * @return the redirector URL for the default redirector
     */
    String getDefaultRedirectorURL();

    /**
     * @return the default redirector name as defined by the Cactus
     *         configuration
     */
    String getDefaultRedirectorName();

    /**
     * @param theRequest the Web request used to connect to the redirector
     * @return the redirector URL for the redirector to use. It is either 
     *         the default redirector name or the redirector defined in 
     *         the Web
     */
    String getRedirectorURL(WebRequest theRequest);

    /**
     * @param theRequest the Web request used to connect to the redirector
     * @return the redirector name to use. It is either the default 
     *         redirector name or the redirector defined in the Web
     *         Request if it has been overriden
     */
    String getRedirectorName(WebRequest theRequest);
}
