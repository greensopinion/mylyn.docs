/*******************************************************************************
 * Copyright (c) 2011, 2016 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.confluence.core;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.HtmlParser;
import org.eclipse.mylyn.wikitext.core.parser.LinkAttributes;
import org.eclipse.mylyn.wikitext.core.parser.builder.AbstractMarkupDocumentBuilder;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

/**
 * a document builder that emits Confluence markup
 *
 * @see HtmlParser
 * @author David Green
 * @since 1.6
 * @see ConfluenceLanguage
 * @see ConfluenceLanguage#createDocumentBuilder(Writer)
 */
public class ConfluenceDocumentBuilder extends AbstractMarkupDocumentBuilder {

	private static final Pattern PATTERN_MULTIPLE_NEWLINES = Pattern.compile("(\r\n|\r|\n){2,}"); //$NON-NLS-1$

	private static final CharMatcher SPAN_MARKUP_CHARACTERS = CharMatcher.anyOf("*_+-^~{}[]?%@"); //$NON-NLS-1$

	private final Map<String, String> entityToLiteral = new HashMap<String, String>();

	{
		entityToLiteral.put("nbsp", " "); //$NON-NLS-1$//$NON-NLS-2$
		entityToLiteral.put("#160", " "); //$NON-NLS-1$//$NON-NLS-2$
		entityToLiteral.put("quot", "\""); //$NON-NLS-1$//$NON-NLS-2$
		entityToLiteral.put("amp", "&"); //$NON-NLS-1$//$NON-NLS-2$
		entityToLiteral.put("lt", "<"); //$NON-NLS-1$//$NON-NLS-2$
		entityToLiteral.put("gt", ">"); //$NON-NLS-1$//$NON-NLS-2$
		entityToLiteral.put("copy", "(c)"); //$NON-NLS-1$//$NON-NLS-2$
		entityToLiteral.put("reg", "(r)"); //$NON-NLS-1$//$NON-NLS-2$
		entityToLiteral.put("#8482", "(t)"); //$NON-NLS-1$//$NON-NLS-2$
		entityToLiteral.put("euro", "\u20ac"); //$NON-NLS-1$//$NON-NLS-2$
		entityToLiteral.put("#36", "$"); //$NON-NLS-1$//$NON-NLS-2$
		entityToLiteral.put("#37", "%"); //$NON-NLS-1$//$NON-NLS-2$
	}

	private interface ConfluenceBlock {

		void writeLineBreak() throws IOException;

	}

	private class ContentBlock extends NewlineDelimitedBlock implements ConfluenceBlock {

		private final String prefix;

		private final String suffix;

		private final boolean requireAdjacentSeparator;

		private final boolean emitWhenEmpty;

		private int consecutiveLineBreakCount = 0;

		ContentBlock(BlockType blockType, String prefix, String suffix, boolean requireAdjacentSeparator,
				boolean emitWhenEmpty, int leadingNewlines, int trailingNewlines) {
			super(blockType, leadingNewlines, trailingNewlines);
			this.prefix = prefix;
			this.suffix = suffix;
			this.requireAdjacentSeparator = requireAdjacentSeparator;
			this.emitWhenEmpty = emitWhenEmpty;
		}

		ContentBlock(String prefix, String suffix, boolean requireAdjacentWhitespace, boolean emitWhenEmpty,
				int leadingNewlines, int trailingNewlines) {
			this(null, prefix, suffix, requireAdjacentWhitespace, emitWhenEmpty, leadingNewlines, trailingNewlines);
		}

		@Override
		public void write(int c) throws IOException {
			consecutiveLineBreakCount = 0;
			if (!isBlockTypePreservingWhitespace()) {
				c = normalizeWhitespace(c);
			}
			ConfluenceDocumentBuilder.this.emitContent(c);
		}

		@Override
		public void write(String s) throws IOException {
			consecutiveLineBreakCount = 0;
			if (!isBlockTypePreservingWhitespace()) {
				s = normalizeWhitespace(s);
			}
			ConfluenceDocumentBuilder.this.emitContent(s);
		}

