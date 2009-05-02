package net.lecousin.dataorganizer.util;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.framework.Pair;
import net.lecousin.framework.ui.eclipse.control.list.LCTable;

import org.eclipse.swt.SWT;

public class DO_UIUtil {

	public static interface Provider<T,T2> {
		public T get(T2 e);
	}
	public static class ProviderPair1<T1,T2> implements Provider<T1,Pair<T1,T2>> {
		public T1 get(Pair<T1,T2> e) { return e.getValue1(); };
	}
	public static class ProviderPair2<T1,T2> implements Provider<T2,Pair<T1,T2>> {
		public T2 get(Pair<T1,T2> e) { return e.getValue2(); };
	}
	
	public static class ColumnData<T> implements LCTable.ColumnProviderText<T> {
		public ColumnData(Provider<Data,T> provider) { this.provider = provider; }
		private Provider<Data,T> provider;
		public String getTitle() { return Local.Path.toString(); }
		public int getAlignment() { return SWT.LEFT; }
		public int getDefaultWidth() { return 250; }
		public String getText(T element) { return provider.get(element).getName(); }
		public org.eclipse.swt.graphics.Font getFont(T element) { return null; };
		public org.eclipse.swt.graphics.Image getImage(T element) { return null; };
		public int compare(T element1, String text1, T element2, String text2) { return text1.compareTo(text2); };
	}
	
	public static class ColumnDataSource<T> implements LCTable.ColumnProviderText<T> {
		public ColumnDataSource(Provider<DataSource,T> provider) { this.provider = provider; }
		private Provider<DataSource,T> provider;
		public String getTitle() { return Local.Data.toString(); }
		public int getAlignment() { return SWT.LEFT; }
		public int getDefaultWidth() { return 250; }
		public String getText(T element) { return provider.get(element).getPathToDisplay()+"/"+provider.get(element).getFileName(); }
		public org.eclipse.swt.graphics.Font getFont(T element) { return null; };
		public org.eclipse.swt.graphics.Image getImage(T element) { return null; };
		public int compare(T element1, String text1, T element2, String text2) { return text1.compareTo(text2); };
	}
	
}
