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
package org.apache.cactus;

/**
 * Cookie containing an HTTP Session id.
 * 
 * @version $Id: HttpSessionCookie.java 238991 2004-05-22 11:34:50Z vmassol $
 * @since 1.5
 */
public class HttpSessionCookie extends Cookie
{
    /**
     * @see Cookie#Cookie(String, String, String)
     * @param theDomain of the sessionCookie
     * @param theName of the SessionCookie
     * @param theValue of the SessionCookie
     */
    public HttpSessionCookie(String theDomain, String theName, String theValue)
    {
        super(theDomain, theName, theValue);
    }
}