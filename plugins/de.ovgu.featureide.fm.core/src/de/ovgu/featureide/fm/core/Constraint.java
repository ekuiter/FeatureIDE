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
package de.ovgu.featureide.fm.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;

import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.prop4j.SatSolver;

import de.ovgu.featureide.fm.core.ConstraintAttribute;
import de.ovgu.featureide.fm.core.FMPoint;
import de.ovgu.featureide.fm.core.FeatureComparator;
import de.ovgu.featureide.fm.core.IGraphicItem;
import de.ovgu.featureide.fm.core.PropertyConstants;
import de.ovgu.featureide.fm.core.IGraphicItem.GraphicItem;
import de.ovgu.featureide.fm.core.base.IFeatureModel;

/**
 * Represents a propositional constraint below the feature diagram.
 * 
 * @author Thomas Thuem
 * @author Florian Proksch
 * @author Stefan Krueger
 */
public class Constraint implements PropertyConstants, IGraphicItem {

	private IFeatureModel featureModel;
	private Node propNode;
	private FMPoint location = new FMPoint(0, 0);
	private boolean featureSelected = false;
	private Collection<IFeature> containedFeatureList = new LinkedList<IFeature>();
	private Collection<IFeature> falseOptionalFeatures = new LinkedList<IFeature>();
	private ConstraintAttribute attribute = ConstraintAttribute.NORMAL;
	private Collection<IFeature> deadFeatures = new LinkedList<IFeature>();

	public Constraint(IFeatureModel featureModel, Node propNode) {
		this.featureModel = featureModel;
		this.propNode = propNode;
	}

	public void setLocation(FMPoint newLocation) {
		location = newLocation;
	}

	public FMPoint getLocation() {
		return location;
	}

	public IFeatureModel getFeatureModel() {
		return featureModel;
	}

	/**
	 * Looks for all dead features if they ares caused dead by this constraint
	 * 
	 * @param solver
	 * @param fm The actual model
	 * @param fmDeadFeatures The dead features the complete model
	 * @return The dead features caused by this constraint
	 */
	public Collection<IFeature> getDeadFeatures(SatSolver solver, IFeatureModel fm, Collection<IFeature> fmDeadFeatures) {
		final Collection<IFeature> deadFeatures;
		final Node propNode = this.getNode();
		final Comparator<IFeature> featComp = new FeatureComparator(true);
		if (propNode != null) {
			deadFeatures = fm.getAnalyser().getDeadFeatures(solver, propNode);
		} else {
			deadFeatures = new TreeSet<IFeature>(featComp);
		}
		final Collection<IFeature> deadFeaturesAfter = new TreeSet<IFeature>(featComp);

		deadFeaturesAfter.addAll(fmDeadFeatures);
		deadFeaturesAfter.retainAll(deadFeatures);
		return deadFeaturesAfter;
	}

	/**
	 * Removes the constraints from the model, and looks for dead features.
	 */
	public Collection<IFeature> getDeadFeatures(IFeatureModel fm, Collection<IFeature> fmDeadFeatures) {
		Collection<IFeature> deadFeaturesBefore = null;
		Node propNode = this.getNode();
		if (propNode != null) {
			fm.removeConstraint(this);
			deadFeaturesBefore = fm.getAnalyser().getDeadFeatures();
			fm.addPropositionalNode(propNode);
			fm.handleModelDataChanged();
		}

		Collection<IFeature> deadFeaturesAfter = new LinkedList<IFeature>();
		for (IFeature f : fmDeadFeatures) {
			IFeature feature = fm.getFeature(f.getName());
			// XXX why can the given feature not be found?
			if (feature != null && !deadFeaturesBefore.contains(feature)) {
				deadFeaturesAfter.add(f);
			}
		}
		return deadFeaturesAfter;
	}

	public void setConstraintAttribute(ConstraintAttribute attri, boolean fire) {
		this.attribute = attri;
		if (fire)
			fire(new PropertyChangeEvent(this, ATTRIBUTE_CHANGED, Boolean.FALSE, Boolean.TRUE));
	}

	public ConstraintAttribute getConstraintAttribute() {
		return attribute;
	}

	public void setFeatureSelected(boolean selected) {
		this.featureSelected = selected;
		fire(new PropertyChangeEvent(this, CONSTRAINT_SELECTED, Boolean.FALSE, Boolean.TRUE));
	}

	public boolean isFeatureSelected() {
		return featureSelected;
	}

	public Node getNode() {
		return propNode;
	}

	/**
	 * Sets the <code>containedFeatureList</code> given by <code>propNode</code>.
	 */
	public void setContainedFeatures() {
		containedFeatureList.clear();
		for (String featureName : propNode.getContainedFeatures()) {
			containedFeatureList.add(featureModel.getFeature(featureName));
		}
	}

	/**
	 * 
	 * @return All {@link IFeature}s contained at this {@link Constraint}.
	 */
	public Collection<IFeature> getContainedFeatures() {
		if (containedFeatureList.isEmpty()) {
			setContainedFeatures();
		}
		return containedFeatureList;
	}

	public boolean setFalseOptionalFeatures(IFeatureModel clone, Collection<IFeature> fmFalseOptionals) {
		falseOptionalFeatures.clear();
		falseOptionalFeatures.addAll(clone.getAnalyser().getFalseOptionalFeatures(fmFalseOptionals));
		fmFalseOptionals.removeAll(falseOptionalFeatures);
		return !falseOptionalFeatures.isEmpty();
	}

	public boolean setFalseOptionalFeatures() {
		falseOptionalFeatures.clear();
		boolean found = false;
		IFeatureModel clonedModel = featureModel.clone();
		clonedModel.removeConstraint(this);
		Collection<IFeature> foFeatures = clonedModel.getAnalyser().getFalseOptionalFeatures();
		for (IFeature feature : featureModel.getAnalyser().getFalseOptionalFeatures()) {
			if (!foFeatures.contains(clonedModel.getFeature(feature.getName())) && !falseOptionalFeatures.contains(feature)) {
				falseOptionalFeatures.add(feature);
				found = true;
			}
		}
		return found;
	}

	public Collection<IFeature> getFalseOptional() {
		return falseOptionalFeatures;
	}

	private Collection<PropertyChangeListener> listenerList = new LinkedList<PropertyChangeListener>();

	public void addListener(PropertyChangeListener listener) {
		if (!listenerList.contains(listener))
			listenerList.add(listener);
	}

	public void removeListener(PropertyChangeListener listener) {
		listenerList.remove(listener);
	}

	public void fire(PropertyChangeEvent event) {
		for (PropertyChangeListener listener : listenerList)
			listener.propertyChange(event);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Constraint))
			return false;

		Constraint other = (Constraint) obj;
		return propNode.equals(other.propNode);
	}

	@Override
	public String toString() {
		return propNode.toString(NodeWriter.textualSymbols);
	}

	/**
	 * 
	 * @return true if constraint has hidden features
	 */

	public boolean hasHiddenFeatures() {
		for (IFeature f : getContainedFeatures()) {
			if (f.isHidden() || f.hasHiddenParent()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set the dead features of this constraint
	 * 
	 * @param deadFeatures
	 */
	public void setDeadFeatures(Collection<IFeature> deadFeatures) {
		this.deadFeatures = deadFeatures;
	}

	/**
	 * Gets the dead features of this constraint without new calculation
	 * 
	 * @return The dead features
	 */
	public Collection<IFeature> getDeadFeatures() {
		return deadFeatures;
	}
	
	@Override
	public GraphicItem getItemType() {
		return GraphicItem.Constraint;
	}

}
