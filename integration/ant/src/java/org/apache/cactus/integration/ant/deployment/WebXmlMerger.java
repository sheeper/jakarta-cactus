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

import java.util.Iterator;

import org.apache.cactus.integration.ant.util.AntLog;
import org.apache.commons.logging.Log;
import org.w3c.dom.Element;

/**
 * Helper class that can merge two web deployment descriptors.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @since Cactus 1.5
 * @version $Id$
 */
public class WebXmlMerger
{

    // Instance Variables ------------------------------------------------------

    /**
     * The original, authorative descriptor onto which the merges are performed.
     */
    private WebXml webXml;
    
    /**
     * The log to use.
     */
    private Log log = AntLog.NULL;

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param theWebXml The original descriptor
     */
    public WebXmlMerger(WebXml theWebXml)
    {
        this.webXml = theWebXml;
    }

    // Public Methods ----------------------------------------------------------

    /**
     * Merges the merge descriptor with the original descriptor. 
     * 
     * @param theMergeWebXml The descriptor to merge in
     */
    public final void merge(WebXml theMergeWebXml)
    {
        checkServletVersions(theMergeWebXml);
        if (WebXmlVersion.V2_3.compareTo(this.webXml.getVersion()) <= 0)
        {
            mergeFilters(theMergeWebXml);
        }
        mergeServlets(theMergeWebXml);
        if (WebXmlVersion.V2_3.compareTo(this.webXml.getVersion()) <= 0)
        {
            mergeResourceEnvironmentReferences(theMergeWebXml);
        }
        mergeResourceReferences(theMergeWebXml);
        mergeSecurityConstraints(theMergeWebXml);
        mergeLoginConfig(theMergeWebXml);
        mergeSecurityRoles(theMergeWebXml);
        mergeEnvironmentEntries(theMergeWebXml);
        mergeEjbRefs(theMergeWebXml);
        if (WebXmlVersion.V2_3.compareTo(this.webXml.getVersion()) <= 0)
        {
            mergeEjbLocalRefs(theMergeWebXml);
        }
    }

    /**
     * Sets the log to which events should be written. This method must be 
     * called before any of the other methods, because the class will rely on 
     * being able to log.
     * 
     * @param theLog The log to use
     */
    public final void setLog(Log theLog)
    {
        this.log = theLog;
    }

    // Protected Methods -------------------------------------------------------

    /**
     * Checks the versions of the servlet API in each descriptor, and logs
     * a warning if a mismatch might result in the loss of definitions.
     * 
     * @param theWebXml The descriptor that will be merged with the original
     */
    protected final void checkServletVersions(WebXml theWebXml)
    {
        if ((this.webXml.getVersion() != null)
         && (this.webXml.getVersion().compareTo(theWebXml.getVersion()) < 0))
        {
            this.log.warn(
                "Merging elements from a version " + theWebXml.getVersion()
                + " descriptor into a version " + this.webXml.getVersion() 
                + ", some elements may be skipped");
        }
    }

    /**
     * Merges the servlet definitions from the specified descriptor into the 
     * original descriptor.
     * 
     * @param theWebXml The descriptor that contains the filter definitions
     *        that are to be merged into the original descriptor
     */
    protected final void mergeFilters(WebXml theWebXml)
    {
        Iterator filterNames = theWebXml.getFilterNames();
        int count = 0;
        while (filterNames.hasNext())
        {
            String filterName = (String) filterNames.next();
            if (!webXml.hasFilter(filterName))
            {
                webXml.addFilter(theWebXml.getFilter(filterName));
            }
            else
            {
                // merge the parameters
                Iterator filterInitParamNames =
                    theWebXml.getFilterInitParamNames(filterName);
                while (filterInitParamNames.hasNext())
                {
                    String paramName = (String) filterInitParamNames.next();
                    String paramValue =
                        theWebXml.getFilterInitParam(filterName, paramName);
                    webXml.addFilterInitParam(
                        filterName, paramName, paramValue);
                }
            }
            // merge the mappings
            Iterator filterMappings = theWebXml.getFilterMappings(filterName);
            while (filterMappings.hasNext())
            {
                String urlPattern = (String) filterMappings.next();
                webXml.addFilterMapping(filterName, urlPattern);
            }
            count++;
        }
        this.log.trace("Merged " + count + " filter definition"
            + (count != 1 ? "s " : " ") + "into the descriptor");
    }

