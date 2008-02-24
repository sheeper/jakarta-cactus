/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation.
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
package org.apache.cactus.spi.client.connector;

/**
 * Hold protocol-related information that need to be exchanged during the 
 * lifecycle of the 
 * {@link org.apache.cactus.spi.client.connector.ProtocolHandler}.
 * For example the HTTP protocol handler needs to pass the HTTP connection
 * around to the different lifecycle methods. However, as this kind of state
 * information is highly protocol dependent, we needed to abstract out the
 * state information, hence this tagging interface. The implementation is free
 * to have any kind of methods. These methods will only be used in the
 * {@link org.apache.cactus.spi.client.connector.ProtocolHandler} implementation
 * classes.
 * 
 * @version $Id: ProtocolState.java 238991 2004-05-22 11:34:50Z vmassol $
 * @since 1.6 
 */
public interface ProtocolState
{
}
