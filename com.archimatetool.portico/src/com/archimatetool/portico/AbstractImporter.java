/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import org.eclipse.emf.ecore.EObject;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IArchimateModelObject;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IIdentifier;

/**
 * Abstract Importer
 * 
 * @author Phillip Beauvoir
 */
abstract class AbstractImporter {
    
    private ModelImporter importer;
    
    protected AbstractImporter(ModelImporter importer) {
        this.importer = importer;
    }
    
    protected boolean doReplaceWithSource() {
        return importer.replaceWithSource;
    }
    
    protected <T extends IIdentifier> T findObjectInTargetModel(T eObject) throws PorticoException {
        return importer.findObjectInTargetModel(eObject);
    }
    
    protected <T extends IArchimateModelObject> T cloneObject(T eObject) {
        return importer.cloneObject(eObject);
    }
    
    protected void updateObject(EObject source, EObject target) {
        importer.updateObject(source, target);
    }
    
    protected IArchimateModel getImportedModel() {
        return importer.importedModel;
    }

    protected IArchimateModel getTargetModel() {
        return importer.targetModel;
    }
    
    /**
     * Add target object to parent folder
     * @param importedObject The imported object
     * @param targetObject The target object
     * @throws PorticoException
     */
    protected void addToParentFolder(EObject importedObject, EObject targetObject) throws PorticoException {
        // Imported object's parent folder
        IFolder importedParentFolder = (IFolder)importedObject.eContainer();

        // Imported object's parent folder is a User folder
        if(importedParentFolder.getType() == FolderType.USER) {
            // Do we have this matching parent folder?
            IFolder targetParentFolder = importer.findObjectInTargetModel(importedParentFolder);
            // Yes, add the object to it
            if(targetParentFolder != null) {
                targetParentFolder.getElements().add(targetObject);
            }
            // No
            else {
                throw new PorticoException("Target parent folder was null"); //$NON-NLS-1$
            }
        }
        // Parent is a top level folder
        else {
            IFolder targetParentFolder = importer.targetModel.getDefaultFolderForObject(targetObject);
            targetParentFolder.getElements().add(targetObject);
        }
    }
}
