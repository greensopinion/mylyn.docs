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

package org.eclipse.mylyn.wikitext.tests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtil {
	public static String tagFragment(String tagName, String html) {
		Pattern pattern = Pattern.compile("<" + tagName + ".*?>.*?</" + tagName + ">", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(html);
		if (!matcher.find()) {
			return null;
		}
		return matcher.group();
	}
}
