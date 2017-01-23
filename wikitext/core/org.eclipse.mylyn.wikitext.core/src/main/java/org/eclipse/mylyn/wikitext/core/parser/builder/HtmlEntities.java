/*******************************************************************************
 * Copyright (c) 2007, 2016 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.wikitext.core.parser.builder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

class HtmlEntities {

	private static HtmlEntities instance = new HtmlEntities();

	private static ListMultimap<String, String> readHtmlEntities() {
		ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					HtmlDocumentBuilder.class.getResourceAsStream("html-entity-references.txt"), Charsets.UTF_8)); //$NON-NLS-1$
			try {
				Splitter splitter = Splitter.on(CharMatcher.WHITESPACE).trimResults().omitEmptyStrings();

				String line;
				while ((line = reader.readLine()) != null) {
					List<String> lineItems = splitter.splitToList(line);
					checkState(lineItems.size() > 1);
					for (int x = 1; x < lineItems.size(); ++x) {
						builder.put(lineItems.get(0), lineItems.get(x));
					}
				}
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
		return builder.build();
	}

	public static HtmlEntities instance() {
		return instance;
	}

	private final ListMultimap<String, String> nameToNumericEntityReferences;

	private final Map<String, String> nameToStringEquivalent;

	private HtmlEntities() {
		nameToNumericEntityReferences = readHtmlEntities();
		nameToStringEquivalent = createNameToStringEquivalent(nameToNumericEntityReferences);
	}

	public List<String> nameToEntityReferences(String name) {
		return nameToNumericEntityReferences.get(name);
	}

	public String nameToStringEquivalent(String name) {
		return nameToStringEquivalent.get(name);
	}

	private Map<String, String> createNameToStringEquivalent(
			ListMultimap<String, String> nameToNumericEntityReferences) {
		ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
		for (String name : nameToNumericEntityReferences.keySet()) {
			mapBuilder.put(name, stringEquivalent(nameToNumericEntityReferences.get(name)));
		}
		return mapBuilder.build();
	}

	private String stringEquivalent(List<String> values) {
		return Normalizer.normalize(values.stream().map(s -> numericEntityToString(s)).collect(Collectors.joining()),
				Normalizer.Form.NFC);
	}

	private String numericEntityToString(String s) {
		checkArgument(s.charAt(0) == '#');
		return String.valueOf((char) Integer.parseInt(s.substring(1)));
	}
}
