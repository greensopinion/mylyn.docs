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

import java.util.regex.Pattern;

public class HtmlType1Block extends AbstractHtmlBlock {

	private final Pattern startPattern = Pattern.compile("\\s{0,3}(<(?:pre|script|style))(\\s|>|$).*",
			Pattern.CASE_INSENSITIVE);

	private final Pattern closePattern = Pattern.compile("\\s{0,3}</(?:pre|script|style)\\s*>",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	@Override
	protected Pattern closePattern() {
		return closePattern;
	}

	@Override
	protected Pattern startPattern() {
		return startPattern;
	}
}
