package net.lecousin.dataorganizer.core.database.content;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.dataorganizer.core.search.DataSearch;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.wizard.adddata.AddData_Page;
import net.lecousin.framework.Pair;
import net.lecousin.framework.eclipse.extension.EclipsePluginExtensionUtil;
import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.files.TypedFolder;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.version.Version;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

public abstract class ContentType {

	public abstract Version getCurrentVersion();
	
	public abstract String getID();
	public abstract String getName();
	public abstract DataContentType create(Data data);

	public abstract FileType[] getEligibleFileTypesForDetection();
	public abstract List<Pair<List<IFileStore>,VirtualData>> detectOnFolder(VirtualDataBase db, TypedFolder folder, Shell shell);
	public abstract List<Pair<List<IFileStore>,VirtualData>> detectOnFile(VirtualDataBase db, TypedFolder folder, IFileStore file, TypedFile typedFile, Shell shell);
	public abstract List<Pair<List<IFileStore>,VirtualData>> detectOnFile(VirtualDataBase db, TypedFolder folder, IFileStore file, Shell shell);
	
	public abstract Image getIcon();
	public abstract Image getDefaultTypeImage();
	
	public abstract AddData_Page createAddDataWizardPage();
	
	public List<DataSearch.Parameter> createSearchParameters() { return null; }
	
	public abstract DataContentType loadContent(Data data, Element elt);
	public abstract DataContentType loadContent(Data data, Element elt, ContentTypeLoader loader);
	
	public abstract Object openLoadDataContentContext(Composite panel);
	public abstract void closeLoadDataContentContext(Object context);
	public abstract void loadDataContent(Data data, Object context, WorkProgress progress, int amount);
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ContentType)) return false;
		return getID().equals(((ContentType)obj).getID());
	}
	@Override
	public int hashCode() {
		return getID().hashCode();
	}
	
	public static Collection<ContentType> getAvailableTypes() { init(); return types.values(); }
	public static Collection<String> getAvailableTypesID() { init(); return types.keySet(); }
	public static ContentType getContentType(String id) { init(); return types.get(id); }
	
	public static ContentTypeLoader getLoader(String id, Version version) {
		init();
		List<Pair<Version,ContentTypeLoader>> list = versions.get(id);
		if (list == null) return null;
		for (Pair<Version,ContentTypeLoader> p : list) {
			if (!p.getValue1().equals(version)) continue;
			return p.getValue2();
		}
		return null;
	}
	
	private static Map<String,ContentType> types = null;
	private static Map<String,List<Pair<Version,ContentTypeLoader>>> versions = null;
	private static void init() {
		if (types != null) return;
		types = new HashMap<String,ContentType>();
		versions = new HashMap<String,List<Pair<Version,ContentTypeLoader>>>();
		for (IConfigurationElement ext : EclipsePluginExtensionUtil.getExtensionsSubNode(EclipsePlugin.ID, "contenttype", "type")) {
			String type_id = ext.getAttribute("id");
			try {
				ContentType type = EclipsePluginExtensionUtil.createInstance(ContentType.class, ext, "class", new Object[][] { new Object[] {} });
				types.put(type_id, type);
				for (IConfigurationElement ver : ext.getChildren("version")) {
					int maj = 0, min = 0, sub = 0;
					try { maj = Integer.parseInt(ver.getAttribute("maj")); }
					catch (NumberFormatException e) {
						if (Log.error(ContentType.class))
							Log.error(ContentType.class, "Invalid major version '" + ver.getAttribute("maj") + "': must be a number.");
						continue;
					}
					if (ver.getAttribute("min") != null) {
						try { min = Integer.parseInt(ver.getAttribute("min")); }
						catch (NumberFormatException e) {
							if (Log.error(ContentType.class))
								Log.error(ContentType.class, "Invalid minor version '" + ver.getAttribute("min") + "': must be a number.");
							continue;
						}
					}
					if (ver.getAttribute("sub") != null) {
						try { sub = Integer.parseInt(ver.getAttribute("sub")); }
						catch (NumberFormatException e) {
							if (Log.error(ContentType.class))
								Log.error(ContentType.class, "Invalid sub-version '" + ver.getAttribute("sub") + "': must be a number.");
							continue;
						}
					}
					try {
						ContentTypeLoader loader = EclipsePluginExtensionUtil.createInstance(ContentTypeLoader.class, ver, "loader", new Object[][] { new Object[] {} });
						List<Pair<Version,ContentTypeLoader>> list = versions.get(type_id);
						if (list == null) {
							list = new LinkedList<Pair<Version,ContentTypeLoader>>();
							versions.put(type_id, list);
						}
						list.add(new Pair<Version,ContentTypeLoader>(new Version(maj, min, sub), loader));
					} catch (InvocationTargetException e) {
						if (Log.error(ContentType.class))
							Log.error(ContentType.class, "Unable to instantiate version loader for content type '" + type_id + "' version " + maj + "." + min + "." + sub + ": the constructor thrown an exception.", e.getTargetException());
					} catch (IllegalAccessException e) {
						if (Log.error(ContentType.class))
							Log.error(ContentType.class, "Unable to instantiate version loader for content type '" + type_id + "' version " + maj + "." + min + "." + sub + ": the class or the constructor is not accessible.", e);
					} catch (InstantiationException e) {
						if (Log.error(ContentType.class))
							Log.error(ContentType.class, "Unable to instantiate version loader for content type '" + type_id + "' version " + maj + "." + min + "." + sub + ": the class is not instantiable (abstract).", e);
					} catch (ClassNotFoundException e) {
						if (Log.error(ContentType.class))
							Log.error(ContentType.class, "Unable to instantiate version loader for content type '" + type_id + "' version " + maj + "." + min + "." + sub + ": the class cannot be found.", e);
					}
				}
			} catch (InvocationTargetException e) {
				if (Log.error(ContentType.class))
					Log.error(ContentType.class, "Unable to instantiate content type '" + type_id + "': the constructor thrown an exception.", e.getTargetException());
			} catch (IllegalAccessException e) {
				if (Log.error(ContentType.class))
					Log.error(ContentType.class, "Unable to instantiate content type '" + type_id + "': the class or the constructor is not accessible.", e);
			} catch (InstantiationException e) {
				if (Log.error(ContentType.class))
					Log.error(ContentType.class, "Unable to instantiate content type '" + type_id + "': the class is not instantiable (abstract).", e);
			} catch (ClassNotFoundException e) {
				if (Log.error(ContentType.class))
					Log.error(ContentType.class, "Unable to instantiate content type '" + type_id + "': the class cannot be found.", e);
			}
		}
	}
}
