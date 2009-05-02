package net.lecousin.dataorganizer.ui.application;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.ui.action.AboutAction;
import net.lecousin.dataorganizer.ui.action.AddDataAction;
import net.lecousin.dataorganizer.ui.action.ConfigAction;
import net.lecousin.dataorganizer.ui.action.RefreshDataBaseAction;
import net.lecousin.dataorganizer.ui.action.ShowLabelsViewAction;
import net.lecousin.dataorganizer.ui.action.UpdateAction;
import net.lecousin.dataorganizer.ui.application.bar.ContactItem;
import net.lecousin.dataorganizer.ui.application.bar.DataViewedItem;
import net.lecousin.dataorganizer.ui.application.bar.DonateItem;
import net.lecousin.dataorganizer.ui.application.bar.PerspectiveButtons;

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
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the
 * actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    private AddDataAction addDataAction;
    private RefreshDataBaseAction refreshDataBaseAction;
    private ShowLabelsViewAction showLabelsViewAction;
    private UpdateAction updateAction;
    private AboutAction aboutAction;
    private ConfigAction configAction;
//    private TestAction testAction;
    
    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }
    
    protected void makeActions(final IWorkbenchWindow window) {
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

        aboutAction = new AboutAction();
        register(aboutAction);

        configAction = new ConfigAction();
        register(configAction);
    }
    
    protected void fillMenuBar(IMenuManager menuBar) {
        MenuManager dbMenu = new MenuManager(Local.MENU_Database.toString(), "DataOrganizer.Database");
        MenuManager optionsMenu = new MenuManager(Local.MENU_Options.toString(), "DataOrganizer.Options");
        MenuManager helpMenu = new MenuManager(Local.MENU_Help.toString(), "DataOrganizer.Help");
        
        menuBar.add(dbMenu);
        // Add a group marker indicating where action set menus will appear.
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(optionsMenu);
        menuBar.add(helpMenu);
        
        // DataBase
        dbMenu.add(addDataAction);
        dbMenu.add(refreshDataBaseAction);
        dbMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        
        // Options
        optionsMenu.add(configAction);
        optionsMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        
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
        toolbar.add(configAction);
        toolbar.add(updateAction);
//        toolbar.add(testAction);
        toolbar.add(new Separator());
        toolbar.add(new DataViewedItem());
        toolbar.add(new Separator());
        toolbar.add(new ContactItem());
        toolbar.add(new DonateItem());
        toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        coolBar.add(new ToolBarContributionItem(toolbar, "Normal"));
        toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        toolbar.add(new PerspectiveButtons());
        coolBar.add(new ToolBarContributionItem(toolbar, "perspectives"));
    }
    
}
