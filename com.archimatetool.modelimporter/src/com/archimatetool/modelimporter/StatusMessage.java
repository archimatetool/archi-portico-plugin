/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.modelimporter;

import java.util.stream.Stream;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

import com.archimatetool.editor.ui.ArchiLabelProvider;
import com.archimatetool.editor.ui.ColorFactory;

/**
 * Status Message
 * 
 * @author Phillip Beauvoir
 */
class StatusMessage {
    
    enum Level {
        INFO(Messages.StatusMessage_0, ColorFactory.get(0, 0, 255)),
        WARNING(Messages.StatusMessage_1, ColorFactory.get(255, 0, 0));
        
        private String text;
        private Color color;

        Level(String text, Color color) {
            this.text = text;
            this.color = color;
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
    
    StyleRange getStyleRange(int start) {
        StyleRange sr = new StyleRange();
        sr.foreground = getLevel().color;
        sr.start = start;
        sr.length = getLevel().text.length();
        return sr;
    }
    
    @Override
    public String toString() {
        return getMessage();
    }
}
