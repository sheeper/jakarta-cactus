/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.cactus.client.authentication;

import java.net.HttpURLConnection;

/**
 * This class was designed with the simple assumption that ALL authentication
 * implementations will have a String <code>UserId</code> and a string
 * <code>Password</code>. Two abstract functions <code>validateUserId</code> and
 * <code>validatePassword</code> provide for concrete implementations to
 * perform character validation. All the work is then done in the
 * <code>configure</code> abstract function. In the
 * <code>BasicAuthentication</code> class, for example, the configuring is done
 * by adding the request property "Authorization" with a value
 * "Basic <base64encode of 'userid:password'>".
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:Jason.Robertson@acs-inc.com">Jason Robertson</a>
 *
 * @version $Id$
 */
public abstract class AbstractAuthentication
{
    /**
     * User id part of the Credential
     */
    private String userId;

    /**
     * Password part of the Credential
     */
    private String password;

    public AbstractAuthentication(String theUserId, String thePassword)
    {
        setUserId(theUserId);
        setPassword(thePassword);
    }

    public void setUserId(String theUserId)
    {
        validateUserId(theUserId);
        this.userId = theUserId;
    }

    public String getUserId()
    {
        return this.userId;
    }

    public void setPassword(String thePassword)
    {
        validatePassword(thePassword);
        this.password = thePassword;
    }

    protected abstract void validateUserId(String theUserId);

    protected abstract void validatePassword(String thePassword);

    public abstract void configure(HttpURLConnection theConnection);

    protected String getPassword()
    {
        return this.password;
    }
}
