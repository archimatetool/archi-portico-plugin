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
    
    public ConceptImporter(ModelImporter importer) {
        this.importer = importer;
    }

    IArchimateConcept importConcept(IArchimateConcept importedConcept) throws PorticoException {
        boolean newConceptCreated = false;

        // Do we have this concept given its ID?
        IArchimateConcept targetConcept = importer.findEObjectInTargetModel(importedConcept);
        
        // We don't have it, so create a new concept
        if(targetConcept == null) {
            targetConcept = importer.createArchimateModelObject(importedConcept);
            newConceptCreated = true;
            
            // Relationship
            if(importedConcept instanceof IArchimateRelationship) {
                IArchimateRelationship importedRelationship = (IArchimateRelationship)importedConcept;
                IArchimateRelationship targetRelationship = (IArchimateRelationship)targetConcept;
                
                IArchimateConcept source = importConcept(importedRelationship.getSource());
                IArchimateConcept target = importConcept(importedRelationship.getTarget());
                
                targetRelationship.setSource(source);
                targetRelationship.setTarget(target);
            }
        }
        
        if(importer.doReplaceWithSource() || newConceptCreated) {
            targetConcept.setName(importedConcept.getName());
            targetConcept.setDocumentation(importedConcept.getDocumentation());
            importer.updateProperties(importedConcept, targetConcept);
            importer.updateFeatures(importedConcept, targetConcept);
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
}
