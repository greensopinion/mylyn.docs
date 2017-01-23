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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.mylyn.internal.wikitext.commonmark.Line;
import org.eclipse.mylyn.internal.wikitext.commonmark.LineSequence;
import org.junit.Test;

public class HtmlBlockTest {

	private final HtmlBlock block = new HtmlBlock();

	@Test
	public void canStart() {
		assertFalse(block.canStart(LineSequence.create("")));
		assertTrue(block.canStart(LineSequence.create("<div>")));
		assertTrue(block.canStart(LineSequence.create("<table>")));
		assertTrue(block.canStart(LineSequence.create("<p>")));
		assertFalse(block.canStart(LineSequence.create("<one>")));
		assertTrue(block.canStart(LineSequence.create("   <p>")));
		assertFalse(block.canStart(LineSequence.create("    <p>")));
		assertTrue(block.canStart(LineSequence.create("<p")));
		assertTrue(block.canStart(LineSequence.create("<p >")));
		assertTrue(block.canStart(LineSequence.create("<p />")));
		assertTrue(block.canStart(LineSequence.create("<p/>")));
		assertTrue(block.canStart(LineSequence.create("<p\n  a=\"b\"\n>")));
	}

	@Test
	public void canStartDoesNotAdvanceLineSequencePosition() {
		LineSequence lineSequence = LineSequence.create("<p\n  a=\"b\"\n>");
		Line firstLine = lineSequence.getCurrentLine();
		assertTrue(block.canStart(lineSequence));
		assertSame(firstLine, lineSequence.getCurrentLine());
	}
}
