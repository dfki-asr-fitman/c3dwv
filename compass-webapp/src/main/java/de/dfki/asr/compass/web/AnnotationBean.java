package de.dfki.asr.compass.fitman.web;

import de.dfki.asr.compass.business.api.ComponentManager;
import de.dfki.asr.compass.business.api.SceneTreeManager;
import de.dfki.asr.compass.business.exception.EntityNotFoundException;
import de.dfki.asr.compass.business.AnnotationManager;
import de.dfki.asr.compass.model.components.Annotation;
import de.dfki.asr.compass.model.components.AnnotationComponent;
import de.dfki.asr.compass.web.util.JSFParameterMap;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import de.dfki.asr.compass.math.Quat4f;
import de.dfki.asr.compass.math.Vector3f;
import static de.dfki.asr.compass.math.Vector3f.fromDOMString;
import de.dfki.asr.compass.model.SceneNode;
import de.dfki.asr.compass.model.SceneNodeComponent;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import org.jboss.logging.Logger;

@Named
@RequestScoped
public class AnnotationBean {
	@Inject
	Logger log;

	@Inject
	JSFParameterMap parameters;

	@Inject
	SceneTreeManager sceneTree;

	@Inject
	ComponentManager componentManager;

	@Inject
	AnnotationManager manager;

	public void attachAnnotation() throws EntityNotFoundException {
		long sceneNode = Long.valueOf(parameters.get("sceneNodeId"));
		Vector3f hitPoint = fromDOMString(parameters.get("position"));
		Vector3f hitNormal = fromDOMString(parameters.get("normal"));

		SceneNode parent = sceneTree.findById(sceneNode);
		hitPoint = getObjectSpace(parent, hitPoint);
		Quat4f orientation = rotationFromUpToNormal(hitNormal, parent);
		AnnotationComponent c = ensureAnnotationComponent(parent);
		Annotation newAnnotation = new Annotation();
		newAnnotation.setText(parameters.get("text"));
		newAnnotation.setTitle(parameters.get("title"));
		newAnnotation.setPosition(hitPoint);
		newAnnotation.setLocalRotation(orientation);
		c.addAnnotation(newAnnotation);
		sceneTree.save(parent);
	}

	public void removeAnnotation(Annotation a) {
		AnnotationComponent c = a.getOwner();
		SceneNode annotatedNode = c.getOwner();
		c.removeAnnotation(a);
		sceneTree.save(annotatedNode);
	}

	private AnnotationComponent ensureAnnotationComponent(SceneNode node) {
		AnnotationComponent component = null;
		for(SceneNodeComponent c : node.getComponents()) {
			if (c instanceof AnnotationComponent) {
				component = (AnnotationComponent)c;
			}
		}
		if (component == null) {
			try {
				component = componentManager.createComponent(AnnotationComponent.class);
			} catch (InstantiationException | IllegalAccessException ex) {
				log.errorv(ex, "Could not create an AnnotationComponent via manager.");
				component = new AnnotationComponent();
			}
			componentManager.addComponentToNode(component, node);
		}
		return component;
	}

	protected Vector3f getObjectSpace(SceneNode parent, Vector3f hit)
	{
		Vector3f transformedHit = new Vector3f(0,0,0);
		Matrix4f parentWorldTransform = parent.getWorldSpaceTransform();
		parentWorldTransform.invert();
		Point3f hitPoint = new Point3f(hit);
		parentWorldTransform.transform(hitPoint);
		transformedHit.set(hitPoint);
		return transformedHit;
	}

	public Quat4f rotationFromUpToNormal(final Vector3f normal, SceneNode parent) {
		Vector3f target = new Vector3f(normal);
		Matrix4f parentWorldTransform = parent.getWorldSpaceTransform();
		parentWorldTransform.invert();
		parentWorldTransform.transform(target);
		Vector3f up     = new Vector3f(0,1,0);

		Vector3f cross  = new Vector3f();
		cross.cross(up, target);
		float w = up.dot(target);
		Quat4f doubleDesiredRotation = new Quat4f(cross.x, cross.y, cross.z, Math.abs(w));
		doubleDesiredRotation.normalize();
		Quat4f noRotation = new Quat4f(0,0,0,1);

		Quat4f desiredRotation = new Quat4f();
		desiredRotation.interpolate(doubleDesiredRotation, noRotation, 0.5f);
		return desiredRotation;
	}

	public void editAnnotation() {
		long annotationID = Long.valueOf(parameters.get("annotationId"));
		String text = parameters.get("text");
		String title = parameters.get("title");
		manager.updateAnnotation(annotationID, title, text);
	}
}
