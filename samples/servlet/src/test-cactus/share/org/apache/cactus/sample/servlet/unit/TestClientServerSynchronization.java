/*
 * ====================================================================
 *
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
package org.apache.cactus.sample.servlet.unit;

import javax.servlet.ServletOutputStream;

import org.apache.cactus.ServletTestCase;

/**
 * Verify that the Cactus client side only reads the test result *after* the
 * test is finished (ie after the test result has been saved in the application
 * scope). This JUnit test need to be the first one to be run. Otherwise, the
 * test result might be that of the previous test and not the current test one,
 * thus proving nothing !!
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestClientServerSynchronization extends ServletTestCase
{
    /**
     * Verify that the test result can be returned correctly even when the
     * logic in the method to test takes a long time and thus it verifies that
     * the test result is only returned after it has been written in the
     * application scope on the server side.
     * 
     * @exception Exception on test failure
     */
    public void testLongProcess() throws Exception
    {
        ServletOutputStream os = response.getOutputStream();

        os.print("<html><head><Long Process></head><body>");
        os.flush();

        // do some processing that takes a while ...
        Thread.sleep(3000);
        os.println("Some data</body></html>");
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that when big amount of data is returned by the servlet output
     * stream, it does not io-block.
     * 
     * @exception Exception on test failure
     */
    public void testLotsOfData() throws Exception
    {
        ServletOutputStream os = response.getOutputStream();

        os.println("<html><head>Lots of Data</head><body>");
        os.flush();

        for (int i = 0; i < 5000; i++)
        {
            os.println("<p>Lots and lots of data here");
        }

        os.println("</body></html>");
    }
}