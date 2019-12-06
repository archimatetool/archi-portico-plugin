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

import com.archimatetool.canvas.model.ICanvasModel;
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IConnectable;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelArchimateComponent;
import com.archimatetool.model.IDiagramModelArchimateConnection;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IDiagramModelConnection;
import com.archimatetool.model.IDiagramModelContainer;
import com.archimatetool.model.IDiagramModelObject;
import com.archimatetool.model.ISketchModel;

/**
 * View Importer
 * 
 * @author Phillip Beauvoir
 */
class ViewImporter extends AbstractImporter {
    
    private IDiagramModel importedView;
    private IDiagramModel targetView;
    
    ViewImporter(ModelImporter importer) {
        super(importer);
    }

    IDiagramModel importView(IDiagramModel importedView) throws PorticoException {
        this.importedView = importedView;
        
        // Do we have this View given its ID?
        targetView = findObjectInTargetModel(importedView);
        
        // We don't have it, so create a new view
        if(targetView == null) {
            targetView = cloneObject(importedView);
            createChildObjects(importedView, targetView);
            createConnections();
        }
        // We have it so update it
        else if(doReplaceWithSource()) {
            updateView();
            targetView.getChildren().clear(); // clear all child objects
            createChildObjects(importedView, targetView);
            createConnections();
        }
        
        // Add to parent folder (we need to do this in any case since it may have moved)
        addToParentFolder(importedView, targetView);
        
        return targetView;
    }
    
    private void updateView() {
        super.updateObject(importedView, targetView);
        
        // Connection Router
        targetView.setConnectionRouterType(importedView.getConnectionRouterType());
        
        // Sketch View
        if(targetView instanceof ISketchModel) {
            // Background
            ((ISketchModel)targetView).setBackground(((ISketchModel)importedView).getBackground());
        }
        
        // Canvas View
        if(targetView instanceof ICanvasModel) {
            // Hint stuff
            ((ICanvasModel)targetView).setHintTitle(((ICanvasModel)importedView).getHintTitle());
            ((ICanvasModel)targetView).setHintContent(((ICanvasModel)importedView).getHintContent());
        }
    }
    
    /**
     * Create and sdd new child diagram objects
     */
    private void createChildObjects(IDiagramModelContainer importedParent, IDiagramModelContainer targetParent) throws PorticoException {
        for(IDiagramModelObject importedObject : importedParent.getChildren()) {
            IDiagramModelObject targetObject = cloneObject(importedObject);
            targetParent.getChildren().add(targetObject);
            
            // Archimate object so set Archimate concept
            if(targetObject instanceof IDiagramModelArchimateObject) {
                setArchimateConcept((IDiagramModelArchimateObject)importedObject, (IDiagramModelArchimateObject)targetObject);
            }
            
            // Recuse child objects
            if(importedObject instanceof IDiagramModelContainer) {
                createChildObjects((IDiagramModelContainer)importedObject, (IDiagramModelContainer)targetObject);
            }
        }
    }
    
    /**
     * Create and add new diagram connections
     * Do this in two passes in case there are connection -> connections
     */
    private void createConnections() throws PorticoException {
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
            
            // Archimate connection so set Archimate concept
            if(targetConnection instanceof IDiagramModelArchimateConnection) {
                setArchimateConcept((IDiagramModelArchimateConnection)importedConnection, (IDiagramModelArchimateConnection)targetConnection);
            }
        }
    }
    
    /**
     * Set the Archimate concept in the IDiagramModelArchimateComponent
     */
    private void setArchimateConcept(IDiagramModelArchimateComponent importedComponent, IDiagramModelArchimateComponent targetComponent) throws PorticoException {
        // Set ArchiMate Concept
        IArchimateConcept targetConcept = findObjectInTargetModel(importedComponent.getArchimateConcept());
        
        if(targetConcept == null) {
            throw new PorticoException("Could not find concept in target: " + importedComponent.getId()); //$NON-NLS-1$
        }
        
        targetComponent.setArchimateConcept(targetConcept);
    }
}
