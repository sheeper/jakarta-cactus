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
package org.apache.cactus.internal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Various utility methods for manipulating IO streams.
 *
 * @version $Id$
 */
public class IoUtil
{
    /**
     * @see #getText(InputStream, String)
     */
    public static String getText(InputStream theStream) throws IOException
    {
        return getText(theStream, null);
    }

    /**
     * Read all data in an Input stream and return them as a 
     * <code>String</code> object.
     *
     * @param theStream the input stream from which to read the data
     * @param theCharsetName the charset name with which to read the data
     * @return the string representation of the data
     * @throws IOException if an error occurs during the read of data
     */
    public static String getText(InputStream theStream, String theCharsetName) 
        throws IOException
    {
        StringBuffer sb = new StringBuffer();

        BufferedReader input;
        if (theCharsetName == null)
        {
            input = new BufferedReader(new InputStreamReader(theStream));
        }
        else
        {
            input = new BufferedReader(
               new InputStreamReader(theStream, theCharsetName));
        }
        
        char[] buffer = new char[2048];
        int nb;

        while (-1 != (nb = input.read(buffer, 0, 2048)))
        {
            sb.append(buffer, 0, nb);
        }

        input.close();

        return sb.toString();
    }
}
