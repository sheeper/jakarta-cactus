/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation.
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
package org.apache.cactus.internal;

/**
 * Common interface that must be implemented by all Cactus test cases.
 * 
 * @version $Id: CactusTestCase.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public interface CactusTestCase
{
    /**
     * Executes JUnit tests on the server side. It is the equivalent of
     * {@link junit.framework.TestCase#runBare()} but run on the server 
     * side. 
     *   
     * @throws Throwable if an error occurred when running the test on the 
     *         server side
     */
    void runBareServer() throws Throwable;
}
