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

import java.util.Collections;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Sets;

public class SpanStrategiesTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void createNullElementTypes() {
		thrown.expect(NullPointerException.class);
		new SpanStrategies(null, Collections.<SpanHtmlElementStrategy> emptyList());
	}

	@Test
	public void createNullSpanStrategies() {
		thrown.expect(NullPointerException.class);
		new SpanStrategies(Collections.<SpanType> emptySet(), null);
	}

	@Test
	public void createEmpty() {
		SpanStrategies strategies = new SpanStrategies(Sets.<SpanType> newHashSet(),
				Collections.<SpanHtmlElementStrategy> emptyList());
		assertNotNull(strategies.getStrategy(SpanType.BOLD, new Attributes()));
	}

	@Test
	public void createNonEmpty() {
		SpanStrategies strategies = new SpanStrategies(Sets.newHashSet(SpanType.BOLD, SpanType.CODE),
				Collections.<SpanHtmlElementStrategy> emptyList());
		assertSupported(strategies, SpanType.BOLD);
		assertSupported(strategies, SpanType.CODE);
		for (SpanType spanType : SpanType.values()) {
			assertNotNull(strategies.getStrategy(spanType, new Attributes()));
		}
	}

	@Test
	public void alternatives() {
		SpanStrategies strategies = new SpanStrategies(Sets.newHashSet(SpanType.BOLD),
				Collections.<SpanHtmlElementStrategy> emptyList());
		assertTrue(strategies.getStrategy(SpanType.STRONG, new Attributes()) instanceof SubstitutionSpanStrategy);
	}

	@Test
	public void spanWithFontWeightToBold() {
		SpanStrategies strategies = new SpanStrategies(Sets.newHashSet(SpanType.BOLD),
				Collections.<SpanHtmlElementStrategy> emptyList());
		SpanStrategy strategy = strategies.getStrategy(SpanType.SPAN,
				new Attributes(null, null, "font-weight:  bold", null));
		assertTrue(strategy instanceof SubstitutionWithoutCssSpanStrategy);
		assertEquals(SpanType.BOLD, ((SubstitutionWithoutCssSpanStrategy) strategy).getType());
	}

	@Test
	public void spanWithFontWeightToStrong() {
		SpanStrategies strategies = new SpanStrategies(Sets.newHashSet(SpanType.STRONG),
				Collections.<SpanHtmlElementStrategy> emptyList());
		SpanStrategy strategy = strategies.getStrategy(SpanType.SPAN,
				new Attributes(null, null, "font-weight:  bold", null));
		assertTrue(strategy instanceof SubstitutionWithoutCssSpanStrategy);
		assertEquals(SpanType.STRONG, ((SubstitutionWithoutCssSpanStrategy) strategy).getType());
	}

	@Test
	public void spanWithTextDecorationUnderlineToUnderlined() {
		SpanStrategies strategies = new SpanStrategies(Sets.newHashSet(SpanType.UNDERLINED),
				Collections.<SpanHtmlElementStrategy> emptyList());
		SpanStrategy strategy = strategies.getStrategy(SpanType.SPAN,
				new Attributes(null, null, "text-decoration:  underline;", null));
		assertTrue(strategy instanceof SubstitutionWithoutCssSpanStrategy);
		assertEquals(SpanType.UNDERLINED, ((SubstitutionWithoutCssSpanStrategy) strategy).getType());
	}

	@Test
	public void spanWithTextDecorationLinethroughToDeleted() {
		SpanStrategies strategies = new SpanStrategies(Sets.newHashSet(SpanType.DELETED),
				Collections.<SpanHtmlElementStrategy> emptyList());
		SpanStrategy strategy = strategies.getStrategy(SpanType.SPAN,
				new Attributes(null, null, "text-decoration:  line-through;", null));
		assertTrue(strategy instanceof SubstitutionWithoutCssSpanStrategy);
		assertEquals(SpanType.DELETED, ((SubstitutionWithoutCssSpanStrategy) strategy).getType());
	}

	@Test
	public void spanWithUnrecognizedCssToUnsupported() {
		SpanStrategies strategies = new SpanStrategies(Sets.newHashSet(SpanType.BOLD),
				Collections.<SpanHtmlElementStrategy> emptyList());
		assertTrue(strategies.getStrategy(SpanType.SPAN,
				new Attributes(null, null, "font-weight:unknown", null)) instanceof UnsupportedSpanStrategy);
	}

	private void assertSupported(SpanStrategies strategies, SpanType spanType) {
		assertTrue(strategies.getStrategy(spanType, new Attributes()) instanceof SupportedSpanStrategy);
	}
}
