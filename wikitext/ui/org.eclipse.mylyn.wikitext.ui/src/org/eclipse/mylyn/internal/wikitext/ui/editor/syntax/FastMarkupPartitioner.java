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
package org.eclipse.mylyn.internal.wikitext.ui.editor.syntax;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.Locator;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.AbstractMarkupLanguage;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.osgi.util.NLS;

/**
 * @author David Green
 */
public class FastMarkupPartitioner extends FastPartitioner {

	public static final String CONTENT_TYPE_MARKUP = "__markup_block"; //$NON-NLS-1$

	public static final String[] ALL_CONTENT_TYPES = new String[] { CONTENT_TYPE_MARKUP };

	static boolean debug = Boolean.getBoolean(FastMarkupPartitioner.class.getName() + ".debug"); //$NON-NLS-1$

	private MarkupLanguage markupLanguage;

	public FastMarkupPartitioner() {
		super(new PartitionTokenScanner(), ALL_CONTENT_TYPES);
	}

	public MarkupLanguage getMarkupLanguage() {
		return markupLanguage;
	}

	public void setMarkupLanguage(MarkupLanguage markupLanguage) {
		this.markupLanguage = markupLanguage;
		getScanner().setMarkupLanguage(markupLanguage);
		resetPartitions();
	}

	PartitionTokenScanner getScanner() {
		return (PartitionTokenScanner) fScanner;
	}

	public void resetPartitions() {
		if (fDocument != null) {
			super.flushRewriteSession();
			initialize();
		} else {
			clearPositionCache();
		}
	}

	static class PartitionTokenScanner implements IPartitionTokenScanner {
		private final Map<Integer, PartitioningResult> cachedPartitioning = new HashMap<>();

		private MarkupLanguage markupLanguage;

		private int index = -1;

		private PartitioningResult lastComputed;

		private static class PartitioningResult {
			int offset;

			int length;

			ITypedRegion[] partitions;

			public PartitioningResult(int offset, int length, ITypedRegion[] partitions) {
				super();
				this.offset = offset;
				this.length = length;
				this.partitions = partitions;
			}

			public boolean overlapsWith(PartitioningResult other) {
				int end = other.offset + other.length;
				int thisEnd = this.offset + this.length;

				if (end > thisEnd) {
					return other.offset < thisEnd;
				} else if (thisEnd > end) {
					return offset < end;
				} else {
					return true;
				}
			}
		}

		public ITypedRegion[] computePartitions(IDocument document, int offset, int length) {
			if (lastComputed != null && lastComputed.offset <= offset
					&& (lastComputed.offset + lastComputed.length) >= (offset + length)) {
				return lastComputed.partitions;
			} else {
				PartitioningResult result = cachedPartitioning.get(offset);
				if (result == null || result.length != length) {
					result = computeOlp(document, offset, length, -1);
					updateCache(result, document.getLength());
				}
				return result.partitions;
			}
		}

		public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {
			lastComputed = computeOlp(document, offset, length, partitionOffset);
			index = -1;
			updateCache(lastComputed, document.getLength());
		}

		private void updateCache(PartitioningResult updated, int maxLength) {
			Iterator<PartitioningResult> it = cachedPartitioning.values().iterator();
			while (it.hasNext()) {
				PartitioningResult result = it.next();

				if (result.offset >= maxLength || (updated != null && result.overlapsWith(updated))) {
					it.remove();
				}
			}
			if (updated != null) {
				cachedPartitioning.put(updated.offset, updated);
			}
		}

