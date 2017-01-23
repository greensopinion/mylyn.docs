/*******************************************************************************
 * Copyright (c) 2015 Stephan Wahlbrink and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.markdown.tests;

import java.io.StringWriter;
import java.util.List;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.Block;
import org.eclipse.mylyn.wikitext.markdown.core.MarkdownLanguage;

import junit.framework.TestCase;

/**
 * Tests without paragraph breaking blocks.
 * <p>
 * Paragraph breaking blocks are disabled by default in some dialects, e.g. Pandoc.
 */
public class MarkdownLanguageParagraphBreakingBlocksDisabledTest extends TestCase {

	private static class Language extends MarkdownLanguage {

		@Override
		protected void addBlockExtensions(List<Block> blocks, List<Block> paragraphBreakingBlocks) {
			super.addBlockExtensions(blocks, paragraphBreakingBlocks);

			paragraphBreakingBlocks.clear();
		}

	}

	private MarkupParser parser;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		parser = new MarkupParser(new Language());
	}

	public String parseToHtml(String markup) {
		StringWriter out = new StringWriter();
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder(out);
		builder.setEmitAsDocument(false);
		parser.setBuilder(builder);
		parser.parse(markup);
		return out.toString();
	}

	public void parseAndAssert(String markup, String expectedHtml) {
		String html = parseToHtml(markup);
		
		assertEquals(expectedHtml, html);
	}

	public void testParagraphsBrokenByHorizontalRuleBlock() {
		String markup = "a paragraph\nfollowed by a horizontal rule\n---";
		String expectedHtml = "<p>a paragraph\nfollowed by a horizontal rule\n---</p>";
		parseAndAssert(markup, expectedHtml);
	}

	public void testParagraphsBrokenByHeadingBlock() {
		String markup = "a paragraph\n# A header";
		String expectedHtml = "<p>a paragraph\n# A header</p>";
		parseAndAssert(markup, expectedHtml);
	}

	public void testParagraphsBrokenByQuoteBlock() {
		String markup = "a paragraph\n> a quote block paragraph";
		String expectedHtml = "<p>a paragraph\n&gt; a quote block paragraph</p>";
		parseAndAssert(markup, expectedHtml);
	}

	public void testParagraphsBrokenByUListBlock() {
		String markup = "a paragraph\n- a list item";
		String expectedHtml = "<p>a paragraph\n- a list item</p>";
		parseAndAssert(markup, expectedHtml);
	}

	public void testParagraphsBrokenByOListBlock() {
		String markup = "a paragraph\n1. a list item";
		String expectedHtml = "<p>a paragraph\n1. a list item</p>";
		parseAndAssert(markup, expectedHtml);
	}

}
