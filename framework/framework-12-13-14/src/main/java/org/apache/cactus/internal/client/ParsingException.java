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
package org.apache.cactus.internal.client;

import org.apache.cactus.util.ChainedException;

/**
 * Thrown when parsing the Web Test result (XML) and trying to build a
 * <code>WebTestResult</code> object.
 *
 * @see WebTestResultParser
 *
 * @version $Id: ParsingException.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class ParsingException extends ChainedException
{
    /**
     * {@inheritDoc}
     * @see ChainedException#ChainedException(String)
     */
    public ParsingException(String theMessage)
    {
        super(theMessage);
    }

    /**
     * {@inheritDoc}
     * @see ChainedException#ChainedException(Throwable)
     */
    public ParsingException(Throwable theException)
    {
        super(theException);
    }

    /**
     * {@inheritDoc}
     * @see ChainedException#ChainedException(String, Throwable)
     */
    public ParsingException(String theMessage, Throwable theException)
    {
        super(theMessage, theException);
    }
}
