package net.lecousin.dataorganizer.ui.plugin;

import org.eclipse.swt.graphics.Image;

public interface Action extends Runnable {

	public static enum Type {
		/** action that open the data */
		OPEN,
		/** action regarding the management of the data in the list (delete, move...) */
		LIST_MANAGEMENT,
		/** action that send the data (i.e. to a player list) */
		SEND,
		/** custom action */
		CUSTOM,
	}
	
	public Image getIcon();
	public String getText();
	public Type getType();
	
	public boolean isSame(Action action);

}
