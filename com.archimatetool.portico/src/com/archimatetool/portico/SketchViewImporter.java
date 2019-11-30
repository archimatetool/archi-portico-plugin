/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.model.IDiagramModelObject;

/**
 * Sketch View Importer
 * 
 * @author Phillip Beauvoir
 */
class SketchViewImporter extends AbstractViewImporter {
    
    SketchViewImporter(ModelImporter importer) {
        super(importer);
    }

    @Override
    protected void updateDiagramModelObject(IDiagramModelObject importedObject, IDiagramModelObject targetObject) throws PorticoException {
    }

}
