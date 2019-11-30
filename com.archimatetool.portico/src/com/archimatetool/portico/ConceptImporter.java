/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateRelationship;


/**
 * Archi Concept Importer
 * 
 * @author Phillip Beauvoir
 */
class ConceptImporter extends AbstractImporter {
    
    ConceptImporter(ModelImporter importer) {
        super(importer);
    }
    
    IArchimateConcept importConcept(IArchimateConcept importedConcept) throws PorticoException {
        // Do we have this concept given its ID?
        IArchimateConcept targetConcept = importer.findObjectInTargetModel(importedConcept);
        
        boolean createdNewConcept = false;
        
        // We don't have it, so create a new concept
        if(targetConcept == null) {
            targetConcept = importer.cloneObject(importedConcept);
            createdNewConcept = true;
        }
        else if(importer.doReplaceWithSource()) {
            importer.updateObject(importedConcept, targetConcept);
        }
        
        // Relationship ends
        if((importer.doReplaceWithSource() || createdNewConcept) && importedConcept instanceof IArchimateRelationship) {
            setRelationshipEnds((IArchimateRelationship)importedConcept, (IArchimateRelationship)targetConcept);
        }
        
        // Add to parent folder
        addToParentFolder(importedConcept, targetConcept);
        
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
