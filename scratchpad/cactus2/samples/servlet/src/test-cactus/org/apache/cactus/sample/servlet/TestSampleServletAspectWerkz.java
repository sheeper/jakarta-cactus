/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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
