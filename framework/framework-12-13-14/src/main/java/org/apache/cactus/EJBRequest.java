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
package org.apache.cactus;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Prototype of EJBRedirector for Cactus.
 * 
 * @version $Id$
 */
public class EJBRequest implements Request, Serializable 
{

    /**
     * Request parameters are stored here.
     */
    private Hashtable requestMaps;

    /**
     * Default constructor.
     */
    public EJBRequest() 
    {
        requestMaps = new Hashtable();
    }

    /**
     * A method to set the name of the class as a request parameter.
     * @param theClassKey to set to
     * @param theName to set
     */
    public void setClassName(String theClassKey, String theName)
    {
        requestMaps.put(theClassKey, theName);
    }
    
    /**
     * A method to get the name of the class as a request parameter.
     * 
     * @param theClassKey the parameter
     * @return the name of the class
     */
    public String getClassName(String theClassKey) 
    {
        return (String) requestMaps.get(theClassKey);
    }
    
    /**
     * A method to set the name of the test-method.
     * @param theMethodKey the method-key
     * @param theName the name of the method
     */
    public void setMethodName(String theMethodKey, String theName) 
    {
        requestMaps.put(theMethodKey, theName);
    }
    
    /**
     * Getter method for the name of the test-method.
     * 
     * @param theMethodKey the parameter
     * @return the name of the method
     */
    public String getMethodName(String theMethodKey) 
    {
        return (String) requestMaps.get(theMethodKey);
    }
}

