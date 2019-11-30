/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IFolder;


/**
 * Archi Folder Importer
 * 
 * @author Phillip Beauvoir
 */
class FolderImporter extends AbstractImporter {
    
    FolderImporter(ModelImporter importer) {
        super(importer);
    }

    IFolder importFolder(IFolder importedFolder) throws PorticoException {
        // Do we have this folder given its ID?
        IFolder targetFolder = importer.findObjectInTargetModel(importedFolder);
        
        // We don't have it
        if(targetFolder == null) {
            // Is it a top level folder?
            targetFolder = importer.getTargetModel().getFolder(importedFolder.getType());
            
            // No, so create a new folder
            if(targetFolder == null) {
                targetFolder = importer.cloneObject(importedFolder);
            }
        }
        else if(importer.doReplaceWithSource()) {
            importer.updateObject(importedFolder, targetFolder);
        }

        // Add to parent folder (if it's a sub-folder)
        if(importedFolder.eContainer() instanceof IFolder) {
            addToParentFolder(importedFolder, targetFolder);
        }
        
        return targetFolder;
    }
    
    /**
     * Add target object to parent folder
     * @param importedObject The imported object
     * @param targetObject The target object
     * @throws PorticoException
     */
    private void addToParentFolder(IFolder importedFolder, IFolder targetFolder) throws PorticoException {
        // Imported object's parent folder
        IFolder importedParentFolder = (IFolder)importedFolder.eContainer();

        // Imported object's parent folder is a User folder
        if(importedParentFolder.getType() == FolderType.USER) {
            // Do we have this matching parent folder?
            IFolder targetParentFolder = importer.findObjectInTargetModel(importedParentFolder);
            // Yes, add the object to it
            if(targetParentFolder != null) {
                targetParentFolder.getFolders().add(targetFolder);
            }
            // No
            else {
                throw new PorticoException("Target parent folder was null"); //$NON-NLS-1$
            }
        }
        // Parent is a top level folder
        else {
            IFolder targetParentFolder = importer.getTargetModel().getFolder(importedParentFolder.getType());
            targetParentFolder.getFolders().add(targetFolder);
        }
    }
}
