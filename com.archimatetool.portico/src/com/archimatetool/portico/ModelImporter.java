/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.archimatetool.editor.model.IArchiveManager;
import com.archimatetool.editor.model.IEditorModelManager;
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IFeature;
import com.archimatetool.model.IFeatures;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IIdentifier;
import com.archimatetool.model.IProperties;
import com.archimatetool.model.IProperty;
import com.archimatetool.model.util.ArchimateResourceFactory;


/**
 * Model Importer
 * 
 * @author Phillip Beauvoir
 */
public class ModelImporter {
    
    private boolean replaceWithSource;
    
    private IArchimateModel importedModel;
    private IArchimateModel targetModel;
    
    private Map<String, IIdentifier> objectIDCache;
    
    public ModelImporter(boolean replaceWithSource) {
        this.replaceWithSource = replaceWithSource;
    }

    public void doImport(File importedFile, IArchimateModel targetModel) throws IOException, PorticoException {
        importedModel = loadModel(importedFile);
        this.targetModel = targetModel;
        
        objectIDCache = createObjectIDCache();
        
        // Don't update model tree on each event
        IEditorModelManager.INSTANCE.firePropertyChange(this, IEditorModelManager.PROPERTY_ECORE_EVENTS_START, false, true);
        
        if(replaceWithSource) {
            targetModel.setName(importedModel.getName());
            targetModel.setPurpose(importedModel.getPurpose());
            updateProperties(importedModel, targetModel);
            updateFeatures(importedModel, targetModel);
        }
        
        // Folders
        FolderImporter folderImporter = new FolderImporter(this);
        for(Iterator<EObject> iter = importedModel.eAllContents(); iter.hasNext();) {
            EObject eObject = iter.next();
            if(eObject instanceof IFolder) {
                folderImporter.importFolder((IFolder)eObject);
            }
        }
        
        // Concepts
        ConceptImporter conceptImporter = new ConceptImporter(this);
        for(Iterator<EObject> iter = importedModel.eAllContents(); iter.hasNext();) {
            EObject eObject = iter.next();
            if(eObject instanceof IArchimateConcept) {
                conceptImporter.importConcept((IArchimateConcept)eObject);
            }
        }
        
        // Now we can update model tree
        IEditorModelManager.INSTANCE.firePropertyChange(this, IEditorModelManager.PROPERTY_ECORE_EVENTS_END, false, true);

        objectIDCache.clear();
    }
    
    boolean doReplaceWithSource() {
        return replaceWithSource;
    }
    
    IArchimateModel getImportedModel() {
        return importedModel;
    }

    IArchimateModel getTargetModel() {
        return targetModel;
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
     * Create a new object based on class of a given object and set its ID to this one
     */
    @SuppressWarnings("unchecked")
    <T extends IIdentifier> T  createArchimateModelObject(T eObject) {
        IIdentifier newObject = (IIdentifier)IArchimateFactory.eINSTANCE.create(eObject.eClass());
        newObject.setId(eObject.getId());
        objectIDCache.put(newObject.getId(), newObject);
        return (T)newObject;
    }
    
    /**
     * Find an object in the target model based on the eObject's identifier and class
     */
    @SuppressWarnings("unchecked")
    <T extends IIdentifier> T findEObjectInTargetModel(T eObject) throws PorticoException {
        EObject foundObject = objectIDCache.get(eObject.getId());
        
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
    
    void updateProperties(IProperties imported, IProperties target) {
        target.getProperties().clear();
        for(IProperty importedProperty : imported.getProperties()) {
            target.getProperties().add(EcoreUtil.copy(importedProperty));
        }
    }
    
    void updateFeatures(IFeatures imported, IFeatures target) {
        target.getFeatures().clear();
        for(IFeature importedFeature : imported.getFeatures()) {
            target.getFeatures().add(EcoreUtil.copy(importedFeature));
        }
    }
    
    private Map<String, IIdentifier> createObjectIDCache() {
        HashMap<String, IIdentifier> map = new HashMap<String, IIdentifier>();
        
        for(Iterator<EObject> iter = targetModel.eAllContents(); iter.hasNext();) {
            EObject eObject = iter.next();
            if(eObject instanceof IIdentifier) {
                map.put(((IIdentifier)eObject).getId(), (IIdentifier)eObject);
            }
        }
        
        return map;
    }
}
