/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2015  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.fm.ui.editors.featuremodel.actions;

import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;

import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelWriter;
import de.ovgu.featureide.fm.ui.editors.ConstraintDialog;

/**
 * Basic implementation for actions on constraints.
 * 
 * @author Christian Becker
 * @author Thomas Thuem
 */
public abstract class AbstractConstraintEditorAction extends Action {

	protected Object viewer;

	protected IFeatureModel featuremodel;

	protected XmlFeatureModelWriter writer;

	protected String featuretext;

	private ISelectionChangedListener listener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			setEnabled(isValidSelection(selection));
		}
	};

	public AbstractConstraintEditorAction(Object viewer, IFeatureModel featuremodel, String menuname) {
		super(menuname);
		this.viewer = viewer;
		this.featuremodel = featuremodel;
		if (viewer instanceof TreeViewer)
			((TreeViewer) viewer).addSelectionChangedListener(listener);
		else
			((GraphicalViewerImpl) viewer).addSelectionChangedListener(listener);
	}

	public void run() {
		writer = new XmlFeatureModelWriter(featuremodel);
		featuretext = writer.writeToString();
	}

	protected void openEditor(IConstraint constraint) {
		new ConstraintDialog(featuremodel, constraint);
	}

	protected abstract boolean isValidSelection(IStructuredSelection selection);

}
