/*******************************************************************************
 * Copyright (c) 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.html.core;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;

public class NoOpBlockStrategy implements BlockStrategy {

	static final NoOpBlockStrategy instance = new NoOpBlockStrategy();

	@Override
	public void beginBlock(DocumentBuilder builder, BlockType type, Attributes attributes) {
	}

	@Override
	public void endBlock(DocumentBuilder builder) {
	}

	@Override
	public BlockSeparator trailingSeparator() {
		return null;
	}
}
