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
package org.apache.cactus.eclipse.quickfix;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

public class CactusQuickFixProcessor implements IQuickFixProcessor {

	private static final int IMPORT_NOT_FOUND = 268435846;
	
	private static final int IS_CLASSPATH_CORRECT = 16777218;
	
	private static final int UNDEFINED_TYPE = 16777218;

	public boolean hasCorrections(final ICompilationUnit unit, final int problemId) {
		if (problemId == 1000) {
			return true;
		}
		return false;
	}

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

	private void process(
			final IInvocationContext context,
			final IProblemLocation problem,
			final ArrayList proposals) {
		if (problem.getProblemId() == 0) { // no proposals for none-problem locations
			return;
		}
		
	    final String source;
	    try {
	      source = context.getCompilationUnit().getSource();
	    }
	    catch (final JavaModelException e) {
	      //ignore
	      return;
	    }
	    final int offset = problem.getOffset();
	    final int length = problem.getLength();

	    final String substring = source.substring(offset, offset + length);
	    
	    
	    
	    IJavaProject theWorkingProject = context.getCompilationUnit().getJavaProject();
	    
	    boolean cactusProblem = (problem.getProblemId() == IMPORT_NOT_FOUND && substring.startsWith("org.apache.cactus")) ||
	    						(problem.getProblemId() == IS_CLASSPATH_CORRECT && isCactusPrefixesMatch(substring));
	    
		if(cactusProblem) {
			final String name = "Cactify this project...";
			proposals.add(new AddCactusClassesCompletionProposal(name, 1, theWorkingProject));
		}
	}
	
	private boolean isCactusPrefixesMatch(String prefix) {
		return (prefix.startsWith("ServletTestCase") || 
				prefix.startsWith("JspTestCase") || 
				prefix.startsWith("EJBTestCase") || 
				prefix.startsWith("JettyTestSetup"));
	}
}