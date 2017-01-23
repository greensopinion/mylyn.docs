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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public abstract class LineSequence implements Iterable<Line> {

	static abstract class ForwardLineSequence extends LineSequence {

		abstract Line getNextLine(int i);
	}

	public static LineSequence create(String content) {
		return new ContentLineSequence(content);
	}

	public abstract Line getCurrentLine();

	public abstract Line getNextLine();

	public abstract void advance();

	public void advance(int count) {
		checkArgument(count >= 0);
		for (int x = 0; x < count; ++x) {
			advance();
		}
	}

	@Override
	public Iterator<Line> iterator() {
		Predicate<Line> predicate = Predicates.<Line> alwaysTrue();
		return iterator(predicate);
	}

	private Iterator<Line> iterator(Predicate<Line> predicate) {
		return new LinesIterable(this, predicate).iterator();
	}

	public LineSequence with(Predicate<Line> predicate) {
		return new PredicateLineSequence(this, predicate);
	}

	public LineSequence transform(Function<Line, Line> transform) {
		return new TransformLineSequence(this, transform);
	}

	public abstract LineSequence lookAhead();

	@Override
	public String toString() {
		return Objects.toStringHelper(LineSequence.class)
				.add("currentLine", getCurrentLine())
				.add("nextLine", getNextLine())
				.toString();
	}
}
