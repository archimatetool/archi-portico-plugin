/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.modelimporter;

import java.util.stream.Stream;

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
    private Object[] objs;
    
    StatusMessage(Level level, String message, Object...objs) {
        this.level = level;
        this.message = message;
        this.objs = objs;
    }
    
    Level getLevel() {
        return level;
    }
    
    String getMessage() {
        Object[] objsList = Stream.of(objs)
                                      .map(obj -> ArchiLabelProvider.INSTANCE.getLabel(obj))
                                      .toArray();
        
        return getLevel().text + " " + NLS.bind(message, objsList); //$NON-NLS-1$
    }
    
    @Override
    public String toString() {
        return getMessage();
    }
}