    /**
     * Merges the servlet definitions from the specified descriptor into the 
     * original descriptor.
     * 
     * @param theWebXml The descriptor that contains the servlet definitions
     *        that are to be merged into the original descriptor
     */
    protected final void mergeServlets(WebXml theWebXml)
    {
        Iterator servletNames = theWebXml.getServletNames();
        int count = 0;
        while (servletNames.hasNext())
        {
            String servletName = (String) servletNames.next();
            if (!webXml.hasServlet(servletName))
            {
                webXml.addServlet(theWebXml.getServlet(servletName));
            }
            else
            {
                // merge the parameters
                Iterator servletInitParamNames =
                    theWebXml.getServletInitParamNames(servletName);
                while (servletInitParamNames.hasNext())
                {
                    String paramName = (String) servletInitParamNames.next();
                    String paramValue =
                        theWebXml.getServletInitParam(servletName, paramName);
                    webXml.addServletInitParam(
                        servletName, paramName, paramValue);
                }
            }
            // merge the mappings
            Iterator servletMappings =
                theWebXml.getServletMappings(servletName);
            while (servletMappings.hasNext())
            {
                String urlPattern = (String) servletMappings.next();
                webXml.addServletMapping(servletName, urlPattern);
            }
            count++;
        }
        this.log.trace("Merged " + count + " servlet definition"
            + (count != 1 ? "s " : " ") + "into the descriptor");
    }

    /**
     * Merges the resource environment references from the provided descriptor
     * into the original descriptor.
     * 
     * @param theWebXml The descriptor that contains the references that are to
     *        be merged into the original descriptor
     */
    protected final void mergeResourceEnvironmentReferences(WebXml theWebXml)
    {
        int count = insertElements(theWebXml, WebXmlTag.RESOURCE_ENV_REF);
        if (count > 0)
        {
            this.log.trace("Merged " + count + " resource environment "
                + "reference" + (count != 1 ? "s " : " ") + "into the "
                + "descriptor");
        }
    }

    /**
     * Merges the resource references from the provided descriptor into the
     * original descriptor.
     * 
     * @param theWebXml The descriptor that contains the resource refs that
     *        are to be merged into the original descriptor
     */
    protected final void mergeResourceReferences(WebXml theWebXml)
    {
        int count = insertElements(theWebXml, WebXmlTag.RESOURCE_REF);
        if (count > 0)
        {
            this.log.trace("Merged " + count + " resource reference"
                + (count != 1 ? "s " : " ") + "into the descriptor");
        }
    }

    /**
     * Merges the 
     * 
     * @param theWebXml The descriptor that contains the security constraints
     *        that are to be merged into the original descriptor
     */
    protected final void mergeSecurityConstraints(WebXml theWebXml)
    {
        int count = insertElements(theWebXml, WebXmlTag.SECURITY_CONSTRAINT);
        if (count > 0)
        {
            this.log.trace("Merged " + count + " security constraint"
                + (count != 1 ? "s " : " ") + "into the descriptor");
        }
    }

    /**
     * Merges the login configuration from the provided descriptor into the
     * original descriptor, thereby eventually replacing the existing login 
     * config.
     * 
     * @param theWebXml The descriptor that contains the login config that
     *        is to be merged into the original descriptor
     */
    protected final void mergeLoginConfig(WebXml theWebXml)
    {
        boolean replaced = replaceElement(theWebXml, WebXmlTag.LOGIN_CONFIG);
        if (replaced)
        {
            this.log.trace(
                "Merged the login configuration into the descriptor");
        }
    }

