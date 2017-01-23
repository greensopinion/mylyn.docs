/*******************************************************************************
 * Copyright (c) 2007, 2008 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.wikitext.core.parser.builder.event;

import static org.eclipse.mylyn.internal.wikitext.core.test.EqualityAsserts.assertEquality;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HorizontalRuleEventTest {

	@Test
	public void testToString() {
		assertEquals("horizontalRule()", new HorizontalRuleEvent().toString());
	}

	@Test
	public void equals() {
		assertEquality(new HorizontalRuleEvent(), new HorizontalRuleEvent());
	}
}