		public void writeLineBreak() throws IOException {
			++consecutiveLineBreakCount;
			if (consecutiveLineBreakCount == 1 || isBlockTypePreservingWhitespace()) {
				if (isBlockTerminatedByNewlines()) {
					ConfluenceDocumentBuilder.this.emitContent("\\\\"); //$NON-NLS-1$
				} else {
					ConfluenceDocumentBuilder.this.emitContent('\n');
				}
			} else {
				if (getLastChar() != '\n') {
					ConfluenceDocumentBuilder.this.emitContent(' ');
				}
				ConfluenceDocumentBuilder.this.emitContent("\\\\"); //$NON-NLS-1$
			}
		}

		@Override
		public void open() throws IOException {
			super.open();
			pushWriter(new StringWriter());
			if (requireAdjacentSeparator) {
				clearRequireAdjacentSeparator();
			}

			// Emit here so that nested blocks can detect parent block type
			emitPrefix();
		}

		@Override
		public final void close() throws IOException {
			Writer thisContent = popWriter();
			String content = thisContent.toString();
			boolean contentIsEmpty = content.equals(prefix);

			if (!contentIsEmpty || emitWhenEmpty) {
				checkState(content.startsWith(prefix), "Expected content to start with prefix \"%s\"", content, prefix); //$NON-NLS-1$
				content = content.substring(prefix.length());

				if (requireAdjacentSeparator && !isSpanSuffixAdjacentToSpanPrefix()) {
					requireAdjacentSeparator();
				} else {
					clearRequireAdjacentSeparator();
				}

				emitPrefix();
				emitContent(content);
				emitSuffix(content);

				if (requireAdjacentSeparator) {
					requireAdjacentSeparator();
				}
			}

			super.close();
			consecutiveLineBreakCount = 0;
		}

		private boolean isSpanSuffixAdjacentToSpanPrefix() {
			if (!Strings.isNullOrEmpty(prefix) && isSpanMarkup(getLastChar()) && isSpanMarkup(prefix.charAt(0))) {
				return true;
			}
			return false;
		}

		protected void emitPrefix() throws IOException {
			ConfluenceDocumentBuilder.this.emitContent(prefix);
		}

		protected void emitContent(String content) throws IOException {
			ConfluenceDocumentBuilder.this.emitContent(content);
		}

		private void emitSuffix(String content) throws IOException {
			final String suffix = isExtended(content) ? this.suffix + "\n" : this.suffix; //$NON-NLS-1$
			ConfluenceDocumentBuilder.this.emitContent(suffix);
		}

		private boolean isExtended(String content) {
			if (getBlockType() != null) {
				switch (getBlockType()) {
				case CODE:
				case PREFORMATTED:
				case QUOTE:
					return PATTERN_MULTIPLE_NEWLINES.matcher(content).find();
				}
			}
			return false;
		}

		private boolean isBlockTerminatedByNewlines() {
			return suffix.isEmpty() && !prefix.isEmpty();
		}

		private boolean isBlockTypePreservingWhitespace() {
			return getBlockType() == BlockType.CODE || getBlockType() == BlockType.PREFORMATTED;
		}
	}

	private class LinkBlock extends ContentBlock {

		private final LinkAttributes attributes;

		private LinkBlock(LinkAttributes attributes) {
			super(null, "[", "]", true, true, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			this.attributes = attributes;
		}

		@Override
		protected void emitContent(String content) throws IOException {
			//[Example|http://example.com|title]
			// [Example|http://example.com]
			String linkContent = content;
			if (!Strings.isNullOrEmpty(content)) {
				linkContent += " | "; //$NON-NLS-1$
			}
			if (attributes.getHref() != null) {
				linkContent += attributes.getHref();
			}
			if (!Strings.isNullOrEmpty(attributes.getTitle())) {
				linkContent += " | "; //$NON-NLS-1$
				linkContent += attributes.getTitle();
			}

			super.emitContent(linkContent);
		}
	}

	private class TableCellBlock extends ContentBlock {
		public TableCellBlock(BlockType blockType) {
			super(blockType, blockType == BlockType.TABLE_CELL_NORMAL ? "|" : "||", "", false, true, 0, 0); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		@Override
		protected void emitContent(String content) throws IOException {
			if (Strings.isNullOrEmpty(content)) {
				content = " "; //$NON-NLS-1$
			}
			content = content.replaceAll("(\\r|\\n)+", " "); //$NON-NLS-1$ //$NON-NLS-2$
			super.emitContent(content);
		}
	}

