/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
package org.apache.cactus.client.authentication;

import org.apache.cactus.WebRequest;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * Basic Authentication support.
 *
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 * @author <a href="mailto:Jason.Robertson@acs-inc.com">Jason Robertson</a>
 *
 * @since 1.3
 * @see AbstractAuthentication
 *
 * @version $Id$
 */
public class BasicAuthentication extends AbstractAuthentication
{
    /**
     * Provides encoding of raw bytes to base64-encoded characters, and
     * decoding of base64 characters to raw bytes.
     */
    private static char[] alphabet =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".
            toCharArray();

    /**
     * Lookup table for converting base64 characters to value in range 0..63
     */
    private static byte[] codes = new byte[256];

    static {
        for (int i = 0; i < 256; i++) {
            codes[i] = -1;
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            codes[i] = (byte) (i - 'A');
        }
        for (int i = 'a'; i <= 'z'; i++) {
            codes[i] = (byte) (26 + i - 'a');
        }
        for (int i = '0'; i <= '9'; i++) {
            codes[i] = (byte) (52 + i - '0');
        }
        codes['+'] = 62;
        codes['/'] = 63;
    }

    /**
     * @param theName user name of the Credential
     * @param thePassword user password of the Credential
     */
    public BasicAuthentication(String theName, String thePassword)
    {
        super(theName, thePassword);
    }

    /**
     * @see AbstractAuthentication#validateName(String)
     */
    protected void validateName(String theName)
    {
        // According to HTTP 1.0 Spec:
        // userid   = [ token ]
        // token    = 1*<any CHAR except CTLs or tspecials>
        // CTL      = <any US-ASCII control character (octets 0-31) and
        //            DEL (127)
        // tspecial = "(" | ")" | "<" | ">" | "@"
        //            "," | ";" | ":" | "\" | <">
        //            "/" | "[" | "]" | "?" | "="
        //            "{" | "}" | SP | HT
        // SP       = <US-ASCII SP, space (32)>
        // HT       = <US-ASCII HT, horizontal-tab (9)>

        // Validate the given theName

        // The theName is optional, it can be blank.
        if (theName == null) {
            return;
        }

        // If it's non-blank, there is no maximum length
        // and it can't contain any illegal characters
        String illegalChars = "()<>@,;:\\\"/[]?={} \t";
        StringCharacterIterator iter = new StringCharacterIterator(theName);

        for (char c = iter.first(); c != CharacterIterator.DONE;
             c = iter.next()) {

            if ((illegalChars.indexOf(c) != -1)
                || ((c >= 0) && (c <= 31))
                || (c == 127)) {

                    // Bad theName! Go to your room!
                throw new IllegalArgumentException(
                        "Given theName contains illegal characters.");
            }
        }
    }

    /**
     * @see AbstractAuthentication#validatePassword(String)
     */
    protected void validatePassword(String thePassword)
    {
        // According to HTTP 1.0 Spec:
        // password = *TEXT
        // TEXT  = <any OCTET except CTLs, but including LWS>
        // OCTET = <any 8-bit sequence of data>
        // CTL   = <any US-ASCII control character (octets 0-31) and DEL (127)
        // LWS   = [CRLF] 1*( SP | HT )
        // CRLF  = CR LF
        // CR    = <US-ASCII CR, carriage return (13)>
        // LF    = <US-ASCII LF, linefeed (10)>
        // SP    = <US-ASCII SP, space (32)>
        // HT    = <US-ASCII HT, horizontal-tab (9)>

        // Validate the given thePassword

        // The thePassword can have zero characters, i.e. be blank.
        if (thePassword == null) {
            return;
        }

        // If it's non-blank, there is no maximum length
        // and it can't contain any illegal characters
        String exceptionChars = "\r\n \t"; // CR LF SP HT
        StringCharacterIterator iter =
                new StringCharacterIterator(thePassword);

        for (char c = iter.first(); c != CharacterIterator.DONE;
             c = iter.next()) {

            if (((c >= 0) && (c <= 31)) || (c == 127)) {

                if (exceptionChars.indexOf(c) != -1) {
                    continue;
                }

                // Bad thePassword! Go to your room!
                throw new IllegalArgumentException(
                        "Given thePassword contains illegal characters.");
            }
        }
    }

    /**
     * @see AbstractAuthentication#configure(WebRequest)
     */
    public void configure(WebRequest theRequest)
    {
        // According to HTTP 1.0 Spec:
        // basic-credentials = "Basic" SP basic-cookie
        // basic-cookie      = <base64 encoding of userid-password,
        //                     except not limited to 76 char/line>
        // userid-password   = [ token ] ":" *TEXT
        //
        // see setName and setPassword for details of token and TEXT

        String basicCookie = getName() + ":" + getPassword();
        String basicCredentials = "Basic "
            + new String(base64Encode(basicCookie.getBytes()));

        theRequest.addHeader("Authorization", basicCredentials);
    }

    /**
     * returns an array of base64-encoded characters to represent the
     * passed theData array.
     *
     * @param theData the array of bytes to encode
     * @return base64-coded character array.
     */
    private static char[] base64Encode(byte[] theData)
    {
        char[] out = new char[((theData.length + 2) / 3) * 4];

        //
        // 3 bytes encode to 4 chars. Output is always an even
        // multiple of 4 characters.
        //
        for (int i = 0, index = 0; i < theData.length; i += 3, index += 4) {
            boolean quad = false;
            boolean trip = false;

            int val = (0xFF & (int) theData[i]);
            val <<= 8;

            if ((i + 1) < theData.length) {
                val |= (0xFF & (int) theData[i + 1]);
                trip = true;
            }

            val <<= 8;

            if ((i + 2) < theData.length) {
                val |= (0xFF & (int) theData[i + 2]);
                quad = true;
            }

            out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
            val >>= 6;

            out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
            val >>= 6;

            out[index + 1] = alphabet[val & 0x3F];
            val >>= 6;

            out[index + 0] = alphabet[val & 0x3F];
        }
        return out;
    }
}
