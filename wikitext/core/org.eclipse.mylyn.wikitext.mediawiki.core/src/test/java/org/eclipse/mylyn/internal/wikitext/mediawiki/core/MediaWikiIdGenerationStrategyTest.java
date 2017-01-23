/*******************************************************************************
 * Copyright (c) 2007, 2010, 2015 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.mediawiki.core;

import junit.framework.TestCase;

public class MediaWikiIdGenerationStrategyTest extends TestCase {

	MediaWikiIdGenerationStrategy generationStrategy;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		generationStrategy = new MediaWikiIdGenerationStrategy();
	}

	public void testSimple() {
		assertEquals("Bugzilla_Connector", generationStrategy.generateId("Bugzilla Connector"));
		assertEquals("JIRA_Connector", generationStrategy.generateId("JIRA Connector"));
		assertEquals("Keyboard_mappings_on_Linux", generationStrategy.generateId("Keyboard mappings on Linux"));
		assertEquals("Alt.2BClick_navigation", generationStrategy.generateId("Alt+Click navigation"));
	}

	public void testWithDots() {
		assertEquals("com.foo.Bar", generationStrategy.generateId("com.foo.Bar"));
	}

	public void testHeadingTextToId() {
		//Bug 388657
		assertEquals("Anchor_Text.3F", generationStrategy.generateId("Anchor Text?"));
		assertEquals("This.2FSection", generationStrategy.generateId("This/Section"));
		assertEquals("C.23_Implementation", generationStrategy.generateId("C# Implementation"));
	}
}
