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

import org.apache.cactus.eclipse.runner.ui.CactifyActionDelegate;
import org.apache.cactus.eclipse.runner.ui.CactusMessages;
import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * CompletionProposal for adding the Cactus classes to the Project's classpath.
 * 
 * @version $Id: CactusQuickFixProcessor.java 238816 2008-03-18 16:36:46Z ptahchiev $
 */
public class AddCactusClassesCompletionProposal  implements IJavaCompletionProposal  {
    /**
     * The name of the proposal
     */
	private final String name;
    /**
     * The buffer for the proposal.
     */
	private final String proposalInfo;
    /**
     * The relevance of the proposal;
     */
	private final int relevance;
    /**
     * The project in which we have the error.
     */
	private IJavaProject project;
  
    /**
     * Constructor.
     */
	public AddCactusClassesCompletionProposal(
			String name,
			int relevance, 
			IJavaProject theWorkingProject) {
		this.name = name;
		this.relevance = relevance;
		this.project = theWorkingProject;

		final StringBuffer buffer = new StringBuffer(80);
		buffer.append(CactusMessages.getString("Cactus.quickFix.description"));
		this.proposalInfo = buffer.toString();
	}
	
    /**
     * Get the relevance of the proposal.
     */
	public int getRelevance() {
		return relevance;
	}
	
    /**
     * Apply the proposal. Actually we only call the CactifyActionDelegate
     * and he does the whole 'magic'.
     */
	public void apply(IDocument document) {
		CactifyActionDelegate cactifyDelegate = new CactifyActionDelegate();
		cactifyDelegate.setSelectedProject(project);
		//We add null and empty string because CactifyActionDelegate does not need these.
		cactifyDelegate.run(null);
	}
	
    /**
     * Get the selection.
     */
	public Point getSelection(IDocument document) {
		return null;
	}
	
    /**
     * Get the proposal info.
     */
	public String getAdditionalProposalInfo() {
		return proposalInfo;
	}
	
    /**
     * Get the display string.
     */
	public String getDisplayString() {
		return name;
	}
	
    /**
     * Get the icon image for the proposal.
     */
	public Image getImage() {
		return CactusPlugin.getDefault().getImageRegistry().get(CactusPlugin.CACTUS_RUN_IMAGE);
	}
    /**
     * Get the context information.
     */
	public IContextInformation getContextInformation() {
		return null;
	}
    /**
     * Create a type-proposal.
     */
	public IJavaCompletionProposal createTypeProposal() {
		return null;
	}
}

