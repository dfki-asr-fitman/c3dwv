package de.dfki.asr.compass.model.components;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import de.dfki.asr.compass.model.SceneNodeComponent;
import de.dfki.asr.compass.model.components.annotations.CompassComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@CompassComponent(ui = "/components/annotation.xhtml", icon = "apps-accessories-text-editor")
@JsonSubTypes.Type(value = AnnotationComponent.class)
public class AnnotationComponent extends SceneNodeComponent{

	@XmlElementWrapper(name = "text")
	@XmlElementRef
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "owner", orphanRemoval = true)
	private final List<Annotation> annotations = new ArrayList<>();

	public AnnotationComponent() {
	}

	public List<Annotation> getAnnotations(){
		return Collections.unmodifiableList(annotations);
	}

	public void addAnnotation(Annotation a) {
		if (!annotations.contains(a)) {
			a.setOwner(this);
			annotations.add(a);
		}
	}

	public void removeAnnotationByIndex(int index){
		annotations.remove(index);
	}

	public void removeAnnotation(Annotation a) {
		annotations.remove(a);
	}

	@Override
	public void clearIdsAfterDeepCopy() {
		setId(0);
		for(Annotation annotation : annotations){
			annotation.clearIdsAfterDeepCopy();
		}
	}
}
