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
package org.apache.cactus.internal;

/**
 * List of valid services that the test redirectors can perform.
 * 
 * @version $Id: ServiceEnumeration.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public final class ServiceEnumeration
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
     * @since 1.5
     */
    public static final ServiceEnumeration CREATE_SESSION_SERVICE = 
        new ServiceEnumeration("CREATE_SESSION");

    /**
     * Service that returns a cactus version identifier. This is used
     * to verify that the server side and client side versions of 
     * Cactus are the same.
     * @since 1.5
     */
    public static final ServiceEnumeration GET_VERSION_SERVICE = 
        new ServiceEnumeration("GET_VERSION");

    /**
     * The service's name.
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
     * {@inheritDoc}
     * @see java.lang.Object#equals(Object)
     * @since 1.5
     */
    public boolean equals(Object theObject)
    {
        return super.equals(theObject);
    }

    /**
     * Delegates to the <code>java.lang.Object</code> implementation.
     * 
     * {@inheritDoc}
     * @see java.lang.Object#equals(Object)
     * @since 1.5
     */
    public int hashCode()
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
     * @since 1.5
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
