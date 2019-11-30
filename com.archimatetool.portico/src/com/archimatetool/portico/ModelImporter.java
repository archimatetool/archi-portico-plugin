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

import com.archimatetool.canvas.model.ICanvasModel;
import com.archimatetool.editor.model.IArchiveManager;
import com.archimatetool.editor.model.IEditorModelManager;
import com.archimatetool.editor.model.compatibility.CompatibilityHandlerException;
import com.archimatetool.editor.model.compatibility.IncompatibleModelException;
import com.archimatetool.editor.model.compatibility.ModelCompatibility;
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateDiagramModel;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IArchimateModelObject;
import com.archimatetool.model.ICloneable;
import com.archimatetool.model.IDocumentable;
import com.archimatetool.model.IFeature;
import com.archimatetool.model.IFeatures;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IIdentifier;
import com.archimatetool.model.INameable;
import com.archimatetool.model.IProperties;
import com.archimatetool.model.IProperty;
import com.archimatetool.model.ISketchModel;
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
    
    // Keep a cache of objects in the target model for speed
    private Map<String, IIdentifier> objectCache;
    
    public ModelImporter(boolean replaceWithSource) {
        this.replaceWithSource = replaceWithSource;
    }

    public void doImport(File importedFile, IArchimateModel targetModel) throws IOException, PorticoException {
        importedModel = loadModel(importedFile);
        this.targetModel = targetModel;
        
        objectCache = createObjectIDCache();
        
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
        ArchimateViewImporter archimateViewImporter = new ArchimateViewImporter(this);
        SketchViewImporter sketchViewImporter = new SketchViewImporter(this);
        CanvasViewImporter canvasViewImporter = new CanvasViewImporter(this);
        
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
            else if(eObject instanceof IArchimateDiagramModel) {
                archimateViewImporter.importView((IArchimateDiagramModel)eObject);
            }
            else if(eObject instanceof ISketchModel) {
                sketchViewImporter.importView((ISketchModel)eObject);
            }
            else if(eObject instanceof ICanvasModel) {
                canvasViewImporter.importView((ICanvasModel)eObject);
            }
        }
        
        // Post processing
        archimateViewImporter.postProcess();
        
        // Now we can update the UI
        IEditorModelManager.INSTANCE.firePropertyChange(this, IEditorModelManager.PROPERTY_ECORE_EVENTS_END, false, true);

        // Flush the Command Stack
        // TODO: Remove this when we have either implemented Undo/Redo or import off-line
        CommandStack stack = (CommandStack)targetModel.getAdapter(CommandStack.class);
        if(stack != null) {
            stack.flush();
        }
        
        objectCache.clear();
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
    <T extends IIdentifier> T findObjectInTargetModel(T eObject) throws PorticoException {
        EObject foundObject = objectCache.get(eObject.getId());
        
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
     * Create a new object based on class of a given object and set its data to that in the given object
     */
    @SuppressWarnings("unchecked")
    <T extends IArchimateModelObject> T cloneObject(T eObject) {
        IArchimateModelObject newObject;
        
        if(eObject instanceof ICloneable) {
            newObject = (IArchimateModelObject)((ICloneable)eObject).getCopy();
        }
        else {
            newObject = (IArchimateModelObject)EcoreUtil.create(eObject.eClass());
            updateObject(eObject, newObject);
        }
        
        newObject.setId(eObject.getId());
        
        objectCache.put(newObject.getId(), newObject);
        
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
    
    private void updateProperties(IProperties imported, IProperties target) {
        target.getProperties().clear();
        for(IProperty importedProperty : imported.getProperties()) {
            target.getProperties().add(EcoreUtil.copy(importedProperty));
        }
    }
    
    private void updateFeatures(IFeatures imported, IFeatures target) {
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
