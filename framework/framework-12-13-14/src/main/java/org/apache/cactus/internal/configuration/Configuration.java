/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.cactus.internal.configuration;

/**
 * Contains all configuration information for the Cactus framework.
 * 
 * @version $Id: Configuration.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public interface Configuration
{
    /**
     * @return the context URL under which our application to test runs.
     */
    String getContextURL();

    /**
     * @return the initializer class (i.e. a class that is executed before the
     *         Cactus tests start on the client side) or null if none has been
     *         defined
     */
    String getInitializer();
}
