/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelContainer;
import com.archimatetool.model.IDiagramModelObject;

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
        
        boolean createdNewView = false;
        
        // We don't have it, so create a new view
        if(targetView == null) {
            targetView = importer.cloneObject(importedView);
            createdNewView = true;
        }
        else if(importer.doReplaceWithSource()) {
            importer.updateObject(importedView, targetView);
            
            // Connection type
            targetView.setConnectionRouterType(importedView.getConnectionRouterType());
        }
        
        // Add to parent folder
        addToParentFolder(importedView, targetView);
        
        // New view created
        if(createdNewView) {
            addChildObjects(importedView, targetView);
        }
        // View exists, update it
        else if(importer.doReplaceWithSource()) {
            updateChildObjects(importedView, targetView);
        }

        return targetView;
    }
    
    protected void addChildObjects(IDiagramModelContainer importedParent, IDiagramModelContainer targetParent) throws PorticoException {
        for(IDiagramModelObject importedObject : importedParent.getChildren()) {
            IDiagramModelObject targetObject = importer.cloneObject(importedObject);
            targetParent.getChildren().add(targetObject);
            
            updateDiagramModelObject(importedObject, targetObject);
            
            if(importedObject instanceof IDiagramModelContainer) {
                addChildObjects((IDiagramModelContainer)importedObject, (IDiagramModelContainer)targetObject);
            }
        }
    }
    
    protected void updateChildObjects(IDiagramModelContainer importedParent, IDiagramModelContainer targetParent) throws PorticoException {
        for(IDiagramModelObject importedObject : importedParent.getChildren()) {
            
        }
    }

    protected abstract void updateDiagramModelObject(IDiagramModelObject importedObject, IDiagramModelObject targetObject) throws PorticoException;
}
