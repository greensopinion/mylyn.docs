/*******************************************************************************
 * Copyright (c) 2012, 2013 Stefan Seelmann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Seelmann - initial API and implementation
 *     Alexander Nyßen - support for fenced code blocks.
 *
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.markdown.core.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;

/**
 * Markdown code block.
 *
 * @author Stefan Seelmann
 * @author Alexander Nyßen
 */
public class CodeBlock extends NestableBlock {

	// simple code blocks with indentation (tabs or spaces)
	private static final Pattern INDENTED_BLOCK = Pattern.compile("(?: {4}|\\t)((?: {4}|\\t)*)(.*)"); //$NON-NLS-1$

	// fenced code block with backticks or tildes
	private static final Pattern FENCED_BLOCK_START = Pattern.compile(" {0,3}(?:```+|~~~+)\\s*(.*)"); //$NON-NLS-1$

	private static final Pattern FENCED_BLOCK = Pattern.compile("((?: {4}|\\t)*)(.*)"); //$NON-NLS-1$

	private static final Pattern FENCED_BLOCK_END = Pattern.compile(" {0,3}(?:```+|~~~+)"); //$NON-NLS-1$

	private boolean fencedBlock = false;

	private int blockLineCount = 0;

	@Override
	public boolean canStart(String line, int lineOffset) {
		if (FENCED_BLOCK_START.matcher(line.substring(lineOffset)).matches()) {
			fencedBlock = true;
			return true;
		}
		fencedBlock = false;
		return INDENTED_BLOCK.matcher(line.substring(lineOffset)).matches();
	}

	@Override
	protected int processLineContent(String line, int offset) {
		// start of block
		if (blockLineCount == 0) {
			builder.beginBlock(BlockType.CODE, new Attributes());
			// if we have a fenced block, the first line will not contain contents
			if (fencedBlock) {
				blockLineCount++;
				return -1;
			}
		}

		Matcher matcher = fencedBlock
				? FENCED_BLOCK.matcher(line.substring(offset))
				: INDENTED_BLOCK.matcher(line.substring(offset));

		// end code block
		if (fencedBlock
				? ((FENCED_BLOCK_END.matcher(line.substring(offset)).matches()) || !matcher.matches())
				: !matcher.matches()) {
			setClosed(true);
			return fencedBlock ? -1 : offset;
		}

		// next line, does not convert to line break
		if (fencedBlock ? blockLineCount > 1 : blockLineCount > 0) {
			builder.characters("\n"); //$NON-NLS-1$
		}

		String indent = matcher.group(1);
		String content = matcher.group(2);

		if (indent != null) {
			// replace intention tabs by 4 spaces
			builder.characters(indent.replace("\t", "    ")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// encode ampersands (&) and angle brackets (< and >)
		builder.characters(content);

		blockLineCount++;
		return -1;
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			builder.endBlock();
		}
		super.setClosed(closed);
	}
}
