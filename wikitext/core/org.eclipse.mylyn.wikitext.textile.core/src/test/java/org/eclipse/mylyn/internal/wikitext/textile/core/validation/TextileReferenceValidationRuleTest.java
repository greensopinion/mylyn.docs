/*******************************************************************************
 * Copyright (c) 2007, 2010 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.textile.core.validation;

import java.util.List;

import org.eclipse.mylyn.wikitext.core.validation.MarkupValidator;
import org.eclipse.mylyn.wikitext.core.validation.ValidationProblem;

import junit.framework.TestCase;

public class TextileReferenceValidationRuleTest extends TestCase {

	private TextileReferenceValidationRule rule;

	private MarkupValidator validator;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		rule = new TextileReferenceValidationRule();

		validator = new MarkupValidator();
		validator.getRules().add(rule);
	}

	public void testNoErrors() {
		final String markup = "h1. Title\n\n\"a link\":#Title";
		List<ValidationProblem> problems = rule.findProblems(markup, 0, markup.length());
		
		assertNotNull(problems);
		assertTrue(problems.isEmpty());
	}

	public void testErrors() {
		final String markup = "h1. Title\n\nsome text \"a link\":#BADTitle more text";
		List<ValidationProblem> problems = rule.findProblems(markup, 0, markup.length());
		
		assertNotNull(problems);
		assertEquals(1, problems.size());
		assertEquals(21, problems.get(0).getOffset());
		assertEquals(18, problems.get(0).getLength());
	}

	public void testFootnoteReference() {
		String markup = "some text with a footnote reference[1]\n\nfn1. a footnote";
		List<ValidationProblem> problems = rule.findProblems(markup, 0, markup.length());
		
		assertNotNull(problems);
		assertEquals(0, problems.size());
	}

	public void testFootnoteReferenceWithErrors() {
		String markup = "some text with a footnote reference[1]\n\nfn2. a footnote";
		List<ValidationProblem> problems = rule.findProblems(markup, 0, markup.length());
		
		assertNotNull(problems);
		assertEquals(1, problems.size());
		assertEquals(35, problems.get(0).getOffset());
		assertEquals(3, problems.get(0).getLength());
	}
}
