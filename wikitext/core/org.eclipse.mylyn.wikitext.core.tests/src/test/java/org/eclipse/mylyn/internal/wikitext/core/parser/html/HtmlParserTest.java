/*******************************************************************************
 * Copyright (c) 2011, 2014 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.core.parser.html;

import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author David Green
 */
public class HtmlParserTest extends AbstractSaxParserTest {

	@Override
	protected AbstractSaxHtmlParser createParser() {
		return new HtmlParser();
	}

	public void testBasicMalformed() throws IOException, SAXException {
		performTest("<p>foo<br>bar</p>", "foo\nbar\n\n");
	}

	public void testBasicMalformed2() throws IOException, SAXException {
		performTest("<p>foo<p>bar", "foo\n\nbar\n\n");

	}

	@Test
	public void testSignificantWhitespaceNotLost() throws IOException, SAXException {
		String input = "<html><body><p>one <b>two</b> three</p></body></html>";
		performTest(input, "one **two** three\n\n");
	}

	@Test
	public void testSignificantWhitespaceNotLost_Clean() throws IOException, SAXException {
		String input = "<html><body><p>one <b>two</b> three</p></body></html>";
		new HtmlCleaner().configure((HtmlParser) parser);

		performTest(input, "one **two** three\n\n");
	}

	@Test
	public void testParseInvalidHtml() throws IOException, SAXException {
		String input = "</font>one <b>two";
		performTest(input, "one **two**\n\n");
	}

	@Test
	public void testParseInvalidHtml_Clean() throws IOException, SAXException {
		String input = "</font>one <b>two";
		new HtmlCleaner().configure((HtmlParser) parser);

		performTest(input, "one **two**\n\n");
	}

	@Test
	public void testParseWhitespaceCleanup() throws IOException, SAXException {
		String input = "one <b>two </b>three";
		new HtmlCleaner().configure((HtmlParser) parser);

		performTest(input, "one **two** three\n\n");
	}

}
