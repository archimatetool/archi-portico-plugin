/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.archimatetool.editor.model.IArchiveManager;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.util.ArchimateModelUtils;
import com.archimatetool.model.util.ArchimateResourceFactory;


/**
 * Archi Model Importer
 * 
 * @author Phillip Beauvoir
 */
public class ArchiModelImporter {
    
    private IArchimateModel fSourceModel;
    private IArchimateModel fTargetModel;
    private IArchimateModel fTargetCopyModel;

    public ArchiModelImporter(IArchimateModel targetModel) {
        fTargetModel = targetModel;
    }

    public void doImport(File file) throws IOException {
        fSourceModel = loadModel(file);

        fTargetCopyModel = EcoreUtil.copy(fTargetModel);
        
        for(Iterator<EObject> iter = fSourceModel.eAllContents(); iter.hasNext();) {
            EObject eObject = iter.next();
            
            if(eObject instanceof IFolder) {
                handleImportFolder((IFolder)eObject);
            }
        }
        
    }
    
    void handleImportFolder(IFolder folder) {
        // Do we have it already?
    }
    
    /**
     * Load a model from file
     */
    IArchimateModel loadModel(File file) throws IOException {
        // Ascertain if this is an archive file
        boolean useArchiveFormat = IArchiveManager.FACTORY.isArchiveFile(file);
        
        // Create the Resource
        Resource resource = ArchimateResourceFactory.createNewResource(useArchiveFormat ?
                                                       IArchiveManager.FACTORY.createArchiveModelURI(file) :
                                                       URI.createFileURI(file.getAbsolutePath()));

        resource.load(null);
        
        IArchimateModel model = (IArchimateModel)resource.getContents().get(0);
        
        model.setFile(file);
        model.setDefaults();
        
        // Do we need to load an ArchiveManager for images in canvasses?
        // IArchiveManager archiveManager = IArchiveManager.FACTORY.createArchiveManager(model);
        // model.setAdapter(IArchiveManager.class, archiveManager);
        // archiveManager.loadImages();
        
        return model;
    }

    EObject findEObject(IArchimateModel model, EClass eClass, String id) throws PorticoException {
        EObject eObject = ArchimateModelUtils.getObjectByID(model, id);
        
        // Found an element with this id
        if(eObject != null) {
            // And the class matches
            if(eObject.eClass() == eClass) {
                return eObject;
            }
            // Not the right class, so that's an error we should report
            else {
                throw new PorticoException("Found EObject with same id but different class: " + id);
            }
        }
        
        // Not found
        return null;
    }
}
