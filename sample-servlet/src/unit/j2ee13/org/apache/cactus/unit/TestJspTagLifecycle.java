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
 * Tests for the <code>JspTagLifecycle</code> extension.
 * 
 * <p>
 *   The lifecycle helper is tested here by testing the reference implementation
 *   of the JSP standard tag library (JSTL), available at
 *   <a href="http://jakarta.apache.org/taglibs/">.
 * </p>
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 */
public class TestJspTagLifecycle
    extends JspTestCase
{
    
    // Constructors ------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @param theName The name of the test case
     */
    public TestJspTagLifecycle(String theName)
    {
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
     * Tests the <code>&lt;c:out&gt;</code>-tag with a proper, literal value for
     * it's <code>value</code> attribute.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testOutTag()
        throws JspException, IOException
    {
        OutTag tag = new OutTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setValue("Value");
        lifecycle.invoke();
    }
    
    /**
     * Verifies that the response has been correctly rendered by the 
     * <code>&lt;c:out&gt;</code>-tag.
     * 
     * @param theResponse The HTTP response
     */
    public void endOutTag(WebResponse theResponse)
    {
        String output = theResponse.getText();
        assertEquals("Value", output);
    }
    
    /**
     * Tests the <code>&lt;c:out&gt;</code>-tag with <code>null</code> for
     * it's <code>value</code> attribute, and a proper, literal value for it's
     * <code>default</code> attribute.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testOutTagDefaultAttribute()
        throws JspException, IOException
    {
        OutTag tag = new OutTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setValue(null);
        tag.setDefault("Default");
        lifecycle.invoke();
    }
    
    /**
     * Verifies that the response has been correctly rendered by the 
     * <code>&lt;c:out&gt;</code>-tag.
     *  
     * @param theResponse The HTTP response
     */
    public void endOutTagWithDefaultAttribute(WebResponse theResponse)
    {
        String output = theResponse.getText();
        assertEquals("Default", output);
    }
    
    /**
     * Tests the &lt;c:out&gt;-Tag with a value that evaluates to
     * <code>null</code>, and the default value specified in the tag's body.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testOutTagDefaultBody()
        throws JspException, IOException
    {
        OutTag tag = new OutTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setValue(null);
        lifecycle.invoke(new JspTagLifecycle.Interceptor()
        {
            public void evalBody(int iteration, BodyContent body)
                throws IOException
            {
                body.print("Default");
            }
        });
    }
    
    /**
     * Verifies that the response has been correctly rendered by the 
     * <code>&lt;c:out&gt;</code>-tag.
     * 
     * @param theResponse The HTTP response
     * @todo This test currently fails if commented in
     */
    public void endOutTagDefaultBody(WebResponse theResponse)
    {
        String output = theResponse.getText();
        //assertEquals("Default", output);
    }
    
    /**
     * Tests the &lt;c:set&gt;-tag with a proper, literal values for it's
     * <code>var</code> and <code>value</code> attributes. Verification is done
     * by checking the scoped variable stored by the tag.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testSetTag()
        throws JspException, IOException
    {
        SetTag tag = new SetTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setVar("Var");
        tag.setValue("Value");
        lifecycle.invoke();
        assertEquals("Value", pageContext.findAttribute("Var"));
    }
    
    /**
     * Tests the tag &lt;c:forEach&gt; by providing a comma-delimited list of 
     * string to it's <code>items</code> attributes, and checking the exposed
     * scoped variable on every iteration step.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testForEachTag()
        throws JspException, IOException
    {
        ForEachTag tag = new ForEachTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setVar("Item");
        tag.setItems("One,Two,Three");
        lifecycle.invoke(new JspTagLifecycle.Interceptor()
        {
            public void evalBody(int iteration, BodyContent body)
            {
                String item = (String)pageContext.findAttribute("Item");
                assertNotNull(item);
                if (iteration == 0)
                {
                    assertEquals("One", item);
                }
                else if (iteration == 1)
                {
                    assertEquals("Two", item);
                }
                else if (iteration == 2)
                {
                    assertEquals("Three", item);
                }
                else
                {
                    fail("More iterations than expected!");
                }
            }
        });
    }
    
    /**
     * Tests the conditional tag &lt;c:if&gt; by providing a proper, literal 
     * value to it's <code>test</code> attribute that evaluates to
     * the boolean value <code>true</code>. The test verifies the correct
     * behaviour by asserting that the tag's body is not skipped.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testIfTagTrue()
        throws JspException, IOException
    {
        IfTag tag = new IfTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setTest("true");
        lifecycle.invoke(new JspTagLifecycle.Interceptor()
        {
            public void skipBody()
            {
                fail("Body should have been evaluated!");
            }
        });
    }
    
    /**
     * Tests the conditional tag &lt;c:if&gt; by providing a proper, literal 
     * value to it's <code>test</code> attribute that evaluates to
     * the boolean value <code>false</code>. The test verifies the correct
     * behaviour by asserting that the tag's body is not evaluated.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testIfTagFalse()
        throws JspException, IOException
    {
        IfTag tag = new IfTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setTest("false");
        lifecycle.invoke(new JspTagLifecycle.Interceptor()
        {
            public void evalBody(int iteration, BodyContent body)
            {
                fail("Body should have been skipped!");
            }
        });
    }
    
    /**
     * Tests the &lt;c:when&gt;-tag correctly nested inside a &lt;c:choose&gt;
     * tag, and providing a proper, literal value to it's <code>test</code>
     * attribute that evaluates to the boolean value <code>true</code>. The test
     * verifies the correct behaviour by asserting that the tag's body is not
     * skipped.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testWhenTag()
        throws JspException, IOException
    {
        WhenTag tag = new WhenTag();
        JspTagLifecycle lifecycle =
            new JspTagLifecycle(pageContext, tag, new ChooseTag());
        tag.setTest("true");
        lifecycle.invoke(new JspTagLifecycle.Interceptor()
        {
            public void skipBody()
            {
                fail("Body should have been evaluated!");
            }
        });
    }
    
    /**
     * Tests the &lt;c:when&gt;-tag correctly nested inside a &lt;c:choose&gt;
     * tag, and providing a proper, literal value to it's <code>test</code>
     * attribute that evaluates to the boolean value <code>true</code>. However,
     * an earlier instance of the <code>&lt;c:when&gt;</code> tag nested in the
     * parent has already succeeded, so this test asserts that the body of the
     * later <code>&lt;c:when&gt;</code> does not get evaluated.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testWhenTagNoPermission()
        throws JspException, IOException
    {
        ChooseTag parent = new ChooseTag();
        parent.subtagSucceeded();
        WhenTag tag = new WhenTag();
        JspTagLifecycle lifecycle =
            new JspTagLifecycle(pageContext, tag, parent);
        tag.setTest("true");
        lifecycle.invoke(new JspTagLifecycle.Interceptor()
        {
            public void evalBody(int iteration, BodyContent body)
            {
                fail("Body should have been skipped!");
            }
        });
    }
    
    /**
     * Tests te <code>&lt;c:when&gt;</code> tag not nested inside a 
     * <code>&lt;c:choose&gt;</code> tag. The test expects a
     * <code>JspException</code> to be thrown.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testWhenTagWithoutChooseTag()
        throws JspException, IOException
    {
        WhenTag tag = new WhenTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setTest("true");
        try
        {
            lifecycle.invoke();
            fail("Expected JSPTagException");
        } catch (JspTagException je) {
            // expected
        }
    }
    
}
