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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.cactus.ServletTestCase;

import junit.framework.TestCase;

/**
 * Smoke test for the unique id generator.
 * 
 * @author <a href="mailto:ndlesiecki@apache.org>Nicholas Lesiecki</a>
 * @author <a href="mailto:cmlenz@apache.org>Christopher Lenz</a>
 *
 * @version $Id$
 */
public class TestUniqueGenerator extends TestCase
{
    /**
     * @see TestCase#setUp
     */
    protected void setUp()
    {
        // let the generator initialize
        UniqueGenerator.generate(new ServletTestCase("foo"));
    }

    /**
     * Simulates several simultaneous id generations using threads.
     * Verifies that there are no duplicates among the generated ids.
     */
    public void testThatSimultaneouslyGeneratedIdsAreUnique()
    {
        final ServletTestCase aTestCase = new ServletTestCase("foo");

        Thread[] threads = new Thread[10];
        final List results = Collections.synchronizedList(new ArrayList());
        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread()
            {
                public void run()
                {
                    results.add(UniqueGenerator.generate(aTestCase));
                }
            };
        }

        // loops separate to make their beginning as simultaneous
        // as possible
        for (int i = 0; i < threads.length; i++)
        {
            threads[i].run();
        }

        try
        {
            // in case the threads need time to finish
            Thread.sleep(200);
        }
        catch (InterruptedException e)
        {
            throw new ChainedRuntimeException(e);
        }

        Set resultSet = new HashSet(results);
        assertEquals(
            "Results contained duplicate ids.",
            results.size(),
            resultSet.size());
    }

    /**
     * Sanity check to verify that different IDs are generated for different
     * instances of the test class.
     */
    public void testThatGeneratedIdsForDifferentTestCasesAreUnique()
    {
        final ServletTestCase firstTestCase = new ServletTestCase("foo");
        final ServletTestCase secondTestCase = new ServletTestCase("foo");
        
        String firstId = UniqueGenerator.generate(firstTestCase, 0);
        String secondId = UniqueGenerator.generate(secondTestCase, 0);

        assertFalse("IDs not unique", firstId.equals(secondId));
    }

    /**
     * Sanity check to verify that different IDs are generated for different
     * test methods/names.
     */
    public void testThatGeneratedIdsForDifferentTestMethodsAreUnique()
    {
        final ServletTestCase aTestCase = new ServletTestCase("foo");
        
        String firstId = UniqueGenerator.generate(aTestCase, 0);
        aTestCase.setName("bar");
        String secondId = UniqueGenerator.generate(aTestCase, 0);

        assertFalse("IDs not unique", firstId.equals(secondId));
    }

}
