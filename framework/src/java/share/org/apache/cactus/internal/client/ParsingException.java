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
package org.apache.cactus.internal.client;

import org.apache.cactus.util.ChainedException;

/**
 * Thrown when parsing the Web Test result (XML) and trying to build a
 * <code>WebTestResult</code> object.
 *
 * @see WebTestResultParser
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ParsingException extends ChainedException
{
    /**
     * @see ChainedException#ChainedException(String)
     */
    public ParsingException(String theMessage)
    {
        super(theMessage);
    }

    /**
     * @see ChainedException#ChainedException(Throwable)
     */
    public ParsingException(Throwable theException)
    {
        super(theException);
    }

    /**
     * @see ChainedException#ChainedException(String, Throwable)
     */
    public ParsingException(String theMessage, Throwable theException)
    {
        super(theMessage, theException);
    }
}
