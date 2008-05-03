/* 
 * ========================================================================
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cactus.integration.api.cactify;

import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlTag;
import org.codehaus.cargo.module.webapp.WebXmlUtils;
import org.codehaus.cargo.module.webapp.WebXmlVersion;
import org.codehaus.cargo.module.webapp.elements.FilterMapping;
import org.codehaus.cargo.util.log.Logger;

/**
 * Implementation of <code>Redirector</code> for filter test redirectors. 
 */
public class FilterRedirector extends Redirector
{

    /**
     * The name of the Cactus filter redirector class.
     */
    private static final String FILTER_REDIRECTOR_CLASS =
        "org.apache.cactus.server.FilterTestRedirector";
    
    /**
     * The default mapping of the Cactus filter redirector.
     */
    private static final String DEFAULT_FILTER_REDIRECTOR_MAPPING =
        "/FilterRedirector";
    
    /**
     * Default constructor.
     */
    public FilterRedirector()
    {
        this.name = "FilterRedirector";
        this.mapping = DEFAULT_FILTER_REDIRECTOR_MAPPING;
    }
    /**
     * Constructor with owner task to set.
     * @param theOwnerTask object
     */
    public FilterRedirector(Logger logger)
    {
        this();
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     * @see CactifyWarTask.Redirector#mergeInto
     */
    public void mergeInto(WebXml theWebXml)
    {
    	//If no version is specified then we accept the version is 2.2
    	//and we don't add the filter redirector.
    	if(theWebXml.getVersion() == null)
    		return;
        if (WebXmlVersion.V2_3.compareTo(theWebXml.getVersion()) <= 0)
        {
            if (WebXmlUtils.getFilterNamesForClass
                (theWebXml,FILTER_REDIRECTOR_CLASS).hasNext() && logger != null) 
            {
                logger.warn("WARNING: Your web.xml already includes " 
                + this.name + " mapping. Cactus is adding another one " 
                + "which may prevent your container from starting.", "WARNING");
            }
            WebXmlUtils.addFilter(theWebXml, this.name, FILTER_REDIRECTOR_CLASS);
              
            
//        	WebXmlTag s = new WebXmlTag(theWebXml.getDescriptorType(), "");
            
//            Iterator iter = WebXmlUtils.getFilterMappingElements(theWebXml, name);
            
            
            //Element filterMappingElement =
            //    theWebXml.getDocument().createElement(WebXmlType.FILTER_MAPPING);
            //filterMappingElement.appendChild(createNestedText(WebXmlTag.FILTER_NAME, filterName));
            //filterMappingElement.appendChild(createNestedText(WebXmlTag.URL_PATTERN, urlPattern));
            //addElement(WebXmlTag.FILTER_MAPPING, filterMappingElement, getRootElement());
            
            
            WebXmlTag tag = (WebXmlTag)theWebXml.getDescriptorType().getTagByName("filter-mapping");
            FilterMapping filterMapping = new FilterMapping(tag);
            filterMapping.setName(this.name);
            filterMapping.setUrlPattern(this.mapping);
            filterMapping.setFilterName(this.name);
            
            
            theWebXml.addTag(filterMapping);
//            
//            theWebXml.addTag(filterM);
//            
//            FilterMapping filterMapping = new FilterMapping();


            
            WebXmlUtils.addFilterMapping(theWebXml, filterMapping);
            if (this.roles != null)
            {
                addSecurity(theWebXml);
            }
        }
    }
    
}
