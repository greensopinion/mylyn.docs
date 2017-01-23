/*******************************************************************************
 * Copyright (c) 2013, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.html.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Collections;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentHandler;
import org.eclipse.mylyn.wikitext.core.util.XmlStreamWriter;
import org.eclipse.mylyn.wikitext.html.core.HtmlLanguage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class HtmlSubsetLanguageTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void createNullName() {
		thrown.expect(NullPointerException.class);
		new HtmlSubsetLanguage(null, null, 6, ImmutableSet.of(BlockType.PARAGRAPH), ImmutableSet.of(SpanType.BOLD),
				ImmutableMap.<SpanType, String> of(), Collections.<SpanHtmlElementStrategy> emptyList());
	}

	@Test
	public void createNullBlockTypes() {
		thrown.expect(NullPointerException.class);
		new HtmlSubsetLanguage("Test", null, 6, null, ImmutableSet.of(SpanType.BOLD),
				ImmutableMap.<SpanType, String> of(), Collections.<SpanHtmlElementStrategy> emptyList());
	}

	@Test
	public void createNullSpanTypes() {
		thrown.expect(NullPointerException.class);
		new HtmlSubsetLanguage("Test", null, 6, ImmutableSet.of(BlockType.PARAGRAPH), null,
				ImmutableMap.<SpanType, String> of(), Collections.<SpanHtmlElementStrategy> emptyList());
	}

	@Test
	public void createNullTagNameSubstitutions() {
		thrown.expect(NullPointerException.class);
		new HtmlSubsetLanguage("Test", null, 6, ImmutableSet.of(BlockType.PARAGRAPH), ImmutableSet.of(SpanType.BOLD),
				null, Collections.<SpanHtmlElementStrategy> emptyList());
	}

	@Test
	public void createInvalidHeadingLevel() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("headingLevel must be between 0 and 6");
		new HtmlSubsetLanguage("Test", null, -1, ImmutableSet.of(BlockType.PARAGRAPH), ImmutableSet.of(SpanType.BOLD),
				ImmutableMap.<SpanType, String> of(), Collections.<SpanHtmlElementStrategy> emptyList());
	}

	@Test
	public void createWithUnsupportedSubstituted() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("SpanType [ITALIC] is unsupported. Cannot add substitution to unsupported span types.");
		new HtmlSubsetLanguage("Test", null, 6, ImmutableSet.of(BlockType.PARAGRAPH), ImmutableSet.of(SpanType.BOLD),
				ImmutableMap.of(SpanType.ITALIC, "italic"), Collections.<SpanHtmlElementStrategy> emptyList());
	}

	@Test
	public void create() {
		HtmlSubsetLanguage language = newHtmlSubsetLanguage(BlockType.PARAGRAPH);
		assertEquals("Test", language.getName());
	}

	@Test
	public void supportedBlockTypes() {
		assertEquals(Sets.newHashSet(BlockType.PARAGRAPH, BlockType.CODE),
				newHtmlSubsetLanguage(BlockType.PARAGRAPH, BlockType.CODE).getSupportedBlockTypes());
	}

	@Test
	public void supportedSpanTypes() {
		assertEquals(Sets.newHashSet(SpanType.BOLD, SpanType.EMPHASIS),
				newHtmlSubsetLanguage(SpanType.BOLD, SpanType.EMPHASIS).getSupportedSpanTypes());
	}

	@Test
	public void tagNameSubstitutions() {
		assertEquals(ImmutableMap.of(SpanType.EMPHASIS, "new-em"),
				newHtmlSubsetLanguage(SpanType.EMPHASIS).getTagNameSubstitutions());
	}

	@Test
	public void supportedHeadingLevel() {
		assertSupportedHeadingLevel(0);
		assertSupportedHeadingLevel(1);
		assertSupportedHeadingLevel(5);
		assertSupportedHeadingLevel(6);
		assertEquals(0, newHtmlSubsetLanguageWithHeadingLevel(0).getSupportedHeadingLevel());
	}

	@Test
	public void cloneSupported() {
		HtmlDocumentHandler documentHandler = new HtmlDocumentHandler() {

			@Override
			public void endDocument(HtmlDocumentBuilder builder, XmlStreamWriter writer) {
				// ignore
			}

			@Override
			public void beginDocument(HtmlDocumentBuilder builder, XmlStreamWriter writer) {
				// ignore
			}
		};
		HtmlSubsetLanguage language = new HtmlSubsetLanguage("Test", documentHandler, 6,
				Sets.newHashSet(BlockType.PARAGRAPH, BlockType.DIV, BlockType.QUOTE),
				Sets.newHashSet(SpanType.CITATION, SpanType.EMPHASIS), ImmutableMap.of(SpanType.EMPHASIS, "new-em"),
				Collections.<SpanHtmlElementStrategy> emptyList());
		HtmlSubsetLanguage cloned = language.clone();

		assertEquals(language.getName(), cloned.getName());
		assertEquals(language.getSupportedBlockTypes(), cloned.getSupportedBlockTypes());
		assertEquals(language.getSupportedHeadingLevel(), cloned.getSupportedHeadingLevel());
		assertEquals(language.getSupportedSpanTypes(), cloned.getSupportedSpanTypes());
		assertEquals(language.getTagNameSubstitutions(), cloned.getTagNameSubstitutions());

	}

	@Test
	public void parseCleansHtmlSetOnClone() {
		HtmlLanguage htmlLanguage = newHtmlSubsetLanguage(BlockType.PARAGRAPH);
		htmlLanguage.setParseCleansHtml(true);
		assertEquals(htmlLanguage.isParseCleansHtml(), htmlLanguage.clone().isParseCleansHtml());
		htmlLanguage.setParseCleansHtml(false);
		assertEquals(htmlLanguage.isParseCleansHtml(), htmlLanguage.clone().isParseCleansHtml());
	}

	@Test
	public void createDocumentBuilder() {
		StringWriter out = new StringWriter();
		HtmlSubsetDocumentBuilder builder = newHtmlSubsetLanguage(BlockType.PARAGRAPH).createDocumentBuilder(out,
				false);
		assertNotNull(builder);
		assertTrue(builder instanceof HtmlSubsetDocumentBuilder);

		builder = newHtmlSubsetLanguage(SpanType.EMPHASIS).createDocumentBuilder(out, false);
		builder.beginSpan(SpanType.EMPHASIS, new Attributes());
		builder.characters("text");
		builder.endSpan();
		assertEquals("<new-em>text</new-em>", out.toString());
	}

	@Test
	public void isXhtmlStrict() {
		assertXhtmlStrict(true);
		assertXhtmlStrict(false);
	}

	private void assertXhtmlStrict(boolean xhtmlStrict) {
		HtmlSubsetLanguage language = createHtmlSubsetLanguage(xhtmlStrict);
		assertEquals(xhtmlStrict, language.isXhtmlStrict());
		assertEquals(xhtmlStrict, language.clone().isXhtmlStrict());
		HtmlSubsetDocumentBuilder documentBuilder = language.createDocumentBuilder(new StringWriter(), false);
		assertEquals(xhtmlStrict, documentBuilder.getDelegate().isXhtmlStrict());
	}

	private HtmlSubsetLanguage createHtmlSubsetLanguage(boolean xhtmlStrict) {
		return new HtmlSubsetLanguage("Test", null, 6, ImmutableSet.<BlockType> of(BlockType.PARAGRAPH),
				ImmutableSet.<SpanType> of(), ImmutableMap.<SpanType, String> of(),
				Collections.<SpanHtmlElementStrategy> emptyList(), xhtmlStrict);
	}

	private void assertSupportedHeadingLevel(int level) {
		assertEquals(level, newHtmlSubsetLanguageWithHeadingLevel(level).getSupportedHeadingLevel());
	}

	private HtmlSubsetLanguage newHtmlSubsetLanguageWithHeadingLevel(int level) {
		return new HtmlSubsetLanguage("Test", null, level, Sets.newHashSet(BlockType.PARAGRAPH),
				ImmutableSet.<SpanType> of(), ImmutableMap.<SpanType, String> of(),
				Collections.<SpanHtmlElementStrategy> emptyList());
	}

	protected HtmlSubsetLanguage newHtmlSubsetLanguage(SpanType... spans) {
		return new HtmlSubsetLanguage("Test", null, 6, Sets.newHashSet(BlockType.PARAGRAPH), Sets.newHashSet(spans),
				ImmutableMap.of(SpanType.EMPHASIS, "new-em"), Collections.<SpanHtmlElementStrategy> emptyList());
	}

	protected HtmlSubsetLanguage newHtmlSubsetLanguage(BlockType... blocks) {
		return new HtmlSubsetLanguage("Test", null, 6, Sets.newHashSet(blocks), ImmutableSet.<SpanType> of(),
				ImmutableMap.<SpanType, String> of(), Collections.<SpanHtmlElementStrategy> emptyList());
	}

}
