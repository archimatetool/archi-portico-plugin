/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.canvas.model.ICanvasModel;
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
    protected void updateView() {
        // Hint stuff
        getTargetView().setHintTitle(getImportedView().getHintTitle());
        getTargetView().setHintContent(getImportedView().getHintContent());
    }
    
    @Override
    protected ICanvasModel getImportedView() {
        return (ICanvasModel)super.getImportedView();
    }
    
    @Override
    protected ICanvasModel getTargetView() {
        return (ICanvasModel)super.getTargetView();
    }

    @Override
    protected void updateDiagramModelComponent(IDiagramModelComponent importedComponent, IDiagramModelComponent targetComponent) throws PorticoException {
    }

}
