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
package org.apache.cactus.server;

/**
 * Servlet Controller that extracts the requested service from the
 * HTTP request and executes the request by calling a
 * <code>ServletTestCaller</code>. There are 2 services available : one for
 * executing the test and one for returning the test result.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class ServletTestController extends AbstractWebTestController
{
    /**
     * @see AbstractWebTestController#getTestCaller(WebImplicitObjects)
     */
    protected AbstractWebTestCaller getTestCaller(WebImplicitObjects theObjects)
    {
        return new ServletTestCaller((ServletImplicitObjects) theObjects);
    }
}
