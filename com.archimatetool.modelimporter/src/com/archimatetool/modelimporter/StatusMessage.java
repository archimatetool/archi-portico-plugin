/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.modelimporter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.osgi.util.NLS;

import com.archimatetool.editor.ui.ArchiLabelProvider;

/**
 * Status Message
 * 
 * @author Phillip Beauvoir
 */
class StatusMessage {
    
    enum Level {
        INFO(Messages.StatusMessage_0),
        WARNING(Messages.StatusMessage_1);
        
        private String text;

        Level(String text) {
            this.text = text;
        }
    }
    
    private Level level;
    private String message;
    private EObject[] objs;
    
    StatusMessage(Level level, String message, EObject...objs) {
        this.level = level;
        this.message = message;
        this.objs = objs;
    }
    
    Level getLevel() {
        return level;
    }
    
    String getMessage() {
        List<String> objsList = new ArrayList<>();
        Stream.of(objs).forEach(o -> objsList.add(ArchiLabelProvider.INSTANCE.getLabel(o)));
        return getLevel().text + " " + NLS.bind(message, objsList.toArray()); //$NON-NLS-1$
    }
    
    @Override
    public String toString() {
        return getMessage();
    }
}
