/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IIdentifier;


/**
 * Archi Folder Importer
 * 
 * @author Phillip Beauvoir
 */
public class FolderImporter {
    
    private boolean replaceWithSource;
    
    public FolderImporter(boolean replaceWithSource) {
        this.replaceWithSource = replaceWithSource;
    }

    void importFolder(IArchimateModel targetModel, IFolder importedFolder) throws PorticoException {
        boolean newFolderCreated = false;

        // Do we have this folder given its ID?
        IFolder targetFolder = ArchiModelImporter.findEObject(targetModel, importedFolder);
        
        // We don't have it
        if(targetFolder == null) {
            // Is it a top level folder?
            targetFolder = targetModel.getFolder(importedFolder.getType());
            
            // No, so create a new folder
            if(targetFolder == null) {
                targetFolder = IArchimateFactory.eINSTANCE.createFolder();
                targetFolder.setId(importedFolder.getId());
                newFolderCreated = true;
            }
        }
        
        if(replaceWithSource || newFolderCreated) {
            targetFolder.setName(importedFolder.getName());
            targetFolder.setDocumentation(importedFolder.getDocumentation());
            ArchiModelImporter.updateProperties(importedFolder, targetFolder);
            ArchiModelImporter.updateFeatures(importedFolder, targetFolder);
        }

        // Determine the parent folder...
        if(importedFolder.eContainer() instanceof IFolder) {
            // Imported folder's parent folder
            IFolder importedParentFolder = (IFolder)importedFolder.eContainer();
            
            // Imported folder's parent folder is a User folder
            if(importedParentFolder.getType() == FolderType.USER) {
                // Do we have this matching parent folder?
                IFolder targetParentFolder = ArchiModelImporter.findEObject(targetModel, importedParentFolder);
                // Yes, add it
                if(targetParentFolder != null) {
                    targetParentFolder.getFolders().add(targetFolder);
                }
                // No
                else {
                    // Get our parent folder, which could be null
                    IIdentifier targetParent = (IIdentifier)targetFolder.eContainer();
                    
                    // This is a new one
                    if(targetParent == null) {
                        System.out.println("targetParent was null");
                    }
                    // Not the same parent
                    else if(!importedParentFolder.getId().equals(targetParent.getId())) {
                        // Does the new parent exist in the target model?
                        System.out.println("targetParent non match");
                    }
                }
            }
            // This is a top level folder
            else {
                IFolder f = targetModel.getFolder(importedParentFolder.getType());
                f.getFolders().add(targetFolder);
            }
        }
    }
}
