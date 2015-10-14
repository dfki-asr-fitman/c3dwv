package de.dfki.asr.compass.business;

import de.dfki.asr.compass.model.components.AnnotationComponent;
import de.dfki.asr.compass.business.api.ComponentAnnouncer;
import de.dfki.asr.compass.business.api.ComponentRegistry;
import javax.ejb.Stateless;

@Stateless
public class WebComponentAnnouncer implements ComponentAnnouncer {

	@Override
	public void announceComponents(final ComponentRegistry registry) {
		registry.registerComponent(AnnotationComponent.class);
	}
}