	private class ImplicitParagraphBlock extends ContentBlock {

		ImplicitParagraphBlock() {
			super(BlockType.PARAGRAPH, "", "", false, false, 2, 2); //$NON-NLS-1$//$NON-NLS-2$
		}

		@Override
		protected boolean isImplicitBlock() {
			return true;
		}
	}

	public ConfluenceDocumentBuilder(Writer out) {
		super(out);
		currentBlock = null;
	}

	@Override
	protected Block computeBlock(BlockType type, Attributes attributes) {
		switch (type) {
		case BULLETED_LIST:
		case DEFINITION_LIST:
		case NUMERIC_LIST:
			if (currentBlock != null) {
				BlockType currentBlockType = currentBlock.getBlockType();
				if (currentBlockType == BlockType.LIST_ITEM || currentBlockType == BlockType.DEFINITION_ITEM
						|| currentBlockType == BlockType.DEFINITION_TERM) {
					return new NewlineDelimitedBlock(type, 1, 1);
				}
			}
			return new NewlineDelimitedBlock(type, 2, 1);
		case CODE:
			return new ContentBlock(type, "{code}", "{code}\n\n", false, false, 2, 2); //$NON-NLS-1$ //$NON-NLS-2$
		case DEFINITION_ITEM:
		case DEFINITION_TERM:
		case LIST_ITEM:
			char prefixChar = computeCurrentListType() == BlockType.NUMERIC_LIST ? '#' : '*';
			return new ContentBlock(type, computePrefix(prefixChar, computeListLevel()) + " ", "", false, true, 1, 1); //$NON-NLS-1$ //$NON-NLS-2$
		case DIV:
			if (currentBlock == null) {
				return new ContentBlock(type, "", "", false, false, 2, 2); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				return new ContentBlock(type, "", "", true, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			}
		case FOOTNOTE:
			return new ContentBlock(type, "fn1. ", "", false, false, 2, 2); //$NON-NLS-1$ //$NON-NLS-2$
		case INFORMATION:
		case NOTE:
		case PANEL:
		case TIP:
		case WARNING:
			attributes.appendCssClass(type.name().toLowerCase());
		case PARAGRAPH:
			String attributesMarkup = computeAttributes(attributes);

			return new ContentBlock(type, attributesMarkup, "", false, false, 2, 2); //$NON-NLS-1$
		case PREFORMATTED:
			return new ContentBlock(type, "{noformat}", "{noformat}", false, false, 2, 2); //$NON-NLS-1$ //$NON-NLS-2$
		case QUOTE:
			return new ContentBlock(type, "{quote}", "{quote}", false, false, 2, 2); //$NON-NLS-1$ //$NON-NLS-2$
		case TABLE:
			return new SuffixBlock(type, "\n"); //$NON-NLS-1$
		case TABLE_CELL_HEADER:
		case TABLE_CELL_NORMAL:
			return new TableCellBlock(type);
		case TABLE_ROW:
			return new SuffixBlock(type, "|\n"); //$NON-NLS-1$
		default:
			Logger.getLogger(getClass().getName()).warning("Unexpected block type: " + type); //$NON-NLS-1$
			return new ContentBlock(type, "", "", false, false, 0, 0); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	@Override
	protected Block computeSpan(SpanType type, Attributes attributes) {
		Block block;
		String spanAttributes = computeAttributes(attributes);
		switch (type) {
		case BOLD:
			block = new ContentBlock("*" + spanAttributes, "*", true, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			break;
		case CITATION:
			block = new ContentBlock("??" + spanAttributes, "??", true, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			break;
		case DELETED:
			block = new ContentBlock("-" + spanAttributes, "-", true, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			break;
		case EMPHASIS:
		case ITALIC:
			block = new ContentBlock("_" + spanAttributes, "_", true, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			break;
		case INSERTED:
			block = new ContentBlock("+" + spanAttributes, "+", true, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			break;
		case CODE:
			block = new ContentBlock("@" + spanAttributes, "@", true, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			break;
		case LINK:
			if (attributes instanceof LinkAttributes) {
				block = new LinkBlock((LinkAttributes) attributes);
			} else {
				block = new ContentBlock("%" + spanAttributes, "%", true, true, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			}
			break;
		case MONOSPACE:
			block = new ContentBlock("{{", "}}", true, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			break;
		case STRONG:
			block = new ContentBlock("*" + spanAttributes, "*", true, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			break;
		case SUPERSCRIPT:
			block = new ContentBlock("^" + spanAttributes, "^", true, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			break;
		case SUBSCRIPT:
			block = new ContentBlock("~" + spanAttributes, "~", true, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			break;
		case UNDERLINED:
			block = new ContentBlock("+", "+", true, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			break;
//			case QUOTE: not supported

		case SPAN:
		default:
			block = null;
			if (attributes.getCssStyle() != null) {
				Matcher colorMatcher = Pattern.compile("color:\\s*([^; \t]+)").matcher(attributes.getCssStyle()); //$NON-NLS-1$
				if (colorMatcher.find()) {
					String color = colorMatcher.group(1);
					if (color.equalsIgnoreCase("black") || color.equals("#010101")) { //$NON-NLS-1$ //$NON-NLS-2$
						color = null;
					}
					if (color != null) {
						block = new ContentBlock("{color:" + color + "}", "{color}", true, false, 0, 0); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
			}
			if (block == null) {
				block = new ContentBlock("", "", false, false, 0, 0); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		return block;
	}

	private String computeAttributes(Attributes attributes) {
		String attributeMarkup = ""; //$NON-NLS-1$

		return attributeMarkup;
	}

	@Override
	protected boolean isSeparator(int i) {
		return !isSpanMarkup((char) i) && super.isSeparator(i);
	}

	private boolean isSpanMarkup(char character) {
		return SPAN_MARKUP_CHARACTERS.matches(character);
	}

	@Override
	protected ContentBlock computeHeading(int level, Attributes attributes) {
		return new ContentBlock("h" + level + ". ", "", false, false, 2, 2); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public void characters(String text) {
		assertOpenBlock();
		try {
			for (int x = 0; x < text.length(); ++x) {
				char c = text.charAt(x);
				switch (c) {
				case '\u00A0':// &nbsp;
					currentBlock.write(' ');
					break;
				case '\u00A9': // &copy;
					currentBlock.write("(c)"); //$NON-NLS-1$
					break;
				case '\u00AE': // &reg;
					currentBlock.write("(r)"); //$NON-NLS-1$
					break;
				default:
					currentBlock.write(c);
					break;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void entityReference(String entity) {
		assertOpenBlock();
		String literal = entityToLiteral.get(entity);
		if (literal == null) {
			literal = "&" + entity + ";"; //$NON-NLS-1$//$NON-NLS-2$
		}
		try {
			currentBlock.write(literal);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void image(Attributes attributes, String url) {
		if (url != null) {
			assertOpenBlock();
			try {
				currentBlock.write('!');
				writeAttributes(attributes);
				currentBlock.write(url);
				currentBlock.write('!');
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void link(Attributes attributes, String hrefOrHashName, String text) {
		assertOpenBlock();
		LinkAttributes linkAttributes = new LinkAttributes();
		linkAttributes.setTitle(attributes.getTitle());
		linkAttributes.setHref(hrefOrHashName);
		beginSpan(SpanType.LINK, linkAttributes);
		characters(text);
		endSpan();
	}

	@Override
	public void imageLink(Attributes linkAttributes, Attributes imageAttributes, String href, String imageUrl) {
		assertOpenBlock();
		try {
			currentBlock.write('!');
			writeAttributes(imageAttributes);
			currentBlock.write(imageUrl);
			currentBlock.write('!');
			currentBlock.write(':');
			currentBlock.write(href);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void acronym(String text, String definition) {
		assertOpenBlock();
		try {
			currentBlock.write(text);
			currentBlock.write('(');
			currentBlock.write(definition);
			currentBlock.write(')');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void lineBreak() {
		assertOpenBlock();
		try {
			if (currentBlock instanceof ConfluenceBlock) {
				((ConfluenceBlock) currentBlock).writeLineBreak();
			} else {
				currentBlock.write('\n');
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeAttributes(Attributes attributes) {

		try {
			currentBlock.write(computeAttributes(attributes));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Block createImplicitParagraphBlock() {
		return new ImplicitParagraphBlock();
	}

}
