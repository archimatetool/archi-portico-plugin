/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.archimatetool.editor.model.ISelectedModelImporter;
import com.archimatetool.editor.utils.PlatformUtils;
import com.archimatetool.model.IArchimateModel;


/**
 * Import Provider
 * 
 * @author Phillip Beauvoir
 */
public class PorticoImportProvider implements ISelectedModelImporter {

    /**
     * If true target data is discarded and source data is used
     * TODO: This will be in the importer wizard
     */
    private boolean replaceWithSource = true;

    public PorticoImportProvider() {
    }

    @Override
    public void doImport(IArchimateModel targetModel) throws IOException {
        File importedFile = askOpenFile();
        if(importedFile == null) {
            return;
        }

        try {
            ModelImporter importer = new ModelImporter(replaceWithSource);
            importer.doImport(importedFile, targetModel);
        }
        catch(PorticoException ex) {
            throw new IOException(ex);
        }
    }

    private File askOpenFile() {
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.archimate", "*.*" } ); //$NON-NLS-1$ //$NON-NLS-2$
        String path = dialog.open();
        
        // TODO: Bug on Mac 10.12 and newer - Open dialog does not close straight away
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=527306
        if(path != null && PlatformUtils.isMac()) {
            while(Display.getCurrent().readAndDispatch());
        }
        
        return path != null ? new File(path) : null;
    }

}
