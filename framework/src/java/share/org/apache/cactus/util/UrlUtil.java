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
package org.apache.cactus.util;

import java.net.URL;

/**
 * Various utility methods for URL manipulation.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public class UrlUtil
{
    /**
     * Returns the path part of the URL. This method is needed for
     * JDK 1.2 support as <code>URL.getPath()</code> does not exist in
     * JDK 1.2 (only for JDK 1.3+).
     *
     * @param theURL the URL from which to extract the path
     * @return the path part of the URL
     */
    public static String getPath(URL theURL)
    {
        String file = theURL.getFile();
        String path = null;

        if (file != null)
        {
            int q = file.lastIndexOf('?');

            if (q != -1)
            {
                path = file.substring(0, q);
            }
            else
            {
                path = file;
            }
        }

        return path;
    }

    /**
     * Returns the query string of the URL. This method is needed for
     * JDK 1.2 support as <code>URL.getQuery()</code> does not exist in
     * JDK 1.2 (only for JDK 1.3+).
     *
     * @param theURL the URL from which to extract the query string
     * @return the query string portion of the URL
     */
    public static String getQuery(URL theURL)
    {
        String file = theURL.getFile();
        String query = null;

        if (file != null)
        {
            int q = file.lastIndexOf('?');

            if (q != -1)
            {
                query = file.substring(q + 1);
            }
            else
            {
                query = "";
            }
        }

        return query;
    }
}
