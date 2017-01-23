/*******************************************************************************
 * Copyright (c) 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.core.maven;

class BuildFailureException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BuildFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public BuildFailureException(String message) {
		super(message);
	}

}
