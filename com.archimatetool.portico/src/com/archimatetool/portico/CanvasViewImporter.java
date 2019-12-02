/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.canvas.model.ICanvasModel;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelComponent;

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
    protected void updateView(IDiagramModel importedView, IDiagramModel targetView) {
        super.updateView(importedView, targetView);
        
        ICanvasModel importedCanvasView = (ICanvasModel)importedView;
        ICanvasModel targetCanvasView = (ICanvasModel)targetView;
        
        // Hint stuff
        targetCanvasView.setHintTitle(importedCanvasView.getHintTitle());
        targetCanvasView.setHintContent(importedCanvasView.getHintContent());
    }
    
    @Override
    protected void updateDiagramModelComponent(IDiagramModelComponent importedComponent, IDiagramModelComponent targetComponent) throws PorticoException {
    }

}
