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
package org.apache.cactus.server.runner;

import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 * Unit tests for {@link XMLFormatter}.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public final class TestXMLFormatter extends TestCase
{   
    /**
     * Instance to unit test 
     */
    private XMLFormatter formatter;
   
    /**
     * TestResult object used for testing
     */
    private TestResult testResult;
    
    /**
     * Set up common mock behaviors.
     */
    public void setUp()
    {
        formatter = new XMLFormatter();
        testResult = new TestResult();
    }

    /**
     * Verify that calling {@link XMLFormatter#toXML(TestResult)} works
     * when using the default encoding. 
     */
    public void testToXmlEmptyWithDefaultEncoding()
    {
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<testsuites><testsuite name=\"null\" tests=\"0\" failures=\"0\""
            + " errors=\"0\" time=\"0\"></testsuite></testsuites>";
        
        String result = formatter.toXML(testResult);
        assertEquals(expected, result);
    }

    /**
     * Verify that calling {@link XMLFormatter#toXML(TestResult)} works
     * when using a custom encoding of ISO-8859-1. 
     */
    public void testToXmlEmptyWithCustomEncoding()
    {
        String expected = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
        + "<testsuites><testsuite name=\"null\" tests=\"0\" failures=\"0\""
        + " errors=\"0\" time=\"0\"></testsuite></testsuites>";
        
        formatter.setEncoding("ISO-8859-1");
        String result = formatter.toXML(testResult);
        assertEquals(expected, result);
    }
}
