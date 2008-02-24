/* 
 * ========================================================================
 * 
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cactus.integration.ant;

import java.io.File;

import junit.framework.Test;

/**
 * Class used to run all tests under Eclipse.
 *
 * @version $Id: $
 */
public class EclipseTestAll
{
  
  /**
   * Sets the testinput.dir property and calls TestAll.suite().
   * @return a test suite (<code>TestSuite</code>) that includes all methods
   *         starting with "test"
   */
  public static Test suite()
  {
    final String inputDir = System.getProperty("testinput.dir");
    if (inputDir == null) 
    {
      final File dir = new File("./src/test-input");
      final String path = dir.getAbsolutePath();
      System.setProperty("testinput.dir", path);
    }
    return TestAll.suite();
  }

}
