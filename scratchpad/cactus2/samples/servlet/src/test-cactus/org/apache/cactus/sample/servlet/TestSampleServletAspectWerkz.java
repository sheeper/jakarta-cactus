/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.cactus.sample.servlet;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import junit.framework.TestCase;

public class TestSampleServletAspectWerkz extends TestCase
{
    /**
     * Intercepts Servlet's doXXX calls and instead redirect the flow of
     * execution to the {@link SampleServlet#getRequestParameters} method to
     * unit test.
     */
    public static class GetRequestParametersTestAdvice extends Aspect
    {
        /**
         * @Execution * *..SampleServlet.do*(..)
         */
        Pointcut interceptServlet;
        
        /**
         * @Around interceptServlet
         */
        public Object catchGetRequestParameters(JoinPoint joinPoint) 
            throws Throwable
        {
            MethodJoinPoint jp = (MethodJoinPoint) joinPoint;
            SampleServlet servlet = (SampleServlet) jp.getTargetInstance();
            Hashtable params = servlet.getRequestParameters(
                (HttpServletRequest) jp.getParameters()[0]);
            assertNotNull(params.get("param1"));
            assertNotNull(params.get("param2"));
            assertEquals("value1", params.get("param1"));
            assertEquals("value2", params.get("param2"));
            return null;
        }
    }

    /**
     * Test {@link SampleServlet#getRequestParameters} by calling the server 
     * side using HttpUnit. On the server side, our aspect will kick in and
     * the {@link GetRequestParametersTestAdvice#testGetRequestParameters(JoinPoint)} test method will
     * be called to unit test our method.    
     */
    public void testGetRequestParameters() throws Exception
    {
        WebConversation conversation = new WebConversation();
        WebRequest request = new GetMethodWebRequest(
            "http://localhost:8080/test/SampleServlet?param1=value1&param2=value2");
        WebResponse response = conversation.getResponse(request);
    }    
}
