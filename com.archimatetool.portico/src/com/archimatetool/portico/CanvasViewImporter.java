/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.canvas.model.ICanvasModel;
import com.archimatetool.model.IDiagramModel;

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
    IDiagramModel importView(IDiagramModel importedView) throws PorticoException {
        ICanvasModel targetView = (ICanvasModel)super.importView(importedView);
        
        targetView.setHintContent(((ICanvasModel)importedView).getHintContent());
        targetView.setHintTitle(((ICanvasModel)importedView).getHintTitle());
        
        return targetView;
    }
    
    @Override
    protected void updateChildObjects(IDiagramModel importedView, IDiagramModel targetView) {
        super.updateChildObjects(importedView, targetView);
    }

}
