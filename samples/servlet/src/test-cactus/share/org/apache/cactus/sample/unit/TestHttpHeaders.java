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
import org.apache.cactus.WebResponse;

/**
 * Tests manipulating HTTP headers.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class TestHttpHeaders extends ServletTestCase
{
    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestHttpHeaders(String theName)
    {
        super(theName);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate several HTTP header values with the same
     * header name.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSendMultivaluedHeader(WebRequest theRequest)
    {
        theRequest.addHeader("testheader", "value1");
        theRequest.addHeader("testheader", "value2");
    }

    /**
     * Verify that we can simulate several HTTP header values with the same
     * header name.
     */
    public void testSendMultivaluedHeader()
    {
        // Note: I am not sure how to retrieve multi valued headers. The
        // problem is that I use
        // URLConnection.setRequestProperty("testheader", "value1,value2") in
        // JdkConnectionHelper to send the headers but request.getHeaders() does
        // not seem to separate the different header values.
        // The RFC 2616 says :
        // message-header = field-name ":" [ field-value ]
        // field-name     = token
        // field-value    = *( field-content | LWS )
        // field-content  = <the OCTETs making up the field-value
        //                  and consisting of either *TEXT or combinations
        //                  of token, separators, and quoted-string>
        // [...]
        // Multiple message-header fields with the same field-name MAY be
        // present in a message if and only if the entire field-value for that
        // header field is defined as a comma-separated list [i.e., #(values)].
        // It MUST be possible to combine the multiple header fields into one
        // "field-name: field-value" pair, without changing the semantics of
        // the message, by appending each subsequent field-value to the first,
        // each separated by a comma. The order in which header fields with the
        // same field-name are received is therefore significant to the
        // interpretation of the combined field value, and thus a proxy MUST
        // NOT change the order of these field values when a message is
        // forwarded.
        // ... so it should be ok ...
        assertEquals("value1,value2", request.getHeader("testheader"));

        // Here is commented out what I would have thought I should have
        // written to verify this test but it does not seem to work this way ...

        /*
        Enumeration values = request.getHeaders("testheader");
        int count = 0;
        while (values.hasMoreElements()) {
            String value = (String)values.nextElement();
            if (!(value.equals("value1") || value.equals("value2"))) {
                fail("unknown value [" + value + "] for header [testheader]");
            }
            count++;
        }
        assertEquals("Should have received 2 values for header [testheader]",
            2, count);
        */
    }

    //-------------------------------------------------------------------------

    /**
     * Verify we can set the content type by setting an HTTP header.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSetContentTypeHeader(WebRequest theRequest)
    {
        theRequest.addHeader("Content-type", "text/xml");
    }

    /**
     * Verify we can set the content type by setting an HTTP header.
     */
    public void testSetContentTypeHeader()
    {
        assertEquals("text/xml", request.getContentType());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can set several headers in the response and
     * assert them in endXXX().
     */
    public void testResponseAddHeaders()
    {
        response.addHeader("X-Test-Header1", "value1");
        response.addHeader("X-Test-Header2", "value2");
    }

    /**
     * Verify that we can set several headers in the response and
     * assert them in endXXX().
     *
     * @param theResponse the response from the server side.
     */
    public void endResponseAddHeaders(WebResponse theResponse) 
    {
        String value1 = 
            theResponse.getConnection().getHeaderField("X-Test-Header1");
        String value2 = 
            theResponse.getConnection().getHeaderField("X-Test-Header2");

        assertEquals("value1", value1);
        assertEquals("value2", value2);
    }

}