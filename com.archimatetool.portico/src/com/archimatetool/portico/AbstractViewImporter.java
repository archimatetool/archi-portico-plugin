/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EObject;

import com.archimatetool.model.IConnectable;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelComponent;
import com.archimatetool.model.IDiagramModelConnection;
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
            addConnections(importedView);
        }
        // View exists, update it
        else if(importer.doReplaceWithSource()) {
            updateChildObjects(importedView, targetView);
        }

        return targetView;
    }
    
    private void addChildObjects(IDiagramModelContainer importedParent, IDiagramModelContainer targetParent) throws PorticoException {
        for(IDiagramModelObject importedObject : importedParent.getChildren()) {
            IDiagramModelObject targetObject = importer.cloneObject(importedObject);
            targetParent.getChildren().add(targetObject);
            
            updateDiagramModelComponent(importedObject, targetObject);
            
            if(importedObject instanceof IDiagramModelContainer) {
                addChildObjects((IDiagramModelContainer)importedObject, (IDiagramModelContainer)targetObject);
            }
        }
    }
    
    private void addConnections(IDiagramModel importedView) throws PorticoException {
        // Do this in two passes in case there are connection -> connections
        
        Hashtable<IDiagramModelConnection, IDiagramModelConnection> connections = new Hashtable<>();
        
        // Create new connections. They will be cached in ModelImporter
        for(Iterator<EObject> iter = importedView.eAllContents(); iter.hasNext();) {
            EObject importedObject = iter.next();
            if(importedObject instanceof IDiagramModelConnection) {
                connections.put((IDiagramModelConnection)importedObject, importer.cloneObject((IDiagramModelConnection)importedObject));
            }
        }
        
        // Now connect the source and target ends
        for(Entry<IDiagramModelConnection, IDiagramModelConnection> entry : connections.entrySet()) {
            IDiagramModelConnection importedConnection = entry.getKey();
            IDiagramModelConnection targetConnection = entry.getValue();
            
            IConnectable targetSource = importer.findObjectInTargetModel(importedConnection.getSource());
            if(targetSource == null) {
                throw new PorticoException("Could not find target component: " + importedConnection.getSource().getId()); //$NON-NLS-1$
            }
            
            IConnectable targetTarget = importer.findObjectInTargetModel(importedConnection.getTarget());
            if(targetTarget == null) {
                throw new PorticoException("Could not find target component: " + importedConnection.getTarget().getId()); //$NON-NLS-1$
            }
            
            targetConnection.connect(targetSource, targetTarget);
            
            updateDiagramModelComponent(importedConnection, targetConnection);
        }
    }
    
    private void updateChildObjects(IDiagramModelContainer importedParent, IDiagramModelContainer targetParent) throws PorticoException {
        for(IDiagramModelObject importedObject : importedParent.getChildren()) {
            
        }
    }

    protected abstract void updateDiagramModelComponent(IDiagramModelComponent importedComponent, IDiagramModelComponent targetComponent) throws PorticoException;
}
