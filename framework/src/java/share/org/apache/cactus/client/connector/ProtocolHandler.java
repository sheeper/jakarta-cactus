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

import junit.framework.Test;

import org.apache.cactus.Request;
import org.apache.cactus.client.ResponseObjectFactory;

/**
 * Any communication protocol (e.g HTTP) used to connect between
 * Cactus client side and Cactus server side must implement this lifecycle 
 * interface. This interface is part of the connector SPI.
 * 
 * Here is the lifecycle followed by Cactus core:
 * <ul>
 *   <li>
 *     Call {@link #createRequest} to create a request object that will be
 *     passed to the <code>begin()</code> and <code>beginXXX()</code> 
 *     methods. They will in turn enrich it with values set by the user.
 *   </li>
 *   <li>
 *     Call <code>begin()</code> and <code>beginXXX()</code> methods.
 *   </li>
 *   <li>
 *     Call {@link #runTest} to execute the tests.
 *   </li>
 *   <li>
 *     Call {@link #createResponseObjectFactory} to create a factory that is 
 *     used to create a test response object that will be passed to the 
 *     <code>endXXX()</code> and <code>end()</code> methods.
 *   </li>
 *   <li>
 *     Call <code>endXXX()</code> and <code>end()</code> methods.
 *   </li>
 *   <li>
 *     Call {@link #afterTest} to let the connector implementor clean up after
 *     the test. For example, the HTTP connector implementation closes the HTTP
 *     connection if the user has not closed it himself.
 *   </li>
 * </ul> 
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 * @since 1.6 
 */
public interface ProtocolHandler
{
    /**
     * Create a request object that will be passed to the <code>begin()</code>
     * and <code>beginXXX()</code> methods. They will in turn enrich it with 
     * values set by the user.
     * 
     * @return the request object
     */
    Request createRequest();
    
    /**
     * Connect to the server side (to the redirector proxy), passing all 
     * information to execute the test there, trigger the test execution and
     * gather the test results.
     *
     * @param theDelegatedTest the Cactus test to execute
     * @param theWrappedTest optionally specify a pure JUnit test case that is
     *        being wrapped and will be executed on the server side
     * @param theRequest the request containing data to connect to the 
     *        redirector proxy
     * @return an object holding state information that should be preserved and
     *         that will be passed to {@link #createResponseObjectFactory} and
     *         {@link #afterTest} later on 
     * @exception Throwable any error that occurred when connecting to the 
     *            server side, when executing the test or when gathering the
     *            test result.
     */
    ProtocolState runTest(Test theDelegatedTest, Test theWrappedTest,
        Request theRequest) throws Throwable;

    /**
     * Create a factory that is used by the core to create test response object 
     * that will be passed to the <code>endXXX()</code> and <code>end()</code> 
     * methods.
     * 
     * @param theState any state information that has been preserved from the
     *        {@link #runTest} method (e.g. the HTTP connection object)  
     * @return the response object factory
     */
    ResponseObjectFactory createResponseObjectFactory(ProtocolState theState);

    /**
     * Let the connector implementor clean up after the test. For example, the 
     * HTTP connector implementation closes the HTTP connection if the user has 
     * not closed it himself.
     *  
     * @param theState any state information that has been preserved from the
     *        {@link #runTest} method (e.g. the HTTP connection object)  
     * @throws Exception on error 
     */
    void afterTest(ProtocolState theState) throws Exception;
}
