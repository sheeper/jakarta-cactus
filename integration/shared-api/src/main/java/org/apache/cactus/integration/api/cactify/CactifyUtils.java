package org.apache.cactus.integration.api.cactify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.module.webapp.DefaultWarArchive;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.util.internal.log.AbstractLogger;
import org.codehaus.cargo.util.log.Logger;

/**
 * Util class for cactifying purposes.
 * @author tahchiev
 */
public class CactifyUtils {
	
    /**
     * The cargo ResourceUtils.
     */
    private ResourceUtils utils = new ResourceUtils();
    /**
     * The abstract cargo logger used both by Ant and Maven2.
     */
    private Logger logger;
	

    /**
     * Log debug the given message.
     * @param msg
     */
    public void debug(String msg)
    {
    	getLogger().debug(msg, this.getClass().getName());
    }
    /**
     * Log info the given message.
     * @param msg
     */
    public void info(String msg)
    {
    	getLogger().info(msg, this.getClass().getName());
    }
    /**
     * Log warn the given message.
     * @param msg
     */
    public void warn(String msg)
    {
    	getLogger().warn(msg, this.getClass().getName());
    }
    
    /**
     * Adds the definitions corresponding to the nested redirector elements to
     * the provided deployment descriptor. 
     * 
     * @param theWebXml The deployment descriptor
     */
    public void addRedirectorDefinitions(WebXml theWebXml, List redirectors)
    {
        boolean filterRedirectorDefined = false;
        boolean jspRedirectorDefined = false;
        boolean servletRedirectorDefined = false;
        
        // add the user defined redirectors
        for (Iterator i = redirectors.iterator(); i.hasNext();)
        {
        
            Redirector redirector = (Redirector) i.next();
            if (redirector instanceof FilterRedirector)
            {
                filterRedirectorDefined = true;
            }
            else if (redirector instanceof JspRedirector)
            {
                jspRedirectorDefined = true;
            }
            else if (redirector instanceof ServletRedirector)
            {
                servletRedirectorDefined = true;
            }
            redirector.mergeInto(theWebXml);
        }

        // now add the default redirectors if they haven't been provided by
        // the user
        if (!filterRedirectorDefined)
        {
            new FilterRedirector(getLogger())
                .mergeInto(theWebXml);
        }
        if (!servletRedirectorDefined)
        {
            new ServletRedirector(getLogger())
                .mergeInto(theWebXml);
        }
        if (!jspRedirectorDefined)
        {
            new JspRedirector(getLogger()).mergeInto(theWebXml);
        }
    }
    /**
     * Getter method for the logger.
     * @return AbstractLogger
     */
	public Logger getLogger() {
		return logger;
	}
	
	/**
	 * Setter method for the logger.
	 * @param logger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

}
