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
package org.apache.cactus.integration.ant.deployment.application;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.cactus.integration.ant.deployment.JarArchive;
import org.apache.cactus.integration.ant.deployment.webapp.WarArchive;
import org.xml.sax.SAXException;

/**
 * Class that encapsulates access to an EAR.
 * 
 * @since Cactus 1.5
 * @version $Id$
 */
public interface EarArchive extends JarArchive
{
    /**
     * Returns the deployment descriptor of the web application.
     * 
     * @return The parsed deployment descriptor
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the EAR
     * @throws SAXException If the deployment descriptor of the EAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    ApplicationXml getApplicationXml()
        throws IOException, SAXException, ParserConfigurationException;

    /**
     * Returns the web-app archive stored in the EAR with the specified URI.
     * 
     * @param theUri The URI of the web module
     * @return The web-app archive, or <code>null</code> if no WAR was found at
     *         the specified URI
     * @throws IOException If there was an errors reading from the EAR or WAR
     */
    WarArchive getWebModule(String theUri) throws IOException;
}
