package de.ovgu.featureide.core.typecheck;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.prop4j.Implies;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Not;
import org.prop4j.Or;
import org.prop4j.SatSolver;
import org.sat4j.specs.TimeoutException;

import de.ovgu.featureide.core.IFeatureProject;
import de.ovgu.featureide.fm.core.AbstractCorePlugin;
import de.ovgu.featureide.fm.core.Feature;
import de.ovgu.featureide.fm.core.FeatureModel;
import de.ovgu.featureide.fm.core.editing.NodeCreator;


public class TypecheckCorePlugin extends AbstractCorePlugin {

	public static final String PLUGIN_ID = "de.ovgu.featureide.core.typecheck";

	public Map<IFeatureProject, TypeChecker> typechecker = new HashMap<IFeatureProject, TypeChecker>();
	
	public static  String[] supportedComposers = { "de.ovgu.featureide.composer.featurehouse" };

	private static TypecheckCorePlugin plugin;
	
	@Override
	public String getID() {
		return PLUGIN_ID;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static TypecheckCorePlugin getDefault() {
		return plugin;
	}
	

	
	public static void logln(String message)
	{
		if(getDefault().isDebugging())
		{
			System.out.println(message);
		}
	}
}
