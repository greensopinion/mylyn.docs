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
package org.eclipse.mylyn.wikitext.ui.annotation;

import org.eclipse.mylyn.wikitext.tests.HeadRequired;

import junit.framework.TestCase;

/**
 * @author David Green
 */
@HeadRequired
public class AnchorHrefAnnotationTest extends TestCase {

	public void testSimple() {
		String href = "http://foo.bar";
		AnchorHrefAnnotation annotation = new AnchorHrefAnnotation(href);
		assertEquals(href, annotation.getAnchorHref());
		assertEquals(AnchorHrefAnnotation.TYPE, annotation.getType());
	}
}
