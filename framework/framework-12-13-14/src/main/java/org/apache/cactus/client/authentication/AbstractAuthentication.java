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

/**
 * This class was designed with the simple assumption that ALL authentication
 * implementations will have a String <code>Name</code> and a String
 * <code>Password</code>. Two abstract functions <code>validateName</code> and
 * <code>validatePassword</code> provide for concrete implementations to
 * perform character validation. All the work is then done in the
 * <code>configure</code> abstract function. In the
 * <code>BasicAuthentication</code> class, for example, the configuring is done
 * by adding the request property "Authorization" with a value
 * "Basic &lt;base64encode of 'userid:password'&gt;".
 *
 * @since 1.3
 *
 * @version $Id: AbstractAuthentication.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public abstract class AbstractAuthentication implements Authentication
{
    /**
     * User name part of the Credential. 
     */
    private String name;

    /**
     * Password part of the Credential. 
     */
    private String password;

    /**
     * @param theName user name of the Credential
     * @param thePassword user password of the Credential
     */
    public AbstractAuthentication(String theName, String thePassword)
    {
        setName(theName);
        setPassword(thePassword);
    }

    /**
     * Sets the user name.
     *
     * @param theName user name of the Credential
     */
    public final void setName(String theName)
    {
        this.name = theName;
    }

    /**
     * @return the user name of the Credential
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * Sets the user password of the Credential.
     *
     * @param thePassword the user password of the Credential
     */
    public final void setPassword(String thePassword)
    {
        this.password = thePassword;
    }

    /**
     * @return the user password of the Credential
     */
    public final String getPassword()
    {
        return this.password;
    }
}
