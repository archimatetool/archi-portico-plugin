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
import com.archimatetool.model.IArchimateConcept;
import com.archimatetool.model.IArchimateRelationship;
import com.archimatetool.model.IDiagramModelArchimateComponent;
import com.archimatetool.model.IDiagramModelArchimateConnection;
import com.archimatetool.model.IDiagramModelComponent;

/**
 * Archimate View Importer
 * 
 * @author Phillip Beauvoir
 */
class ArchimateViewImporter extends AbstractViewImporter {
    
    ArchimateViewImporter(ModelImporter importer) {
        super(importer);
    }

    @Override
    protected void updateDiagramModelComponent(IDiagramModelComponent importedComponent, IDiagramModelComponent targetComponent) throws PorticoException {
        // Set ArchiMate Concept
        if(targetComponent instanceof IDiagramModelArchimateComponent) {
            IArchimateConcept targetConcept = findObjectInTargetModel(((IDiagramModelArchimateComponent)importedComponent).getArchimateConcept());
            if(targetConcept == null) {
                throw new PorticoException("Could not find concept in target: " + importedComponent.getId()); //$NON-NLS-1$
            }
            
            ((IDiagramModelArchimateComponent)targetComponent).setArchimateConcept(targetConcept);
        }
    }

    // =============================================================================================
    // Post processing
    // =============================================================================================
    
    /**
     * Iterate through the target model for post-processing
     */
    void postProcess() {
        if(doReplaceWithSource()) {
            for(Iterator<EObject> iter = getTargetModel().eAllContents(); iter.hasNext();) {
                EObject eObject = iter.next();
                
                // Archimate View Connections might need reconnecting
                if(eObject instanceof IDiagramModelArchimateConnection) {
                    doArchimateReconnection((IDiagramModelArchimateConnection)eObject);
                }
            }
        }
    }
    
    /**
     * Reconnect archimate connections in case of relationship ends having changed
     */
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
