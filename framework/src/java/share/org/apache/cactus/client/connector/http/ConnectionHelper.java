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
package org.apache.cactus.client.connector.http;

import java.net.HttpURLConnection;

import org.apache.cactus.WebRequest;
import org.apache.cactus.configuration.Configuration;

/**
 * Helper class to open an HTTP connection to the server redirector and pass
 * to it HTTP parameters, Cookies and HTTP headers. It enables different
 * possible implementations of an HTTP connection (ex: using the JDK
 * <code>HttpURLConnection</code> or using Jakarta HttpClient).
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public interface ConnectionHelper
{
    /**
     * Connects to the Cactus Redirector using HTTP.
     *
     * @param theRequest the request containing all data to pass to the
     *        server redirector.
     * @param theConfiguration the Cactus configuration
     * @return the HTTP Connection used to connect to the redirector.
     * @exception Throwable if an unexpected error occured
     */
    HttpURLConnection connect(WebRequest theRequest, 
        Configuration theConfiguration) throws Throwable;
}
