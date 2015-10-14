package de.dfki.asr.compass.model.components;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.dfki.asr.compass.math.Quat4f;
import de.dfki.asr.compass.math.Vector3f;
import de.dfki.asr.compass.model.AbstractCompassEntity;
import java.io.Serializable;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.oxm.annotations.XmlInverseReference;

@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Annotation extends AbstractCompassEntity implements Serializable {

	private static final long serialVersionUID = 889605772157740531L;

	@Id
	@XmlTransient
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull
	@ManyToOne(cascade = CascadeType.ALL)
	@JsonIgnore
	@XmlInverseReference(mappedBy = "texts")
	@JoinColumn(name = "owner_id")
	private AnnotationComponent owner;

	@XmlAttribute
	@JsonProperty
	private String text;

	@XmlAttribute
	@JsonProperty
	private String title;

	@NotNull
	@XmlElement
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "x", column = @Column(name = "translate_x")),
		@AttributeOverride(name = "y", column = @Column(name = "translate_y")),
		@AttributeOverride(name = "z", column = @Column(name = "translate_z"))
	})
	private Vector3f localPosition;

	@NotNull
	@XmlElement
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "w", column = @Column(name = "rotate_w")),
		@AttributeOverride(name = "x", column = @Column(name = "rotate_x")),
		@AttributeOverride(name = "y", column = @Column(name = "rotate_y")),
		@AttributeOverride(name = "z", column = @Column(name = "rotate_z"))
	})
	private Quat4f localRotation;

	public Annotation(){
		text = "";
		title = "";
		localPosition = new Vector3f();
		localRotation = new Quat4f();
	}

	public Annotation(String title, String text, AnnotationComponent comp){
		this.text = text;
		this.title = title;
		this.owner = comp;
		localPosition = new Vector3f();
		localRotation = new Quat4f();
	}

	public void setPosition(Vector3f pos){
		this.localPosition = pos;
	}

	public Vector3f getPosition(){
		return localPosition;
	}

	public void setText(String text){
		this.text = text;
	}

	public String getText(){
		return text;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}

	public void setOwner(AnnotationComponent comp){
		this.owner = comp;
	}

	public AnnotationComponent getOwner(){
		return this.owner;
	}

	public Quat4f getLocalRotation() {
		return localRotation;
	}

	public void setLocalRotation(Quat4f localRotation) {
		this.localRotation = localRotation;
	}

	@Override
	public long getId(){
		return id;
	}

	@Override
	public void setId(final long id){
		this.id = id;
	}

	@Override
	public void clearIdsAfterDeepCopy() {
		setId(0);
	}

	@Override
	public void forceEagerFetch() {
		return;
	}
}
