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
package org.apache.cactus.integration.ant.container;

import org.apache.cactus.integration.ant.deployment.ApplicationXml;
import org.apache.cactus.integration.ant.deployment.EarArchive;

import com.mockobjects.dynamic.Mock;

import junit.framework.TestCase;

/**
 * Unit tests for {@link EarParser}.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public final class TestEarParser extends TestCase
{   
    /**
     * Control mock for {@link ApplicationXml}.
     */
    private Mock mockApplicationXml;

    /**
     * Mock for {@link ApplicationXml}.
     */
    private ApplicationXml applicationXml;

    /**
     * Control mock for {@link EarArchive}.
     */
    private Mock mockArchive;

    /**
     * Mock for {@link EarArchive}.
     */
    private EarArchive archive;
    
    /**
     * @see TestCase#setUp()
     */
    protected void setUp()
    {
        mockApplicationXml = new Mock(ApplicationXml.class);
        applicationXml = (ApplicationXml) mockApplicationXml.proxy();

        mockArchive = new Mock(EarArchive.class);
        archive = (EarArchive) mockArchive.proxy();
        mockArchive.expectAndReturn("getApplicationXml", applicationXml); 
    }

    /**
     * Verify that if the <code>application.xml</code> defines a
     * <code>context-root</code> element, then Cactus will use it
     * as the test context to use when polling the container to see
     * if it is started.
     * 
     * @exception Exception on error
     */
    public void testParseTestContextWhenWebUriDefined() throws Exception
    {
        mockApplicationXml.expectAndReturn("getWebModuleContextRoot", 
            "test.war", "/testcontext");

        String context = EarParser.parseTestContext(archive, "test.war");
        assertEquals("testcontext", context);
    }

    /**
     * Verify that if the <code>application.xml</code> does not define a
     * <code>context-root</code> element, then Cactus will use the web URI
     * as the test context (minus the ".war" extension). This test context
     * is used when polling the container to see if it is started.
     * 
     * @exception Exception on error
     */
    public void testParseTestContextWhenNoWebUriInApplicationXml() 
        throws Exception
    {
        mockApplicationXml.expectAndReturn("getWebModuleContextRoot", 
            "test.war", null);
        
        String context = EarParser.parseTestContext(archive, "test.war");
        assertEquals("test", context);
    }
}