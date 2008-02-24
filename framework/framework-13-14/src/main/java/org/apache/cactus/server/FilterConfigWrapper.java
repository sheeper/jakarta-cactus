/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * Wrapper around <code>FilterConfig</code> which overrides the
 * <code>getServletContext()</code> method to return our own wrapper around
 * <code>ServletContext</code>.
 *
 * @version $Id: FilterConfigWrapper.java 239054 2004-10-24 01:30:23Z felipeal $
 * @see ServletContext
 */
public class FilterConfigWrapper implements FilterConfig
{
    /**
     * The original filter config object.
     */
    private FilterConfig originalConfig;

    /**
     * List of parameters set using the <code>setInitParameter()</code> method.
     */
    private Hashtable initParameters;

    /**
     * Simulated name of the filter.
     */
    private String filterName;

    /**
     * @param theOriginalConfig the original filter config object
     */
    public FilterConfigWrapper(FilterConfig theOriginalConfig)
    {
        this.originalConfig = theOriginalConfig;
        this.initParameters = new Hashtable();
    }

    /**
     * Sets a parameter as if it were set in the <code>web.xml</code> file.
     *
     * @param theName the parameter's name
     * @param theValue the parameter's value
     */
    public void setInitParameter(String theName, String theValue)
    {
        this.initParameters.put(theName, theValue);
    }

    /**
     * Sets the filter name. That will be the value returned by the
     * <code>getFilterName()</code> method.
     *
     * @param theFilterName the filter name
     */
    public void setFilterName(String theFilterName)
    {
        this.filterName = theFilterName;
    }

    //--Overridden methods ----------------------------------------------------

    /**
     * @return the simulated filter's name if defined or the redirector
     *         filter's name
     */
    public String getFilterName()
    {
        if (this.filterName != null)
        {
            return this.filterName;
        }

        return this.originalConfig.getFilterName();
    }

    /**
     * @return our own wrapped servlet context object
     */
    public ServletContext getServletContext()
    {
        return new ServletContextWrapper(
            this.originalConfig.getServletContext());
    }

    /**
     * Return the union of the parameters defined in the Redirector
     * <code>web.xml</code> file and the one set using the
     * <code>setInitParameter()</code> method. The parameters with the same
     * name (and same case) are only returned once.
     *
     * @return the init parameters
     */
    public Enumeration getInitParameterNames()
    {
        Vector names = new Vector();

        // Add parameters that were added using setInitParameter()
        Enumeration en = this.initParameters.keys();

        while (en.hasMoreElements())
        {
            String value = (String) en.nextElement();

            names.add(value);
        }

        // Add parameters from web.xml
        en = this.originalConfig.getInitParameterNames();

        while (en.hasMoreElements())
        {
            String value = (String) en.nextElement();

            if (!names.contains(value))
            {
                names.add(value);
            }
        }

        return names.elements();
    }

    /**
     * @param theName the name of the parameter's value to return
     * @return the value of the parameter, looking for it first in the list of
     *         parameters set using the <code>setInitParameter()</code> method
     *         and then in those set in <code>web.xml</code>.
     */
    public String getInitParameter(String theName)
    {
        // Look first in the list of parameters set using the
        // setInitParameter() method.
        String value = (String) this.initParameters.get(theName);

        if (value == null)
        {
            value = this.originalConfig.getInitParameter(theName);
        }

        return value;
    }
}
