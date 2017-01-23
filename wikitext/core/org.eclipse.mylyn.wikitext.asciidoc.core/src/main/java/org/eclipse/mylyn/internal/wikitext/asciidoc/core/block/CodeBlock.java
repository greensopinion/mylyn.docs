/*******************************************************************************
 * Copyright (c) 2015, 2016 Max Rydahl Andersen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Max Rydahl Andersen - Bug 474084: initial API and implementation
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 474084
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.asciidoc.core.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;

/**
 * Text block containing code, matches blocks that start with {@code ----}. Creates a block type of {@link CodeBlock}.
 *
 * @author Max Rydahl Andersen
 */
public class CodeBlock extends AsciiDocBlock {

	public CodeBlock() {
		super(Pattern.compile("^----.*\\s*")); //$NON-NLS-1$
	}

	String title;

	@Override
	protected void processBlockStart() {
		title = getAsciiDocState().getLastTitle();

		Attributes attributes = new Attributes();
		attributes.setCssClass("listingblock"); //$NON-NLS-1$
		builder.beginBlock(BlockType.DIV, attributes);

		if (title != null) {
			attributes = new Attributes();
			attributes.setCssClass("title"); //$NON-NLS-1$
			builder.beginBlock(BlockType.DIV, attributes);
			builder.characters(title);
			builder.endBlock();
		}

		attributes = new Attributes();
		attributes.setCssClass("content"); //$NON-NLS-1$
		builder.beginBlock(BlockType.DIV, attributes);

		List<String> postitonalParams = new ArrayList<>();
		postitonalParams.add("type"); //$NON-NLS-1$
		postitonalParams.add("language"); //$NON-NLS-1$

		Map<String, String> properties = getAsciiDocState().getLastProperties(postitonalParams);
		attributes = new Attributes();
		attributes.setCssClass("nowrap"); //$NON-NLS-1$

		if (properties.containsKey("language")) { //$NON-NLS-1$
			// This is a workaround to mark which code is a specific source.
			// should be 'data-lang' attribute but that is currently not supported.
			attributes.appendCssClass("source-" + properties.get("language")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		builder.beginBlock(BlockType.CODE, attributes);
	}

	@Override
	protected void processBlockContent(String line) {
		// getMarkupLanguage().emitMarkupLine(getParser(), state, line, offset);
		builder.characters(line);
		builder.lineBreak();
	}

	@Override
	protected void processBlockEnd() {
		builder.endBlock(); // code
		builder.endBlock(); // content div
		builder.endBlock(); // codelisting div
	}

}
