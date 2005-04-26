/* 
 * ========================================================================
 * 
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant;

import java.io.File;
import java.util.Iterator;

import org.codehaus.cargo.module.application.ApplicationXml;
import org.codehaus.cargo.module.application.DefaultEarArchive;
import org.codehaus.cargo.module.application.EarArchive;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlTag;
import org.codehaus.cargo.module.webapp.weblogic.WeblogicXml;
import org.codehaus.cargo.module.webapp.weblogic.WeblogicXmlTag;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test class for the CactifyEar task.
 *
 * @version $Id$
 */
public class TestCactifyEarTask extends AntTestCase
{
    /**
     * @see AntTestCase#AntTestCase
     */
    public TestCactifyEarTask()
    {
        super("org/apache/cactus/integration/ant/test-cactifyear.xml");
    }
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        getProject().addTaskDefinition("cactifyear", CactifyEarTask.class);
    }

    /**
     * Tests that the basic function of the task works, 
     * that is to add a cactified war to the ear.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testCanAddCactifiedWar() throws Exception
    {   
        executeTestTarget();
        
        File destFile = getProject().resolveFile("work/cactified.ear");
        EarArchive destEar = new DefaultEarArchive(destFile);
        ApplicationXml appXml = destEar.getApplicationXml();
        assertEquals("/cactus", appXml.getWebModuleContextRoot("cactus.war"));
        WarArchive cactusWar = destEar.getWebModule("cactus.war");
        WebXml webXml = cactusWar.getWebXml();
        assertNotNull(webXml.getServlet("ServletRedirector"));
    }

    /**
     * Tests that the context of the cactus.war can be specified.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testCustomCactusWarContext() throws Exception
    {
        executeTestTarget();

        File destFile = getProject().resolveFile("work/cactified.ear");
        EarArchive destEar = new DefaultEarArchive(destFile);
        ApplicationXml appXml = destEar.getApplicationXml();
        assertEquals("/myTestFramework", 
            appXml.getWebModuleContextRoot("cactus.war"));
    }
    
    /**
     * @throws Exception If an unexpected error occurs
     */
    public void testAddEjbReferences() throws Exception
    {
        executeTestTarget();
        
        File destFile = getProject().resolveFile("work/cactified.ear");
        EarArchive destEar = new DefaultEarArchive(destFile);
        WarArchive cactusWar = destEar.getWebModule("cactus.war");
        
        // test web.xml
        WebXml webXml = cactusWar.getWebXml();
        Iterator i = webXml.getElements(WebXmlTag.EJB_LOCAL_REF);
        assertEjbRef((Element) i.next(), "ejb/Session2", "Session", 
                     "com.wombat.Session2", "com.wombat.Session2Home");
        assertEjbRef((Element) i.next(), "ejb/Entity1", "Entity", 
                     "com.wombat.Entity1", "com.wombat.Entity1Home");
        assertFalse(i.hasNext());
        
        // test weblogic.xml
        WeblogicXml weblogicXml = (WeblogicXml) webXml.getVendorDescriptor();
        i = weblogicXml.getElements(WeblogicXmlTag.EJB_REFERENCE_DESCRIPTION);
        assertWeblogicEjbRef((Element) i.next(), 
                             "ejb/Session2", "/wombat/Session2");
        assertWeblogicEjbRef((Element) i.next(), 
                             "ejb/Entity1", "/wombat/Entity1");
        assertFalse(i.hasNext());
    }
    
    /**
     * Help method to check that a given element is a correct ejb-ref
     * 
     * @param theElement the Element to check
     * @param theName correct name of the ejb-ref
     * @param theType correct ejb-ref type
     * @param theLocal correct local interface of the ejb-ref
     * @param theLocalHome correct local home interface of the ejb-ref
     */
    private void assertEjbRef(Element theElement, String theName, 
                              String theType, String theLocal, 
                              String theLocalHome)
    {
        NodeList nl = theElement.getElementsByTagName("ejb-ref-name");
        Element f = (Element) nl.item(0);
        assertEquals(theName, f.getFirstChild().getNodeValue());
        nl = theElement.getElementsByTagName("ejb-ref-type");
        f = (Element) nl.item(0);
        assertEquals(theType, f.getFirstChild().getNodeValue());
        nl = theElement.getElementsByTagName("local-home");
        f = (Element) nl.item(0);
        assertEquals(theLocalHome, f.getFirstChild().getNodeValue());
        nl = theElement.getElementsByTagName("local");
        f = (Element) nl.item(0);
        assertEquals(theLocal, f.getFirstChild().getNodeValue());
    }
    
    /**
     * Help method to check that a given element is a correct weblogic ejb-ref
     *  
     * @param theElement the Element to check
     * @param theName correct name of the ejb-ref
     * @param theJndiName correct jndi name of the ejb-ref
     */
    private void assertWeblogicEjbRef(Element theElement, String theName, 
                                      String theJndiName)
    {
        NodeList nl = theElement.getElementsByTagName("ejb-ref-name");
        Element f = (Element) nl.item(0);
        assertEquals(theName, f.getFirstChild().getNodeValue());
        nl = theElement.getElementsByTagName("jndi-name");
        f = (Element) nl.item(0);
        assertEquals(theJndiName, f.getFirstChild().getNodeValue());
    }
}
