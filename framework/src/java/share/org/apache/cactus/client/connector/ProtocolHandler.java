/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Cactus" and "Apache Software
 *    Foundation" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
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
