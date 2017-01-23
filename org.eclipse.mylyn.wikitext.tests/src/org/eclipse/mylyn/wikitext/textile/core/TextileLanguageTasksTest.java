/*******************************************************************************
 * Copyright (c) 2009, 2012 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.wikitext.textile.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.eclipse.mylyn.internal.wikitext.tasks.ui.util.Util;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguageConfiguration;
import org.eclipse.mylyn.wikitext.tests.EclipseRuntimeRequired;
import org.eclipse.mylyn.wikitext.tests.TestUtil;

import junit.framework.TestCase;

/**
 * tests for Textile that involve the tasks plug-in and dependencies on the Eclipse runtime.
 * 
 * @author David Green
 */
@EclipseRuntimeRequired
public class TextileLanguageTasksTest extends TestCase {

	private MarkupParser parser;

	private TextileLanguage markupLanguage;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		initParser();
	}

	private void initParser() throws IOException {
		parser = new MarkupParser();
		markupLanguage = new TextileLanguage();

		MarkupLanguageConfiguration configuration = Util.create("bugzilla");
		markupLanguage.configure(configuration);

		parser.setMarkupLanguage(markupLanguage);
	}

	public void testSubversiveBugReport() throws IOException {

		StringWriter out = new StringWriter();
		parser.setBuilder(new HtmlDocumentBuilder(out));

		Reader reader = new InputStreamReader(
				TextileLanguageTest.class.getResourceAsStream("resources/subversive-bug-report.txt"), "utf-8");
		try {
			long time = System.currentTimeMillis();
			parser.parse(reader);
			long endTime = System.currentTimeMillis();
			TestUtil.println(String.format("Took %s millis", endTime - time));
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			reader.close();
		}
//		String html = out.toString();

//		TestUtil.println(html);
	}
}
