/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.model.IDiagramModel;

/**
 * View Importer
 * 
 * @author Phillip Beauvoir
 */
abstract class AbstractViewImporter extends AbstractImporter {
    
    AbstractViewImporter(ModelImporter importer) {
        super(importer);
    }

    IDiagramModel importView(IDiagramModel importedView) throws PorticoException {
        // Do we have this View given its ID?
        IDiagramModel targetView = importer.findObjectInTargetModel(importedView);
        
        // We don't have it, so create a new concept
        if(targetView == null) {
            targetView = importer.cloneObject(importedView);
        }
        else if(importer.doReplaceWithSource()) {
            importer.updateObject(importedView, targetView);
        }
        
        // Connection Router type
        targetView.setConnectionRouterType(importedView.getConnectionRouterType());
        
        // Add to parent folder
        addToParentFolder(importedView, targetView);
        
        // Update contents
        updateChildObjects(importedView, targetView);

        return targetView;
    }
    
    protected void updateChildObjects(IDiagramModel importedView, IDiagramModel targetView) {
    }

}
