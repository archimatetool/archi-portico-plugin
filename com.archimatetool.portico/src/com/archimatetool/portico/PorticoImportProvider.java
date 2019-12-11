/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.archimatetool.editor.model.ISelectedModelImporter;
import com.archimatetool.editor.model.ModelChecker;
import com.archimatetool.editor.ui.components.ExtendedWizardDialog;
import com.archimatetool.model.IArchimateModel;


/**
 * Import Provider
 * 
 * @author Phillip Beauvoir
 */
public class PorticoImportProvider implements ISelectedModelImporter {

    public PorticoImportProvider() {
    }

    @Override
    public void doImport(IArchimateModel targetModel) throws IOException {
        ImportModelWizard wizard = new ImportModelWizard();
        
        WizardDialog dialog = new ExtendedWizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                wizard,
                "ImportModelWizard") { //$NON-NLS-1$

            @Override
            protected void createButtonsForButtonBar(Composite parent) {
                super.createButtonsForButtonBar(parent); // Change "Finish" to "Import"
                Button b = getButton(IDialogConstants.FINISH_ID);
                b.setText(Messages.PorticoImportProvider_0);
            }
        };
        
        if(dialog.open() == Window.OK) {
            boolean replaceWithSource = wizard.doReplaceWithSource();
            
            File importedFile = wizard.getFile();
            if(importedFile == null) {
                return;
            }
            
            ModelImporter importer = new ModelImporter();
            Exception[] ex = new Exception[1];

            BusyIndicator.showWhile(Display.getCurrent(), () -> {
                try {
                    importer.doImport(importedFile, targetModel, replaceWithSource);
                }
                catch(Exception ex1) {
                    ex[0] = ex1;
                }
            });
            
            if(ex[0] != null) {
                throw new IOException(ex[0]);
            }
            
            // Run the Model checker now
            ModelChecker checker = new ModelChecker(targetModel);
            if(!checker.checkAll()) {
                checker.showErrorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
            }
        }
    }
}
