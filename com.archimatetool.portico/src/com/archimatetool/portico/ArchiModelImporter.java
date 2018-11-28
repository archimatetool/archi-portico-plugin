/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import java.io.File;

import org.eclipse.emf.ecore.EObject;

import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.util.ArchimateModelUtils;


/**
 * Archi Model Importer
 * 
 * @author Phillip Beauvoir
 */
public class ArchiModelImporter {
    
    private IArchimateModel fSourceModel;
    private IArchimateModel fTargetModel;

    public ArchiModelImporter(IArchimateModel targetModel) {
        fTargetModel = targetModel;
    }

    public void doImport(File file) {
        
    }

    private EObject getObjectByID(IArchimateModel model, String id) {
        EObject eObject = ArchimateModelUtils.getObjectByID(model, id);
        return eObject;
    }
}
