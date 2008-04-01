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
package org.apache.cactus.eclipse.runner.containers;

import java.net.URL;

/**
 * Configuration information for containers. It contains all information needed
 * for container start and stop actions.
 * 
 * @version $Id: ContainerInfo.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class ContainerInfo
{
    /**
     * @return the URL for the web application context under which the
     *         cactus test is running. Example: http://localhost:8080/test.
     */
    private URL contextURL;

    /**
     * Constructor.
     * @param theURL the URL related to this container info
     */
    ContainerInfo(URL theURL)
    {
        contextURL = theURL;
    }

    /**
     * Returns the contextURL.
     * @return URL
     */
    public URL getContextURL()
    {
        return contextURL;
    }
    
    /**
     * Sets the contextURL.
     * @param theURL the context URL to set
     */
    public void setContextURL(URL theURL)
    {
        contextURL = theURL;
    }
}
