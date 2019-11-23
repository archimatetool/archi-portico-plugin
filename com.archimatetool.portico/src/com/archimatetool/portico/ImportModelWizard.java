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

    private ImportModelPage fPage;
    
    private File file;
    private boolean doReplaceWithSource;

    public ImportModelWizard() {
        setWindowTitle(Messages.ImportModelWizard_0);
    }

    @Override
    public void addPages() {
        fPage = new ImportModelPage();
        addPage(fPage);
    }

    @Override
    public boolean performFinish() {
        file = new File(fPage.getFileName());
        doReplaceWithSource = fPage.doReplaceWithSource();
        fPage.storePreferences();
        return true;
    }

    File getFile() {
        return file;
    }
    
    boolean doReplaceWithSource() {
        return doReplaceWithSource;
    }
}
