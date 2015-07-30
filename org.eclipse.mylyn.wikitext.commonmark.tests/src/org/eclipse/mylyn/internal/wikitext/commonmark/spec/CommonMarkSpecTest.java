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

package org.eclipse.mylyn.internal.wikitext.commonmark.spec;

import static org.eclipse.mylyn.internal.wikitext.commonmark.CommonMarkAsserts.assertContent;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mylyn.wikitext.commonmark.CommonMarkLanguage;
import org.eclipse.mylyn.wikitext.core.util.LocationTrackingReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;

@RunWith(Parameterized.class)
public class CommonMarkSpecTest {

	private static final String SPEC_VERSION = "0.21";

	private static final URI COMMONMARK_SPEC_URI = URI
			.create(String.format("https://raw.githubusercontent.com/jgm/CommonMark/%s/spec.txt", SPEC_VERSION));

	private static final Set<String> HEADING_EXCLUSIONS = ImmutableSet.of();

	private static final Set<Integer> LINE_EXCLUSIONS = ImmutableSet.of(//
			281, // Tabs
			1831, // HTML blocks
			1843, // HTML blocks
			1853, // HTML blocks
			1869, // HTML blocks
			1883, // HTML blocks
			1915, // HTML blocks
			2008, // HTML blocks
			2016, // HTML blocks
			2054, // HTML blocks
			2070, // HTML blocks
			2078, // HTML blocks
			2399, // Link reference definitions
			2478, // Link reference definitions
			2515, // Link reference definitions
			3380, // List items
			3404, // List items
			3766, // List items
			3789, // List items
			3801, // List items
			3817, // List items
			4401, // Lists
			4425, // Lists
			4664, // Lists
			4681, // Lists
			4814, // Backslash escapes
			4863, // Entities
			4880, // Entities
			4928, // Entities
			4948, // Entities
			5117, // Code spans
			6505, // Links
			6965, // Links
			6974, // Links
			7646, // Raw HTML
			7703, // Raw HTML
			7737, // Raw HTML
			7772, // Raw HTML
			7780 // Raw HTML
	);

	public static class Expectation {

		final String input;

		final String expected;

		public Expectation(String input, String expected) {
			this.input = input;
			this.expected = expected;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(Expectation.class).add("input", input).add("expected", expected).toString();
		}
	}

	private final Expectation expectation;

	private final String heading;

	private final int lineNumber;

	@Before
	public void preconditions() {
		assumeTrue(!HEADING_EXCLUSIONS.contains(heading));
		assumeTrue(!LINE_EXCLUSIONS.contains(Integer.valueOf(lineNumber)));
	}

	@Test
	public void test() {
		try {
			CommonMarkLanguage language = createCommonMarkLanguage();
			assertContent(language, expectation.expected, expectation.input);
		} catch (AssertionError e) {
			System.out.println(lineNumber + ", // " + heading);
			System.out.flush();
			throw e;
		}
	}

	private CommonMarkLanguage createCommonMarkLanguage() {
		CommonMarkLanguage language = new CommonMarkLanguage();
		language.setStrictlyConforming(true);
		return language;
	}

	@Parameters //(name = "{0} test {index}")
	public static List<Object[]> parameters() {
		ImmutableList.Builder<Object[]> parameters = ImmutableList.builder();

		loadSpec(parameters);

		return parameters.build();
	}

	public CommonMarkSpecTest(String title, String heading, int lineNumber, Expectation expectation) {
		this.heading = heading;
		this.lineNumber = lineNumber;
		this.expectation = expectation;
	}

	private static void loadSpec(ImmutableList.Builder<Object[]> parameters) {
		Pattern headingPattern = Pattern.compile("#+\\s*(.+)");
		try {
			String spec = loadCommonMarkSpec();
			LocationTrackingReader reader = new LocationTrackingReader(new StringReader(spec));
			String heading = "unspecified";
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.replace('→', '\t');
				if (line.trim().equals(".")) {
					int testLineNumber = reader.getLineNumber();
					Expectation expectation = readExpectation(reader);
					parameters.add(
							new Object[] { heading + ":line " + testLineNumber, heading, testLineNumber, expectation });
				}
				Matcher headingMatcher = headingPattern.matcher(line);
				if (headingMatcher.matches()) {
					heading = headingMatcher.group(1);
				}
			}
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	private static Expectation readExpectation(LocationTrackingReader reader) throws IOException {
		String input = readUntilDelimiter(reader);
		String expected = readUntilDelimiter(reader);
		return new Expectation(input, expected);
	}

	private static String readUntilDelimiter(LocationTrackingReader reader) throws IOException {
		List<String> lines = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.replace('→', '\t');
			if (line.trim().equals(".")) {
				break;
			}
			lines.add(line);
		}
		return Joiner.on("\n").join(lines);
	}

	private static String loadCommonMarkSpec() throws IOException {
		File tmpFolder = new File("./tmp");
		if (!tmpFolder.exists()) {
			tmpFolder.mkdir();
		}
		assertTrue(tmpFolder.getAbsolutePath(), tmpFolder.exists());
		File spec = new File(tmpFolder, String.format("spec%s.txt", SPEC_VERSION));
		if (!spec.exists()) {
			try (FileOutputStream out = new FileOutputStream(spec)) {
				Resources.copy(COMMONMARK_SPEC_URI.toURL(), out);
			}
		}
		try (InputStream in = new FileInputStream(spec)) {
			return CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
		}
	}

}