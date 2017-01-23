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

package org.eclipse.mylyn.internal.wikitext.commonmark.blocks;

import static org.eclipse.mylyn.internal.wikitext.commonmark.CommonMarkAsserts.assertContent;
import static org.junit.Assert.assertEquals;

import org.eclipse.mylyn.internal.wikitext.commonmark.LineSequence;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class BlockQuoteBlockTest {

	@Test
	public void canStart() {
		assertCanStart(true, ">");
		assertCanStart(true, "> ");
		assertCanStart(true, "> test");
		assertCanStart(true, " > test");
		assertCanStart(true, "  > test");
		assertCanStart(true, "   > test");
		assertCanStart(false, "    > test");
		assertCanStart(false, "test");
		assertCanStart(false, " test");
	}

	@Test
	public void blockQuoteSimple() {
		assertContent("<p>test</p><blockquote><p>bq one bq two</p></blockquote><p>three</p>",
				"test\n > bq one\n > bq two\n\nthree");
	}

	@Test
	public void blockQuoteSimpleWithLazyContinuation() {
		assertContent("<p>test</p><blockquote><p>bq one bq two</p></blockquote><p>three</p>",
				"test\n > bq one\nbq two\n\nthree");
	}

	@Test
	public void blockQuoteContainsBlocks() {
		assertContent("<p>test</p><blockquote><ul><li>one</li></ul></blockquote><ul><li>two</li></ul><p>three</p>",
				"test\n > * one\n* two\n\nthree");
	}

	@Test
	public void blockQuoteLazyContinuationStopped() {
		assertContent("<blockquote><p>one</p></blockquote><hr/>", "> one\n****");
	}

	@Test
	public void blockQuoteParagraphNewlines() {
		for (String newline : ImmutableList.of("\n", "\r", "\r\n")) {
			assertContent(
					"<blockquote><p>p1 first p1 second p1 third</p></blockquote><blockquote><p>p2 first</p></blockquote>",
					"> p1 first" + newline + "> p1 second" + newline + "> p1 third" + newline + newline + "> p2 first");
		}
	}

	private void assertCanStart(boolean expected, String string) {
		assertEquals(expected, new BlockQuoteBlock().canStart(LineSequence.create(string)));
	}
}
