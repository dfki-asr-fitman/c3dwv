package de.dfki.asr.compass.web.plugins;

import de.dfki.asr.compass.business.api.PrefabSetManager;
import de.dfki.asr.compass.business.PrefabImporter;
import de.dfki.asr.compass.model.PrefabSet;
import de.dfki.asr.compass.model.SceneNode;
import de.dfki.asr.compass.web.backingbeans.editor.ScenarioEditorBean;
import de.dfki.asr.compass.web.util.JSFParameterMap;
import java.io.IOException;
import java.io.Serializable;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.ViewAccessScoped;
import org.jboss.logging.Logger;

@Named
@ViewAccessScoped
public class ImportBean implements Serializable {
	private static final long serialVersionUID = 3987738405789761919L;

	@Inject
	private Logger log;

	@Inject
	private JSFParameterMap jsfParameters;

	@Inject
	private PrefabImporter prefabImporter;

	@Inject
	private PrefabSetManager prefabSetManager;

	@Inject
	private ScenarioEditorBean scenarioEditorBean;

	private boolean importCompleteAssetHierarchy;

	public void setImportCompleteAssetHierarchy(boolean val) {
		importCompleteAssetHierarchy = val;
	}

	public boolean getImportCompleteAssetHierarchy() {
		return importCompleteAssetHierarchy;
	}

	public void importAssetAsPrefab() {
		String atlasURL = jsfParameters.get("atlasURL");
		String assetName = jsfParameters.get("assetName");
		try {
			SceneNode prefab;
			if (importCompleteAssetHierarchy) {
				prefab = prefabImporter.createSceneNodeHierachyForAsset(atlasURL, assetName);
			} else {
				prefab = prefabImporter.createSceneNodeForAsset(atlasURL, assetName);
			}
			if (prefab != null) {
				PrefabSet activePrefabSet = scenarioEditorBean.getSelectedPrefabSet();
				prefabSetManager.addPrefabToSet(prefab, activePrefabSet);
			}
		} catch (IOException | IllegalArgumentException e) {
			log.error("Error creating RenderGeometry for external prefab ", e);
		}
	}
}
