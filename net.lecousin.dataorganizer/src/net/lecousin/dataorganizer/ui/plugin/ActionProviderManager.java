package net.lecousin.dataorganizer.ui.plugin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.eclipse.extension.EclipsePluginExtensionUtil;
import net.lecousin.framework.log.Log;

import org.eclipse.core.runtime.IConfigurationElement;

public class ActionProviderManager {

	private static Map<String,List<ActionProvider>> providers = null;
	
	public static List<ActionProvider> getProviders(String contentTypeID) {
		if (providers == null)
			loadProviders();
		return providers.get(contentTypeID);
	}
	
	private static void loadProviders() {
		providers = new HashMap<String,List<ActionProvider>>();
		for (IConfigurationElement ext : EclipsePluginExtensionUtil.getExtensionsSubNode(EclipsePlugin.ID, "contenttype_action", "action")) {
			String id = ext.getAttribute("content_type_id");
			List<ActionProvider> list = providers.get(id);
			if (list == null) {
				list = new LinkedList<ActionProvider>();
				providers.put(id, list);
			}
			try {
				ActionProvider provider = EclipsePluginExtensionUtil.createInstance(ActionProvider.class, ext, "provider", new Object[][] { new Object[] { } });
				list.add(provider);
			} catch (Throwable t) {
				if (Log.error(ActionProviderManager.class))
					Log.error(ActionProviderManager.class, "Unable to instantiate ActionProvider", t);
			}
		}
	}
	
}
