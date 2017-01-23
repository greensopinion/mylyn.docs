/*******************************************************************************
 * Copyright (c) 2013, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.html.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentHandler;
import org.eclipse.mylyn.wikitext.html.core.HtmlLanguage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class HtmlSubsetLanguage extends HtmlLanguage {

	private final Set<BlockType> supportedBlockTypes;

	private final Set<SpanType> supportedSpanTypes;

	private final int headingLevel;

	private final HtmlDocumentHandler documentHandler;

	private final List<SpanHtmlElementStrategy> spanElementStrategies;

	private final Map<SpanType, String> tagNameSubstitutions;

	private final boolean xhtmlStrict;

	public HtmlSubsetLanguage(String name, HtmlDocumentHandler documentHandler, int headingLevel,
			Set<BlockType> blockTypes, Set<SpanType> spanTypes, Map<SpanType, String> tagNameSubstitutions,
			List<SpanHtmlElementStrategy> spanElementStrategies) {
		this(name, documentHandler, headingLevel, blockTypes, spanTypes, tagNameSubstitutions, spanElementStrategies,
				false);
	}

	public HtmlSubsetLanguage(String name, HtmlDocumentHandler documentHandler, int headingLevel,
			Set<BlockType> blockTypes, Set<SpanType> spanTypes, Map<SpanType, String> tagNameSubstitutions,
			List<SpanHtmlElementStrategy> spanElementStrategies, boolean xhtmlStrict) {
		setName(checkNotNull(name));
		this.documentHandler = documentHandler;
		checkArgument(headingLevel >= 0 && headingLevel <= 6, "headingLevel must be between 0 and 6"); //$NON-NLS-1$
		this.headingLevel = headingLevel;
		this.supportedBlockTypes = ImmutableSet.copyOf(checkNotNull(blockTypes));
		this.supportedSpanTypes = ImmutableSet.copyOf(checkNotNull(spanTypes));
		this.tagNameSubstitutions = ImmutableMap.copyOf(checkNotNull(tagNameSubstitutions));
		this.spanElementStrategies = ImmutableList.copyOf(checkNotNull(spanElementStrategies));
		this.xhtmlStrict = xhtmlStrict;

		assertSubstitutedAreSupported();
	}

	public Set<BlockType> getSupportedBlockTypes() {
		return supportedBlockTypes;
	}

	public Set<SpanType> getSupportedSpanTypes() {
		return supportedSpanTypes;
	}

	public int getSupportedHeadingLevel() {
		return headingLevel;
	}

	public Map<SpanType, String> getTagNameSubstitutions() {
		return tagNameSubstitutions;
	}

	@Override
	public HtmlSubsetDocumentBuilder createDocumentBuilder(Writer out, boolean formatting) {
		HtmlSubsetDocumentBuilder builder = new HtmlSubsetDocumentBuilder(out, formatting);
		builder.setSupportedHeadingLevel(headingLevel);
		builder.setSupportedSpanTypes(supportedSpanTypes, spanElementStrategies);
		builder.setSupportedBlockTypes(supportedBlockTypes);
		builder.setXhtmlStrict(xhtmlStrict);
		addSpanTagNameSubstitutions(builder);
		if (documentHandler != null) {
			builder.setDocumentHandler(documentHandler);
		}
		return builder;
	}

	private void addSpanTagNameSubstitutions(HtmlSubsetDocumentBuilder builder) {
		for (Entry<SpanType, String> entry : tagNameSubstitutions.entrySet()) {
			builder.setElementNameOfSpanType(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public HtmlSubsetLanguage clone() {
		HtmlSubsetLanguage copy = new HtmlSubsetLanguage(getName(), documentHandler, headingLevel, supportedBlockTypes,
				supportedSpanTypes, tagNameSubstitutions, spanElementStrategies, xhtmlStrict);
		copy.setFileExtensions(getFileExtensions());
		copy.setExtendsLanguage(getExtendsLanguage());
		copy.setParseCleansHtml(isParseCleansHtml());
		return copy;
	}

	private void assertSubstitutedAreSupported() {
		for (SpanType spanType : tagNameSubstitutions.keySet()) {
			checkState(supportedSpanTypes.contains(spanType),
					"SpanType [%s] is unsupported. Cannot add substitution to unsupported span types.", spanType); //$NON-NLS-1$
		}
	}

	public boolean isXhtmlStrict() {
		return xhtmlStrict;
	}
}
