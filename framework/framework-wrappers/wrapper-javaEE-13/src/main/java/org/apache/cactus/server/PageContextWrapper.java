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
package org.apache.cactus.server;

import org.apache.cactus.ServletURL;

import javax.servlet.jsp.PageContext;

/**
 * Provide implementation of 
 * {@link javax.servlet.jsp.PageContext} for the Servlet 2.3 
 * API specifications.
 *
 * @see AbstractPageContextWrapper23
 * @version $Id: PageContextWrapper.java 238993 2004-05-22 16:39:34Z vmassol $
 */
public class PageContextWrapper extends AbstractPageContextWrapper23
{
    /**
     * {@inheritDoc}
     * @see AbstractPageContextWrapper23#AbstractPageContextWrapper23(PageContext, ServletURL)
     */
    public PageContextWrapper(PageContext theOriginalPageContext, 
        ServletURL theURL)
    {
        super(theOriginalPageContext, theURL);
    }
}