		private PartitioningResult computeOlp(IDocument document, int offset, int length, int partitionOffset) {
			if (markupLanguage == null) {
				return new PartitioningResult(offset, length, null);
			}
			int startOffset = partitionOffset == -1 ? offset : Math.min(offset, partitionOffset);
			int endOffset = offset + length;

			boolean blocksOnly = partitionOffset != -1;
			MarkupParser markupParser = new MarkupParser(markupLanguage);
			if (markupLanguage instanceof AbstractMarkupLanguage) {
				AbstractMarkupLanguage language = (AbstractMarkupLanguage) markupLanguage;
				language.setFilterGenerativeContents(true);
				language.setBlocksOnly(blocksOnly);
			}
			PartitionBuilder partitionBuilder = new PartitionBuilder(startOffset, blocksOnly);
			markupParser.setBuilder(partitionBuilder);

			String markupContent;
			try {
				markupContent = document.get(startOffset, endOffset - startOffset);
			} catch (BadLocationException e) {
				markupContent = document.get();
			}
			markupParser.parse(markupContent);

			ITypedRegion[] latestPartitions = partitionBuilder.partitions.toArray(new ITypedRegion[partitionBuilder.partitions.size()]);
			List<ITypedRegion> partitioning = new ArrayList<>(latestPartitions.length);

			ITypedRegion previous = null;
			for (ITypedRegion region : latestPartitions) {
				if (region.getLength() == 0) {
					// ignore 0-length partitions
					continue;
				}
				if (previous != null && region.getOffset() < (previous.getOffset() + previous.getLength())) {
					String message = NLS.bind(Messages.FastMarkupPartitioner_0, new Object[] { region, previous,
							markupLanguage.getName() });
					if (FastMarkupPartitioner.debug) {
						String markupSavePath = saveToTempFile(markupLanguage, markupContent);
						message = NLS.bind(Messages.FastMarkupPartitioner_1, new Object[] { message, markupSavePath });
					}
					throw new IllegalStateException(message);
				}
				previous = region;
				if (region.getOffset() >= startOffset && region.getOffset() < endOffset) {
					partitioning.add(region);
				} else if (region.getOffset() >= (offset + length)) {
					break;
				}
			}
			return new PartitioningResult(offset, length, partitioning.toArray(new ITypedRegion[partitioning.size()]));
		}

		public void setMarkupLanguage(MarkupLanguage markupLanguage) {
			this.markupLanguage = markupLanguage;
		}

		public int getTokenLength() {
			return lastComputed.partitions[index].getLength();
		}

		public int getTokenOffset() {
			return lastComputed.partitions[index].getOffset();
		}

		public IToken nextToken() {
			if (lastComputed == null || lastComputed.partitions == null || ++index >= lastComputed.partitions.length) {
				return Token.EOF;
			}
			return new Token(lastComputed.partitions[index].getType());
		}

		public void setRange(IDocument document, int offset, int length) {
			setPartialRange(document, offset, length, null, -1);
		}
	}

	public static class MarkupPartition implements ITypedRegion {

		private final Block block;

		private int offset;

		private int length;

		private List<Span> spans;

		private MarkupPartition(Block block, int offset, int length) {
			this.block = block;
			this.offset = offset;
			this.length = length;
		}

		public int getOffset() {
			return offset;
		}

		public int getLength() {
			return length;
		}

		public String getType() {
			return CONTENT_TYPE_MARKUP;
		}

		public Block getBlock() {
			return block;
		}

		public List<Span> getSpans() {
			if (spans == null) {

				List<Span> spans = new ArrayList<>();
				getSpans(block, spans);
				this.spans = spans;
			}
			return spans;
		}

		private void getSpans(Block block, List<Span> spans) {
			for (Segment<?> s : block.getChildren().asList()) {
				if (s.getOffset() >= offset && s.getOffset() < (offset + length)) {
					if (s instanceof Span) {
						spans.add((Span) s);
					} else {
						getSpans((Block) s, spans);
					}
				}
			}
		}

		@Override
		public String toString() {
			return String.format("MarkupPartition(type=%s,offset=%s,length=%s,end=%s)", block.getType(), offset, //$NON-NLS-1$
					length, offset + length);
		}
	}

	private static class PartitionBuilder extends DocumentBuilder {

