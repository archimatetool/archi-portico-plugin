/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateDiagramModel;
import com.archimatetool.model.IDiagramModelArchimateComponent;
import com.archimatetool.model.IDiagramModelComponent;

/**
 * Archimate View Importer
 * 
 * @author Phillip Beauvoir
 */
class ArchimateViewImporter extends AbstractViewImporter {
    
    ArchimateViewImporter(ModelImporter importer) {
        super(importer);
    }

    @Override
    protected void updateView() {
        super.updateView();
        
        // Viewpoint
        getTargetView().setViewpoint(getImportedView().getViewpoint());
    }
    
    @Override
    protected IArchimateDiagramModel getImportedView() {
        return (IArchimateDiagramModel)super.getImportedView();
    }
    
    @Override
    protected IArchimateDiagramModel getTargetView() {
        return (IArchimateDiagramModel)super.getTargetView();
    }

    @Override
    protected void updateDiagramModelComponent(IDiagramModelComponent importedComponent, IDiagramModelComponent targetComponent) throws PorticoException {
        // Set ArchiMate Concept
        if(targetComponent instanceof IDiagramModelArchimateComponent) {
            IArchimateConcept targetConcept = findObjectInTargetModel(((IDiagramModelArchimateComponent)importedComponent).getArchimateConcept());
            if(targetConcept == null) {
                throw new PorticoException("Could not find concept in target: " + importedComponent.getId()); //$NON-NLS-1$
            }
            
            ((IDiagramModelArchimateComponent)targetComponent).setArchimateConcept(targetConcept);
        }
    }

}
