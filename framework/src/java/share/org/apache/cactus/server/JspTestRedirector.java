/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.cactus.server;

import javax.servlet.ServletException;

import org.apache.cactus.internal.configuration.ConfigurationInitializer;
import org.apache.cactus.internal.server.JspImplicitObjects;
import org.apache.cactus.internal.server.JspTestController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extension of the <code>jspRedirector.jsp</code> JSP Redirector in the java
 * realm in order to provide a symmetry with the <code>ServletRedirector</code>
 * and minimize the amount of java code in <code>jspRedirector.jsp</code>.
 *
 * @version $Id$
 */
public class JspTestRedirector
{
    /**
     * As this class is the first one loaded on the server side, we ensure
     * that the Cactus configuration has been initialized. A better 
     * implementation might be to perform this initialization in the 
     * init() method. However, that requires removing the static LOGGER
     * object.
     */
    static
    {
        ConfigurationInitializer.initialize();
    }

    /**
     * The logger
     */
    private static final Log LOGGER = 
        LogFactory.getLog(JspTestRedirector.class);

    /**
     * Handles requests from the <code>jspRedirector.jsp</code> JSP Redirector.
     * @param theObjects the implicit objects that will be passed to the test
     *        case
     * @exception ServletException if an error occurs servicing the request
     */
    public void doGet(JspImplicitObjects theObjects) throws ServletException
    {
        // Mark beginning of test on server side
        LOGGER.debug("------------- Start JSP service");

        JspTestController controller = new JspTestController();

        controller.handleRequest(theObjects);
    }
}
