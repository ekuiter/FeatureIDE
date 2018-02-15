package de.ovgu.featureide.fm.attributes.computations.impl;

import java.util.List;

import org.eclipse.swt.graphics.Image;

import de.ovgu.featureide.fm.attributes.base.IFeatureAttribute;
import de.ovgu.featureide.fm.attributes.base.impl.DoubleFeatureAttribute;
import de.ovgu.featureide.fm.attributes.base.impl.ExtendedFeature;
import de.ovgu.featureide.fm.attributes.base.impl.LongFeatureAttribute;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.ovgu.featureide.fm.ui.views.outline.IOutlineEntry;

/**
 * Estimates the minimum of a numerical attribute given a partial configuration Only supposed to be used on numerical attributes
 * 
 * @author Chico Sundermann
 */
public class EstimatedMinimumComputation implements IOutlineEntry {

	private static final String LABEL = "Minimal sum of attribute value (est.): ";
	Configuration config;
	IFeatureAttribute attribute;

	public EstimatedMinimumComputation(Configuration config, IFeatureAttribute attribute) {
		this.config = config;
		this.attribute = attribute;
	}

	private Object getSelectionSum() {
		List<IFeature> selectedFeatures = config.getSelectedFeatures();
		double sum = 0.0;
		for (IFeature feat : selectedFeatures) {
			if (feat instanceof ExtendedFeature) {
				ExtendedFeature ext = (ExtendedFeature) feat;
				if (ext.isContainingAttribute(attribute)) {
					for (IFeatureAttribute att : ext.getAttributes()) {
						if (att.getName().equals(attribute.getName())) {
							if (att.getValue() instanceof Long) {
								sum += (long) att.getValue();
							} else if (att.getValue() instanceof Double) {
								sum += (double) att.getValue();
							}
						}
					}
				}
			}
		}
		return sum;
	}

	@Override
	public boolean supportsType(Object element) {
		return attribute instanceof LongFeatureAttribute || attribute instanceof DoubleFeatureAttribute;
	}

	@Override
	public String getLabel() {
		if (attribute instanceof LongFeatureAttribute) {
			return LABEL + String.valueOf(((Double) getSelectionSum()).longValue());
		}
		return LABEL + getSelectionSum().toString();
	}

	@Override
	public Image getLabelImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public List<IOutlineEntry> getChildren() {
		return null;
	}

	@Override
	public void setConfig(Configuration config) {
		this.config = config;
	}

}
