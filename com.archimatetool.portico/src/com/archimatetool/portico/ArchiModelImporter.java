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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.archimatetool.editor.model.IArchiveManager;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IFeature;
import com.archimatetool.model.IFeatures;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IIdentifier;
import com.archimatetool.model.IProperties;
import com.archimatetool.model.IProperty;
import com.archimatetool.model.util.ArchimateModelUtils;
import com.archimatetool.model.util.ArchimateResourceFactory;


/**
 * Archi Model Importer
 * 
 * @author Phillip Beauvoir
 */
public class ArchiModelImporter {
    
    private boolean replaceWithSource;
    
    public ArchiModelImporter(boolean replaceWithSource) {
        this.replaceWithSource = replaceWithSource;
    }

    public void doImport(IArchimateModel targetModel, File importedFile) throws IOException, PorticoException {
        IArchimateModel importedModel = loadModel(importedFile);
        
        if(replaceWithSource) {
            targetModel.setName(importedModel.getName());
            targetModel.setPurpose(importedModel.getPurpose());
            updateProperties(importedModel, targetModel);
            updateFeatures(importedModel, targetModel);
        }
        
        FolderImporter folderImporter = new FolderImporter(replaceWithSource);

        for(Iterator<EObject> iter = importedModel.eAllContents(); iter.hasNext();) {
            EObject eObject = iter.next();
            
            if(eObject instanceof IFolder) {
                folderImporter.importFolder(targetModel, (IFolder)eObject);
            }
        }
    }
    
    /**
     * Load a model from file
     */
    private IArchimateModel loadModel(File file) throws IOException {
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

    // ===================================================================================
    // Utility methods
    // ===================================================================================
    
    /**
     * Find an object in the model based on the object's identifier and class
     */
    @SuppressWarnings("unchecked")
    static <T extends IIdentifier> T findEObject(IArchimateModel model, T eObject) throws PorticoException {
        EObject foundObject = ArchimateModelUtils.getObjectByID(model, eObject.getId());
        
        // Not found
        if(foundObject == null) {
            return null;
        }
        
        // Found an element with this id and the class is the same
        if(foundObject.eClass() == eObject.eClass()) {
            return (T)foundObject;
        }
        // Not the right class, so that's an error we should report
        else {
            throw new PorticoException("Found object with same id but different class: " + eObject.getId()); //$NON-NLS-1$
        }
    }
    
    static void updateProperties(IProperties imported, IProperties target) {
        target.getProperties().clear();
        for(IProperty importedProperty : imported.getProperties()) {
            target.getProperties().add(EcoreUtil.copy(importedProperty));
        }
    }
    
    static void updateFeatures(IFeatures imported, IFeatures target) {
        target.getFeatures().clear();
        for(IFeature importedFeature : imported.getFeatures()) {
            target.getFeatures().add(EcoreUtil.copy(importedFeature));
        }
    }
}
