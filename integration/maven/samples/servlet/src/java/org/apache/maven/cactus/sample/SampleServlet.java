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
package org.apache.maven.cactus.sample;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Sample servlet that implements some very simple business logic. The goal is
 * to provide some functional tests for Cactus and examples for Cactus users.
 * This servlet simply checks is a user is authenticated
 *
 * @version $Id$
 */
public class SampleServlet extends HttpServlet
{
    /**
     *Take a request object and return whether the user is authenticated o not.
     *
     * @param theRequest the HttpServletRequest object
     *
     * @return boolean whether the request is by an authenticated user or not
     *
     */
    public boolean isAuthenticated(HttpServletRequest theRequest)
    {
        HttpSession session = theRequest.getSession(false);

        if (session == null)
        {
            return false;
        }

        String authenticationAttribute =
            (String) session.getAttribute("authenticated");

        return Boolean.valueOf(authenticationAttribute).booleanValue();
    }
}
