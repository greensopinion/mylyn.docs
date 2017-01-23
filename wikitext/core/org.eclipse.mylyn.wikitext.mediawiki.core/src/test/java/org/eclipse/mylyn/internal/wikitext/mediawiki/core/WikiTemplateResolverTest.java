/*******************************************************************************
 * Copyright (c) 2007, 2010 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.mediawiki.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
import org.eclipse.mylyn.wikitext.mediawiki.core.Template;

import junit.framework.TestCase;

public class WikiTemplateResolverTest extends TestCase {

	private static final String BUG_TEMPLATE_CONTENT = "[https://bugs.eclipse.org/{{{1}}} Bug {{{1}}}]";

	private static final String TEST_TEMPLATE_CONTENT = "XXX";

	private static final String OTHER_TEMPLATE_CONTENT = "alt";

	private WikiTemplateResolver resolver;

	private TemplateProcessor templateProcessor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TestWikiTemplateResolver resolverUnderTest = new TestWikiTemplateResolver();
		resolverUnderTest.setWikiBaseUrl("http://wiki.eclipse.org");

		Map<String, String> serverContent = new HashMap<>();
		serverContent.put("http://wiki.eclipse.org/index.php?title=Template%3Abug&action=raw", BUG_TEMPLATE_CONTENT);
		serverContent.put("http://wiki.eclipse.org/index.php?title=Test&action=raw", TEST_TEMPLATE_CONTENT);
		serverContent.put("http://wiki.eclipse.org/index.php?title=Other%3ATest&action=raw", OTHER_TEMPLATE_CONTENT);
		resolverUnderTest.setServerContent(serverContent);

		MediaWikiLanguage markupLanguage = new MediaWikiLanguage();
		markupLanguage.getTemplateProviders().add(resolverUnderTest);

		this.templateProcessor = new TemplateProcessor(markupLanguage);
		this.resolver = resolverUnderTest;
	}

	public void testResolveTemplateDefault() {
		Template template = resolver.resolveTemplate("bug");
		assertNotNull(template);
		assertEquals("bug", template.getName());
		assertEquals(BUG_TEMPLATE_CONTENT, template.getTemplateMarkup().trim());
	}

	public void testResolveTemplateNoNamespace() {
		Template template = resolver.resolveTemplate(":Test");
		assertNotNull(template);
		assertEquals(TEST_TEMPLATE_CONTENT, template.getTemplateMarkup().trim());
	}

	public void testResolveTemplateOtherNamespace() {
		Template template = resolver.resolveTemplate("Other:Test");
		assertNotNull(template);
		assertEquals("Other:Test", template.getName());
		assertEquals(OTHER_TEMPLATE_CONTENT, template.getTemplateMarkup().trim());
	}

	public void testProcessTemplatesDefault() {
		String markup = templateProcessor.processTemplates("See {{bug|468237}}!");
		assertEquals("See [https://bugs.eclipse.org/468237 Bug 468237]!", markup);
	}

	public void testProcessTemplatesNoNamespace() {
		String markup = templateProcessor.processTemplates("Include {{:Test}} content!");
		assertEquals("Include XXX content!", markup);
	}

	public void testProcessTemplatesOtherNamespace() {
		String markup = templateProcessor.processTemplates("Include {{Other:Test}} content!");
		assertEquals("Include alt content!", markup);
	}

}
