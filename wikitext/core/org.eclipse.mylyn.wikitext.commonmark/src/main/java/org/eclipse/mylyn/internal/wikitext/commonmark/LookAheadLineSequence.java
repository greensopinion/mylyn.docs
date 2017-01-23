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

import static com.google.common.base.Preconditions.checkState;

class LookAheadLineSequence extends LineSequence {

	private final ForwardLineSequence lineSequence;

	private Line currentLine;

	private final Line referenceLine;

	private int index;

	public LookAheadLineSequence(ForwardLineSequence lineSequence) {
		this.lineSequence = lineSequence;
		this.currentLine = lineSequence.getCurrentLine();
		this.referenceLine = currentLine;
		this.index = -1;
	}

	public LookAheadLineSequence(LookAheadLineSequence lookAheadLineSequence) {
		this.lineSequence = lookAheadLineSequence.lineSequence;
		this.currentLine = lookAheadLineSequence.currentLine;
		this.referenceLine = lookAheadLineSequence.referenceLine;
		this.index = lookAheadLineSequence.index;
	}

	@Override
	public Line getCurrentLine() {
		return currentLine;
	}

	@Override
	public Line getNextLine() {
		checkConcurrentModification();
		return lineSequence.getNextLine(index + 1);
	}

	@Override
	public void advance() {
		checkConcurrentModification();
		currentLine = getNextLine();
		++index;
	}

	private void checkConcurrentModification() {
		checkState(referenceLine == lineSequence.getCurrentLine());
	}

	@Override
	public LineSequence lookAhead() {
		return new LookAheadLineSequence(this);
	}

}