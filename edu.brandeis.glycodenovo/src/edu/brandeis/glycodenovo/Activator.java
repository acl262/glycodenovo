package edu.brandeis.glycodenovo;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This class controls the plug-in life cycle
 *
 * @author Wangshu Hong
 *
 */
public class Activator implements BundleActivator {

	private static BundleContext context;
	public static final String PLUGIN_ID = "edu.brandeis.glycodenovo";


	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
