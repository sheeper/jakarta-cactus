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
package org.apache.cactus.client.authentication;

import org.apache.cactus.WebRequest;
import org.apache.cactus.internal.configuration.Configuration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;

/**
 * Basic Authentication support.
 *
 * @since 1.3
 * @see AbstractAuthentication
 *
 * @version $Id: BasicAuthentication.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class BasicAuthentication extends AbstractAuthentication
{
    /**
     * @param theName user name of the Credential
     * @param thePassword user password of the Credential
     */
    public BasicAuthentication(String theName, String thePassword)
    {
        super(theName, thePassword);
    }

    /**
     * {@inheritDoc}
     * @see Authentication#configure
     */
    public void configure(HttpState theState, HttpMethod theMethod,
        WebRequest theRequest, Configuration theConfiguration)
    {
        theState.setAuthenticationPreemptive(true);
        theState.setCredentials(null, null, 
            new UsernamePasswordCredentials(getName(), getPassword()));
        theMethod.setDoAuthentication(true);
    }
}
