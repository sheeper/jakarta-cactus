/*
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
 * 4. The names "The Jakarta Project", "Cactus", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
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
 */
package org.apache.cactus.unit;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.extension.jsp.JspTagLifecycle;
import org.apache.cactus.JspTestCase;
import org.apache.cactus.WebResponse;
import org.apache.taglibs.standard.tag.common.core.ChooseTag;
import org.apache.taglibs.standard.tag.el.core.ForEachTag;
import org.apache.taglibs.standard.tag.el.core.IfTag;
import org.apache.taglibs.standard.tag.el.core.OutTag;
import org.apache.taglibs.standard.tag.el.core.SetTag;
import org.apache.taglibs.standard.tag.el.core.WhenTag;

/**
 * 
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 */
public class TestJspTagLifecycle
    extends JspTestCase {
    
    // Constructors ------------------------------------------------------------
    
    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestJspTagLifecycle(String theName) {
        super(theName);
    }
    
    // Public Static Methods ---------------------------------------------------
    
    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite()
    {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestJspTagLifecycle.class);
    }

    // Test Methods ------------------------------------------------------------
    
    /**
     * 
     * @throws JspException
     * @throws IOException
     */
    public void testOutTag()
        throws JspException, IOException {
        
        OutTag tag = new OutTag();
        tag.setValue("TEST");
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        lifecycle.invoke(new JspTagLifecycle.Interceptor() {
            public void evalBody(int iteration, BodyContent body) {
                assertEquals("TEST", body.getString());
            }
        });
    }
    
    /**
     * 
     * @param theResponse
     */
    public void endOutTag(WebResponse theResponse) {
        
        String output = theResponse.getText();
        assertEquals("TEST", output);
    }
    
    /**
     * 
     * @throws JspException
     * @throws IOException
     */
    public void testOutTagDefaultAttribute()
        throws JspException, IOException {
        
        OutTag tag = new OutTag();
        tag.setValue(null);
        tag.setDefault("Default Value");
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        lifecycle.invoke();
    }
    
    /**
     * 
     * @param theResponse
     */
    public void endOutTagWithDefaultAttribute(WebResponse theResponse) {
        
        String output = theResponse.getText();
        assertEquals("Default Value", output);
    }
    
    /**
     * 
     * @throws JspException
     * @throws IOException
     */
    public void testOutTagDefaultBody()
        throws JspException, IOException {
/*      
        OutTag tag = new OutTag();
        tag.setValue(null);
        new JspTagLifecycle(tag) {
            protected void evalBody(int iteration, BodyContent body)
                throws IOException {
                body.print("Default Value");
            }
        }.invoke(pageContext, null);*/
    }
    
    /**
     * 
     * @param theResponse
     */
    public void endOutTagDefaultBody(WebResponse theResponse) {
/*        
        String output = theResponse.getText();
        assertEquals("Default Value", output);*/
    }
    
    /**
     * 
     * @throws JspException
     * @throws IOException
     */
    public void testSetTag()
        throws JspException, IOException {
        
        SetTag tag = new SetTag();
        tag.setVar("name");
        tag.setValue("value");
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        lifecycle.invoke();
        assertEquals("value", pageContext.findAttribute("name"));
    }
    
    /**
     * 
     * @throws JspException
     * @throws IOException
     */
    public void testForEachTag()
        throws JspException, IOException {
        
        ForEachTag tag = new ForEachTag();
        tag.setVar("item");
        tag.setItems("uno,dos,tres");
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        lifecycle.invoke(new JspTagLifecycle.Interceptor() {
            public void evalBody(int iteration, BodyContent body) {
                if (iteration == 0) {
                    assertEquals("uno", pageContext.findAttribute("item"));
                } else if (iteration == 1) {
                    assertEquals("dos", pageContext.findAttribute("item"));
                } else if (iteration == 2) {
                    assertEquals("tres", pageContext.findAttribute("item"));
                } else {
                    fail("More iterations than expected!");
                }
            }
        });
    }
    
    /**
     * 
     * @throws JspException
     * @throws IOException
     */
    public void testIfTagTrue()
        throws JspException, IOException {
        
        IfTag tag = new IfTag();
        tag.setTest("true");
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        lifecycle.invoke(new JspTagLifecycle.Interceptor() {
            public void skipBody() {
                fail("Body should have been evaluated!");
            }
        });
    }
    
    /**
     * 
     * @throws JspException
     * @throws IOException
     */
    public void testIfTagFalse()
        throws JspException, IOException {
        
        IfTag tag = new IfTag();
        tag.setTest("false");
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        lifecycle.invoke(new JspTagLifecycle.Interceptor() {
            public void evalBody(int iteration, BodyContent body) {
                fail("Body should have been skipped!");
            }
        });
    }
    
    /**
     * 
     * @throws JspException
     * @throws IOException
     */
    public void testWhenTag()
        throws JspException, IOException
    {
        
        WhenTag tag = new WhenTag();
        tag.setTest("true");
        JspTagLifecycle lifecycle =
            new JspTagLifecycle(pageContext, tag, new ChooseTag());
        lifecycle.invoke(new JspTagLifecycle.Interceptor() {
            public void skipBody() {
                fail("Body should have been evaluated!");
            }
        });
    }
    
    /**
     * 
     * @throws JspException
     * @throws IOException
     */
    public void testWhenTagNoPermission()
        throws JspException, IOException
    {
        
        ChooseTag parent = new ChooseTag();
        parent.subtagSucceeded();
        WhenTag tag = new WhenTag();
        tag.setTest("true");
        JspTagLifecycle lifecycle =
            new JspTagLifecycle(pageContext, tag, parent);
        lifecycle.invoke(new JspTagLifecycle.Interceptor() {
            public void evalBody(int iteration, BodyContent body) {
                fail("Body should have been skipped!");
            }
        });
    }
    
    /**
     * 
     * @throws JspException
     * @throws IOException
     */
    public void testWhenTagWithoutChooseTag()
        throws JspException, IOException {
        
        WhenTag tag = new WhenTag();
        tag.setTest("true");
        try {
            JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
            lifecycle.invoke();
            fail("Expected JSPTagException");
        } catch (JspTagException je) {
            // expected
        }
    }
    
}
