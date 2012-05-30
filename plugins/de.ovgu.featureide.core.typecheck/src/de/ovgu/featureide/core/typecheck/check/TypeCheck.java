/* FeatureIDE - An IDE to support feature-oriented software development
 * Copyright (C) 2005-2011  FeatureIDE Team, University of Magdeburg
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 *
 * See http://www.fosd.de/featureide/ for further information.
 */
package de.ovgu.featureide.core.typecheck.check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import AST.ClassDecl;
import AST.CompilationUnit;
import AST.InterfaceDecl;
import AST.ReferenceType;
import AST.TypeAccess;
import AST.TypeDecl;
import AST.UnknownType;
import de.ovgu.featureide.core.typecheck.helper.FujiWrapper;
import de.ovgu.featureide.fm.core.Feature;
import de.ovgu.featureide.fm.core.FeatureModel;

/**
 * TODO description
 * 
 * @author soenke
 */
public class TypeCheck extends AbstractCheckPlugin {
    private Map<Feature, List<ReferenceType>> intros;

    public TypeCheck() {
	plugin_name = "Type Check Plugin";
	registerNodeType(ClassDecl.class);
	registerNodeType(CompilationUnit.class);
    }

    public void init() {
	Map<Feature, List<CompilationUnit>> cumap = getNodesByType(CompilationUnit.class);

	intros = new HashMap<Feature, List<ReferenceType>>();
	for (Feature f : cumap.keySet()) {
	    if (!intros.containsKey(f)) {
		intros.put(f, new ArrayList<ReferenceType>());
	    }
	    for (CompilationUnit cu : cumap.get(f)) {
		for (TypeDecl td : cu.getTypeDecls()) {
		    if (td instanceof ReferenceType) {
			ReferenceType rt = (ReferenceType) td;
			if (!(rt.isAnonymous() || rt.isLocalClass() || rt
				.isArrayDecl())) {
			    intros.get(f).add(rt);
			}
		    }
		}
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.ovgu.featureide.core.typecheck.check.ICheckPlugin#invokeCheck(de.ovgu
     * .featureide.core.IFeatureProject,
     * de.ovgu.featureide.core.typecheck.parser.ClassTable)
     */
    @Override
    public void invokeCheck(FeatureModel fm) {

	// doesn't work with annotations and stuff
	Map<Feature, List<ClassDecl>> cdmap = getNodesByType(ClassDecl.class);

	for (Feature f : cdmap.keySet()) {
	    for (ClassDecl cd : cdmap.get(f)) {
		for (TypeAccess ta : FujiWrapper.getChildNodesByType(cd,
			TypeAccess.class)) {
		    if (ta.type() instanceof UnknownType) {
			Set<Feature> providing_features = providesType(
				ta.name()).keySet();
			if (!checkFeatureImplication(fm, f, providing_features)) {
			    newProblem(new CheckProblem(f, cd.compilationUnit()
				    .pathName(), ta.lineNumber(),
				    "Missing type dependency " + ta.name(), providing_features));
			}
		    }
		}
	    }
	}
    }

    private Map<Feature, ReferenceType> providesType(String type) {
	Map<Feature, ReferenceType> providing_features = new HashMap<Feature, ReferenceType>();

	for (Feature f : intros.keySet()) {
	    for (ReferenceType rt : intros.get(f)) {
		if (rt.name().equals(type)) {
		    providing_features.put(f, rt);
		}
	    }
	}

	return providing_features;
    }
}
