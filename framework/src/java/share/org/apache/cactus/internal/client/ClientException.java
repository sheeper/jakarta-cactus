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
 * A Cactus client side exception.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ClientException extends ChainedException
{
    /**
     * @see ChainedException#ChainedException(String)
     */
    public ClientException(String theMessage)
    {
        super(theMessage);
    }

    /**
     * @see ChainedException#ChainedException(String, Throwable)
     */
    public ClientException(String theMessage, Throwable theException)
    {
        super(theMessage, theException);
    }

    /**
     * @see ChainedException#ChainedException(Throwable)
     */
    public ClientException(Throwable theException)
    {
        super(theException);
    }
}
