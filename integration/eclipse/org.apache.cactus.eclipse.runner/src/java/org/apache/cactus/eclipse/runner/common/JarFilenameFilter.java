/*
 * Created on Mar 27, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.cactus.eclipse.runner.common;

import java.io.File;
import java.io.FilenameFilter;


/**
 * Filter for jar files.
 * i.e. accepts files like 'library.jar'
 * 
 * @author <a href="mailto:jruaux@octo.com">Julien Ruaux</a>
 * 
 * @version $Id$
 */
public class JarFilenameFilter implements FilenameFilter
{
    /**
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    public boolean accept(File theDir, String theFilename)
    {
        return theFilename.endsWith(".jar");
    }
}