    /**
     * Merges the security roles from the provided descriptor into the original
     * descriptor.
     * 
     * @param theWebXml The descriptor that contains the security roles that
     *        are to be merged into the original descriptor
     */
    protected final void mergeSecurityRoles(WebXml theWebXml)
    {
        Iterator securityRoleNames = theWebXml.getSecurityRoleNames();
        int count = 0;
        while (securityRoleNames.hasNext())
        {
            String securityRoleName = (String) securityRoleNames.next();
            if (!this.webXml.hasSecurityRole(securityRoleName))
            {
                this.webXml.addSecurityRole(securityRoleName);
            }
        }
        if (count > 0)
        {
            this.log.trace("Merged " + count + " security role"
                + (count != 1 ? "s " : " ") + "into the descriptor");
        }
    }

    /**
     * Merges the environment entries from the provided descriptor into the
     * original descriptor.
     * 
     * @param theWebXml The descriptor that contains the environment entries
     *        that are to be merged into the original descriptor
     */
    protected final void mergeEnvironmentEntries(WebXml theWebXml)
    {
        int count = insertElements(theWebXml, WebXmlTag.ENV_ENTRY);
        if (count > 0)
        {
            this.log.trace("Merged " + count + " environment entr"
                + (count != 1 ? "ies " : "y ") + "into the descriptor");
        }
    }

    /**
     * Merges the EJB references from the provided descriptor into the original
     * descriptor.
     * 
     * @param theWebXml The descriptor that contains the EJB refs that are to be
     *        merged into the original descriptor
     */
    protected final void mergeEjbRefs(WebXml theWebXml)
    {
        int count = insertElements(theWebXml, WebXmlTag.EJB_REF);
        if (count > 0)
        {
            this.log.trace("Merged " + count + " EJB reference"
                + (count != 1 ? "s " : "y ") + "into the descriptor");
        }
    }

    /**
     * Merges the EJB local references from the provided descriptor into the
     * original descriptor.
     * 
     * @param theWebXml The descriptor that contains the EJB local refs that are
     *        to be merged into the original descriptor
     */
    protected final void mergeEjbLocalRefs(WebXml theWebXml)
    {
        int count = insertElements(theWebXml, WebXmlTag.EJB_LOCAL_REF);
        if (count > 0)
        {
            this.log.trace("Merged " + count + " EJB local reference"
                + (count != 1 ? "s " : "y ") + "into the descriptor");
        }
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Insert all elements of the specified tag from the given descriptor into
     * the original descriptor, and returns the number of elements that were
     * added.
     * 
     * @param theWebXml The descriptor that contains the elements that are to be
     *        merged into the original descriptor
     * @param theTag Defines which elements will get merged
     * @return The number of elements inserted into the original descriptor
     */
    private int insertElements(WebXml theWebXml, WebXmlTag theTag)
    {
        Iterator elements = theWebXml.getElements(theTag);
        int count = 0;
        while (elements.hasNext())
        {
            Element element = (Element) elements.next();
            webXml.addElement(theTag, element);
            count++;
        }
        return count;
    }

    /**
     * Replaces the element of the specified tag in the original descriptor with
     * the equivalent element in the specified descriptor.
     * 
     * @param theWebXml The descriptor that contains the element that is to be
     *        added to the original descriptor, replacing the original
     *        definition
     * @param theTag Defines which element will get replaced
     * @return Whether the element was replaced
     */
    private boolean replaceElement(WebXml theWebXml, WebXmlTag theTag)
    {
        Iterator elements = theWebXml.getElements(theTag);
        if (elements.hasNext())
        {
            webXml.replaceElement(theTag, (Element) elements.next());
            return true;
        }
        return false;
    }

}
