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
package org.apache.cactus.client.authentication;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.apache.cactus.WebRequest;
import org.apache.cactus.configuration.Configuration;

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
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
        .toCharArray();

    /**
     * Lookup table for converting base64 characters to value in range 0..63
     */
    private static byte[] codes = new byte[256];

    static
    {
        for (int i = 0; i < 256; i++)
        {
            codes[i] = -1;
        }

        for (int i = 'A'; i <= 'Z'; i++)
        {
            codes[i] = (byte) (i - 'A');
        }

        for (int i = 'a'; i <= 'z'; i++)
        {
            codes[i] = (byte) (26 + i - 'a');
        }

        for (int i = '0'; i <= '9'; i++)
        {
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
        if (theName == null)
        {
            return;
        }

        // If it's non-blank, there is no maximum length
        // and it can't contain any illegal characters
        String illegalChars = "()<>@,;:\\\"/[]?={} \t";
        StringCharacterIterator iter = new StringCharacterIterator(theName);

        for (char c = iter.first(); c != CharacterIterator.DONE;
             c = iter.next())
        {
            if ((illegalChars.indexOf(c) != -1) || ((c >= 0) && (c <= 31))
                || (c == 127))
            {
                // Bad theName! Go to your room!
                throw new IllegalArgumentException(
                    "[" + theName + "] contains illegal characters.");
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
        if (thePassword == null)
        {
            return;
        }

        // If it's non-blank, there is no maximum length
        // and it can't contain any illegal characters
        String exceptionChars = "\r\n \t"; // CR LF SP HT
        StringCharacterIterator iter = new StringCharacterIterator(thePassword);

        for (char c = iter.first(); c != CharacterIterator.DONE; 
            c = iter.next())
        {
            if (((c >= 0) && (c <= 31)) || (c == 127))
            {
                if (exceptionChars.indexOf(c) != -1)
                {
                    continue;
                }

                // Bad thePassword! Go to your room!
                throw new IllegalArgumentException(
                    "Given thePassword contains illegal characters.");
            }
        }
    }

    /**
     * @see AbstractAuthentication#configure(WebRequest, Configuration)
     */
    public void configure(WebRequest theRequest,
        Configuration theConfiguration)
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
        for (int i = 0, index = 0; i < theData.length; i += 3, index += 4)
        {
            boolean quad = false;
            boolean trip = false;

            int val = (0xFF & (int) theData[i]);

            val <<= 8;

            if ((i + 1) < theData.length)
            {
                val |= (0xFF & (int) theData[i + 1]);
                trip = true;
            }

            val <<= 8;

            if ((i + 2) < theData.length)
            {
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
