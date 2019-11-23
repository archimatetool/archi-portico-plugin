/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class PorticoPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.archimatetool.portico"; //$NON-NLS-1$

	// The shared instance
	public static PorticoPlugin INSTANCE;
	
	/**
	 * The constructor
	 */
	public PorticoPlugin() {
	    INSTANCE = this;
	}
}
