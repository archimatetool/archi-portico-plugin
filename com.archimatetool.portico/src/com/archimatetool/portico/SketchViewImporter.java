/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelComponent;
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
    protected void updateView(IDiagramModel importedView, IDiagramModel targetView) {
        super.updateView(importedView, targetView);
        
        ISketchModel importedSketchView = (ISketchModel)importedView;
        ISketchModel targetSketchView = (ISketchModel)targetView;
        
        // Background
        targetSketchView.setBackground(importedSketchView.getBackground());
    }

    @Override
    protected void updateDiagramModelComponent(IDiagramModelComponent importedComponent, IDiagramModelComponent targetComponent) throws PorticoException {
    }

}
