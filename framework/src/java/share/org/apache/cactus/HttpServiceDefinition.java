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
package org.apache.cactus;

/**
 * Constants that define HTTP parameters required for defining a service that
 * is performed by the <code>ServletTestRedirector</code> servlet.
 *
 * <p>
 *   <strong>WARNING</strong><br/>
 *   This interface is not intended for use by API clients. It may be altered in
 *   backwards-incompatible ways and even moved or removed at any time without
 *   further notice.
 * </p>
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public interface HttpServiceDefinition
{
    /**
     * Name of the parameter in the HTTP request that represents the name of the
     * Test class to call. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    static String CLASS_NAME_PARAM = "Cactus_TestClass";

    /**
     * Name of the parameter in the HTTP request that represents the name of the
     * Test method to call. The name is voluntarily long so that it will not
     * clash with a user-defined parameter.
     */
    static String METHOD_NAME_PARAM = "Cactus_TestMethod";

    /**
     * Name of the parameter in the HTTP request that specify if a session
     * should be automatically created for the user or not.
     */
    static String AUTOSESSION_NAME_PARAM = "Cactus_AutomaticSession";

    /**
     * Name of the parameter in the HTTP request that specify the service asked
     * to the Redirector Servlet. It can be either to ask the Redirector Servlet
     * to call the test method or to ask the Redirector Servlet to return the
     * result of the last test.
     *
     * @see ServiceEnumeration
     */
    static String SERVICE_NAME_PARAM = "Cactus_Service";
}
