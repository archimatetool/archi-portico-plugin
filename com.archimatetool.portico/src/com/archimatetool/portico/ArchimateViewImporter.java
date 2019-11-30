/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.archimatetool.editor.diagram.commands.DiagramCommandFactory;
import com.archimatetool.editor.model.DiagramModelUtils;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateRelationship;
import com.archimatetool.model.IDiagramModelArchimateComponent;
import com.archimatetool.model.IDiagramModelArchimateConnection;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IDiagramModelObject;

/**
 * ArchiMate View Importer
 * 
 * @author Phillip Beauvoir
 */
class ArchimateViewImporter extends AbstractViewImporter {
    
    ArchimateViewImporter(ModelImporter importer) {
        super(importer);
    }

    @Override
    protected void updateDiagramModelObject(IDiagramModelObject importedObject, IDiagramModelObject targetObject) throws PorticoException {
        if(targetObject instanceof IDiagramModelArchimateObject) {
            // Set ArchiMate Element
            IArchimateElement targetElement = importer.findObjectInTargetModel(((IDiagramModelArchimateObject)importedObject).getArchimateElement());
            if(targetElement == null) {
                throw new PorticoException("Could not find element: " + importedObject.getId()); //$NON-NLS-1$
            }
            
            ((IDiagramModelArchimateObject)targetObject).setArchimateElement(targetElement);
        }
    }

    /**
     * Iterate through the target model for post-processing
     */
    void postProcess() {
        if(importer.doReplaceWithSource()) {
            for(Iterator<EObject> iter = importer.getTargetModel().eAllContents(); iter.hasNext();) {
                EObject eObject = iter.next();
                
                // Archimate View Connections might need reconnecting
                if(eObject instanceof IDiagramModelArchimateConnection) {
                    doArchimateReconnection((IDiagramModelArchimateConnection)eObject);
                }
            }
        }
    }
    
    private void doArchimateReconnection(IDiagramModelArchimateConnection connection) {
        IArchimateRelationship relationship = connection.getArchimateRelationship();

        // Is source object valid?
        if(((IDiagramModelArchimateComponent)connection.getSource()).getArchimateConcept() != relationship.getSource()) {
            // Get the first instance of the new source in this view and connect to that
            List<IDiagramModelArchimateComponent> list = DiagramModelUtils.findDiagramModelComponentsForArchimateConcept(connection.getDiagramModel(),
                    relationship.getSource());
            if(!list.isEmpty()) {
                IDiagramModelArchimateComponent matchingComponent = list.get(0);
                connection.connect(matchingComponent, connection.getTarget());
            }
            // Not found, so delete the matching connection
            else {
                DiagramCommandFactory.createDeleteDiagramConnectionCommand(connection).execute();
            }
        }

        // Is target object valid?
        if(((IDiagramModelArchimateComponent)connection.getTarget()).getArchimateConcept() != relationship.getTarget()) {
            // Get the first instance of the new source in this view and connect to that
            List<IDiagramModelArchimateComponent> list = DiagramModelUtils.findDiagramModelComponentsForArchimateConcept(connection.getDiagramModel(), relationship.getTarget());
            if(!list.isEmpty()) {
                IDiagramModelArchimateComponent matchingComponent = list.get(0);
                connection.connect(connection.getSource(), matchingComponent);
            }
            // Not found, so delete the matching connection
            else {
                DiagramCommandFactory.createDeleteDiagramConnectionCommand(connection).execute();
            }
        }
    }
}
