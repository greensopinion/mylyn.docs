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

import static org.junit.Assert.assertEquals;

import org.eclipse.mylyn.internal.wikitext.commonmark.LineSequence;
import org.junit.Test;

public class HtmlType1BlockTest {

	private final HtmlType1Block block = new HtmlType1Block();

	@Test
	public void canStart() {
		for (String tagName : new String[] { "script", "pre", "style" }) {
			assertCanStart(true, "<" + tagName);
			assertCanStart(true, " <" + tagName);
			assertCanStart(true, "   <" + tagName);
			assertCanStart(false, "    <" + tagName);

			assertCanStart(true, "<" + tagName + ">");
			assertCanStart(true, "<" + tagName + "> ");
			assertCanStart(true, "<" + tagName + ">with some text");
			assertCanStart(false, "<" + tagName + "/>");
			assertCanStart(true, "<" + tagName + "></" + tagName + ">");
			assertCanStart(true, "<" + tagName + "></" + tagName + " >");
			assertCanStart(true, "<" + tagName + ">  sdf</" + tagName + " >");
		}
	}

	private void assertCanStart(boolean expected, String string) {
		assertEquals(expected, block.canStart(LineSequence.create(string)));
	}
}