		private final Block outerBlock = new Block(null, 0, Integer.MAX_VALUE / 2);

		private Block currentBlock = outerBlock;

		private Span currentSpan = null;

		private final int offset;

		private List<MarkupPartition> partitions;

		private final boolean blocksOnly;

		public PartitionBuilder(int offset, boolean blocksOnly) {
			this.offset = offset;
			this.blocksOnly = blocksOnly;
		}

		@Override
		public void acronym(String text, String definition) {
		}

		@Override
		public void beginBlock(BlockType type, Attributes attributes) {
			final int newBlockOffset = getLocator().getDocumentOffset() + offset;
			Block newBlock = new Block(type, attributes, newBlockOffset, currentBlock.getLength()
					- (newBlockOffset - currentBlock.getOffset()));
			newBlock.setSpansComputed(!blocksOnly);
			currentBlock.add(newBlock);
			currentBlock = newBlock;
		}

		@Override
		public void beginDocument() {
		}

		@Override
		public void beginHeading(int level, Attributes attributes) {
			final int newBlockOffset = getLocator().getDocumentOffset() + offset;
			Block newBlock = new Block(level, attributes, newBlockOffset, currentBlock.getLength()
					- (newBlockOffset - currentBlock.getOffset()));
			newBlock.setSpansComputed(!blocksOnly);
			currentBlock.add(newBlock);
			currentBlock = newBlock;
		}

		@Override
		public void beginSpan(SpanType type, Attributes attributes) {
			Span span = new Span(type, attributes, getLocator().getDocumentOffset() + offset,
					getLocator().getLineSegmentEndOffset() - getLocator().getLineCharacterOffset());
			if (currentSpan != null) {
				currentSpan.add(span);
				currentSpan = span;
			} else {
				currentSpan = span;
				currentBlock.add(span);
			}
		}

		@Override
		public void characters(String text) {
		}

		@Override
		public void charactersUnescaped(String literal) {
		}

		@Override
		public void endBlock() {
			currentBlock = currentBlock.getParent();
			if (currentBlock == null) {
				throw new IllegalStateException();
			}
		}

		@Override
		public void endDocument() {
			if (currentBlock != outerBlock) {
				throw new IllegalStateException();
			}
			Locator locator = getLocator();
			outerBlock.setLength((locator == null ? 0 : locator.getDocumentOffset()) + offset);

			partitions = new ArrayList<>();

			// here we flatten our hierarchy of blocks into partitions
			for (Segment<?> child : outerBlock.getChildren().asList()) {
				createRegions(null, child);
			}
		}

		public MarkupPartition createRegions(MarkupPartition parent, Segment<?> segment) {
			if (segment.getLength() == 0) {
				return parent;
			}
			if (segment instanceof Block) {
				Block block = (Block) segment;

				if (!filtered(block)) {
					MarkupPartition partition = new MarkupPartition(block, segment.getOffset(), segment.getLength());
					if (parent == null) {
						partitions.add(partition);
					} else {
						// parent needs adjusting to prevent overlap
						int parentIndex = partitions.indexOf(parent);

						// start on the same offset
						if (partition.offset == parent.offset) {
							if (partition.length == parent.length) {
								// same length, so remove parent all together
								partitions.remove(parentIndex);
								partitions.add(parentIndex, partition);
							} else {
								// start on same offset, but new partition is smaller
								// so move parent after new partition and shrink it by the corresponding amount
								parent.offset = partition.offset + partition.length;
								parent.length -= partition.length;
								partitions.add(parentIndex, partition);
							}
						} else {
							if (partition.length + partition.offset == parent.length + parent.offset) {
								// end on the same offset, so shrink the parent
								parent.length = partition.offset - parent.offset;
								partitions.add(parentIndex + 1, partition);
							} else {
								// split the parent
								int parentLength = parent.length;
								parent.length = partition.offset - parent.offset;
								final int splitOffset = partition.offset + partition.length;
								MarkupPartition trailer = new MarkupPartition(parent.block, splitOffset, parent.offset
										+ parentLength - splitOffset);
								partitions.add(parentIndex + 1, partition);
								partitions.add(parentIndex + 2, trailer);
								parent = trailer;
							}
						}
					}
					if (!block.getChildren().isEmpty()) {
						for (Segment<?> child : block.getChildren().asList()) {
							partition = createRegions(partition, child);
						}
					}
				}
			}
			return parent;
		}

