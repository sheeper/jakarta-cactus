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
package org.apache.cactus.integration.ant.deployment;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.codehaus.cargo.module.application.ApplicationXml;
import org.codehaus.cargo.module.application.EarArchive;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.mockobjects.dynamic.Mock;

/**
 * Unit tests for {@link EarParser}.
 *
 * @version $Id: TestEarParser.java 239003 2004-05-31 20:05:27Z vmassol $
 */
public final class TestEarParser extends TestCase
{   
    /**
     * This is the actual content of the application.xml
     */
    private String webXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    							+ "<!DOCTYPE application PUBLIC \"-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN\" "
    							+ "\"http://java.sun.com/j2ee/dtds/application_1_2.dtd\">"
    							+ "<application>"
    							+ "  <display-name>EJB ear</display-name>"
    							+ "  <module>"
    							+ "    <web>"
    							+ "      <web-uri>test.war</web-uri>"
    							+ "      <context-root>/testcontext</context-root>"
    							+ "    </web>"
    							+ "  </module>"
    							+ "</application>";
    /**
     * This is the document we use to store the application.xml
     */
    private Document document = null;
    
    /**
     * The document builder factory.
     */
    private DocumentBuilderFactory factory;
    
    /**
     * The JAXP document builder.
     */
    private DocumentBuilder builder; 

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
        factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
    	try {
    		builder = factory.newDocumentBuilder();
    		document = builder.parse(new ByteArrayInputStream(webXml.getBytes()));
    	} catch (SAXException e) {
			// This should never happen;
		} catch (IOException e) {
			// This should never happen;
		} catch (ParserConfigurationException e) {
			//This shouldn't happen;
			e.printStackTrace();
		}
    	applicationXml = new ApplicationXml(document);

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
        webXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!DOCTYPE application PUBLIC \"-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN\" "
			+ "\"http://java.sun.com/j2ee/dtds/application_1_2.dtd\">"
			+ "<application>"
			+ "  <display-name>EJB ear</display-name>"
			+ "  <module>"
			+ "    <web>"
			+ "      <web-uri>test.war</web-uri>"
			+ "      <context-root></context-root>"
			+ "    </web>"
			+ "  </module>"
			+ "</application>";
        
        setUp();
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
