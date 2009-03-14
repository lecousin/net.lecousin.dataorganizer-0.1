package net.lecousin.dataorganizer.ui.application;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.ui.action.AddDataAction;
import net.lecousin.dataorganizer.ui.action.RefreshDataBaseAction;
import net.lecousin.dataorganizer.ui.action.ShowLabelsViewAction;
import net.lecousin.dataorganizer.ui.action.UpdateAction;
import net.lecousin.dataorganizer.ui.application.bar.ContactItem;
import net.lecousin.dataorganizer.ui.application.bar.DataViewedItem;
import net.lecousin.dataorganizer.ui.application.bar.DonateItem;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the
 * actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    private IWorkbenchAction exitAction;
    private IWorkbenchAction aboutAction;
    
    private AddDataAction addDataAction;
    private RefreshDataBaseAction refreshDataBaseAction;
    private ShowLabelsViewAction showLabelsViewAction;
    private UpdateAction updateAction;
//    private TestAction testAction;
    
    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }
    
    protected void makeActions(final IWorkbenchWindow window) {
        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);
        
        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);
        
        addDataAction = new AddDataAction();
        register(addDataAction);
        
        refreshDataBaseAction = new RefreshDataBaseAction();
        register(refreshDataBaseAction);

        showLabelsViewAction = new ShowLabelsViewAction();
        register(showLabelsViewAction);

        updateAction = new UpdateAction();
        register(updateAction);
        
//        testAction = new TestAction();
//        register(testAction);
    }
    
    protected void fillMenuBar(IMenuManager menuBar) {
        MenuManager fileMenu = new MenuManager(Local.MENU_File.toString(), "DataOrganizer.File"/*IWorkbenchActionConstants.M_FILE*/);
        MenuManager helpMenu = new MenuManager(Local.MENU_Help.toString(), "DataOrganizer.Help"/*IWorkbenchActionConstants.M_HELP*/);
        
        menuBar.add(fileMenu);
        // Add a group marker indicating where action set menus will appear.
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(helpMenu);
        
        // File
        fileMenu.add(addDataAction);
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        fileMenu.add(new Separator());
        fileMenu.add(exitAction);
        
        // Help
        helpMenu.add(aboutAction);
    }
    
    protected void fillCoolBar(ICoolBarManager coolBar) {
        IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        coolBar.add(new ToolBarContributionItem(toolbar, "main")); 
        toolbar.add(addDataAction);
        toolbar.add(refreshDataBaseAction);
        toolbar.add(showLabelsViewAction);
        toolbar.add(new Separator());
        toolbar.add(updateAction);
//        toolbar.add(testAction);
        toolbar.add(new Separator());
        toolbar.add(new DataViewedItem());
        toolbar.add(new Separator());
        toolbar.add(new ContactItem());
        toolbar.add(new DonateItem());
        toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        coolBar.add(new ToolBarContributionItem(toolbar, "Normal")); 
    }
}
