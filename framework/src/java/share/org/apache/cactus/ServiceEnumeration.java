/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Cactus" and "Apache Software
 *    Foundation" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
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
 *
 */
package org.apache.cactus;

/**
 * List of valid services that the test redirectors can perform.
 * 
 * <p>
 *   <strong>WARNING</strong><br/>
 *   This class is not intended for use by API clients. It may be altered in
 *   backwards-incompatible ways and even moved or removed at any time without
 *   further notice.
 * </p>
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class ServiceEnumeration
{
    /**
     * Call test method Service.
     */
    public static final ServiceEnumeration CALL_TEST_SERVICE = 
        new ServiceEnumeration("CALL_TEST");

    /**
     * Get the previous test results Service.
     */
    public static final ServiceEnumeration GET_RESULTS_SERVICE = 
        new ServiceEnumeration("GET_RESULTS");

    /**
     * Noop service for testing.
     */
    public static final ServiceEnumeration RUN_TEST_SERVICE = 
        new ServiceEnumeration("RUN_TEST");

    /**
     * Service used to create an HTTP session so that it is returned
     * in a cookie.
     */
    public static final ServiceEnumeration CREATE_SESSION_SERVICE = 
        new ServiceEnumeration("CREATE_SESSION");

    /**
     * Service that returns a cactus version identifier. This is used
     * to verify that the server side and client side versions of 
     * Cactus are the same.
     */
    public static final ServiceEnumeration GET_VERSION_SERVICE = 
        new ServiceEnumeration("GET_VERSION");

    /**
     * The service's name
     */
    private String name;

    /**
     * Private constructor to only allow the predefined instances of the
     * enumeration.
     *
     * @param theServiceName the name of the service
     */
    private ServiceEnumeration(String theServiceName)
    {
        this.name = theServiceName;
    }

    /**
     * Compares a string representing the name of the service with the service
     * enumerated type.
     *
     * @param theString the string to compare with this Service name
     * @return true if the string corresponds to the current Service
     * @deprecated Use {@link ServiceEnumeration#valueOf} and identity
     *              comparison instead of this method
     */
    public boolean equals(String theString)
    {
        return theString.equals(this.name);
    }

    /**
     * Always compares object identity.
     * 
     * @see java.lang.Object#equals(Object)
     */
    public final boolean equals(Object theObject)
    {
        return super.equals(theObject);
    }

    /**
     * Delegates to the <code>java.lang.Object</code> implementation.
     * 
     * @see java.lang.Object#equals(Object)
     */
    public final int hashCode()
    {
        return super.hashCode();
    }

    /**
     * Returns the string representation of the service.
     * 
     * @return the service's name
     * @see java.lang.Object#toString
     */
    public String toString()
    {
        return this.name;
    }
    
    /**
     * Returns the enumeration instance corresponding to the provided service 
     * name.
     * 
     * @param theName The name of the service
     * @return The corresponding service instance
     * @since Cactus 1.5
     */
    public static ServiceEnumeration valueOf(String theName)
    {
        if (CALL_TEST_SERVICE.name.equals(theName))
        {
            return CALL_TEST_SERVICE;
        }
        else if (GET_RESULTS_SERVICE.name.equals(theName))
        {
            return GET_RESULTS_SERVICE;
        }
        else if (RUN_TEST_SERVICE.name.equals(theName))
        {
            return RUN_TEST_SERVICE;
        }
        else if (CREATE_SESSION_SERVICE.name.equals(theName))
        {
            return CREATE_SESSION_SERVICE;
        }
        else if (GET_VERSION_SERVICE.name.equals(theName))
        {
            return GET_VERSION_SERVICE;
        }
        return null;
    }
    
}
