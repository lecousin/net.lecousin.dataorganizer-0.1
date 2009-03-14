package net.lecousin.dataorganizer.neufbox.mediacenter.internal;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.neufbox.mediacenter.Media;
import net.lecousin.neufbox.mediacenter.eclipse.SharedDataView;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EclipsePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String ID = "net.lecousin.dataorganizer.neufbox.mediacenter";

	// The shared instance
	private static EclipsePlugin plugin;
	
	/**
	 * The constructor
	 */
	public EclipsePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		SharedDataView.mediaRead.addListener(new Listener<Media>() {
			public void fire(Media event) {
				Object o = event.getData();
				if (o instanceof Data)
					((Data)o).opened();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static EclipsePlugin getDefault() {
		return plugin;
	}

}
