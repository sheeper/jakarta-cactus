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
package org.apache.cactus.spi.client;

import org.apache.cactus.Request;
import org.apache.cactus.internal.client.ClientException;

/**
 * Constructs response object that are passed as parameter to
 * <code>endXXX()</code> and global <code>end()</code> methods.
 *
 * @version $Id: ResponseObjectFactory.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public interface ResponseObjectFactory
{
    /**
     * Constructs response object that are passed as parameter to
     * <code>endXXX()</code> and global end methods.
     *
     * @param theClassName the class name of the object to construct
     * @param theRequest the request that was sent by Cactus to the server side
     * @return the response object that will be passed to <code>endXXX()</code>
     *         and <code>end()</code> methods 
     * @throws ClientException if it fails to construct the response object
     */
    Object getResponseObject(String theClassName, Request theRequest) 
        throws ClientException;
}
