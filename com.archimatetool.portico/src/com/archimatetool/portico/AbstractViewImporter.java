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
 * Absract View Importer
 * 
 * @author Phillip Beauvoir
 */
abstract class AbstractViewImporter extends AbstractImporter {
    
    private IDiagramModel importedView;
    private IDiagramModel targetView;
    
    AbstractViewImporter(ModelImporter importer) {
        super(importer);
    }

    IDiagramModel importView(IDiagramModel importedView) throws PorticoException {
        this.importedView = importedView;
        
        boolean createdNewView = false;
        
        // Do we have this View given its ID?
        targetView = findObjectInTargetModel(importedView);
        
        // We don't have it, so create a new view
        if(targetView == null) {
            targetView = cloneObject(importedView);
            createdNewView = true;
        }
        else if(doReplaceWithSource()) {
            updateView();
        }
        
        // Add to parent folder
        addToParentFolder(importedView, targetView);
        
        // New view created
        if(createdNewView) {
            createChildObjects(importedView, targetView);
            createConnections(importedView);
        }
        // View exists, update it
        else if(doReplaceWithSource()) {
            updateChildObjects(importedView, targetView);
        }

        return targetView;
    }
    
    protected IDiagramModel getImportedView() {
        return importedView;
    }
    
    protected IDiagramModel getTargetView() {
        return targetView;
    }
    
    protected void updateView() {
        super.updateObject(getImportedView(), getTargetView());
        
        // Connection Router
        getTargetView().setConnectionRouterType(getImportedView().getConnectionRouterType());
    }
    
    /**
     * Create and sdd new child diagram objects
     */
    private void createChildObjects(IDiagramModelContainer importedParent, IDiagramModelContainer targetParent) throws PorticoException {
        for(IDiagramModelObject importedObject : importedParent.getChildren()) {
            IDiagramModelObject targetObject = cloneObject(importedObject);
            targetParent.getChildren().add(targetObject);
            
            updateDiagramModelComponent(importedObject, targetObject);
            
            if(importedObject instanceof IDiagramModelContainer) {
                createChildObjects((IDiagramModelContainer)importedObject, (IDiagramModelContainer)targetObject);
            }
        }
    }
    
    /**
     * Create and sdd new diagram connections
     * Do this in two passes in case there are connection -> connections
     */
    private void createConnections(IDiagramModel importedView) throws PorticoException {
        Hashtable<IDiagramModelConnection, IDiagramModelConnection> connections = new Hashtable<>();
        
        // Create new connections. They will be cached in ModelImporter
        for(Iterator<EObject> iter = importedView.eAllContents(); iter.hasNext();) {
            EObject importedObject = iter.next();
            if(importedObject instanceof IDiagramModelConnection) {
                connections.put((IDiagramModelConnection)importedObject, cloneObject((IDiagramModelConnection)importedObject));
            }
        }
        
        // Now connect the source and target ends
        for(Entry<IDiagramModelConnection, IDiagramModelConnection> entry : connections.entrySet()) {
            IDiagramModelConnection importedConnection = entry.getKey();
            IDiagramModelConnection targetConnection = entry.getValue();
            
            IConnectable targetSource = findObjectInTargetModel(importedConnection.getSource());
            if(targetSource == null) {
                throw new PorticoException("Could not find target component: " + importedConnection.getSource().getId()); //$NON-NLS-1$
            }
            
            IConnectable targetTarget = findObjectInTargetModel(importedConnection.getTarget());
            if(targetTarget == null) {
                throw new PorticoException("Could not find target component: " + importedConnection.getTarget().getId()); //$NON-NLS-1$
            }
            
            targetConnection.connect(targetSource, targetTarget);
            
            updateDiagramModelComponent(importedConnection, targetConnection);
        }
    }
    
    /**
     * Update a DiagramModelComponent as necessary
     * @throws PorticoException 
     */
    protected abstract void updateDiagramModelComponent(IDiagramModelComponent importedComponent, IDiagramModelComponent targetComponent) throws PorticoException;
    
    private void updateChildObjects(IDiagramModelContainer importedParent, IDiagramModelContainer targetParent) throws PorticoException {
        for(IDiagramModelObject importedObject : importedParent.getChildren()) {
            
        }
    }
}
