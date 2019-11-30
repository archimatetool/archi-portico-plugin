/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.model.IDiagramModelObject;

/**
 * Canvas View Importer
 * 
 * @author Phillip Beauvoir
 */
class CanvasViewImporter extends AbstractViewImporter {
    
    CanvasViewImporter(ModelImporter importer) {
        super(importer);
    }

    @Override
    protected void updateDiagramModelObject(IDiagramModelObject importedObject, IDiagramModelObject targetObject) throws PorticoException {
    }

}
