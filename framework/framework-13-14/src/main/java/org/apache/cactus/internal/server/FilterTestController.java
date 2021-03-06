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
package org.apache.cactus.internal.server;

/**
 * Filter Controller that extracts the requested service from the
 * HTTP request and executes the request by calling a
 * <code>FilterTestCaller</code>. There are 2 services available : one for
 * executing the test and one for returning the test result.
 *
 * @version $Id: FilterTestController.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class FilterTestController extends AbstractWebTestController
{
    /**
     * {@inheritDoc}
     * @see AbstractWebTestController#getTestCaller(WebImplicitObjects)
     */
    protected AbstractWebTestCaller getTestCaller(WebImplicitObjects theObjects)
    {
        return new FilterTestCaller((FilterImplicitObjects) theObjects);
    }
}
