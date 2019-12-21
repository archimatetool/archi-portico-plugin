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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.archimatetool.editor.ui.IArchiImages;
import com.archimatetool.editor.ui.components.ExtendedTitleAreaDialog;
import com.archimatetool.modelimporter.StatusMessage.Level;

/**
 * Status Dialog
 * 
 * @author Phillip Beauvoir
 */
class StatusDialog extends ExtendedTitleAreaDialog {
    
    private static String HELP_ID = "com.archimatetool.help.ImportModel"; //$NON-NLS-1$
    
    private Text textControl;
    
    private List<StatusMessage> messages;
    
    private Button btnInfo, btnWarning;

    public StatusDialog(Shell parentShell, List<StatusMessage> messages) {
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
        
        GridLayout layout = (GridLayout)composite.getLayout();
        layout.marginWidth = 10;
        layout.verticalSpacing = 5;
        
        textControl = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
        textControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        textControl.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        textControl.setFont(JFaceResources.getTextFont());
        
        btnInfo = new Button(composite, SWT.CHECK);
        btnInfo.setText(Messages.StatusDialog_4);
        btnInfo.setSelection(true);
        btnInfo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(hasMessageType(Level.INFO)) {
                    showMessages();
                }
            }
        });
        
        btnWarning = new Button(composite, SWT.CHECK);
        btnWarning.setText(Messages.StatusDialog_5);
        btnWarning.setSelection(true);
        btnWarning.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(hasMessageType(Level.WARNING)) {
                    showMessages();
                }
            }
        });
        
        showMessages();
        
        return composite;
    }
    
    private boolean hasMessageType(Level level) {
        return messages.stream().anyMatch(msg -> msg.getLevel() == level);
    }
    
    private void showMessages() {
        if(messages.isEmpty()) {
            textControl.setText(Messages.StatusDialog_3 + "\n"); //$NON-NLS-1$
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        
        boolean showInfo = btnInfo.getSelection();
        boolean showWarn = btnWarning.getSelection();
        
        for(StatusMessage msg : messages) {
            if(showInfo && msg.getLevel() == Level.INFO) {
                sb.append(msg + "\n"); //$NON-NLS-1$
            }
            
            if(showWarn && msg.getLevel() == Level.WARNING) {
                sb.append(msg + "\n"); //$NON-NLS-1$
            }
        }

        getShell().getDisplay().asyncExec(() -> {
            if(!textControl.isDisposed()) {
                textControl.setText(sb.toString());
            }
        });
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK button
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected Point getDefaultDialogSize() {
        return new Point(800, 640);
    }
}