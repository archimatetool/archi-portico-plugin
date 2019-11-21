/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IIdentifier;


/**
 * Archi Folder Importer
 * 
 * @author Phillip Beauvoir
 */
public class FolderImporter {
    
    private ModelImporter importer;
    
    public FolderImporter(ModelImporter importer) {
        this.importer = importer;
    }

    void importFolder(IFolder importedFolder) throws PorticoException {
        boolean newFolderCreated = false;

        // Do we have this folder given its ID?
        IFolder targetFolder = importer.findEObjectInTargetModel(importedFolder);
        
        // We don't have it
        if(targetFolder == null) {
            // Is it a top level folder?
            targetFolder = importer.getTargetModel().getFolder(importedFolder.getType());
            
            // No, so create a new folder
            if(targetFolder == null) {
                targetFolder = importer.createArchimateModelObject(importedFolder);
                newFolderCreated = true;
            }
        }
        
        if(importer.doReplaceWithSource() || newFolderCreated) {
            targetFolder.setName(importedFolder.getName());
            targetFolder.setDocumentation(importedFolder.getDocumentation());
            importer.updateProperties(importedFolder, targetFolder);
            importer.updateFeatures(importedFolder, targetFolder);
        }

        // Determine the parent folder...
        if(importedFolder.eContainer() instanceof IFolder) {
            // Imported folder's parent folder
            IFolder importedParentFolder = (IFolder)importedFolder.eContainer();
            
            // Imported folder's parent folder is a User folder
            if(importedParentFolder.getType() == FolderType.USER) {
                // Do we have this matching parent folder?
                IFolder targetParentFolder = importer.findEObjectInTargetModel(importedParentFolder);
                // Yes, add it
                if(targetParentFolder != null) {
                    targetParentFolder.getFolders().add(targetFolder);
                }
                // No
                else {
                    // TODO: Figure out when the following might occur
                    
                    // Get our parent folder, which could be null
                    IIdentifier targetParent = (IIdentifier)targetFolder.eContainer();
                    
                    // This is a new one
                    if(targetParent == null) {
                        throw new PorticoException("Target parent folder was null"); //$NON-NLS-1$
                    }
                    // Not the same parent
                    else if(!importedParentFolder.getId().equals(targetParent.getId())) {
                        // Does the new parent exist in the target model?
                        throw new PorticoException("Parent folder new parent"); //$NON-NLS-1$
                    }
                }
            }
            // This is a top level folder
            else {
                IFolder f = importer.getTargetModel().getFolder(importedParentFolder.getType());
                f.getFolders().add(targetFolder);
            }
        }
    }
}
