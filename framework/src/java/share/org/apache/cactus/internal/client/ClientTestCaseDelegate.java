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
package org.apache.cactus.internal.client;

import org.apache.commons.logging.Log;

/**
 * Provides useful methods for the Cactus <code>XXXTestCase</code> classes.
 * All the methods provided are independent of any communication protocol 
 * between client side and server side (HTTP, JMS, etc). Subclasses will 
 * define additional behaviour that depends on the protocol.
 *  
 * It provides the ability to run common code before each test on the client 
 * side (note that calling common tear down code is delegated to child classes 
 * as the method signature depends on the protocol used).
 *
 * In addition it provides the ability to execute some one time (per-JVM)
 * initialisation code (a pity this is not provided in JUnit). It can be 
 * useful to start an embedded server for example. Note: In the future this
 * should be refatored and provided using a custom JUnit TestSuite.
 * 
 * @author <a href="mailto:vmassol@apache.org">Vincent Massol</a>
 *
 * @version $Id$
 */
public interface ClientTestCaseDelegate
{
    /**
     * @return The logger used by the <code>TestCase</code> class and
     *         subclasses to perform logging.
     */
    Log getLogger();
    
    /**
     * Perform client side initializations before each test, such as
     * re-initializating the logger and printing some logging information.
     */
    void runBareInit();

    /**
     * Runs a test case. This method is overriden from the JUnit
     * <code>TestCase</code> class in order to seamlessly call the
     * Cactus redirection servlet.
     *
     * @exception Throwable if any error happens during the execution of
     *            the test
     */
    void runTest() throws Throwable;    
}
