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
package org.apache.cactus.integration.ant.deployment.webapp;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.deployment.JarArchive;
import org.xml.sax.SAXException;

/**
 * Encapsulates access to a WAR.
 * 
 * @since Cactus 1.5
 * @version $Id$
 */
public interface WarArchive extends JarArchive
{
    /**
     * Returns the deployment descriptor of the web application.
     * 
     * @return The parsed deployment descriptor
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    WebXml getWebXml()
        throws IOException, SAXException, ParserConfigurationException;
}