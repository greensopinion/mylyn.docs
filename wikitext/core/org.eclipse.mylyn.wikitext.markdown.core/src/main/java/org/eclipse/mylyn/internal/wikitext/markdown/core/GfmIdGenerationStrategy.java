/*******************************************************************************
 * Copyright (c) 2007, 2014 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.markdown.core;

import java.util.Locale;

import org.eclipse.mylyn.wikitext.core.parser.markup.IdGenerationStrategy;

import com.google.common.base.CharMatcher;

public class GfmIdGenerationStrategy extends IdGenerationStrategy {

	@Override
	public String generateId(String headingText) {
		String id = headingText.toLowerCase(Locale.getDefault());
		id = id.replaceAll("[^a-z0-9_-]", "-"); //$NON-NLS-1$//$NON-NLS-2$
		CharMatcher hyphenMatcher = CharMatcher.is('-');
		id = hyphenMatcher.trimFrom(hyphenMatcher.collapseFrom(id, '-'));
		return id;
	}

}
