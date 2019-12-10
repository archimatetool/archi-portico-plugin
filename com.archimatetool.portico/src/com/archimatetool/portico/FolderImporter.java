/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import org.eclipse.gef.commands.Command;

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
        IFolder targetFolder = findObjectInTargetModel(importedFolder);
        
        // We don't have it
        if(targetFolder == null) {
            // Is it a top level folder?
            targetFolder = getTargetModel().getFolder(importedFolder.getType());
            
            // No, so create a new folder
            if(targetFolder == null) {
                targetFolder = cloneObject(importedFolder);
            }
        }
        else if(doReplaceWithSource()) {
            updateObject(importedFolder, targetFolder);
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
            IFolder targetParentFolder = findObjectInTargetModel(importedParentFolder);
            // Yes, add the sub-folder to it
            if(targetParentFolder != null) {
                addCommand(new AddFolderCommand(targetParentFolder, targetFolder));
            }
            // No
            else {
                throw new PorticoException("Target parent folder was null"); //$NON-NLS-1$
            }
        }
        // Parent is a top level folder
        else {
            IFolder targetParentFolder = getTargetModel().getFolder(importedParentFolder.getType());
            addCommand(new AddFolderCommand(targetParentFolder, targetFolder));
        }
    }
    
    
    // ====================================================================================================
    // Commands
    // ====================================================================================================

    private static class AddFolderCommand extends Command {
        private IFolder parent;
        private IFolder subFolder;
        IFolder oldParent;
        int oldPosition;

        private AddFolderCommand(IFolder parent, IFolder subFolder) {
            this.parent = parent;
            this.subFolder = subFolder;
            oldParent = (IFolder)subFolder.eContainer();
        }
        
        @Override
        public boolean canExecute() {
            return !parent.getFolders().contains(subFolder);
        }
        
        @Override
        public void undo() {
            if(oldParent != null) {
                oldParent.getFolders().add(oldPosition, subFolder);
            }
            else {
                parent.getFolders().remove(subFolder);
            }
        }

        @Override
        public void execute() {
            if(oldParent != null) {
                oldPosition = oldParent.getFolders().indexOf(subFolder);
            }
            
            parent.getFolders().add(subFolder);
        }
        
        @Override
        public void dispose() {
            parent = null;
            subFolder = null;
            oldParent = null;
        }
    }
}
