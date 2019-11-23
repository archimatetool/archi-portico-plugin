/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateRelationship;
import com.archimatetool.model.IFolder;


/**
 * Archi Concept Importer
 * 
 * @author Phillip Beauvoir
 */
public class ConceptImporter {
    
    private ModelImporter importer;
    
    ConceptImporter(ModelImporter importer) {
        this.importer = importer;
    }

    IArchimateConcept importConcept(IArchimateConcept importedConcept) throws PorticoException {
        // Do we have this concept given its ID?
        IArchimateConcept targetConcept = importer.findObjectInTargetModel(importedConcept);
        
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
            }
        }

        // Imported concept's parent folder
        IFolder importedParentFolder = (IFolder)importedConcept.eContainer();

        // Imported folder's parent folder is a User folder
        if(importedParentFolder.getType() == FolderType.USER) {
            // Do we have this matching parent folder?
            IFolder targetParentFolder = importer.findObjectInTargetModel(importedParentFolder);
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
        IArchimateConcept source = importer.findObjectInTargetModel(importedRelationship.getSource());
        if(source == null) {
            source = importConcept(importedRelationship.getSource());
        }
        targetRelationship.setSource(source);
        
        IArchimateConcept target = importer.findObjectInTargetModel(importedRelationship.getTarget());
        if(target == null) {
            source = importConcept(importedRelationship.getTarget());
        }
        targetRelationship.setTarget(target);
    }
}
