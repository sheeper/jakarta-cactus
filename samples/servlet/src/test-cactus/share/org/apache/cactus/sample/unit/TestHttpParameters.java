/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */
package org.apache.cactus.sample.unit;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;

/**
 * Test passing HTTP parameters to the server side.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestHttpParameters extends ServletTestCase
{
    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestHttpParameters(String theName)
    {
        super(theName);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that multi value parameters can be sent in the
     * <code>beingXXX()</code> method to the server redirector.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginMultiValueParameters(WebRequest theRequest)
    {
        theRequest.addParameter("multivalue", "value 1");
        theRequest.addParameter("multivalue", "value 2");
    }

    /**
     * Verify that multi value parameters can be sent in the
     * <code>beingXXX()</code> method to the server redirector.
     */
    public void testMultiValueParameters()
    {
        String[] values = request.getParameterValues("multivalue");

        if (values[0].equals("value 1"))
        {
            assertEquals("value 2", values[1]);
        }
        else if (values[0].equals("value 2"))
        {
            assertEquals("value 1", values[1]);
        }
        else
        {
            fail("Shoud have returned a vector with the "
                + "values \"value 1\" and \"value 2\"");
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Verify we can set and retrieve several parameters.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSeveralParameters(WebRequest theRequest)
    {
        theRequest.addParameter("PostParameter1", "EMPLOYEE0145", 
            WebRequest.POST_METHOD);
        theRequest.addParameter("PostParameter2", "W", WebRequest.GET_METHOD);
        theRequest.addParameter("PostParameter3", "07/08/2002", 
            WebRequest.POST_METHOD);
        theRequest.addParameter("PostParameter4", "/tas/ViewSchedule.esp", 
            WebRequest.GET_METHOD);
    }

    /**
     * Verify we can set and retrieve several parameters.
     */
    public void testSeveralParameters()
    {
        assertEquals("parameter4", "/tas/ViewSchedule.esp", 
            request.getParameter("PostParameter4"));
        assertEquals("parameter1", "EMPLOYEE0145", 
            request.getParameter("PostParameter1"));
        assertEquals("parameter2", "W", request.getParameter("PostParameter2"));
        assertEquals("parameter3", "07/08/2002", 
            request.getParameter("PostParameter3"));
    }

}