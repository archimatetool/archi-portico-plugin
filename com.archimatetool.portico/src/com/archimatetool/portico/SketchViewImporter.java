/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.ISketchModel;

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
    IDiagramModel importView(IDiagramModel importedView) throws PorticoException {
        ISketchModel targetView = (ISketchModel)super.importView(importedView);
        
        targetView.setBackground(((ISketchModel)importedView).getBackground());
        
        return targetView;
    }
    
    @Override
    protected void updateChildObjects(IDiagramModel importedView, IDiagramModel targetView) {
        super.updateChildObjects(importedView, targetView);
    }

}
