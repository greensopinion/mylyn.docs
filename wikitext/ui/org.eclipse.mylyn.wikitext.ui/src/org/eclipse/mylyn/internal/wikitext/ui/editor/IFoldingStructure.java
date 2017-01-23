/*******************************************************************************
 * Copyright (c) 2009, 2011 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.ui.editor;

import java.util.Collection;

import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineItem;

/**
 * a means of controlling the folding structure Obtain an instance of this interface as follows: <code>
 * editor.getAdapter(IFoldingStructure.class)
 * </code>
 * 
 * @author dgreen
 */
public interface IFoldingStructure {
	/**
	 * collapse the given items. Does nothing if folding is not {@link #isFoldingEnabled() enabled}.
	 * 
	 * @param items
	 *            the items to collapse
	 * @param collapseRegionContainingCaret
	 *            indicate if the region containing the caret should be collapsed
	 */
	public void collapseElements(Collection<OutlineItem> items, boolean collapseRegionContainingCaret);

	/**
	 * expand the given items. Does nothing if folding is not {@link #isFoldingEnabled() enabled}.
	 * 
	 * @param items
	 *            the items to expand
	 * @param collapseRegionContainingCaret
	 *            indicate if the region containing the caret should be collapsed
	 */
	public void expandElements(Collection<OutlineItem> items);

	/**
	 * expand the given items and collapse all others. Does nothing if folding is not {@link #isFoldingEnabled()
	 * enabled}.
	 * 
	 * @param items
	 *            the items to expand
	 * @param collapseRegionContainingCaret
	 *            indicate if the region containing the caret should be collapsed
	 */
	public void expandElementsExclusive(Collection<OutlineItem> items, boolean collapseRegionContainingCaret);

	/**
	 * collapse all items. Does nothing if folding is not {@link #isFoldingEnabled() enabled}.
	 * 
	 * @param collapseRegionContainingCaret
	 *            indicate if the region containing the caret should be collapsed
	 */
	public void collapseAll(boolean collapseRegionContainingCaret);

	/**
	 * expand all items. Does nothing if folding is not {@link #isFoldingEnabled() enabled}.
	 */
	public void expandAll();

	/**
	 * indicate if folding is enabled.
	 * 
	 * @return true if folding is enabled, otherwise false
	 */
	public boolean isFoldingEnabled();
}
