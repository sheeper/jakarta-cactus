/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */

package org.apache.cactus.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.*;

import java.util.*;

/**
 * This task converts path and classpath information to a specific target OS format.
 * The resulting formatted path is placed into a specified property.
 * <p>
 * LIMITATION: Currently this implementation groups all machines into one of two
 * types: Unix or Windows.  Unix is defined as NOT windows.
 *
 * @author Larry Streepy <a href="mailto:streepy@healthlanguage.com">streepy@healthlanguage.com</a>
 */
public class PathConvert extends Task {

    /**
     * Helper class, holds the nested <map> values.  Elements will look like this:
     * &lt;map from="d:" to="/foo"/>
     * <p>
     * When running on windows, the prefix comparison will be case insensitive.
     */
    public class MapEntry {

        /**
         * Set the "from" attribute of the map entry
         */
        public void setFrom( String from ) {
            this.from = from;
        }

        /**
         * Set the "to" attribute of the map entry
         */
        public void setTo( String to ) {
            this.to = to;
        }

        /**
         * Apply this map entry to a given path element
         * @param elem Path element to process
         * @return String Updated path element after mapping
         */
        public String apply( String elem ) {
            if( from == null || to == null ) {
                throw new BuildException( "Both 'from' and 'to' must be set in a map entry" );
            }

            // If we're on windows, then do the comparison ignoring case
            String cmpElem = onWindows ? elem.toLowerCase() : elem;
            String cmpFrom = onWindows ? from.toLowerCase() : from;

            // If the element starts with the configured prefix, then convert the prefix
            // to the configured 'to' value.

            if( cmpElem.startsWith( cmpFrom ) ) {
                int len = from.length();

                if( len >= elem.length() ) {
                    elem = to;
                } else {
                    elem = to + elem.substring( len );
                }
            }

            return elem;
        }

        // Members
        private String from = null;
        private String to = null;
    }

    /**
     * Create a nested PATH element
     */
    public Path createPath() {

        if( isReference() )
            throw noChildrenAllowed();

        if( path == null ) {
            path = new Path(getProject());
        }
        return path.createPath();
    }

    /**
     * Create a nested MAP element
     */
    public MapEntry createMap() {

        MapEntry entry = new MapEntry();
        prefixMap.add( entry );
        return entry;
    }

    /**
     * Set the value of the targetos attribute
     */
    public void setTargetos( String target ) {

        targetOS = target.toLowerCase();

        if( ! targetOS.equals( "windows" ) && ! target.equals( "unix" ) ) {
            throw new BuildException( "targetos must be one of 'unix' or 'windows'" );
        }

        // Currently, we deal with only two path formats: Unix and Windows
        // And Unix is everything that is not Windows

        targetWindows = targetOS.equals("windows");
    }

    /**
     * Set the value of the proprty attribute - this is the property into which our
     * converted path will be placed.
     */
    public void setProperty( String p ) {
        property = p;
    }

    /**
     * Adds a reference to a PATH defined elsewhere.
     */
    public void setPathRef(Reference r) {
        if( path != null )
            throw noChildrenAllowed();

        pathref = r;
    }

    /**
     * Has the pathref attribute of this element been set?
     */
    public boolean isReference() {
        return pathref != null;
    }

    /**
     * Do the execution.
     */
    public void execute() throws BuildException {

        // If we are a reference, the create a Path from the reference
        if( isReference() ) {
            path = new Path(getProject()).createPath();
            path.setRefid(pathref);
        }

        validateSetup();                    // validate our setup

        // Currently, we deal with only two path formats: Unix and Windows
        // And Unix is everything that is not Windows

        String osname = System.getProperty("os.name").toLowerCase();
        onWindows = ( osname.indexOf("windows") >= 0 );

        StringBuffer rslt = new StringBuffer( 100 );

        // If we're on the same platform type as the target, then there is no
        // conversion work to do.

        if( onWindows != targetWindows ) {

            char pathSep = targetWindows ? ';' : ':';

            // Get the list of path components in canonical form
            String[] elems = path.list();

            for( int i=0; i < elems.length; i++ ) {
                String elem = elems[i];

                elem = mapElement( elem );      // Apply the path prefix map

                // Now convert the path and file separator characters from the
                // current os to the target os.

                if( targetWindows ) {
                    elem = elem.replace( '/', '\\' );
                } else {
                    elem = elem.replace( '\\', '/' );
                }

                if( i != 0 ) rslt.append( pathSep );
                rslt.append( elem );
            }

        } else {
            rslt.append( path.toString() );     // No additional work to do
        }

        // Place the result into the specified property
        String value = rslt.toString();

        log( "Set property " + property + " = " + value, Project.MSG_VERBOSE );

        getProject().setProperty( property, value );
    }

    /**
     * Apply the configured map to a path element.  The map is used to convert
     * between Windows drive letters and Unix paths.  If no map is configured,
     * the default operation is:
     * <UL>
     * <LI>when target is unix: remove all drive letters</LI>
     * <LI>when target is windows: add "C:"
     * </UL>
     *
     * @param elem The path element to apply the map to
     * @return String Updated element
     */
    private String mapElement( String elem ) {

        int size = prefixMap.size();

        if( size == 0 ) {

            if( targetWindows ) {
                elem = "C:" + elem;
            } else {
                // If the element starts with a drive letter, remove it
                if( elem.charAt(1) == ':' )
                    elem = elem.substring(2);
            }

        } else {
            // Iterate over the map entries and apply each one.  Stop when one of the
            // entries actually changes the element

            for( int i=0; i < size; i++ ) {
                MapEntry entry = (MapEntry)prefixMap.get(i);
                String newElem = entry.apply( elem );

                // Note I'm using "!=" to see if we got a new object back from
                // the apply method.

                if( newElem != elem ) {
                    elem = newElem;
                    break;                  // We applied one, so we're done
                }
            }
        }

        return elem;
    }

    /**
     * Validate that all our parameters have been properly initialized.
     * @throws BuildException if something is not setup properly
     */
    private void validateSetup() throws BuildException {

        if( path == null )
            throw new BuildException( "You must specify a path to convert" );

        if( targetOS == null )
            throw new BuildException( "You must specify a target OS" );

        if( property == null )
            throw new BuildException( "You must specify a property" );
    }

    /**
     * Creates an exception that indicates that this XML element must
     * not have child elements if the pathrefid attribute is set.
     */
    private BuildException noChildrenAllowed() {
        return new BuildException("You must not specify nested PATH elements when using pathref");
    }


    // Members
    private Path path = null;               // Path to be converted
    private Reference pathref = null;       // Reference to path to convert
    private String targetOS = null;         // The target OS type
    private boolean targetWindows = false;  // Set when targetOS is set
    private boolean onWindows = false;      // Set if we're running on windows
    private String property = null;         // The property to receive the results
    private ArrayList prefixMap = new ArrayList();  // Path prefix map
}

