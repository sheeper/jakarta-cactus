/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
package org.apache.cactus.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.cactus.AbstractWebServerTestCase;

/**
 * Generates a quasi-unique id for a test case.
 *
 * @author <a href="mailto:ndlesiecki@apache.org>Nicholas Lesiecki</a>
 * @author <a href="mailto:cmlenz@apache.org>Christopher Lenz</a>
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
    public static String generate(AbstractWebServerTestCase theTestCase)
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
    public static String generate(AbstractWebServerTestCase theTestCase,
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
