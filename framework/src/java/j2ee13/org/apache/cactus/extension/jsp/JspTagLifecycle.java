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
package org.apache.cactus.extension.jsp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TryCatchFinally;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenience class that supports the testing of JSP tag by managing the tag's
 * lifecycle as required by the JSP specification.
 * 
 * <p>
 *   This class is basically a stub implementation of the tag management
 *   facilities that an actual JSP container would provide. The implementation
 *   attempts to follow the specification as closely as possible, but the tag
 *   handling functionality of real JSP implementations may vary in some
 *   details.
 * </p>
 * 
 * <p>
 *   Although this class works quite well when used in the test methods of a 
 *   {@link org.apache.cactus.JspTestCase JspTestCase}, it can also safely be
 *   used outside of the Cactus testing framework, for example when following
 *   a mock objects approach.
 * </p>
 * 
 * <h4>Testing Simple Tags</h4>
 * <p>
 *   This is how you would use this class when testing the
 *   <code>&lt;c:set&gt;</code>-tag of the JSTL reference implementation:
 *   <blockquote><pre>
  SetTag tag = new SetTag();
  JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
  tag.setVar("name");
  tag.setValue("value");
  lifecycle.invoke();
  assertEquals("value", pageContext.findAttribute("name"));</pre>
 *   </blockquote>
 *   The order is important:
 *   <ol>
 *     <li>
 *       Instantiation of the tag under test
 *     </li>
 *     <li>
 *       Instantiation of the lifecycle helper, passing in the page context and
 *       the tag instance
 *     </li>
 *     <li>
 *       Set the tag's attributes
 *     </li>
 *     <li>
 *       Start the tag's lifecycle by calling
 *       {@link #invoke() JspTagLifecycle.invoke()}
 *     </li>
 *     <li>
 *       Make assertions
 *     </li>
 *   </ol>
 * </p>
 * 
 * <h4>Adding Expectations to the Lifecycle</h4>
 * <p>
 *   <code>JspTagLifecycle</code> features a couple of methods that let you 
 *   easily add expectations about the tag's lifecycle to the test. For example,
 *   the method {@link #expectBodySkipped expectBodySkipped()} can be used to 
 *   verify that tag's body is not evaluated under the conditions set up by the
 *   test:
 *   <pre>
 * IfTag tag = new IfTag();
 * JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
 * tag.setTest("false");
 * lifecycle.expectBodySkipped();
 * lifecycle.invoke();</pre>
 * </p>
 * <p>
 *   An example of a more sophisticated expectationion is the
 *   {@link #expectScopedVariableExposed(String, Object[])}
 *   method, which can verify that a specific scoped variable gets exposed in
 *   the body of the tag, and that the exposed variable has a specific value in
 *   each iteration step:
 *   <pre>
 * ForEachTag tag = new ForEachTag();
 * JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
 * tag.setVar("item");
 * tag.setItems("One,Two,Three");
 * lifecycle.expectBodyEvaluated(3);
 * lifecycle.expectScopedVariableExposed(
 *     "item", new Object[] {"One", "Two", "Three"});
 * lifecycle.invoke();</pre>
 * </p>
 * 
 * <h4>Custom Expectations</h4>
 * <p>
 *   In some cases, using the expectations offered by 
 *   <code>JspTagLifecycle</code> does not suffice. In such cases, you need to 
 *   use custom expectations. You can add custom expectations by creating a 
 *   concrete subclass of the {@link JspTagLifecycle.Interceptor Interceptor}
 *   class, and adding it to the list of the tag lifecycles interceptors through
 *   {@link JspTagLifecycle#addInterceptor addInterceptor()}:
 *   <pre>
 * ForEachTag tag = new ForEachTag();
 * JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
 * tag.setVarStatus("status");
 * tag.setBegin("0");
 * tag.setEnd("2");
 * lifecycle.addInterceptor(new JspTagLifecycle.Interceptor() {
 *     public void evalBody(int theIteration, BodyContent theBody) {
 *         LoopTagStatus status = (LoopTagStatus)
 *             pageContext.findAttribute("status");
 *         assertNotNull(status);
 *         if (theIteration == 0) {
 *             assertTrue(status.isFirst());
 *             assertFalse(status.isLast());
 *         }
 *         else if (theIteration == 1) {
 *             assertFalse(status.isFirst());
 *             assertFalse(status.isLast());
 *         }
 *         else if (theIteration == 2) {
 *             assertFalse(status.isFirst());
 *             assertTrue(status.isLast());
 *         }
 *     }
 * });
 * lifecycle.invoke();</pre>
 * </p>
 * 
 * <h4>Specifying Nested Content</h4>
 * <p>
 *   <code>JspTagLifecycle</code> let's you add nested tempate text as well as 
 *   nested tags to the tag under test. The most important use of this feature 
 *   is testing of collaboration between tags, but it also allows you to easily
 *   check whether a tag correctly handles its body content.
 * </p>
 * <p>
 *   The following example demonstrates how to add nested template text to the 
 *   tag, and how to assert that the body was written to the HTTP response on
 *   the client side:
 *   <pre>
 * public void testOutTagDefaultBody() throws JspException, IOException {
 *     OutTag tag = new OutTag();
 *     JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
 *     tag.setValue(null);
 *     lifecycle.addNestedText("Default");
 *     lifecycle.expectBodyEvaluated();
 *     lifecycle.invoke();
 * }
 * public void endOutTagDefaultBody(WebResponse theResponse) {
 *     String output = theResponse.getText();
 *     assertEquals("Default", output);
 * }</pre>
 * </p>
 * <p>
 *   In sophisticated tag libraries, there will be many cases where tags need 
 *   to collaborate with each other in some way. This is usually done by nesting
 *   such tags within eachother. <code>JspTagLifecycle</code> supports such 
 *   scenarios by allowing you to add nested tags to an existing tag lifecycle.
 *   The nested tags can than be decorated with expectations themselves, as you
 *   can see in the following example:
 *   <pre>
 * ChooseTag chooseTag = new ChooseTag();
 * JspTagLifecycle chooseLifecycle =
 *     new JspTagLifecycle(pageContext, chooseTag);
 * WhenTag whenTag = new WhenTag();
 * JspTagLifecycle whenLifecycle =
 *     chooseLifecycle.addNestedTag(whenTag);
 * whenTag.setTest("false");
 * whenLifecycle.expectBodySkipped();
 * OtherwiseTag otherwiseTag = new OtherwiseTag();
 * JspTagLifecycle otherwiseLifecycle =
 *     chooseLifecycle.addNestedTag(otherwiseTag);
 * otherwiseLifecycle.expectBodyEvaluated();
 * chooseLifecycle.invoke();</pre>
 *   The code above creates a constellation of tags equivalent to the following
 *   JSP fragment:
 *   <pre>
 * &lt;c:choose&gt;
 *  &lt;c:when test='false'&gt;
 *   &lt;%-- body content not significant for the test --%&gt;
 *  &lt;/c:when&gt;
 *  &lt;c:otherwise&gt;
 *   &lt;%-- body content not significant for the test --%&gt;
 *  &lt;/c:otherwise&gt;
 * &lt;/c:choose&gt;</pre>
 * </p>
 * 
 * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
 * @since Cactus 1.5
 * 
 * @version $Id$
 * @see org.apache.cactus.JspTestCase
 */
public final class JspTagLifecycle
{   
    // Inner Classes -----------------------------------------------------------
    
    /**
     * Abstract class for intercepting the tag lifecycle. You can override any
     * of the methods to insert expectations about the tag's behaviour while it
     * is being executed.
     * 
     * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
     * @since Cactus 1.5
     */
    public abstract static class Interceptor
    {
        
        /**
         * Method called when the body of the tag would be evaluated. Can be
         * used in specific test cases to perform assertions.
         * 
         * Please note that if you're testing a <code>BodyTag</code>, you
         * should not write content to the
         * {@link org.apache.cactus.JspTestCase#out} instance variable while 
         * the body is being evaluated. This is because the actual implicit
         * object <code>out</code> in JSP pages gets replaced by the current 
         * nested <code>BodyContent</code>, whereas in <code>JspTestCase</code>
         * the <code>out</code> variable always refers to the top level
         * <code>JspWriter</code>. Instead, simply use the 
         * <code>BodyContent</code> parameter passed into the
         * {@link JspTagLifecycle.Interceptor#evalBody evalBody()} method or 
         * the <code>JspWriter</code> retrieved by a call to 
         * {javax.servlet.jsp.PageContext#getOut pageContext.getOut()}. 
         * 
         * @param theIteration The number of times the body has been evaluated
         * @param theBody The body content, or <tt>null</tt> if the tag isn't a
         *        <tt>BodyTag</tt>
         * @throws JspException If thrown by a nested tag
         * @throws IOException If an error occurs when reading or writing the
         *         body content
         */
        public void evalBody(int theIteration, BodyContent theBody)
            throws JspException, IOException
        {
            // default implementation does nothing
        }
        
        /**
         * Method called when the body of the tag would be skipped. Can be used 
         * in specific test cases to perform assertions.
         */
        public void skipBody()
        {
            // default implementation does nothing
        }
        
    }
    
    /**
     * A specialized interceptor that verifies that the tag's body is evaluated
     * at least once.
     * 
     * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
     * @since Cactus 1.5
     */
    private static class ExpectBodyEvaluatedInterceptor
        extends Interceptor
    {
        /**
         * The actual number of times the tag's body has been evaluated.
         */
        private int actualNumIterations;
        
        /**
         * The number of times the tag's body is expected to be evaluated.
         */
        private int expectedNumIterations;
        
        /**
         * Constructor.
         * 
         * @param theNumIterations The number of iterations expected
         */
        public ExpectBodyEvaluatedInterceptor(int theNumIterations)
        {
            this.expectedNumIterations = theNumIterations;
        }
        
        /**
         * Overridden to assert that the body doesn't get evaluated more often
         * than expected.
         * 
         * @see JspTagLifecycle.Interceptor#evalBody(int,BodyContent)
         */
        public void evalBody(int theIteration, BodyContent theBody)
        {
            actualNumIterations++;
            if (actualNumIterations > expectedNumIterations)
            {
                Assert.fail("Expected " + expectedNumIterations
                    + " iterations, but was " + actualNumIterations);
            }
        }
        
        /**
         * Overridden to assert that the body got evaluated as often as
         * expected.
         */
        public void skipBody()
        {
            if (actualNumIterations < expectedNumIterations)
            {
                Assert.fail("Expected " + expectedNumIterations
                    + " iterations, but was " + actualNumIterations);
            }
        }
    }
    
    /**
     * A specialized interceptor that asserts that the tag's body is skipped.
     * 
     * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
     * @since Cactus 1.5
     */
    private static class ExpectBodySkippedInterceptor
        extends Interceptor
    {
        /**
         * Overridden to assert that the body doesn't get evaluated.
         * 
         * @see JspTagLifecycle.Interceptor#evalBody(int,BodyContent)
         */
        public void evalBody(int theIteration, BodyContent theBody)
        {
            Assert.fail("Tag body should have been skipped");
        }
    }
    
    /**
     * A specialized interceptor that checks whether a specific scoped variable
     * is exposed in the body of the tag with a specific value.
     * 
     * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
     * @since Cactus 1.5
     */
    private class ExpectScopedVariableExposedInterceptor
        extends Interceptor
    {
        /**
         * The name of the scoped variable.
         */
        private String name;
        
        /**
         * The list of expected values of the variable.
         */
        private Object[] expectedValues;
        
        /**
         * The scope in which the variable is stored.
         */
        private int scope;
        
        /**
         * Constructor.
         * 
         * @param theName The name of the scoped variable to check for
         * @param theExpectedValues An array containing the expected values, 
         *        one item for every iteration step
         * @param theScope The scope to search for the scoped variable
         */
        public ExpectScopedVariableExposedInterceptor(String theName,
            Object[] theExpectedValues, int theScope)
        {
            this.name = theName;
            this.expectedValues = theExpectedValues;
            this.scope = theScope;
        }
        
        /**
         * Overridden to assert that the scoped variable is exposed as expected.
         * 
         * @see JspTagLifecycle.Interceptor#evalBody(int,BodyContent)
         */
        public void evalBody(int theIteration, BodyContent theBody)
        {
            Assert.assertEquals(expectedValues[theIteration],
                pageContext.getAttribute(name, scope));
        }
    }
    
    /**
     * A specialized interceptor that invokes the lifecycle of a nested tag.
     * 
     * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
     * @since Cactus 1.5
     */
    private class NestedTagInterceptor
        extends Interceptor
    {
        /**
         * The lifecycle object of the nested tag.
         */
        private JspTagLifecycle lifecycle;
        
        /**
         * Constructor.
         * 
         * @param theLifecycle The lifecycle instance associated with the nested
         *        tag
         */
        public NestedTagInterceptor(JspTagLifecycle theLifecycle)
        {
            this.lifecycle = theLifecycle;
        }
        
        /**
         * Overridden to invoke the lifecycle of the nested tag.
         * 
         * @see JspTagLifecycle.Interceptor#evalBody(int,BodyContent)
         */
        public void evalBody(int theIteration, BodyContent theBody)
            throws JspException, IOException
        {
            lifecycle.invoke();
        }
    }
    
    /**
     * A specialized interceptor that prints nested template text when the tag's
     * body is evaluated.
     * 
     * @author <a href="mailto:cmlenz@apache.org">Christopher Lenz</a>
     * @since Cactus 1.5
     */
    private class NestedTextInterceptor
        extends Interceptor
    {
        /**
         * The nested text.
         */
        private String text;
        
        /**
         * Constructor.
         * 
         * @param theText The nested text
         */
        public NestedTextInterceptor(String theText)
        {
            this.text = theText;
        }
        
        /**
         * Overridden to write the nested text to the current writer.
         * 
         * @see JspTagLifecycle.Interceptor#evalBody(int,BodyContent)
         */
        public void evalBody(int theIteration, BodyContent theBody)
            throws IOException
        {
            if (theBody != null)
            {
                theBody.print(text);
            }
            else
            {
                pageContext.getOut().print(text);
            }
        }
    }
    
    // Class Variables ---------------------------------------------------------
    
    /**
     * The log target.
     */
    private static Log log = LogFactory.getLog(JspTagLifecycle.class);
    
    // Instance Variables ------------------------------------------------------
    
    /**
     * The JSP page context.
     */
    protected PageContext pageContext;
    
    /**
     * The JSP tag handler.
     */
    private Tag tag;
        
    /**
     * The interceptor chain.
     */
    private List interceptors;
    
    // Constructors ------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @param thePageContext The JSP page context
     * @param theTag The JSP tag
     */
    public JspTagLifecycle(PageContext thePageContext, Tag theTag)
    {
        if ((thePageContext == null) || (theTag == null))
        {
            throw new NullPointerException();
        }
        this.tag = theTag;
        this.pageContext = thePageContext;
        this.tag.setPageContext(this.pageContext);
    }
    
    // Public Methods ----------------------------------------------------------
    
    /**
     * Adds an interceptor to the interceptor chain.
     * 
     * @param theInterceptor The interceptor to add
     */
    public void addInterceptor(Interceptor theInterceptor)
    {
        if (theInterceptor == null)
        {
            throw new NullPointerException();
        }
        if (this.interceptors == null)
        {
            this.interceptors = new ArrayList();
        }
        this.interceptors.add(theInterceptor);
    }
    
    /**
     * Adds a nested tag. The tag will be invoked when the body content of the
     * enclosing tag is evaluated.
     * 
     * @return The lifecycle wrapper for the nested tag, can be used to add 
     *         expectations for the nested tag
     * @param theNestedTag The tag to be nested
     */
    public JspTagLifecycle addNestedTag(Tag theNestedTag)
    {
        if (theNestedTag == null)
        {
            throw new NullPointerException();
        }
        JspTagLifecycle lifecycle =
            new JspTagLifecycle(this.pageContext, theNestedTag);
        theNestedTag.setParent(this.tag);
        addInterceptor(new NestedTagInterceptor(lifecycle));
        return lifecycle;
    }
    
    /**
     * Adds template text to nest inside the tag. The text will be printed to 
     * the body content when it is evaluated.
     * 
     * @param theNestedText The string containing the template text
     */
    public void addNestedText(String theNestedText)
    {
        if (theNestedText == null)
        {
            throw new NullPointerException();
        }
        addInterceptor(new NestedTextInterceptor(theNestedText));
    }
    
    /**
     * Adds the expectation that the tag body must be evaluated once in the 
     * course of the tags lifecycle.
     */
    public void expectBodyEvaluated()
    {
        addInterceptor(new ExpectBodyEvaluatedInterceptor(1));
    }
    
    /**
     * Adds the expectation that the tag body must be evaluated a specific
     * number of times in the course of the tags lifecycle.
     * 
     * @param theNumIterations The number of times the body is expected to get 
     *        evaluated
     */
    public void expectBodyEvaluated(int theNumIterations)
    {
        addInterceptor(new ExpectBodyEvaluatedInterceptor(theNumIterations));
    }
    
    /**
     * Adds the expectation that the tag body must be skipped. Essentially, this
     * expectation verifies that the tag returns <code>SKIP_BODY</code> from
     * <code>doStartTag()</code>.
     */
    public void expectBodySkipped()
    {
        addInterceptor(new ExpectBodySkippedInterceptor());
    }
    
    /**
     * Adds a special expectation that verifies that a specific scoped variable
     * is exposed in the body of the tag.
     * 
     * @param theName The name of the variable
     * @param theExpectedValues An ordered list containing the expected values 
     *        values of the scoped variable, one for each expected iteration
     *        step
     */
    public void expectScopedVariableExposed(String theName,
        Object[] theExpectedValues)
    {
        expectScopedVariableExposed(theName, theExpectedValues,
            PageContext.PAGE_SCOPE);
    }
    
    /**
     * Adds a special expectation that verifies that a specific scoped variable
     * is exposed in the body of the tag.
     * 
     * @param theName The name of the variable
     * @param theExpectedValues An ordered list containing the expected values 
     *        values of the scoped variable, one for each expected iteration
     *        step
     * @param theScope The scope under which the variable is stored
     */
    public void expectScopedVariableExposed(String theName,
        Object[] theExpectedValues, int theScope)
    {
        if ((theName == null) || (theExpectedValues == null))
        {
            throw new NullPointerException();
        }
        if (theExpectedValues.length == 0)
        {
            throw new IllegalArgumentException();
        }
        if ((theScope != PageContext.PAGE_SCOPE)
         && (theScope != PageContext.REQUEST_SCOPE)
         && (theScope != PageContext.SESSION_SCOPE)
         && (theScope != PageContext.APPLICATION_SCOPE))
        {
            throw new IllegalArgumentException();
        }
        addInterceptor(
            new ExpectScopedVariableExposedInterceptor(theName,
                theExpectedValues, theScope));
    }
    
    /**
     * Invokes the tag with the provided interceptor. The tag should have been
     * populated with its properties before calling this method. The tag is not
     * released after the tag's lifecycle is over.
     * 
     * @throws JspException If the tag throws an exception
     * @throws IOException If an error occurs when reading or writing the body
     *         content
     */
    public void invoke() throws JspException, IOException
    {
        if (this.tag instanceof TryCatchFinally)
        {
            TryCatchFinally tryCatchFinally = (TryCatchFinally) this.tag;
            try
            {
                invokeInternal();
            }
            catch (Throwable t1)
            {
                try
                {
                    tryCatchFinally.doCatch(t1);
                }
                catch (Throwable t2)
                {
                    throw new JspException(t2.getMessage());
                }
            }
            finally
            {
                tryCatchFinally.doFinally();
            }
        }
        else
        {
            invokeInternal();
        }
    }
    
    // Private Methods ---------------------------------------------------------
    
    /**
     * Notify all interceptors about a body evaluation.
     * 
     * @param theIteration The iteration
     * @param theBody The body content
     * @throws JspException If thrown by a nested tag
     * @throws IOException If an error occurs when reading or writing the body
     *         content
     */
    private void fireEvalBody(int theIteration, BodyContent theBody)
        throws JspException, IOException
    {
        if (this.interceptors != null)
        {
            for (Iterator i = this.interceptors.iterator(); i.hasNext();)
            {
                ((Interceptor) i.next()).evalBody(theIteration, theBody);
            }
        }
    }
    
    /**
     * Notify all interceptors that the body has been skipped.
     */
    private void fireSkipBody()
    {
        if (this.interceptors != null)
        {
            for (Iterator i = this.interceptors.iterator(); i.hasNext();)
            {
                ((Interceptor) i.next()).skipBody();
            }
        }
    }
    
    /**
     * Internal method to invoke a tag without doing exception handling.
     * 
     * @throws JspException If the tag throws an exception
     * @throws IOException If an error occurs when reading or writing the body
     *         content
     */
    private void invokeInternal()
        throws JspException, IOException
    {
        int status = this.tag.doStartTag();
        if (this.tag instanceof IterationTag)
        {
            if (status != Tag.SKIP_BODY)
            {
                BodyContent body = null;
                try
                {
                    IterationTag iterationTag = (IterationTag) this.tag;
                    if ((status == BodyTag.EVAL_BODY_BUFFERED)
                        && (this.tag instanceof BodyTag))
                    {
                        BodyTag bodyTag = (BodyTag) this.tag;
                        body = pageContext.pushBody();
                        if (log.isDebugEnabled())
                        {
                            log.debug("Pushed body content ["
                                + body.getString() + "]");
                        }
                        bodyTag.setBodyContent(body);
                        bodyTag.doInitBody();
                    }
                    int iteration = 0;
                    do
                    {
                        fireEvalBody(iteration, body);
                        if (log.isDebugEnabled())
                        {
                            log.debug("Body evaluated for the ["
                                + iteration + "] time");
                        }
                        status = iterationTag.doAfterBody();
                        iteration++;
                    } while (status == IterationTag.EVAL_BODY_AGAIN);
                    if (log.isDebugEnabled())
                    {
                        log.debug("Body skipped");
                    }
                    fireSkipBody();
                }
                finally
                {
                    if (body != null)
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Popping body content ["
                                + body.getString() + "]");
                        }
                        pageContext.popBody();
                        body = null;
                    }
                }
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Body skipped");
                }
                fireSkipBody();
            }
        }
        status = tag.doEndTag();
    }
    
}