		private boolean filtered(Block block) {
			if (block.getType() == null) {
				return false;
			}
			switch (block.getType()) {
			case DEFINITION_ITEM:
			case LIST_ITEM:
			case TABLE_CELL_HEADER:
			case TABLE_CELL_NORMAL:
			case TABLE_ROW:
				return true;
			case PARAGRAPH:
				// bug 249615: ignore paras that are nested inside a quote block
				if (block.getParent() != null && block.getParent().getType() == BlockType.QUOTE) {
					return true;
				}
				break;
			case BULLETED_LIST:
			case NUMERIC_LIST:
			case DEFINITION_LIST:
			case DEFINITION_TERM:
				return block.getParent() != null && filtered(block.getParent());
			}
			return false;
		}

		@Override
		public void endHeading() {
			currentBlock = currentBlock.getParent();
			if (currentBlock == null) {
				throw new IllegalStateException();
			}
		}

		@Override
		public void endSpan() {
			if (currentSpan == null) {
				throw new IllegalStateException();
			}
			if (currentSpan.getParent() instanceof Span) {
				currentSpan = (Span) currentSpan.getParent();
			} else {
				currentSpan = null;
			}
		}

		@Override
		public void entityReference(String entity) {
		}

		@Override
		public void image(Attributes attributes, String url) {
		}

		@Override
		public void imageLink(Attributes linkAttributes, Attributes attributes, String href, String imageUrl) {
		}

		@Override
		public void lineBreak() {
		}

		@Override
		public void link(Attributes attributes, String hrefOrHashName, String text) {
		}

	}

	public void reparse(IDocument document, Block block) {
		MarkupParser markupParser = new MarkupParser(markupLanguage);
		if (markupLanguage instanceof AbstractMarkupLanguage) {
			AbstractMarkupLanguage language = (AbstractMarkupLanguage) markupLanguage;
			language.setFilterGenerativeContents(true);
			language.setBlocksOnly(false);
		}
		PartitionBuilder partitionBuilder = new PartitionBuilder(block.getOffset(), false);
		markupParser.setBuilder(partitionBuilder);

		try {
			markupParser.parse(document.get(block.getOffset(), block.getLength()));
			for (Segment<?> s : partitionBuilder.outerBlock.getChildren().asList()) {
				if (s.getOffset() == block.getOffset()) {
					if (s instanceof Block) {
						block.replaceChildren(s);
						block.setSpansComputed(true);
						break;
					}
				}
			}
		} catch (BadLocationException e) {
			throw new IllegalStateException(e);
		}

	}

	/**
	 * save markup content to a temporary file to facilitate analysis of the problem
	 * 
	 * @return the absolute path to the saved file, or null if the file was not saved
	 */
	private static String saveToTempFile(MarkupLanguage markupLanguage, String markupContent) {
		String markupSavePath = null;
		try {
			File file = File.createTempFile("markup-content-", "." //$NON-NLS-1$ //$NON-NLS-2$
					+ markupLanguage.getName().toLowerCase().replaceAll("[^a-z]", "")); //$NON-NLS-1$ //$NON-NLS-2$
			Writer writer = new FileWriter(file);
			try {
				writer.write(markupContent);
			} finally {
				writer.close();
			}
			markupSavePath = file.getAbsolutePath();
		} catch (IOException e) {
		}
		return markupSavePath;
	}
}
