/*******************************************************************************
 * Copyright (c) 2007, 2013 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.textile.core.block;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CommentBlockTest {

	@Test
	public void mustStartAtLineStart() {
		assertTrue(new CommentBlock().canStart("###. comment", 0));
		assertFalse(new CommentBlock().canStart(" ###. comment", 0));
		assertFalse(new CommentBlock().canStart(" ###. comment", 1));
	}
}
