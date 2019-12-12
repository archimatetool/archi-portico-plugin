/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import java.io.File;

import org.eclipse.jface.wizard.Wizard;


/**
 * Import Model Wizard
 * 
 * @author Phillip Beauvoir
 */
public class ImportModelWizard extends Wizard {

    private ImportModelPage page;
    
    private File file;
    private boolean doUpdate;
    private boolean doUpdateRoot;

    public ImportModelWizard() {
        setWindowTitle(Messages.ImportModelWizard_0);
    }

    @Override
    public void addPages() {
        page = new ImportModelPage();
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        file = new File(page.getFileName());
        doUpdate = page.doUpdate();
        doUpdateRoot = page.doUpdateRoot();
        
        page.storePreferences();
        
        return true;
    }

    File getFile() {
        return file;
    }
    
    boolean doUpdate() {
        return doUpdate;
    }
    
    boolean doUpdateRoot() {
        return doUpdateRoot;
    }
}
