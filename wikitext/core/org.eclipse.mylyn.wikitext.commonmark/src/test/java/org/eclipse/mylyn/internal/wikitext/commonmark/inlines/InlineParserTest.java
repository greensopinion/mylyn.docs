/*******************************************************************************
 * Copyright (c) 2015 David Green.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.commonmark.inlines;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.mylyn.internal.wikitext.commonmark.Line;
import org.eclipse.mylyn.internal.wikitext.commonmark.LineSequence;
import org.eclipse.mylyn.internal.wikitext.commonmark.ProcessingContext;
import org.eclipse.mylyn.internal.wikitext.commonmark.TextSegment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InlineParserTest {

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	private final Line line = new Line(0, 1, "test");

	@Test
	public void requiresSpans() {
		thrown.expect(NullPointerException.class);
		assertNotNull(new InlineParser((SourceSpan[]) null));
	}

	@Test
	public void requiresListOfSpans() {
		thrown.expect(NullPointerException.class);
		assertNotNull(new InlineParser((List<SourceSpan>) null));
	}

	@Test
	public void parse() {
		assertParse("");
		assertParse("one\ntwo", new Characters(line, 0, 3, "one"), new SoftLineBreak(line, 3, 1),
				new Characters(line, 4, 3, "two"));
		assertParse("one\ntwo three", new Characters(line, 0, 3, "one"), new SoftLineBreak(line, 3, 1),
				new Characters(line, 4, 9, "two three"));
	}

	@Test
	public void toStringContent() {
		InlineParser parser = new InlineParser(new CodeSpan(), new AllCharactersSpan());
		String stringContent = parser.toStringContent(ProcessingContext.builder().build(),
				new TextSegment(Collections.singletonList(new Line(1, 0, "one `two` three"))));
		assertEquals("one two three", stringContent);
	}

	private void assertParse(String content, Inline... inlines) {
		List<Inline> expected = Arrays.asList(inlines);
		List<Inline> actual = createInlines().parse(ProcessingContext.builder().build(),
				new TextSegment(LineSequence.create(content)));
		for (int x = 0; x < expected.size() && x < actual.size(); ++x) {
			assertEquivalent(x, expected.get(x), actual.get(x));
		}
		assertEquals(expected, actual);
	}

	private void assertEquivalent(int index, Inline expected, Inline actual) {
		String message = "inline at " + index;
		assertEquals(message + " type", expected.getClass(), actual.getClass());
		assertEquals(message + " offset", expected.getOffset(), actual.getOffset());
		assertEquals(message + " length", expected.getLength(), actual.getLength());
		assertEquals(message, expected, actual);
	}

	private InlineParser createInlines() {
		return new InlineParser(new LineBreakSpan(), new StringCharactersSpan(), new AllCharactersSpan());
	}
}
