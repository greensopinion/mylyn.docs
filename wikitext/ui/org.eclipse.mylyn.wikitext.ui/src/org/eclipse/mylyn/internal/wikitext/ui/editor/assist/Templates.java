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
package org.eclipse.mylyn.internal.wikitext.ui.editor.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.templates.Template;

/**
 * @author David Green
 */
public class Templates {

	private String markupLanguageName;

	private final List<Template> template = new ArrayList<>();

	private final Set<Template> blockTemplates = new HashSet<>();

	private Templates parent;

	public void setMarkupLanguageName(String markupLanguageName) {
		this.markupLanguageName = markupLanguageName;
	}

	public String getMarkupLanguageName() {
		return markupLanguageName;
	}

	public List<Template> getTemplate() {
		if (parent != null) {
			List<Template> parentTemplate = parent.getTemplate();
			List<Template> list = new ArrayList<>(template.size() + parentTemplate.size());
			list.addAll(parentTemplate);
			list.addAll(template);
			return Collections.unmodifiableList(list);
		}
		return Collections.unmodifiableList(template);
	}

	public void addTemplate(Template template, boolean block) {
		this.template.add(template);
		if (block) {
			this.blockTemplates.add(template);
		}
	}

	public void addAll(Templates other) {
		this.template.addAll(other.template);
		this.blockTemplates.addAll(other.template);
	}

	public boolean isBlock(Template template) {
		return blockTemplates.contains(template) || (parent != null && parent.isBlock(template));
	}

	public Templates getParent() {
		return parent;
	}

	public void setParent(Templates parent) {
		this.parent = parent;
	}
}
