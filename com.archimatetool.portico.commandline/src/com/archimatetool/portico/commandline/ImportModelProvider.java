/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico.commandline;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.eclipse.osgi.util.NLS;

import com.archimatetool.commandline.AbstractCommandLineProvider;
import com.archimatetool.commandline.CommandLineState;
import com.archimatetool.editor.utils.StringUtils;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.portico.ModelImporter;

/**
 * Command Line interface for Model Import
 * 
 * Typical usage - (should be all on one line):
 * 
 * Archi -consoleLog -nosplash -application com.archimatetool.commandline.app
   --createEmptyModel
   --portico.import "mymodel.archimate" --portico.replace
 * 
 * @author Phillip Beauvoir
 */
public class ImportModelProvider extends AbstractCommandLineProvider {

    static final String PREFIX = Messages.ImportModelProvider_0;
    
    static final String OPTION_IMPORT_MODEL = "portico.import"; //$NON-NLS-1$
    static final String OPTION_REPLACE_WITH_SOURCE = "portico.replace"; //$NON-NLS-1$
    
    public ImportModelProvider() {
    }
    
    @Override
    public void run(CommandLine commandLine) throws Exception {
        if(!hasCorrectOptions(commandLine)) {
            return;
        }
        
        // Get the current model that should be loaded
        IArchimateModel model = CommandLineState.getModel();
        
        if(model == null) {
            throw new IOException(Messages.ImportModelProvider_1);
        }
        
        // Archi model File
        String value = commandLine.getOptionValue(OPTION_IMPORT_MODEL);
        if(!StringUtils.isSet(value)) {
            logError(Messages.ImportModelProvider_2);
            return;
        }
        File modelFile = new File(value);
        if(!modelFile.exists()) {
            logError(NLS.bind(Messages.ImportModelProvider_3, value));
            return;
        }

        boolean replaceWithSource = commandLine.hasOption(OPTION_REPLACE_WITH_SOURCE);
        
        ModelImporter importer = new ModelImporter();
        importer.doImport(modelFile, model, replaceWithSource);

        logMessage(Messages.ImportModelProvider_4);
    }
    
    @Override
    public Options getOptions() {
        Options options = new Options();
        
        Option option = Option.builder()
                .longOpt(OPTION_IMPORT_MODEL)
                .hasArg()
                .argName(Messages.ImportModelProvider_5)
                .desc(Messages.ImportModelProvider_6)
                .build();
        options.addOption(option);
        
        // Replace with source option
        option = Option.builder()
                .longOpt(OPTION_REPLACE_WITH_SOURCE)
                .desc(Messages.ImportModelProvider_7)
                .build();
        options.addOption(option);
        
        return options;
    }
    
    private boolean hasCorrectOptions(CommandLine commandLine) {
        return commandLine.hasOption(OPTION_IMPORT_MODEL);
    }
    
    @Override
    public int getPriority() {
        return PRIORITY_IMPORT;
    }
    
    @Override
    protected String getLogPrefix() {
        return PREFIX;
    }
}
