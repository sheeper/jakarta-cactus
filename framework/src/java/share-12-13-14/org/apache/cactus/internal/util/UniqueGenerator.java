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

import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

/**
 * Generates a quasi-unique id for a test case.
 *
 * @version $Id$
 */
public class UniqueGenerator
{
    /**
     * Counter with synchronized access to prevent possibly
     * identical ids from two threads requesting an id in the
     * same millisecond.
     */
    private static byte count = 0;
    
    /**
     * Lock for count.
     */
    private static Object lock = new Object();

    /**
     * The local IP address in hexadecimal format.
     */
    private static String ipAddress;
    static
    {
        try
        {
            byte ip[] = InetAddress.getLocalHost().getAddress();
            ipAddress = toHex(((ip[0] & 0xff) << 24)
                | ((ip[1] & 0xff) << 16) | ((ip[2] & 0xff) << 8)
                | (ip[3] & 0xff));
        }
        catch (UnknownHostException e)
        {
            ipAddress = "";
        }
    }

    /**
     * Generates a unique identifier for a Cactus test.
     * 
     * @param theTestCase The Test to generate a unique ID for
     * @return The generated ID
     */
    public static String generate(TestCase theTestCase)
    {
        long time = System.currentTimeMillis();
        synchronized (lock)
        {
            time += count++;
        }
        return generate(theTestCase, time);
    }

    /**
     * Generates a unique identifier for a Cactus test.
     * 
     * @param theTestCase The Test to generate a unique ID for
     * @param theTime The time component to include in the generated ID
     * @return The generated ID
     */
    public static String generate(TestCase theTestCase,
        long theTime)
    {
        String id = ipAddress;
        id += "-" + toHex(theTime);
        id += "-" + toHex(System.identityHashCode(theTestCase));
        id += toHex(theTestCase.getName().hashCode());
        return id;
    }

    /**
     * Returns the hexadecimal representation of an integer as string.
     * 
     * @param theValue The integer value
     * @return The integer value as string of hexadecimal digits
     */
    private static String toHex(long theValue)
    {
        return Long.toString(theValue, 16).toUpperCase();
    }

}
