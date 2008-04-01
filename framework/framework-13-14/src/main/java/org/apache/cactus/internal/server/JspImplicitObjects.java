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
package org.apache.cactus.internal.server;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;


/**
 * Holder class that contains the instances of the implicit objects that will
 * be accessible in the test classes (ie subclasses of
 * <code>JspTestCase</code>).
 *
 * @version $Id: JspImplicitObjects.java 238991 2004-05-22 11:34:50Z vmassol $
 */
public class JspImplicitObjects extends ServletImplicitObjects
{
    /**
     * The JSP redirector <code>PageContext</code> object.
     */
    protected PageContext pageContext;

    /**
     * The JSP redirector <code>JspWriter</code> object (same as
     * <code>pagecontext.getOut()</code>).
     */
    protected JspWriter jspWriter;

    /**
     * @return the <code>PageContext</code> implicit object
     */
    public PageContext getPageContext()
    {
        return this.pageContext;
    }

    /**
     * @param thePageContext the <code>PageContext</code> implicit object
     */
    public void setPageContext(PageContext thePageContext)
    {
        this.pageContext = thePageContext;
    }

    /**
     * @return the <code>JspWriter</code> implicit object
     */
    public JspWriter getJspWriter()
    {
        return this.jspWriter;
    }

    /**
     * @param theWriter the <code>JspWriter</code> implicit object
     */
    public void setJspWriter(JspWriter theWriter)
    {
        this.jspWriter = theWriter;
    }
}
