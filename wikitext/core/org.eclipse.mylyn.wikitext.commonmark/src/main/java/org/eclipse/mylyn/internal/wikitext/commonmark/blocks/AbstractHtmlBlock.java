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

import static com.google.common.base.Preconditions.checkState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mylyn.internal.wikitext.commonmark.Line;
import org.eclipse.mylyn.internal.wikitext.commonmark.LineSequence;
import org.eclipse.mylyn.internal.wikitext.commonmark.ProcessingContext;
import org.eclipse.mylyn.internal.wikitext.commonmark.SourceBlock;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;

abstract class AbstractHtmlBlock extends SourceBlock {

	@Override
	public void process(ProcessingContext context, DocumentBuilder builder, LineSequence lineSequence) {
		Line line = lineSequence.getCurrentLine();
		final Line firstLine = line;
		while (line != null) {
			String lineText = line.getText();
			builder.charactersUnescaped(lineText);
			builder.charactersUnescaped("\n");

			lineSequence.advance();

			if (firstLine.equals(line)) {
				Matcher matcher = startPattern().matcher(lineText);
				checkState(matcher.matches());
				int offset = matcher.end(1);
				if (offset < lineText.length() - 1) {
					Matcher closeMatcher = closePattern().matcher(lineText);
					closeMatcher.region(offset, lineText.length());
					if (closeMatcher.find()) {
						break;
					}
				}
			} else if (closePattern().matcher(lineText).find()) {
				break;
			}

			line = lineSequence.getCurrentLine();
		}
	}

	@Override
	public boolean canStart(LineSequence lineSequence) {
		Line line = lineSequence.getCurrentLine();
		if (line != null) {
			return startPattern().matcher(line.getText()).matches();
		}
		return false;
	}

	protected abstract Pattern closePattern();

	/**
	 * Provides a pattern that must be matched for the block to start. The pattern must provide a first group which
	 * cannot match the close pattern.
	 * 
	 * @return the pattern
	 */
	protected abstract Pattern startPattern();
}
