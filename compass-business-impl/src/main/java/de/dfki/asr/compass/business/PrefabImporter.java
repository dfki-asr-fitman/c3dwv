/*
 * This file is part of COMPASS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.compass.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dfki.asr.compass.model.ListingFolder;
import de.dfki.asr.compass.math.Quat4f;
import de.dfki.asr.compass.math.Vector3f;
import de.dfki.asr.compass.model.SceneNode;
import de.dfki.asr.compass.model.components.RenderGeometry;
import javax.vecmath.Matrix4f;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.jboss.logging.Logger;

@Named
public class PrefabImporter implements Serializable {

	private static final long serialVersionUID = -7150310931394475439L;

	private static final int MAX_NAME_LENGTH = 128;

	private static final String ATLAS_ASSET_REST_PATH = "rest/asset";

	@Inject
	private Logger log;

	private String assetName;

	public SceneNode createSceneNodeForAsset(final String atlasURL, final String assetName) throws IllegalArgumentException {
		this.assetName = assetName;
		UriBuilder builder = UriBuilder.fromUri(atlasURL);
		builder.path(ATLAS_ASSET_REST_PATH).path(assetName).fragment(assetName);
		return createSceneNode(builder.build().toString(), assetName);
	}

	private SceneNode createSceneNode(final String meshURL, final String assetName) throws IllegalArgumentException {
		SceneNode node = new SceneNode();
		node.setName(makeNameValid(assetName));
		node.updateOrderingIndex();
		addRenderGeometryToSceneNode(node, meshURL);
		return node;
	}

	private String makeNameValid(final String name) {
		//@Hack we do not get the information from the SceneNode class annotaions
		if (name == null || name.isEmpty()) {
			return "(unnamed)";
		}
		String validName = name.trim();
		validName = makeNameLengthValid(validName);
		validName = makeNameContentValid(validName);
		return validName;
	}

	private String makeNameLengthValid(final String name) {
		if (name.length() > MAX_NAME_LENGTH) {
			return name.substring(0, MAX_NAME_LENGTH);
		}
		return name;
	}

	private String makeNameContentValid(final String name) {
		return name.replaceAll("[^-\\w :.()]", "_");
	}

	private void addRenderGeometryToSceneNode(final SceneNode node, final String meshURL) throws IllegalArgumentException {
		RenderGeometry mesh = new RenderGeometry();
		mesh.setMeshSource(meshURL);
		mesh.setOwner(node);
		node.addComponent(mesh);
	}

	public SceneNode createSceneNodeHierachyForAsset(final String assetURL, final String assetName) throws IOException, IllegalArgumentException {
		this.assetName = assetName;
		String json = retrieveHierachyAsJSON(assetURL);
		if (json.isEmpty()) {
			return null;
		}
		ListingFolder rootFolder = parseJSONToFolderHierachy(json);
		SceneNode rootNode = createSceneNodeHierachyFromFolderHierachy(rootFolder, null);
		rootNode.setName(assetName);
		return rootNode;
	}

	private String retrieveHierachyAsJSON(final String assetURL) {
		//Clear fragment, just in case something is still in there
		URI url = UriBuilder.fromUri(assetURL).path(ATLAS_ASSET_REST_PATH).path(assetName).fragment("").build();
		if (url.getHost() == null) {
			return "";
		}
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(url);
		Response response = target.request().accept(MediaType.APPLICATION_JSON).get();
		if(isErrorResponse(response)){
			log.errorv("Error while trying to retrieve the asset hierarchy as json: {0} - {1}",
					response.getStatus(), response.readEntity(String.class));
			return "";
		}
		String json = response.readEntity(String.class);
		response.close();
		return json;
	}

	private boolean isErrorResponse(Response response) {
		return response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL;
	}

	private ListingFolder parseJSONToFolderHierachy(final String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, ListingFolder.class);
	}

	private SceneNode createSceneNodeHierachyFromFolderHierachy(final ListingFolder folder, final SceneNode parent) throws IllegalArgumentException {
		UriBuilder builder = UriBuilder.fromUri(folder.getUrl());
		builder.fragment(assetName).queryParam("includeChildren", "false");
		SceneNode node = createSceneNode(builder.build().toString(), folder.getName());
		if (parent != null) {
			node.setParent(parent);
		}
		updateTransformInNodeFromFolder(node, folder);
		for (ListingFolder child : folder.getChildren()) {
			createSceneNodeHierachyFromFolderHierachy(child, node);
		}
		return node;
	}

	private void updateTransformInNodeFromFolder(SceneNode node, ListingFolder folder) {
		List<Float> transform = folder.getTransform();
		Iterator<Float> it = transform.iterator();
		// thanks, vecmath, for not having sensible constructors.
		Matrix4f matrix = new Matrix4f(it.next(), it.next(), it.next(), it.next(),
				it.next(), it.next(), it.next(), it.next(),
				it.next(), it.next(), it.next(), it.next(),
				it.next(), it.next(), it.next(), it.next());
		node.setLocalScale(matrix.getScale());
		Vector3f translation = new Vector3f();
		matrix.get(translation);
		node.setLocalTranslation(translation);
		Quat4f rotation = new Quat4f();
		matrix.get(rotation);
		node.setLocalRotation(rotation);
	}
}
