/*
 * This file is part of COMPASS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.compass.fitman.web.plugins;

import de.dfki.asr.compass.web.plugins.PluginViewRegistry;
import de.dfki.asr.compass.web.plugins.api.PluginAnnouncer;
import javax.ejb.Stateless;

@Stateless
public class WebPluginAnnouncer implements PluginAnnouncer {

	@Override
	public void announcePlugin(final PluginViewRegistry registry) {
		// Growl is used in serval places
		registry.registerPluginView("body", "/plugins/plugin_bootstrap_growl.xhtml");

		// Annotations Plugins
		registry.registerPluginView("body", "/plugins/plugin_nubsi_script.xhtml");
		registry.registerPluginView("body", "/plugins/plugin_nubsi_dialog.xhtml");
		registry.registerPluginView("menuBar", "/plugins/plugin_nubsi_button.xhtml");

		// Atlas Plugins
		registry.registerPluginView("body", "/plugins/prefab_importer_dialogs.xhtml");
		registry.registerPluginView("prefabButtons", "/plugins/prefab_importer_button.xhtml");
		registry.registerPluginView("renderGeometryEditor", "/plugins/prefab_importer_rendergeometry.xhtml");
	}
}
