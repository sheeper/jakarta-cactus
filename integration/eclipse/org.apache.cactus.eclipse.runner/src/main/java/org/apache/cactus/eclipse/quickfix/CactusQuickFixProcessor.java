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
package org.apache.cactus.eclipse.quickfix;

import java.util.ArrayList;

import org.apache.cactus.eclipse.runner.ui.CactusMessages;
import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
/**
 * QuickFix processor to propose cactifying of the project.
 * 
 * @version $Id: CactusQuickFixProcessor.java 238816 2008-03-18 16:36:46Z ptahchiev $
 */
public class CactusQuickFixProcessor implements IQuickFixProcessor {
    /**
     * Constant for error code when the import is not found
     */
	private static final int IMPORT_NOT_FOUND = 268435846;
    /**
     * Constant for error code when the classpath is not correct
     */
	private static final int IS_CLASSPATH_CORRECT = 16777218;
    /**
     * The default relevance constant.
     */
	private static final int DEFAULT_RELEVANCE = 90;
    /**
     * A method for getting only the corrections we need.
     */
	public boolean hasCorrections(final ICompilationUnit unit, final int problemId) {
		if (problemId == IMPORT_NOT_FOUND || problemId == IS_CLASSPATH_CORRECT) {
			return true;
		}
		return false;
	}

    /**
     * A method for getting the corrections.
     */
	public IJavaCompletionProposal[] getCorrections(
			final IInvocationContext context,
			final IProblemLocation[] locations) throws CoreException {
		if (locations == null || locations.length == 0) {
			return null;
		}
		final ArrayList resultingCollections = new ArrayList();
		for (int i=0;i< locations.length;i++) {
			IProblemLocation problemLocation = (IProblemLocation) locations[i];
			process(context, problemLocation, resultingCollections);
		}
		IJavaCompletionProposal[] proposals = new IJavaCompletionProposal[resultingCollections.size()];
    
		for(int i=0;i<resultingCollections.size();i++) {
			proposals[i] = (IJavaCompletionProposal) resultingCollections.get(i);
		}
		return proposals;
	}
	
    /**
     * Process when corrections found.
     */
	private void process(
			final IInvocationContext context,
			final IProblemLocation problem,
			final ArrayList proposals) {
		if (problem.getProblemId() == 0) { // no proposals
			return;
		}
		
	    final String source;
	    try {
	      source = context.getCompilationUnit().getSource();
	    }
	    catch (final JavaModelException e) {
	      CactusPlugin.log(e.getMessage());
	      return;
	    }
	    final int offset = problem.getOffset();
	    final int length = problem.getLength();

	    final String substring = source.substring(offset, offset + length);
	    
	    
	    
	    IJavaProject theWorkingProject = context.getCompilationUnit().getJavaProject();
	    
	    boolean cactusProblem = (problem.getProblemId() == IMPORT_NOT_FOUND && substring.startsWith("org.apache.cactus")) ||
	    						(problem.getProblemId() == IS_CLASSPATH_CORRECT && isCactusPrefixesMatch(substring));
	    
		if(cactusProblem) {
			final String name = CactusMessages.getString("Cactus.quickFix.name");
			proposals.add(new AddCactusClassesCompletionProposal(name, DEFAULT_RELEVANCE, theWorkingProject));
		}
	}
	
    /**
     * If the error prefix matches some of the Cactus 'keywords'
     * then we have to add the Cactus correction.
     */
	private boolean isCactusPrefixesMatch(String prefix) {
		return (prefix.startsWith("ServletTestCase") || 
				prefix.startsWith("JspTestCase") || 
				prefix.startsWith("EJBTestCase") || 
				prefix.startsWith("JettyTestSetup"));
	}
}