/*******************************************************************************
 * Copyright (c) 2007, 2011 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.wikitext.ui.editor.syntax;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;

/**
 * @author David Green
 */
public class Block extends Segment<Segment<?>> {

	private final BlockType type;

	private final int headingLevel;

	private boolean spansComputed = false;

	public Block(BlockType type, int offset, int length) {
		super(offset, length);
		this.type = type;
		headingLevel = 0;
	}

	public Block(int headingLevel, int offset, int length) {
		super(offset, length);
		if (headingLevel <= 0) {
			throw new IllegalArgumentException();
		}
		this.headingLevel = headingLevel;
		type = null;
	}

	public Block(BlockType type, Attributes attributes, int offset, int length) {
		super(attributes, offset, length);
		this.type = type;
		headingLevel = 0;
	}

	public Block(int headingLevel, Attributes attributes, int offset, int length) {
		super(attributes, offset, length);
		this.headingLevel = headingLevel;
		type = null;
	}

	@Override
	public Block getParent() {
		return (Block) super.getParent();
	}

	/**
	 * the type of block
	 * 
	 * @return the block type, or null if this block is a heading
	 */
	public BlockType getType() {
		return type;
	}

	/**
	 * the heading level, or 0 if this is not a heading.
	 */
	public int getHeadingLevel() {
		return headingLevel;
	}

	public boolean isSpansComputed() {
		return spansComputed;
	}

	public void setSpansComputed(boolean spansComputed) {
		this.spansComputed = spansComputed;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("<"); //$NON-NLS-1$
		String elementName;
		if (type != null) {
			elementName = type.name();
		} else {
			elementName = "h" + headingLevel; //$NON-NLS-1$
		}
		buf.append(elementName);
		buf.append(" offset=\""); //$NON-NLS-1$
		buf.append(getOffset());
		buf.append("\" length=\""); //$NON-NLS-1$
		buf.append(getLength());
		buf.append('"');
		if (getChildren() == null || getChildren().isEmpty()) {
			buf.append("/>\n"); //$NON-NLS-1$
		} else {
			buf.append(">\n"); //$NON-NLS-1$
			StringBuilder buf2 = new StringBuilder();
			buf2.append("\t"); //$NON-NLS-1$
			for (Segment<?> child : getChildren().asList()) {
				buf2.append(child);
			}
			String children = buf2.toString();
			children = children.replace("\n", "\n\t"); //$NON-NLS-1$ //$NON-NLS-2$
			if (children.endsWith("\t")) { //$NON-NLS-1$
				children = children.substring(0, children.length() - 1);
			} else {
				children = children + "\n"; //$NON-NLS-1$
			}
			buf.append(children);
			buf.append("</"); //$NON-NLS-1$
			buf.append(elementName);
			buf.append(">\n"); //$NON-NLS-1$
		}
		return buf.toString();
	}
}
