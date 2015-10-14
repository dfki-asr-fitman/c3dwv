package de.dfki.asr.compass.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListingFolder implements Serializable {
	private static final long serialVersionUID = -2023298770715831080L;

	@JsonProperty
	protected String name;

	@JsonProperty
	protected String url;

	@JsonProperty
	protected List<Float> transform;

	@JsonProperty
	protected List<ListingFolder> children;

	public ListingFolder() {
		children = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public List<ListingFolder> getChildren() {
		return children;
	}

	public void setChildren(final List<ListingFolder> children) {
		this.children = children;
	}

	public List<Float> getTransform() {
		return transform;
	}

}
