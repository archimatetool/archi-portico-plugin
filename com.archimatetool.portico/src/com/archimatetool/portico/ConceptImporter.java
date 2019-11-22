/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import java.util.List;

import com.archimatetool.editor.diagram.commands.DiagramCommandFactory;
import com.archimatetool.editor.model.DiagramModelUtils;
import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateRelationship;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelArchimateComponent;
import com.archimatetool.model.IDiagramModelArchimateConnection;
import com.archimatetool.model.IFolder;


/**
 * Archi Concept Importer
 * 
 * @author Phillip Beauvoir
 */
public class ConceptImporter {
    
    private ModelImporter importer;
    
    public ConceptImporter(ModelImporter importer) {
        this.importer = importer;
    }

    IArchimateConcept importConcept(IArchimateConcept importedConcept) throws PorticoException {
        // Do we have this concept given its ID?
        IArchimateConcept targetConcept = importer.findEObjectInTargetModel(importedConcept);
        
        // We don't have it, so create a new concept
        if(targetConcept == null) {
            targetConcept = importer.cloneObject(importedConcept);
            
            // Relationship
            if(importedConcept instanceof IArchimateRelationship) {
                setRelationshipEnds((IArchimateRelationship)importedConcept, (IArchimateRelationship)targetConcept);
            }
        }
        else if(importer.doReplaceWithSource()) {
            importer.updateObject(importedConcept, targetConcept);

            // Relationship
            if(importedConcept instanceof IArchimateRelationship) {
                setRelationshipEnds((IArchimateRelationship)importedConcept, (IArchimateRelationship)targetConcept);
                updateRelationshipDiagramInstances((IArchimateRelationship)targetConcept);
            }
        }

        // Imported concept's parent folder
        IFolder importedParentFolder = (IFolder)importedConcept.eContainer();

        // Imported folder's parent folder is a User folder
        if(importedParentFolder.getType() == FolderType.USER) {
            // Do we have this matching parent folder?
            IFolder targetParentFolder = importer.findEObjectInTargetModel(importedParentFolder);
            // Yes, add the concept to it
            if(targetParentFolder != null) {
                targetParentFolder.getElements().add(targetConcept);
            }
            // No
            else {
                throw new PorticoException("Target parent folder was null"); //$NON-NLS-1$
            }
        }
        // Parent is a top level folder
        else {
            IFolder f = importer.getTargetModel().getDefaultFolderForObject(targetConcept);
            f.getElements().add(targetConcept);
        }
        
        return targetConcept;
    }
    
    private void setRelationshipEnds(IArchimateRelationship importedRelationship, IArchimateRelationship targetRelationship) throws PorticoException {
        IArchimateConcept source = importConcept(importedRelationship.getSource());
        IArchimateConcept target = importConcept(importedRelationship.getTarget());
        
        targetRelationship.setSource(source);
        targetRelationship.setTarget(target);
    }
    
    private void updateRelationshipDiagramInstances(IArchimateRelationship relationship) {
        for(IDiagramModel dm : importer.getTargetModel().getDiagramModels()) {
            // Matching connections
            for(IDiagramModelArchimateConnection matchingConnection : DiagramModelUtils.findDiagramModelConnectionsForRelation(dm, relationship)) {
                // Is source object valid?
                if(((IDiagramModelArchimateComponent)matchingConnection.getSource()).getArchimateConcept() != relationship.getSource()) {
                    // Get the first instance of the new source in this view and connect to that
                    List<IDiagramModelArchimateComponent> list = DiagramModelUtils.findDiagramModelComponentsForArchimateConcept(dm, relationship.getSource());
                    if(!list.isEmpty()) {
                        IDiagramModelArchimateComponent matchingComponent = list.get(0);
                        matchingConnection.connect(matchingComponent, matchingConnection.getTarget());
                    }
                    // Not found, so delete the matching connection
                    else {
                        DiagramCommandFactory.createDeleteDiagramConnectionCommand(matchingConnection).execute();
                    }
                }

                // Is target object valid?
                if(((IDiagramModelArchimateComponent)matchingConnection.getTarget()).getArchimateConcept() != relationship.getTarget()) {
                    // Get the first instance of the new source in this view and connect to that
                    List<IDiagramModelArchimateComponent> list = DiagramModelUtils.findDiagramModelComponentsForArchimateConcept(dm, relationship.getTarget());
                    if(!list.isEmpty()) {
                        IDiagramModelArchimateComponent matchingComponent = list.get(0);
                        matchingConnection.connect(matchingConnection.getSource(), matchingComponent);
                    }
                    // Not found, so delete the matching connection
                    else {
                        DiagramCommandFactory.createDeleteDiagramConnectionCommand(matchingConnection).execute();
                    }
                }
            }
        }
    }
}
