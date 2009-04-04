package net.lecousin.dataorganizer.core.database.info;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.eclipse.extension.EclipsePluginExtensionUtil;
import net.lecousin.framework.log.Log;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.Image;

public class InfoRetrieverPluginRegistry {

	private InfoRetrieverPluginRegistry() {}
	
	public static List<InfoRetrieverPlugin> getRetrievers(String contentTypeID) {
		List<InfoRetrieverPlugin> plugins = new LinkedList<InfoRetrieverPlugin>();
		for (IConfigurationElement ext : getPlugins()) {
			if (isContentType(ext, contentTypeID)) {
				InfoRetrieverPlugin pi = getPlugin(ext);
				if (pi != null && pi.isSupportingLanguage(Application.language))
					plugins.add(pi);
			}
		}
		return plugins;
	}
	
	public static String getNameForSource(String sourceID, String contentTypeID) {
		InfoRetrieverPlugin pi = getPlugin(sourceID, contentTypeID);
		if (pi == null) return "Plug-in missing";
		return pi.getName();
	}
	public static Image getIconForSource(String sourceID, String contentTypeID) {
		InfoRetrieverPlugin pi = getPlugin(sourceID, contentTypeID);
		if (pi == null) return null;
		return pi.getIcon();
	}
	
	private static IConfigurationElement getPluginForSource(String sourceID, String contentTypeID) {
		for (IConfigurationElement cfg : getPlugins()) {
			if (cfg.getAttribute("id").equals(sourceID) && cfg.getAttribute("content_type_id").equals(contentTypeID))
				return cfg;
		}
		return null;
	}
	
	private static Map<String,Map<String,InfoRetrieverPlugin>> instances = new HashMap<String,Map<String,InfoRetrieverPlugin>>();
	public static InfoRetrieverPlugin getPlugin(String id, String contentTypeID) {
		Map<String,InfoRetrieverPlugin> map = instances.get(id);
		if (map == null) {
			map = new HashMap<String,InfoRetrieverPlugin>();
			instances.put(id, map);
		}
		InfoRetrieverPlugin pi = map.get(contentTypeID);
		if (pi == null) {
			IConfigurationElement cfg = getPluginForSource(id, contentTypeID);
			if (cfg == null) return null;
			pi = instantiate(cfg);
			if (pi == null) return null;
			map.put(contentTypeID, pi);
		}
		if (!pi.isSupportingLanguage(Application.language))
			return null;
		return pi;
	}
	private static InfoRetrieverPlugin getPlugin(IConfigurationElement cfg) {
		String id = cfg.getAttribute("id");
		Map<String,InfoRetrieverPlugin> map = instances.get(id);
		if (map == null) {
			map = new HashMap<String,InfoRetrieverPlugin>();
			instances.put(id, map);
		}
		String contentTypeID = cfg.getAttribute("content_type_id");
		InfoRetrieverPlugin pi = map.get(contentTypeID);
		if (pi == null) {
			pi = instantiate(cfg);
			if (pi == null) return null;
			map.put(contentTypeID, pi);
		}
		return pi;
	}
	private static Collection<IConfigurationElement> getPlugins() {
		return EclipsePluginExtensionUtil.getExtensionsSubNode(EclipsePlugin.ID, "inforetriever", "retriever");
	}
	private static boolean isContentType(IConfigurationElement plugin, String contentTypeID) {
		return plugin.getAttribute("content_type_id").equals(contentTypeID);
	}
	private static InfoRetrieverPlugin instantiate(IConfigurationElement plugin) {
		try {
			return EclipsePluginExtensionUtil.createInstance(InfoRetrieverPlugin.class, plugin, "class", new Object[][] { new Object[] {} });
		} catch (InstantiationException e) {
			if (Log.error(InfoRetrieverPluginRegistry.class))
				Log.error(InfoRetrieverPluginRegistry.class, "Unable to instantiate plug-in id '" + plugin.getAttribute("id") + "': class is not instantiable.");
		} catch (IllegalAccessException e) {
			if (Log.error(InfoRetrieverPluginRegistry.class))
				Log.error(InfoRetrieverPluginRegistry.class, "Unable to instantiate plug-in id '" + plugin.getAttribute("id") + "': class or constructor is not accessible.");
		} catch (InvocationTargetException e) {
			if (Log.error(InfoRetrieverPluginRegistry.class))
				Log.error(InfoRetrieverPluginRegistry.class, "Unable to instantiate plug-in id '" + plugin.getAttribute("id") + "': constructor thrown an exception.", e.getTargetException());
		} catch (ClassNotFoundException e) {
			if (Log.error(InfoRetrieverPluginRegistry.class))
				Log.error(InfoRetrieverPluginRegistry.class, "Unable to instantiate plug-in id '" + plugin.getAttribute("id") + "': class cannot be found.", e);
		}
		return null;
	}
}
