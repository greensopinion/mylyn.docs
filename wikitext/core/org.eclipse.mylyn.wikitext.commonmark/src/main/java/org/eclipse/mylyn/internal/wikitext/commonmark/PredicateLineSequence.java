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

package org.eclipse.mylyn.internal.wikitext.commonmark;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Predicate;

class PredicateLineSequence extends LineSequence {

	private final LineSequence delegate;

	private final Predicate<Line> predicate;

	public PredicateLineSequence(LineSequence delegate, Predicate<Line> predicate) {
		this.delegate = checkNotNull(delegate);
		this.predicate = checkNotNull(predicate);
	}

	@Override
	public Line getCurrentLine() {
		return filter(delegate.getCurrentLine());
	}

	@Override
	public Line getNextLine() {
		return filter(delegate.getNextLine());
	}

	@Override
	public void advance() {
		if (getCurrentLine() != null) {
			delegate.advance();
		}
	}

	@Override
	public LineSequence lookAhead() {
		return new PredicateLineSequence(delegate.lookAhead(), predicate);
	}

	private Line filter(Line line) {
		if (line != null && predicate.apply(line)) {
			return line;
		}
		return null;
	}
}
