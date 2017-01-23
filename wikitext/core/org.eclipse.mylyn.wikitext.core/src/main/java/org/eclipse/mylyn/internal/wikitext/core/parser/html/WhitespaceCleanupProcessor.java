/*******************************************************************************
 * Copyright (c) 2011, 2015 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.core.parser.html;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.google.common.collect.ImmutableSet;

/**
 * @author David Green
 */
class WhitespaceCleanupProcessor extends DocumentProcessor {

	private final Set<String> CHILD_TAGS = ImmutableSet.of("li", "th", "tr", "td"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	@Override
	public void process(Document document) {
		Element body = document.body();

		moveLeadingOrTrailingSpaceOutOfElements(body);
		removeWhitespaceImmeditatelyPrecedingBrTags(body);
	}

	private void moveLeadingOrTrailingSpaceOutOfElements(Element body) {
		Set<Node> affectedParents = new HashSet<Node>();
		for (Element element : body.getAllElements()) {
			if (!Html.isWhitespacePreserve(element)) {
				normalizeTextNodes(element);
				List<Node> children = element.childNodes();
				if (!children.isEmpty()) {
					Node firstChild = children.get(0);
					if (firstChild instanceof TextNode) {
						TextNode textNode = (TextNode) firstChild;
						String text = textNode.getWholeText();
						int nonWhitespaceIndex = firstIndexOfNonWhitespace(text);
						if (nonWhitespaceIndex > 0) {
							affectedParents.add(textNode.parent());

							// split
							textNode.splitText(nonWhitespaceIndex);
							// move outside
							textNode.remove();
							computeBeforeTarget(element).before(textNode);

							affectedParents.add(textNode.parent());
						} else if (nonWhitespaceIndex == -1) {
							// move outside
							textNode.remove();
							computeAfterTarget(element).after(textNode);

							affectedParents.add(textNode.parent());
						}
					}
					normalizeEmptySpaceBetweenNodes(element);
					children = element.childNodes();
					if (!children.isEmpty()) {

						Node lastChild = children.get(children.size() - 1);
						if (lastChild instanceof TextNode) {

							TextNode textNode = (TextNode) lastChild;
							String text = textNode.getWholeText();
							int lastNonWhitespaceIndex = lastIndexOfNonWhitespace(text);
							if (lastNonWhitespaceIndex < 0) {
								// move outside
								textNode.remove();
								computeAfterTarget(element).after(textNode);

								affectedParents.add(textNode.parent());
							} else if (lastNonWhitespaceIndex < (text.length() - 1)) {
								affectedParents.add(textNode.parent());

								// split
								textNode.splitText(lastNonWhitespaceIndex + 1);
								// move outside
								textNode = (TextNode) textNode.nextSibling();
								textNode.remove();
								computeAfterTarget(element).after(textNode);

								affectedParents.add(textNode.parent());
							}
						}
					}
				}
				if (!affectedParents.isEmpty()) {
					for (Node parent : affectedParents) {
						if (parent instanceof Element) {
							normalizeTextNodes((Element) parent);
						}
					}
					affectedParents.clear();
				}
			}
		}
	}

	private void normalizeEmptySpaceBetweenNodes(Element parent) {
		List<Node> children = parent.childNodes();
		if (!children.isEmpty()) {
			children = new ArrayList<>(children);
			for (Node child : children) {
				Node previousSibling = child.previousSibling();
				Node nextSibling = child.nextSibling();
				if (child instanceof TextNode && previousSibling instanceof Element && nextSibling instanceof Element) {
					TextNode textNode = (TextNode) child;
					Element prevElement = (Element) previousSibling;
					Element nextElement = (Element) nextSibling;
					normalizeTextBetweenNodes(textNode, prevElement, nextElement);
				}
			}
		}
	}

	private void normalizeTextBetweenNodes(TextNode textNode, Element prevElement, Element nextElement) {
		String wholeText = StringUtil.normaliseWhitespace(textNode.getWholeText()).trim();
		if (wholeText.isEmpty()) {
			boolean isSurroundedByEqualTags = nextElement.tagName().equals(prevElement.tagName())
					&& CHILD_TAGS.contains(nextElement.tagName());
			if (isSurroundedByEqualTags) {
				textNode.remove();
			}
		}
	}

	private void removeWhitespaceImmeditatelyPrecedingBrTags(Element body) {
		for (Element element : body.getElementsByTag("br")) { //$NON-NLS-1$
			removeWhitespaceBefore(element);
		}
	}

	private void removeWhitespaceBefore(Element element) {
		Node previousSibling = element.previousSibling();
		if (previousSibling instanceof TextNode) {
			TextNode textNode = (TextNode) previousSibling;
			String text = textNode.getWholeText();
			int startOfTrailingWhitespace = lastIndexOfNonWhitespace(text) + 1;
			if (startOfTrailingWhitespace <= 0) {
				textNode.remove();
			} else if (startOfTrailingWhitespace < text.length()) {
				textNode.splitText(startOfTrailingWhitespace);
				textNode.nextSibling().remove();
			}
		}
	}

	private Element computeAfterTarget(Element element) {
		if (element.parent() != null && !element.nodeName().equalsIgnoreCase("html")) { //$NON-NLS-1$
			List<Node> elementParentChildNodes = element.parent().childNodes();
			if (elementParentChildNodes.size() == 1
					|| elementParentChildNodes.get(elementParentChildNodes.size() - 1) == element) {
				return computeAfterTarget(element.parent());
			}
		}
		return element;
	}

	private Element computeBeforeTarget(Element element) {
		if (element.parent() != null && !element.parent().nodeName().equalsIgnoreCase("html")) { //$NON-NLS-1$
			List<Node> elementParentChildNodes = element.parent().childNodes();
			if (elementParentChildNodes.size() == 1 || elementParentChildNodes.get(0) == element) {
				return computeBeforeTarget(element.parent());
			}
		}
		return element;
	}

	private static int lastIndexOfNonWhitespace(String text) {
		int i = text.length() - 1;
		while (i > -1) {
			if (!Character.isWhitespace(text.charAt(i))) {
				return i;
			}
			--i;
		}
		return i;
	}

	private static int firstIndexOfNonWhitespace(String text) {
		int i = 0;
		while (i < text.length()) {
			if (!Character.isWhitespace(text.charAt(i))) {
				return i;
			}
			++i;
		}
		return -1;
	}

}
