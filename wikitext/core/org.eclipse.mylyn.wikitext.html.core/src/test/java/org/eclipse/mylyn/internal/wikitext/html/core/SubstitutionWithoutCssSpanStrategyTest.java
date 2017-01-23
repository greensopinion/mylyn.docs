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

import java.util.List;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.core.parser.builder.EventDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.builder.event.BeginSpanEvent;
import org.eclipse.mylyn.wikitext.core.parser.builder.event.DocumentBuilderEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableList;

public class SubstitutionWithoutCssSpanStrategyTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void createNull() {
		thrown.expect(NullPointerException.class);
		new SubstitutionWithoutCssSpanStrategy(null);
	}

	@Test
	public void test() {
		SubstitutionWithoutCssSpanStrategy strategy = new SubstitutionWithoutCssSpanStrategy(SpanType.BOLD);
		EventDocumentBuilder builder = new EventDocumentBuilder();
		strategy.beginSpan(builder, SpanType.ITALIC, new Attributes("1", "class", "style", "lang"));
		List<DocumentBuilderEvent> events = builder.getDocumentBuilderEvents().getEvents();
		assertEquals(ImmutableList.of(new BeginSpanEvent(SpanType.BOLD, new Attributes("1", "class", null, "lang"))),
				events);
	}
}
