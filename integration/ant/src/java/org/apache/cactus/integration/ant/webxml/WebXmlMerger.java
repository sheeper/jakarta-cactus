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
package org.apache.cactus.integration.ant.webxml;

import java.util.Iterator;

import org.w3c.dom.Element;

/**
 * Helper class that can merge two web deployment descriptors.
 *
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 *
 * @version $Id$
 */
public class WebXmlMerger
{
    
    /**
     * The original, authorative descriptor onto which the merges are performed.
     */
    private WebXml webXml;
    
    /**
     * Constructor.
     * 
     * @param theWebXml The original descriptor
     */
    public WebXmlMerger(WebXml theWebXml)
    {
        this.webXml = theWebXml;
    }
    
    /**
     * Merges the servlet definitions from the specified descriptor into the 
     * original descriptor.
     * 
     * @param theWebXml The descriptor that contains the filter definitions
     *         that are to be merged into the original descriptor
     * @return The number of filters merged into the original descriptor
     */
    public int mergeFilters(WebXml theWebXml)
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
        return count;
    }

    /**
     * Merges the servlet definitions from the specified descriptor into the 
     * original descriptor.
     * 
     * @param theWebXml The descriptor that contains the servlet definitions
     *         that are to be merged into the original descriptor
     * @return The number of servlets merged into the original descriptor
     */
    public int mergeServlets(WebXml theWebXml)
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
        return count;
    }

    /**
     * 
     * 
     * @param theWebXml The descriptor that contains the security constraints
     *         that are to be merged into the original descriptor
     * @return The number of security constraints merged into the original
     *          descriptor
     */
    public int mergeSecurityConstraints(WebXml theWebXml)
    {
        Iterator securityConstraints =
            theWebXml.getElements(WebXmlTag.SECURITY_CONSTRAINT);
        int count = 0;
        while (securityConstraints.hasNext())
        {
            Element securityConstraint = (Element) securityConstraints.next();
            webXml.addElement(WebXmlTag.SECURITY_CONSTRAINT,
                securityConstraint);
            count++;
        }
        return count;
    }

    /**
     * 
     * 
     * @param theWebXml The descriptor that contains the login config that
     *         is to be merged into the original descriptor
     * @return Whether the login config was merged
     */
    public boolean mergeLoginConfig(WebXml theWebXml)
    {
        Iterator loginConfigs = theWebXml.getElements(WebXmlTag.LOGIN_CONFIG);
        if (loginConfigs.hasNext())
        {
            webXml.replaceElement(WebXmlTag.LOGIN_CONFIG,
                (Element) loginConfigs.next());
            return true;
        }
        return false;
    }

    /**
     * 
     * 
     * @param theWebXml The descriptor that contains the security roles that
     *         are to be merged into the original descriptor
     * @return The number of security constraints merged into the original
     *          descriptor
     */
    public int mergeSecurityRoles(WebXml theWebXml)
    {
        Iterator securityRoles =
            theWebXml.getElements(WebXmlTag.SECURITY_ROLE);
        int count = 0;
        while (securityRoles.hasNext())
        {
            Element securityRole = (Element) securityRoles.next();
            webXml.addElement(WebXmlTag.SECURITY_ROLE,
                securityRole);
            count++;
        }
        return count;
    }

}
