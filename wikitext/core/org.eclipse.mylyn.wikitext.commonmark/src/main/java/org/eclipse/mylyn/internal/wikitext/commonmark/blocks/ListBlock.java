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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mylyn.internal.wikitext.commonmark.CommonMark;
import org.eclipse.mylyn.internal.wikitext.commonmark.Line;
import org.eclipse.mylyn.internal.wikitext.commonmark.LineSequence;
import org.eclipse.mylyn.internal.wikitext.commonmark.ProcessingContext;
import org.eclipse.mylyn.internal.wikitext.commonmark.ProcessingContextBuilder;
import org.eclipse.mylyn.internal.wikitext.commonmark.SourceBlock;
import org.eclipse.mylyn.internal.wikitext.commonmark.SourceBlocks;
import org.eclipse.mylyn.internal.wikitext.commonmark.SourceBlocks.BlockContext;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.ListAttributes;
import org.eclipse.mylyn.wikitext.core.parser.builder.NoOpDocumentBuilder;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class ListBlock extends BlockWithNestedBlocks {

	private final Pattern bulletPattern = Pattern
			.compile("\\s{0,3}(([*+-])|(([0-9]{0,5})[.)]))(?:(?:\\s(\\s*)(.*))|\\s*$)");

	private final HorizontalRuleBlock horizontalRuleBlock = new HorizontalRuleBlock();

	private enum ListMode {
		TIGHT, LOOSE, TIGHT_WITH_TRAILING_EMPTY_LINE
	}

	private interface ListItemHandler {

		void emitListItem(ProcessingContext context, DocumentBuilder builder, ListMode listMode,
				LineSequence lineSequence);
	}

	@Override
	public void process(ProcessingContext context, DocumentBuilder builder, LineSequence lineSequence) {
		process(context, builder, lineSequence, new ListItemHandler() {

			@Override
			public void emitListItem(ProcessingContext context, DocumentBuilder builder, ListMode listMode,
					LineSequence lineSequence) {
				emitListItem(context, builder, listMode, lineSequence);
			}
		});
	}

	@Override
	public void createContext(final ProcessingContextBuilder contextBuilder, LineSequence lineSequence) {
		process(ProcessingContext.builder().build(), new NoOpDocumentBuilder(), lineSequence,
				new ListItemHandler() {

					@Override
					public void emitListItem(ProcessingContext dummyContext, DocumentBuilder builder, ListMode listMode,
							LineSequence lineSequence) {
						CommonMark.sourceBlocks().createContext(contextBuilder, listItemLineSequence(lineSequence));
					}
				});
	}

	private void process(ProcessingContext context, DocumentBuilder builder, LineSequence lineSequence,
			ListItemHandler listItemHandler) {
		builder.setLocator(lineSequence.getCurrentLine().toLocator());

		char bulletType = bulletType(lineSequence.getCurrentLine());

		ListAttributes listAttributes = new ListAttributes();
		listAttributes.setStart(listStart(lineSequence.getCurrentLine()));

		ListMode listMode = calculateListMode(context, lineSequence.lookAhead(), bulletType);

		builder.beginBlock(listBlockType(bulletType), listAttributes);

		while (currentLineIsInList(lineSequence, bulletType)) {
			builder.setLocator(lineSequence.getCurrentLine().toLocator());
			builder.beginBlock(BlockType.LIST_ITEM, new Attributes());

			emitListItem(context, builder, listMode, lineSequence);

			builder.endBlock();
		}

		builder.endBlock();
	}

	private boolean currentLineIsInList(LineSequence lineSequence, char bulletType) {
		return lineSequence.getCurrentLine() != null && isBulletListItem(lineSequence.getCurrentLine())
				&& bulletType == bulletType(lineSequence.getCurrentLine())
				&& !horizontalRuleBlock.canStart(lineSequence);
	}

	private ListMode calculateListMode(ProcessingContext context, LineSequence lineSequence, char bulletType) {
		ListMode listMode = ListMode.TIGHT;
		while (listMode != ListMode.LOOSE && currentLineIsInList(lineSequence, bulletType)) {
			if (listMode == ListMode.TIGHT_WITH_TRAILING_EMPTY_LINE) {
				listMode = ListMode.LOOSE;
			} else {
				listMode = calculateListItemListMode(context, lineSequence);
			}
		}
		return listMode == ListMode.TIGHT_WITH_TRAILING_EMPTY_LINE ? ListMode.TIGHT : listMode;
	}

	private ListMode calculateListItemListMode(ProcessingContext context, LineSequence lineSequence) {
		if (isSingleEmptyLineListItem(lineSequence)) {
			lineSequence.advance();
			return ListMode.TIGHT;
		}
		List<SourceBlock> blocks = calculateListItemBlocks(context, lineSequence);
		if (blocks.isEmpty()) {
			lineSequence.advance();
			return ListMode.TIGHT;
		}
		return getListItemListMode(blocks, lineSequence);
	}

	private boolean isSingleEmptyLineListItem(LineSequence lineSequence) {
		LineSequence listItemLineSequence = listItemLineSequence(lineSequence.lookAhead());
		return listItemLineSequence.getCurrentLine().isEmpty() && listItemLineSequence.getNextLine() == null;
	}

	private ListMode getListItemListMode(List<SourceBlock> blocks, LineSequence lineSequence) {
		if (blocks.isEmpty()) {
			return ListMode.TIGHT;
		}
		for (int x = 1; x < blocks.size() - 1; ++x) {
			SourceBlock block = blocks.get(x);
			if (block instanceof EmptyBlock) {
				return ListMode.LOOSE;
			}
		}
		if (blocks.get(blocks.size() - 1) instanceof EmptyBlock) {
			return ListMode.TIGHT_WITH_TRAILING_EMPTY_LINE;
		}
		return ListMode.TIGHT;
	}

	private List<SourceBlock> calculateListItemBlocks(ProcessingContext context, LineSequence lineSequence) {
		SourceBlocks sourceBlocks = CommonMark.sourceBlocks();
		return sourceBlocks.calculateSourceBlocks(context, listItemLineSequence(lineSequence),
				listItemBlockContextPredicate());
	}

	private Predicate<BlockContext> listItemBlockContextPredicate() {
		return new Predicate<SourceBlocks.BlockContext>() {

			@Override
			public boolean apply(BlockContext blockContext) {
				if (blockContext.getPreviousBlock() != null && blockContext.getCurrentBlock() instanceof EmptyBlock
						&& nextLineIsEmpty(blockContext.getLineSequence())) {
					return false;
				}
				return true;
			}

			private boolean nextLineIsEmpty(LineSequence lineSequence) {
				Line nextLine = lineSequence.getNextLine();
				return nextLine != null && nextLine.isEmpty();
			}
		};
	}

	private BlockType listBlockType(char bulletType) {
		switch (bulletType) {
		case '*':
		case '+':
		case '-':
			return BlockType.BULLETED_LIST;
		default:
			return BlockType.NUMERIC_LIST;
		}
	}

	private void emitListItem(ProcessingContext context, DocumentBuilder builder, ListMode listMode,
			LineSequence lineSequence) {
		List<SourceBlock> blocks = calculateListItemBlocks(context, lineSequence.lookAhead());
		if (blocks.isEmpty()) {
			lineSequence.advance();
			return;
		}
		LineSequence contentLineSequence = listItemLineSequence(lineSequence);

		if (!blocks.isEmpty() && blocks.get(0) instanceof EmptyBlock) {
			blocks.remove(0);
			while (contentLineSequence.getCurrentLine() != null && contentLineSequence.getCurrentLine().isEmpty()) {
				contentLineSequence.advance();
			}
		}
		for (SourceBlock block : blocks) {
			if (listMode == ListMode.TIGHT && block instanceof ParagraphBlock) {
				ParagraphBlock paragraphBlock = (ParagraphBlock) block;
				paragraphBlock.processInlines(context, builder, contentLineSequence, false);
			} else {
				block.process(context, builder, contentLineSequence);
			}
		}
	}

	private LineSequence listItemLineSequence(LineSequence lineSequence) {
		final int indentOffset = calculateLineItemIndent(lineSequence.getCurrentLine());
		LineSequence itemLinesSequence = lineSequence.with(new Predicate<Line>() {

			int firstLineNumber = -1;

			@Override
			public boolean apply(Line line) {
				if (firstLineNumber == -1) {
					firstLineNumber = line.getLineNumber();
				}
				return firstLineNumber == line.getLineNumber() || line.isEmpty() || isIndented(line, indentOffset);
			}
		});
		return itemLinesSequence.transform(new Function<Line, Line>() {

			@Override
			public Line apply(Line line) {
				if (line.isEmpty()) {
					return line;
				}
				int length = Math.max(line.getText().length() - indentOffset, 0);
				int offset = Math.min(indentOffset, line.getText().length());
				return line.segment(offset, length);
			}
		});
	}

	private boolean isIndented(Line line, int indentSize) {
		int firstNonWhitespace = CharMatcher.WHITESPACE.negate().indexIn(line.getText());
		return firstNonWhitespace >= indentSize;
	}

	private int calculateLineItemIndent(Line line) {
		Matcher matcher = bulletPattern.matcher(line.getText());
		checkState(matcher.matches());
		int start = matcher.start(6);
		if (start == -1) {
			start = line.getText().length() + 1;
		} else {
			String whitespaceAfterListMarker = matcher.group(5);
			if (whitespaceAfterListMarker != null && whitespaceAfterListMarker.length() >= 4) {
				start = matcher.start(5);
			}
		}
		return start;
	}

	@Override
	public boolean canStart(LineSequence lineSequence) {
		Line line = lineSequence.getCurrentLine();
		return line != null && isBulletListItem(line);
	}

	private char bulletType(Line line) {
		Matcher matcher = bulletPattern.matcher(line.getText());
		checkState(matcher.matches());
		String text = matcher.group(1);
		return text.charAt(text.length() - 1);
	}

	private String listStart(Line line) {
		Matcher matcher = bulletPattern.matcher(line.getText());
		checkState(matcher.matches());
		String marker = matcher.group(4);
		if ("1".equals(marker)) {
			marker = null;
		}
		return marker;
	}

	private boolean isBulletListItem(Line line) {
		return bulletPattern.matcher(line.getText()).matches();
	}
}
