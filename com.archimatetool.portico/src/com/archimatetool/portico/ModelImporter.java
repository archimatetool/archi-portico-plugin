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
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.osgi.util.NLS;

import com.archimatetool.editor.model.IArchiveManager;
import com.archimatetool.editor.model.IEditorModelManager;
import com.archimatetool.editor.model.compatibility.CompatibilityHandlerException;
import com.archimatetool.editor.model.compatibility.IncompatibleModelException;
import com.archimatetool.editor.model.compatibility.ModelCompatibility;
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDocumentable;
import com.archimatetool.model.IFeature;
import com.archimatetool.model.IFeatures;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IIdentifier;
import com.archimatetool.model.INameable;
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
        
        // Don't update UI on each event
        IEditorModelManager.INSTANCE.firePropertyChange(this, IEditorModelManager.PROPERTY_ECORE_EVENTS_START, false, true);
        
        // Upate root model information
        if(replaceWithSource) {
            // TODO: Should we set model name etc?
            //targetModel.setName(importedModel.getName());
            targetModel.setPurpose(importedModel.getPurpose());
            updateProperties(importedModel, targetModel);
            updateFeatures(importedModel, targetModel);
        }
        
        FolderImporter folderImporter = new FolderImporter(this);
        ConceptImporter conceptImporter = new ConceptImporter(this);
        ViewImporter viewImporter = new ViewImporter(this);
        
        // Iterate through all model contents
        for(Iterator<EObject> iter = importedModel.eAllContents(); iter.hasNext();) {
            EObject eObject = iter.next();
            
            // Update folders
            if(eObject instanceof IFolder) {
                folderImporter.importFolder((IFolder)eObject);
            }
            // Update concepts
            else if(eObject instanceof IArchimateConcept) {
                conceptImporter.importConcept((IArchimateConcept)eObject);
            }
            // Update views
            else if(eObject instanceof IDiagramModel) {
                viewImporter.importView((IDiagramModel)eObject);
            }
        }
        
        // Post processing
        viewImporter.postProcess();
        
        // Now we can update the UI
        IEditorModelManager.INSTANCE.firePropertyChange(this, IEditorModelManager.PROPERTY_ECORE_EVENTS_END, false, true);

        // Flush the Command Stack
        // TODO: Remove this when we have either implemented Undo/Redo or import off-line
        CommandStack stack = (CommandStack)targetModel.getAdapter(CommandStack.class);
        if(stack != null) {
            stack.flush();
        }
        
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

        ModelCompatibility modelCompatibility = new ModelCompatibility(resource);

        try {
            resource.load(null);
        }
        catch(IOException ex) {
            // Error occured loading model. Was it a disaster?
            try {
                modelCompatibility.checkErrors();
            }
            // Incompatible
            catch(IncompatibleModelException ex1) {
                throw new IOException(NLS.bind(Messages.ModelImporter_0, file)
                        + "\n" + ex1.getMessage()); //$NON-NLS-1$
            }
        }
        
        // And then fix any backward compatibility issues
        try {
            modelCompatibility.fixCompatibility();
        }
        catch(CompatibilityHandlerException ex) {
        }
        
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
    // Shared methods
    // ===================================================================================
    
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
    
    /**
     * Create a new object based on class of a given object and set its data to this one
     */
    @SuppressWarnings("unchecked")
    <T extends IIdentifier> T cloneObject(T eObject) {
        IIdentifier newObject = (IIdentifier)IArchimateFactory.eINSTANCE.create(eObject.eClass());
        newObject.setId(eObject.getId());
        
        updateObject(eObject, newObject);
        
        objectIDCache.put(newObject.getId(), newObject);
        
        return (T)newObject;
    }
    
    /**
     * Update target object with data from source object
     */
    void updateObject(EObject source, EObject target) {
        // Name
        if(source instanceof INameable && target instanceof INameable) {
            ((INameable)target).setName(((INameable)source).getName());
        }
        
        // Documentation
        if(source instanceof IDocumentable && target instanceof IDocumentable) {
            ((IDocumentable)target).setDocumentation(((IDocumentable)source).getDocumentation());
        }
        
        // Properties
        if(source instanceof IProperties  && target instanceof IProperties) {
            updateProperties((IProperties)source, (IProperties)target);
        }

        // Features
        if(source instanceof IFeatures && target instanceof IFeatures) {
            updateFeatures((IFeatures)source, (IFeatures)target);
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
