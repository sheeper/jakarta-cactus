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
package org.apache.cactus.integration.ant.container;

import org.apache.cactus.integration.ant.deployment.ApplicationXml;
import org.apache.cactus.integration.ant.deployment.EarArchive;
import org.apache.tools.ant.BuildException;

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
     * <code>context-root</code> element, an exception is raised.
     * 
     * @exception Exception on error
     */
    public void testParseTestContextWhenNoWebUriInApplicationXml()
        throws Exception
    {
        mockApplicationXml.expectAndReturn("getWebModuleContextRoot", 
            "test.war", null);

        try
        {
            EarParser.parseTestContext(archive, "test.war");
        }
        catch (BuildException expected)
        {
            assertTrue(true);
        }
    }
}
