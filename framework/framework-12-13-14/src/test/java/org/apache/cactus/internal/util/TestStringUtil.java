/*
 * ========================================================================
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import junit.framework.TestCase;
import org.apache.cactus.internal.client.AssertionFailedErrorWrapper;
import org.apache.cactus.internal.client.ServletExceptionWrapper;

/**
 * Unit tests for the {@link StringUtil} class.
 *
 * @version $Id: TestStringUtil.java 239169 2005-05-05 09:21:54Z vmassol $
 */
public class TestStringUtil extends TestCase
{
    /**
     * Verify package-based stack-trace filtering.
     */
    public void testFilterLinePackageTrue()
    {
        String[] filterPatterns = new String[] {"my.package" };
        assertTrue(StringUtil.filterLine(
            "    at my.package.MyClass.method(MyClass.java:100)",
            filterPatterns));
    }

    /**
     * Verify package-based stack-trace filtering.
     */
    public void testFilterLinePackageFalse()
    {
        String[] filterPatterns = new String[] {"my.package" };
        assertTrue(!StringUtil.filterLine(
            "    at other.package.MyClass.method(MyClass.java:100)",
            filterPatterns));
    }

    /**
     * Verify class-based stack-trace filtering.
     */
    public void testFilterLineClassTrue()
    {
        String[] filterPatterns = new String[] {"my.package.MyClass" };
        assertTrue(StringUtil.filterLine(
            "    at my.package.MyClass.method(MyClass.java:100)",
            filterPatterns));
    }

    /**
     * Verify class-based stack-trace filtering.
     */
    public void testFilterLineClassFalse1()
    {
        String[] filterPatterns = new String[] {"my.package.MyClass" };
        assertTrue(!StringUtil.filterLine(
            "    at my.package.OtherClass.method(MyClass.java:100)",
            filterPatterns));
    }

    /**
     * Verify class-based stack-trace filtering.
     */
    public void testFilterLineClassFalse2()
    {
        String[] filterPatterns = new String[] {"my.package.MyClass" };
        assertTrue(!StringUtil.filterLine(
            "    at other.package.MyClass.method(MyClass.java:100)",
            filterPatterns));
    }

    /**
     * Verify character-replacement algorithm.
     */
    public void testReplace()
    {
        assertEquals("you&amp;me",
            StringUtil.replace("you&me", '&', "&amp;"));
        assertEquals("&lt;tag",
            StringUtil.replace("<tag", '<', "&lt;"));
        assertEquals("tag&gt;",
            StringUtil.replace("tag>", '>', "&gt;"));
        assertEquals("12<X>456<X>89",
            StringUtil.replace("12x456x89", 'x', "<X>"));
    }

    public void testTestExceptionStackTraceToString() {
        final String stackTrace =
                // Exception class and message
                "javax.ejb.EJBTransactionRolledbackException: nested exception is: javax.ejb.EJBException: See nested exception; nested exception is: java.lang.RuntimeException: Connection refused (Connection refused)\n" +
                        // stacktrace with class.method(file:line)
                        "\tat com.ibm.ejs.container.BusinessExceptionMappingStrategy.mapCSIException(BusinessExceptionMappingStrategy.java:152)\n" +
                        // with class.method(file)  without line
                        "\tat com.backgroundprocessing.EJSLocal0SLBatchServiceDelegator_7a375f34.generateAllPdf(EJSLocal0SLBatchServiceDelegator_7a375f34.java)\n" +
                        "\tat com.backgroundprocessing.internal.businessobject.BatchServiceTest.testCompressFiles_Ok(Unknown Source)\n" +
                        // with method aspect
                        "\tat org.apache.cactus.internal.server.AbstractWebTestController.handleRequest_aroundBody1$advice(AbstractWebTestController.java:225)\n" +
                        // native method
                        "\tat org.apache.cactus.server.ServletTestRedirector.doPost(Native Method)\n" +
                        // with inner classes
                        "\tat com.ibm.io.async.AsyncChannelFuture$1.run(AsyncChannelFuture.java:205)\n" +
                        // another inner class
                        "\tat com.ibm.ws.util.ThreadPool$Worker.run(ThreadPool.java:1909)\n" +
                        // cause without message
                        "Caused by: javax.ejb.EJBException\n" +
                        // without stack
                        // Cause 3
                        "Caused by: java.lang.RuntimeException: Connection refused (Connection refused)\n" +
                        "\tat org.apache.fop.fonts.LazyFont.load(LazyFont.java:148)\n" +
                        "\tat org.apache.fop.svg.NativeTextPainter.paintTextRuns(NativeTextPainter.java:91)\n" +
                        "\tat org.apache.batik.gvt.renderer.StrokingTextPainter.paint(Unknown Source)\n" +
                        "\tat org.apache.batik.gvt.TextNode.primitivePaint(Unknown Source)" +
                        // trailing linebreak
                        "\n";

        final Throwable servletExceptionWrapper = StringUtil.stringToException(
                ServletExceptionWrapper.class,
                null,
                null,
                stackTrace);
        String exceptionString = StringUtil.exceptionToString(servletExceptionWrapper, new String[]{});
        exceptionString = exceptionString.replaceAll("\r", ""); // Fixing windows serialization issues

        assertEquals(stackTrace, exceptionString);

        Throwable assertionFailedErrorWrapper = StringUtil.stringToException(
                AssertionFailedErrorWrapper.class,
                null,
                null,
                stackTrace);
        exceptionString = StringUtil.exceptionToString(assertionFailedErrorWrapper);
        exceptionString = exceptionString.replaceAll("\r", ""); // Fixing windows serialization issues

        assertEquals(stackTrace, exceptionString);
    }

    public void testTestExceptionWithoutStackToString() {
        Throwable exceptionWrapper = StringUtil.stringToException(AssertionFailedErrorWrapper.class, "TestException", "myDummyTest", null);
        String actualStackTrace = StringUtil.exceptionToString(exceptionWrapper);
        actualStackTrace = actualStackTrace.replaceAll("\r", ""); // Fixing windows serialization issues

        assertEquals("TestException: myDummyTest\n", actualStackTrace);
    }
}
