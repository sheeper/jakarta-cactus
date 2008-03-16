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
package org.apache.cactus.extension.jetty;

import java.net.URL;

import org.apache.cactus.internal.configuration.Configuration;
import org.apache.cactus.internal.configuration.FilterConfiguration;
import org.apache.cactus.internal.configuration.ServletConfiguration;

import com.mockobjects.dynamic.Mock;

import junit.framework.TestCase;

/**
 * Unit tests of the {@link JettyTestSetup} class.
 * 
 * Note: For this test to work, it must be passed the <code>cactus.port</code>
 * system property. If not it will default to 8080.
 *
 * @version $Id: TestJettyTestSetup.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class TestJettyTestSetup extends TestCase
{
    /**
     * Control mock for {@link Configuration}.
     */
    private Mock mockConfiguration;

    /**
     * Mock for {@link Configuration}.
     */
    private Configuration configuration;

    /**
     * Control mock for {@link ServletConfiguration}.
     */
    private Mock mockServletConfiguration;

    /**
     * Mock for {@link ServletConfiguration}.
     */
    private ServletConfiguration servletConfiguration;

    /**
     * Control mock for {@link FilterConfiguration}.
     */
    private Mock mockFilterConfiguration;

    /**
     * Mock for {@link FilterConfiguration}.
     */
    private FilterConfiguration filterConfiguration;
    
    /**
     * URL pointing to the test context. Note that if the port is not
     * passed as a system property it defaults to 8080.
     */
    private static final String CONTEXT_URL = 
        "http://localhost:" + System.getProperty("cactus.port", "8080");

    /**
     * Object to unit test.
     */
    private Jetty5xTestSetup jettyTestSetup; 

    /**
     * Fake test case object used only to construct an instance of 
     * {@link JettyTestSetup}.
     */
    public class SampleTestCase extends TestCase
    {
    }

    /**
     * {@inheritDoc}
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        mockConfiguration = new Mock(Configuration.class);
        configuration = (Configuration) mockConfiguration.proxy();

        mockServletConfiguration = new Mock(ServletConfiguration.class);
        servletConfiguration = 
            (ServletConfiguration) mockServletConfiguration.proxy();
        mockFilterConfiguration = new Mock(FilterConfiguration.class);
        filterConfiguration = 
            (FilterConfiguration) mockFilterConfiguration.proxy();

        mockConfiguration.matchAndReturn("getContextURL", CONTEXT_URL); 
        mockServletConfiguration.matchAndReturn("getDefaultRedirectorName",
            "ServletRedirector");

        URL testURL = new URL(CONTEXT_URL + "/" 
            + servletConfiguration.getDefaultRedirectorName());
            
        mockServletConfiguration.matchAndReturn("getDefaultRedirectorURL",
            testURL.getPath());
        
        jettyTestSetup = new Jetty5xTestSetup(new SampleTestCase(),
            configuration, servletConfiguration, filterConfiguration); 
        
        // Ensure that the Jetty server is not already started.
        if (jettyTestSetup.isAvailable(
            jettyTestSetup.testConnectivity(testURL)))
        {
            fail("No server serving the [" + testURL.getPath()
                + "] URL should be started.");
        }
    }

    /**
     * {@inheritDoc}
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        // Ensure that the server is stopped after each test
        jettyTestSetup.tearDown();
        
        mockConfiguration.verify();
        mockServletConfiguration.verify();
        mockFilterConfiguration.verify();
    }

    /**
     * Verify that calling the {@link JettyTestSetup#setUp()} method
     * works when Jetty is not already started.
     * 
     * @throws Exception in case of error
     */
    public void testSetUpWhenServerNotAlreadyStarted() throws Exception
    {
        jettyTestSetup.setUp();
        assertTrue(jettyTestSetup.isRunning());
    }

    /**
     * Verify that calling the {@link JettyTestSetup#setUp()} method
     * does not start Jetty a second time if Jetty is already started.
     * 
     * @throws Exception in case of error
     */
    public void testSetUpWhenServerIsAlreadyStarted() throws Exception
    {
        jettyTestSetup.setUp();

        // If we succeed calling a second time setUp(), it means that it's
        // not starting the container again as otherwise it would fail to
        // bind to an already bound port.
        jettyTestSetup.setUp();

        // Ensure that tearDown will shutdown the container
        jettyTestSetup.setForceShutdown(true);
    }    
}
