/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.modelimporter;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.archimatetool.editor.ui.IArchiImages;
import com.archimatetool.editor.ui.components.ExtendedTitleAreaDialog;

/**
 * Status Dialog
 * 
 * @author Phillip Beauvoir
 */
class StatusDialog extends ExtendedTitleAreaDialog {
    
    private static String HELP_ID = "com.archimatetool.help.ImportModel"; //$NON-NLS-1$
    
    private List<String> messages;

    public StatusDialog(Shell parentShell, List<String> messages) {
        super(parentShell, "ImporterStatusDialog"); //$NON-NLS-1$
        this.messages = messages;
        setTitleImage(IArchiImages.ImageFactory.getImage(IArchiImages.ECLIPSE_IMAGE_IMPORT_PREF_WIZARD));
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.StatusDialog_0);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, HELP_ID);
        
        setTitle(Messages.StatusDialog_1);
        setMessage(Messages.StatusDialog_2);
        
        Composite composite = (Composite)super.createDialogArea(parent);
        
        Text text = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        text.setFont(JFaceResources.getTextFont());
        
        StringBuilder sb = new StringBuilder();
        
        if(messages.isEmpty()) {
            sb.append(Messages.StatusDialog_3 + "\n"); //$NON-NLS-1$
        }
        else {
            for(String msg : messages) {
                sb.append(msg + "\n"); //$NON-NLS-1$
            }
        }
        
        parent.getDisplay().asyncExec(() -> {
            if(!text.isDisposed()) {
                text.setText(sb.toString());
            }
        });
        
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK button
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected Point getDefaultDialogSize() {
        return new Point(500, 600);
    }
}