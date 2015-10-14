package de.dfki.asr.compass.business;

import de.dfki.asr.compass.business.exception.EntityNotFoundException;
import de.dfki.asr.compass.business.services.CRUDService;
import de.dfki.asr.compass.model.components.Annotation;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jboss.logging.Logger;

@Stateless
public class AnnotationManager {
	@Inject
	private CRUDService crudService;

	@Inject
	private Logger log;

	public void updateAnnotation(long annotationId, String title, String text) {
		try {
			Annotation updatee = crudService.findById(Annotation.class, annotationId);
			updatee.setText(text);
			updatee.setTitle(title);
			crudService.save(updatee.getOwner().getOwner());
		} catch (EntityNotFoundException ex) {
			log.errorv(ex, "Trying to update a nonexistent annotation");
		}
	}
}
