/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation.
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
package org.apache.cactus.client.connector;

/**
 * Hold protocol-related information that need to be exchanged during the 
 * lifecycle of the {@link org.apache.cactus.client.connector.ProtocolHandler}.
 * For example the HTTP protocol handler needs to pass the HTTP connection
 * around to the different lifecycle methods. However, as this kind of state
 * information is highly protocol dependent, we needed to abstract out the
 * state information, hence this tagging interface. The implementation is free
 * to have any kind of methods. These methods will only be used in the
 * {@link org.apache.cactus.client.connector.ProtocolHandler} implementation
 * classes.
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 * @since 1.6 
 */
public interface ProtocolState
{
}
