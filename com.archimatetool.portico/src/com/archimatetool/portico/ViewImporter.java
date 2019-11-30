/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EObject;

import com.archimatetool.editor.diagram.commands.DiagramCommandFactory;
import com.archimatetool.editor.model.DiagramModelUtils;
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateRelationship;
import com.archimatetool.model.IConnectable;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelArchimateComponent;
import com.archimatetool.model.IDiagramModelArchimateConnection;
import com.archimatetool.model.IDiagramModelComponent;
import com.archimatetool.model.IDiagramModelConnection;
import com.archimatetool.model.IDiagramModelContainer;
import com.archimatetool.model.IDiagramModelObject;

/**
 * View Importer
 * 
 * @author Phillip Beauvoir
 */
class ViewImporter extends AbstractImporter {
    
    ViewImporter(ModelImporter importer) {
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
            createChildObjects(importedView, targetView);
            createConnections(importedView);
        }
        // View exists, update it
        else if(importer.doReplaceWithSource()) {
            updateChildObjects(importedView, targetView);
        }

        return targetView;
    }
    
    /**
     * Create and sdd new child diagram objects
     */
    private void createChildObjects(IDiagramModelContainer importedParent, IDiagramModelContainer targetParent) throws PorticoException {
        for(IDiagramModelObject importedObject : importedParent.getChildren()) {
            IDiagramModelObject targetObject = importer.cloneObject(importedObject);
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
    
    /**
     * Update a DiagramModelComponent
     */
    private void updateDiagramModelComponent(IDiagramModelComponent importedComponent, IDiagramModelComponent targetComponent) throws PorticoException {
        // Set ArchiMate Concept
        if(targetComponent instanceof IDiagramModelArchimateComponent) {
            IArchimateConcept targetConcept = importer.findObjectInTargetModel(((IDiagramModelArchimateComponent)importedComponent).getArchimateConcept());
            if(targetConcept == null) {
                throw new PorticoException("Could not find concept in target: " + importedComponent.getId()); //$NON-NLS-1$
            }
            
            ((IDiagramModelArchimateComponent)targetComponent).setArchimateConcept(targetConcept);
        }
    }
    
    private void updateChildObjects(IDiagramModelContainer importedParent, IDiagramModelContainer targetParent) throws PorticoException {
        for(IDiagramModelObject importedObject : importedParent.getChildren()) {
            
        }
    }

    // =============================================================================================
    // Post processing
    // =============================================================================================
    
    /**
     * Iterate through the target model for post-processing
     */
    void postProcess() {
        if(importer.doReplaceWithSource()) {
            for(Iterator<EObject> iter = importer.getTargetModel().eAllContents(); iter.hasNext();) {
                EObject eObject = iter.next();
                
                // Archimate View Connections might need reconnecting
                if(eObject instanceof IDiagramModelArchimateConnection) {
                    doArchimateReconnection((IDiagramModelArchimateConnection)eObject);
                }
            }
        }
    }
    
    /**
     * Reconnect archimate connections in case of relationship ends having changed
     */
    private void doArchimateReconnection(IDiagramModelArchimateConnection connection) {
        IArchimateRelationship relationship = connection.getArchimateRelationship();

        // Is source object valid?
        if(((IDiagramModelArchimateComponent)connection.getSource()).getArchimateConcept() != relationship.getSource()) {
            // Get the first instance of the new source in this view and connect to that
            List<IDiagramModelArchimateComponent> list = DiagramModelUtils.findDiagramModelComponentsForArchimateConcept(connection.getDiagramModel(),
                    relationship.getSource());
            if(!list.isEmpty()) {
                IDiagramModelArchimateComponent matchingComponent = list.get(0);
                connection.connect(matchingComponent, connection.getTarget());
            }
            // Not found, so delete the matching connection
            else {
                DiagramCommandFactory.createDeleteDiagramConnectionCommand(connection).execute();
            }
        }

        // Is target object valid?
        if(((IDiagramModelArchimateComponent)connection.getTarget()).getArchimateConcept() != relationship.getTarget()) {
            // Get the first instance of the new source in this view and connect to that
            List<IDiagramModelArchimateComponent> list = DiagramModelUtils.findDiagramModelComponentsForArchimateConcept(connection.getDiagramModel(), relationship.getTarget());
            if(!list.isEmpty()) {
                IDiagramModelArchimateComponent matchingComponent = list.get(0);
                connection.connect(connection.getSource(), matchingComponent);
            }
            // Not found, so delete the matching connection
            else {
                DiagramCommandFactory.createDeleteDiagramConnectionCommand(connection).execute();
            }
        }
    }
}
