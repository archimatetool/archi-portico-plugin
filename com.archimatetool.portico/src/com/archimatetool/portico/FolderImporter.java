/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import org.eclipse.emf.ecore.EObject;

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
    
    @Override
    protected IFolder getDefaultTargetFolderForTargetObject(IFolder importedParentFolder, EObject targetObject) {
        return importer.getTargetModel().getFolder(importedParentFolder.getType());
    }
}
