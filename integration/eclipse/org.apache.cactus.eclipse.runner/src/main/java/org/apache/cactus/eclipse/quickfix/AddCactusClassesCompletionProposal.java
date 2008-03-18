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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.cactus.eclipse.runner.ui.CactifyActionDelegate;
import org.apache.cactus.eclipse.runner.ui.CactusPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class AddCactusClassesCompletionProposal  implements IJavaCompletionProposal  {

	private final String name;
	private final String proposalInfo;
	private final int relevance;
	private IJavaProject project;
  
	public AddCactusClassesCompletionProposal(
			String name,
			int relevance, 
			IJavaProject theWorkingProject) {
		this.name = name;
		this.relevance = relevance;
		this.project = theWorkingProject;

		final StringBuffer buffer = new StringBuffer(80);
		buffer.append("Cactify this project ...<br>" +
						"Add the Cactus jars to the classpath <br/>");
		this.proposalInfo = buffer.toString();
	}

	public int getRelevance() {
		return relevance;
	}

	public void apply(IDocument document) {
		CactifyActionDelegate cactifyDelegate = new CactifyActionDelegate();
		cactifyDelegate.setSelectedProject(project);
		//We add null and empty string because CactifyActionDelegate does not need these.
		cactifyDelegate.run(null);
	}

	public Point getSelection(IDocument document) {
		return null;
	}

	public String getAdditionalProposalInfo() {
		return proposalInfo;
	}

	public String getDisplayString() {
		return name;
	}

	public Image getImage() {
		return CactusPlugin.getDefault().getImageRegistry().get(CactusPlugin.CACTUS_RUN_IMAGE);
	}

	public IContextInformation getContextInformation() {
		return null;
	}
  
	public IJavaCompletionProposal createTypeProposal() {
		return null;
	}
}

